package io.micronaut.starter.feature.database

import groovy.xml.XmlSlurper
import io.micronaut.core.version.SemanticVersion
import io.micronaut.starter.BuildBuilder
import io.micronaut.starter.application.generator.GeneratorContext
import io.micronaut.starter.build.BuildTestUtil
import io.micronaut.starter.build.BuildTestVerifier
import io.micronaut.starter.build.dependencies.Scope
import io.micronaut.starter.feature.Features
import io.micronaut.starter.feature.migration.Flyway
import io.micronaut.starter.feature.migration.Liquibase
import io.micronaut.starter.feature.testresources.DbType
import io.micronaut.starter.options.BuildTool
import io.micronaut.starter.options.JdkVersion
import io.micronaut.starter.options.Language
import io.micronaut.starter.options.MicronautJdkVersionConfiguration
import spock.lang.Issue
import spock.lang.Requires

import static io.micronaut.starter.build.dependencies.MicronautDependencyUtils.GROUP_ID_MICRONAUT_DATA
import static io.micronaut.starter.feature.database.DataFeature.MICRONAUT_DATA_PROCESSOR_ARTIFACT

@Requires({ jvm.current.isJava11Compatible() })
class DataHibernateReactiveSpec extends BaseHibernateReactiveSpec {

    void "test data jpa reactive requires db"() {
        when:
        getFeatures([DataHibernateReactive.NAME])

        then:
        IllegalArgumentException exception = thrown()

        and:
        exception.message == "Hibernate Reactive requires $MariaDB.NAME, $MySQL.NAME, $Oracle.NAME, $PostgreSQL.NAME, or $SQLServer.NAME"
    }

    void "test data jpa reactive features for #db"() {
        when:
        Features features = getFeatures([DataHibernateReactive.NAME, db])

        then:
        features.contains("data")
        features.contains(db)
        !features.contains("jdbc-hikari")
        features.contains(DataHibernateReactive.NAME)

        where:
        db << [MySQL.NAME, MariaDB.NAME, PostgreSQL.NAME, Oracle.NAME, SQLServer.NAME]
    }

    void "test data jpa reactive features with #migration for #db"(String db, String migration) {
        when:
        Features features = getFeatures([DataHibernateReactive.NAME, db, migration])

        then:
        features.contains("data")
        features.contains(db)
        features.contains(migration)
        features.contains("jdbc-hikari")
        features.contains(DataHibernateReactive.NAME)

        where:
        [db, migration] << [[MySQL.NAME, MariaDB.NAME, PostgreSQL.NAME, Oracle.NAME, SQLServer.NAME], [Liquibase.NAME, Flyway.NAME]].combinations()
    }

    void "data hibernate reactive requires java 11"() {
        when:
        new BuildBuilder(beanContext, BuildTool.GRADLE)
                .jdkVersion(JdkVersion.JDK_8)
                .features([DataHibernateReactive.NAME, MySQL.NAME])
                .render()

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "Hibernate Reactive requires at least JDK 11"

        when:
        new BuildBuilder(beanContext, BuildTool.GRADLE)
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .features([DataHibernateReactive.NAME, MySQL.NAME])
                .render()

        then:
        noExceptionThrown()

        when:
        new BuildBuilder(beanContext, BuildTool.GRADLE)
                .jdkVersion(JdkVersion.JDK_17)
                .features([DataHibernateReactive.NAME, MySQL.NAME])
                .render()

        then:
        noExceptionThrown()
    }

    void "test dependencies are present for gradle with #db (#client)"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.GRADLE)
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .features([DataHibernateReactive.NAME, db])
                .render()

        then:
        template.contains('id("io.micronaut.test-resources") version')
        template.contains('annotationProcessor("io.micronaut.data:micronaut-data-processor")')
        template.contains('implementation("io.micronaut.data:micronaut-data-hibernate-reactive")')
        !template.contains('implementation("io.micronaut.sql:micronaut-hibernate-reactive")')
        !template.contains('implementation("io.micronaut.sql:micronaut-jdbc-hikari")')
        template.contains($/implementation("$HibernateReactiveFeature.IO_VERTX_DEPENDENCY_GROUP:$client")/$)

        !template.contains('testImplementation("org.testcontainers:testcontainers")')
        !template.contains($/testImplementation("org.testcontainers:$container")/$)

        and: "postgres needs another dependency with vert.x"
        template.contains('implementation("com.ongres.scram:client:') == (db == PostgreSQL.NAME)

        and: 'the driver and correct module are included for test-resources'
        !template.contains($/runtimeOnly("$driver.groupId:$driver.artifactId")/$)
        template.contains($/testResourcesService("$driver.groupId:$driver.artifactId")/$)
        template.contains($/additionalModules.add("$testResourcesModule")/$)

        where:
        db              | client                                    | container     | testResourcesModule                                        | driver
        MySQL.NAME      | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | 'mysql'       | DbType.MYSQL.hibernateReactiveTestResourcesModuleName      | MySQL.DEPENDENCY_MYSQL_CONNECTOR_JAVA.build()
        MariaDB.NAME    | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | 'mariadb'     | DbType.MARIADB.hibernateReactiveTestResourcesModuleName    | MariaDB.DEPENDENCY_MARIADB_JAVA_CLIENT.build()
        PostgreSQL.NAME | PostgreSQL.VERTX_PG_CLIENT                | 'postgresql'  | DbType.POSTGRESQL.hibernateReactiveTestResourcesModuleName | PostgreSQL.DEPENDENCY_POSTGRESQL.build()
        Oracle.NAME     | Oracle.VERTX_ORACLE_CLIENT                | 'oracle-xe'   | DbType.ORACLEXE.hibernateReactiveTestResourcesModuleName   | Oracle.DEPENDENCY_OJDBC8.build()
        SQLServer.NAME  | SQLServer.VERTX_MSSQL_CLIENT              | 'mssqlserver' | DbType.SQLSERVER.hibernateReactiveTestResourcesModuleName  | SQLServer.DEPENDENCY_MSSQL_JDBC.build()
    }

    void "test dependencies are present for gradle with #db (#client) and #migration"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.GRADLE)
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .features([DataHibernateReactive.NAME, db, migration])
                .render()

        then:
        template.contains('annotationProcessor("io.micronaut.data:micronaut-data-processor")')
        template.contains('implementation("io.micronaut.data:micronaut-data-hibernate-reactive")')
        !template.contains('implementation("io.micronaut.sql:micronaut-hibernate-reactive")')
        template.contains('implementation("io.micronaut.sql:micronaut-jdbc-hikari")')
        template.contains($/implementation("io.micronaut.$migration:micronaut-$migration")/$)
        template.contains($/runtimeOnly("$migrationDriver.groupId:$migrationDriver.artifactId")/$)
        template.contains('runtimeOnly("org.flywaydb:flyway-mysql")') == (migration == Flyway.NAME && db in [MySQL.NAME, MariaDB.NAME])
        template.contains($/implementation("$HibernateReactiveFeature.IO_VERTX_DEPENDENCY_GROUP:$client")/$)

        !template.contains('testImplementation("org.testcontainers:testcontainers")')
        !template.contains($/testImplementation("org.testcontainers:$container")/$)

        and: "postgres needs another dependency with vert.x"
        template.contains('implementation("com.ongres.scram:client:') == (db == PostgreSQL.NAME)

        and: 'the correct module is included for test-resources'
        !template.contains($/testResourcesService("$migrationDriver.groupId:$migrationDriver.artifactId")/$)
        template.contains($/additionalModules.add("$testResourcesModule")/$)

        where:
        db              | client                                    | container     | migration      | testResourcesModule                                        | migrationDriver
        MySQL.NAME      | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | 'mysql'       | Liquibase.NAME | DbType.MYSQL.hibernateReactiveTestResourcesModuleName      | MySQL.DEPENDENCY_MYSQL_CONNECTOR_JAVA.build()
        MariaDB.NAME    | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | 'mariadb'     | Liquibase.NAME | DbType.MARIADB.hibernateReactiveTestResourcesModuleName    | MariaDB.DEPENDENCY_MARIADB_JAVA_CLIENT.build()
        PostgreSQL.NAME | PostgreSQL.VERTX_PG_CLIENT                | 'postgresql'  | Liquibase.NAME | DbType.POSTGRESQL.hibernateReactiveTestResourcesModuleName | PostgreSQL.DEPENDENCY_POSTGRESQL.build()
        Oracle.NAME     | Oracle.VERTX_ORACLE_CLIENT                | 'oracle-xe'   | Liquibase.NAME | DbType.ORACLEXE.hibernateReactiveTestResourcesModuleName   | Oracle.DEPENDENCY_OJDBC8.build()
        SQLServer.NAME  | SQLServer.VERTX_MSSQL_CLIENT              | 'mssqlserver' | Liquibase.NAME | DbType.SQLSERVER.hibernateReactiveTestResourcesModuleName  | SQLServer.DEPENDENCY_MSSQL_JDBC.build()
        MySQL.NAME      | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | 'mysql'       | Flyway.NAME    | DbType.MYSQL.hibernateReactiveTestResourcesModuleName      | MySQL.DEPENDENCY_MYSQL_CONNECTOR_JAVA.build()
        MariaDB.NAME    | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | 'mariadb'     | Flyway.NAME    | DbType.MARIADB.hibernateReactiveTestResourcesModuleName    | MariaDB.DEPENDENCY_MARIADB_JAVA_CLIENT.build()
        PostgreSQL.NAME | PostgreSQL.VERTX_PG_CLIENT                | 'postgresql'  | Flyway.NAME    | DbType.POSTGRESQL.hibernateReactiveTestResourcesModuleName | PostgreSQL.DEPENDENCY_POSTGRESQL.build()
        Oracle.NAME     | Oracle.VERTX_ORACLE_CLIENT                | 'oracle-xe'   | Flyway.NAME    | DbType.ORACLEXE.hibernateReactiveTestResourcesModuleName   | Oracle.DEPENDENCY_OJDBC8.build()
        SQLServer.NAME  | SQLServer.VERTX_MSSQL_CLIENT              | 'mssqlserver' | Flyway.NAME    | DbType.SQLSERVER.hibernateReactiveTestResourcesModuleName  | SQLServer.DEPENDENCY_MSSQL_JDBC.build()
    }

    void "test kotlin jpa plugin is present for gradle kotlin project"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.GRADLE)
                .features([DataHibernateReactive.NAME, MySQL.NAME])
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .language(Language.KOTLIN)
                .render()

        then:
        template.contains('id("org.jetbrains.kotlin.plugin.jpa")')
    }

    void "test dependencies are present for maven with #db (#client)"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.MAVEN)
                .features([DataHibernateReactive.NAME, db])
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .render()
        BuildTestVerifier verifier = BuildTestUtil.verifier(BuildTool.MAVEN, template)

        then:
        containsDataProcessor(template)
        !containsDataHibernateJpa(template)
        containsDataHibernateReactive(template)
        !containsHikariDependency(template)
        containsVertXDriver(template, client)
        !containsJdbcDriver(template, migrationDriver)

        and: "postgres needs another dependency with vert.x"
        db != PostgreSQL.NAME || verifier.hasDependency('com.ongres.scram', "client")

        and: 'test resources module is correct, and driver is added to the plugin dependencies'
        verifier.hasTestResourceDependency("micronaut-test-resources-$testResourcesModule")
        verifier.hasTestResourceDependency(migrationDriver.groupId, migrationDriver.artifactId)
        and:
        DataJdbcSpec.assertTemplateDoesNotContainMicronautDataVersionProperty(template)

        where:
        db              | client                                    | testResourcesModule                                        | migrationDriver
        MySQL.NAME      | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | DbType.MYSQL.hibernateReactiveTestResourcesModuleName      | MySQL.DEPENDENCY_MYSQL_CONNECTOR_JAVA.build()
        MariaDB.NAME    | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | DbType.MARIADB.hibernateReactiveTestResourcesModuleName    | MariaDB.DEPENDENCY_MARIADB_JAVA_CLIENT.build()
        PostgreSQL.NAME | PostgreSQL.VERTX_PG_CLIENT                | DbType.POSTGRESQL.hibernateReactiveTestResourcesModuleName | PostgreSQL.DEPENDENCY_POSTGRESQL.build()
        Oracle.NAME     | Oracle.VERTX_ORACLE_CLIENT                | DbType.ORACLEXE.hibernateReactiveTestResourcesModuleName   | Oracle.DEPENDENCY_OJDBC8.build()
        SQLServer.NAME  | SQLServer.VERTX_MSSQL_CLIENT              | DbType.SQLSERVER.hibernateReactiveTestResourcesModuleName  | SQLServer.DEPENDENCY_MSSQL_JDBC.build()
    }

    void "test dependencies are present for maven with #db (#client) and #migration"() {
        when:
        BuildTool buildTool = BuildTool.MAVEN
        String template = new BuildBuilder(beanContext, buildTool)
                .features([DataHibernateReactive.NAME, db, migration])
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .render()
        BuildTestVerifier verifier = BuildTestUtil.verifier(buildTool, template)

        then:
        containsDataProcessor(template)
        !containsDataHibernateJpa(template)
        containsDataHibernateReactive(template)
        containsHikariDependency(template)
        containsVertXDriver(template, client)
        containsMigrationLibrary(template, migration)
        containsJdbcDriver(template, migrationDriver)

        and: "postgres needs another dependency with vert.x"
        db != PostgreSQL.NAME || verifier.hasDependency('com.ongres.scram', "client")

        and: 'test resources module is correct, and driver is not added to the plugin dependencies'
        verifier.hasTestResourceDependency("micronaut-test-resources-$testResourcesModule")
        !verifier.hasTestResourceDependencyWithGroupId(migrationDriver.groupId)
        and:
        DataJdbcSpec.assertTemplateDoesNotContainMicronautDataVersionProperty(template)

        where:
        db              | client                                    | migration      | testResourcesModule                                        | migrationDriver
        MySQL.NAME      | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | Liquibase.NAME | DbType.MYSQL.hibernateReactiveTestResourcesModuleName      | MySQL.DEPENDENCY_MYSQL_CONNECTOR_JAVA.build()
        MariaDB.NAME    | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | Liquibase.NAME | DbType.MARIADB.hibernateReactiveTestResourcesModuleName    | MariaDB.DEPENDENCY_MARIADB_JAVA_CLIENT.build()
        PostgreSQL.NAME | PostgreSQL.VERTX_PG_CLIENT                | Liquibase.NAME | DbType.POSTGRESQL.hibernateReactiveTestResourcesModuleName | PostgreSQL.DEPENDENCY_POSTGRESQL.build()
        Oracle.NAME     | Oracle.VERTX_ORACLE_CLIENT                | Liquibase.NAME | DbType.ORACLEXE.hibernateReactiveTestResourcesModuleName   | Oracle.DEPENDENCY_OJDBC8.build()
        SQLServer.NAME  | SQLServer.VERTX_MSSQL_CLIENT              | Liquibase.NAME | DbType.SQLSERVER.hibernateReactiveTestResourcesModuleName  | SQLServer.DEPENDENCY_MSSQL_JDBC.build()
        MySQL.NAME      | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | Flyway.NAME    | DbType.MYSQL.hibernateReactiveTestResourcesModuleName      | MySQL.DEPENDENCY_MYSQL_CONNECTOR_JAVA.build()
        MariaDB.NAME    | MySQLCompatibleFeature.VERTX_MYSQL_CLIENT | Flyway.NAME    | DbType.MARIADB.hibernateReactiveTestResourcesModuleName    | MariaDB.DEPENDENCY_MARIADB_JAVA_CLIENT.build()
        PostgreSQL.NAME | PostgreSQL.VERTX_PG_CLIENT                | Flyway.NAME    | DbType.POSTGRESQL.hibernateReactiveTestResourcesModuleName | PostgreSQL.DEPENDENCY_POSTGRESQL.build()
        Oracle.NAME     | Oracle.VERTX_ORACLE_CLIENT                | Flyway.NAME    | DbType.ORACLEXE.hibernateReactiveTestResourcesModuleName   | Oracle.DEPENDENCY_OJDBC8.build()
        SQLServer.NAME  | SQLServer.VERTX_MSSQL_CLIENT              | Flyway.NAME    | DbType.SQLSERVER.hibernateReactiveTestResourcesModuleName  | SQLServer.DEPENDENCY_MSSQL_JDBC.build()
    }

    void "test kotlin jpa plugin is present for maven kotlin project"() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.MAVEN)
                .features([DataHibernateReactive.NAME, MySQL.NAME])
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .language(Language.KOTLIN)
                .render()
        def xml = new XmlSlurper().parseText(template)
        def kotlinMavenPlugin = xml.build.plugins.plugin.find { it.artifactId.text() == 'kotlin-maven-plugin' }

        then:
        kotlinMavenPlugin.configuration.compilerPlugins.plugin*.text() == ['jpa', 'all-open']

        and: 'jpa plugin requires kotlin-maven-noarg dependency'
        with(kotlinMavenPlugin.dependencies.dependency) {
            it.groupId.every { it.text() == 'org.jetbrains.kotlin' }
            it.artifactId*.text() == ['kotlin-maven-allopen', 'kotlin-maven-noarg']
            it.version.every { it.text() == '${kotlinVersion}' }
        }
    }

    void "test config for #db"() {
        when:
        GeneratorContext ctx = buildGeneratorContext([DataHibernateReactive.NAME, db])

        then:
        !ctx.configuration.containsKey("datasources.default.url")
        !ctx.configuration.containsKey("datasources.default.username")
        !ctx.configuration.containsKey("datasources.default.password")
        !ctx.configuration.containsKey("datasources.default.dialect")
        ctx.configuration."jpa.default.reactive" == true
        ctx.configuration."jpa.default.properties.hibernate.connection.db-type" == dbType
        !ctx.configuration.containsKey("jpa.default.properties.hibernate.connection.url")
        !ctx.configuration.containsKey("jpa.default.properties.hibernate.connection.username")
        !ctx.configuration.containsKey("jpa.default.properties.hibernate.connection.password")

        where:
        db              | dbType
        MySQL.NAME      | 'mysql'
        MariaDB.NAME    | 'mariadb'
        PostgreSQL.NAME | 'postgres'
        Oracle.NAME     | 'oracle'
        SQLServer.NAME  | 'mssql'
    }

    void "test config for #db with #migration"() {
        when:
        GeneratorContext ctx = buildGeneratorContext([DataHibernateReactive.NAME, db, migration])

        then:
        !ctx.configuration.containsKey("datasources.default.url")
        !ctx.configuration.containsKey("datasources.default.username")
        !ctx.configuration.containsKey("datasources.default.password")
        !ctx.configuration.containsKey("datasources.default.dialect")
        ctx.configuration."datasources.default.db-type" == type
        ctx.configuration."jpa.default.reactive" == true
        ctx.configuration."jpa.default.properties.hibernate.connection.db-type" == type
        !ctx.configuration.containsKey("jpa.default.properties.hibernate.connection.url")
        !ctx.configuration.containsKey("jpa.default.properties.hibernate.connection.username")
        !ctx.configuration.containsKey("jpa.default.properties.hibernate.connection.password")

        where:
        db              | type       | migration
        MySQL.NAME      | 'mysql'    | Flyway.NAME
        MariaDB.NAME    | 'mariadb'  | Flyway.NAME
        PostgreSQL.NAME | 'postgres' | Flyway.NAME
        Oracle.NAME     | 'oracle'   | Flyway.NAME
        SQLServer.NAME  | 'mssql'    | Flyway.NAME
        MySQL.NAME      | 'mysql'    | Liquibase.NAME
        MariaDB.NAME    | 'mariadb'  | Liquibase.NAME
        PostgreSQL.NAME | 'postgres' | Liquibase.NAME
        Oracle.NAME     | 'oracle'   | Liquibase.NAME
        SQLServer.NAME  | 'mssql'    | Liquibase.NAME
    }

    @Issue("https://github.com/micronaut-projects/micronaut-starter/issues/686")
    void 'test data-processor dependency is in provided scope for Groovy and Maven'() {
        when:
        String template = new BuildBuilder(beanContext, BuildTool.MAVEN)
                .language(Language.GROOVY)
                .jdkVersion(MicronautJdkVersionConfiguration.DEFAULT_OPTION)
                .features([DataHibernateReactive.NAME, MySQL.NAME])
                .render()
        BuildTestVerifier verifier = BuildTestUtil.verifier(BuildTool.MAVEN, template)

        then:
        verifier.hasDependency(GROUP_ID_MICRONAUT_DATA, MICRONAUT_DATA_PROCESSOR_ARTIFACT, Scope.COMPILE_ONLY)
    }
}
