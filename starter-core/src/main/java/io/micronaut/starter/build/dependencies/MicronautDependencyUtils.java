/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.starter.build.dependencies;

import io.micronaut.core.annotation.NonNull;

public final class MicronautDependencyUtils {
    public static final String ARTIFACT_ID_MICRONAUT_INJECT = "micronaut-inject";
    public static final Dependency MICRONAUT_INJECT = coreDependency()
            .artifactId(ARTIFACT_ID_MICRONAUT_INJECT)
            .compile()
            .build();

    public static final String GROUP_ID_MICRONAUT = "io.micronaut";
    public static final String ARTIFACT_ID_MICRONAUT_INJECT_JAVA = "micronaut-inject-java";
    public static final String GROUP_ID_MICRONAUT_AWS = "io.micronaut.aws";
    public static final String GROUP_ID_MICRONAUT_AZURE = "io.micronaut.azure";
    public static final String GROUP_ID_MICRONAUT_CRAC = "io.micronaut.crac";
    public static final String GROUP_ID_MICRONAUT_GCP = "io.micronaut.gcp";
    public static final String GROUP_ID_MICRONAUT_OCI = "io.micronaut.oraclecloud";
    public static final String GROUP_ID_MICRONAUT_SERDE = "io.micronaut.serde";
    public static final String GROUP_ID_MICRONAUT_SECURITY = "io.micronaut.security";

    public static final String GROUP_ID_MICRONAUT_SESSION = "io.micronaut.session";
    public static final String GROUP_ID_MICRONAUT_SERVLET = "io.micronaut.servlet";
    public static final String GROUP_ID_MICRONAUT_TRACING = "io.micronaut.tracing";
    public static final String GROUP_ID_MICRONAUT_TEST = "io.micronaut.test";
    public static final String GROUP_ID_MICRONAUT_R2DBC = "io.micronaut.r2dbc";
    public static final String GROUP_ID_MICRONAUT_DATA = "io.micronaut.data";
    public static final String GROUP_ID_MICRONAUT_SQL = "io.micronaut.sql";

    public static final String GROUP_ID_MICRONAUT_STARTER = "io.micronaut.starter";
    public static final String GROUP_ID_MICRONAUT_KOTLIN = "io.micronaut.kotlin";

    public static final String GROUP_ID_MICRONAUT_GROOVY = "io.micronaut.groovy";
    public static final String GROUP_ID_IO_MICRONAUT_NEO4J = "io.micronaut.neo4j";
    public static final String GROUP_ID_IO_MICRONAUT_OPENAPI = "io.micronaut.openapi";
    public static final String GROUP_ID_IO_MICRONAUT_VALIDATION = "io.micronaut.validation";

    public static final String GROUP_ID_MICRONAUT_PLATFORM = "io.micronaut.platform";

    public static final String GROUP_ID_MICRONAUT_GRPC = "io.micronaut.grpc";
    public static final String GROUP_ID_IO_MICRONAUT_JMS = "io.micronaut.jms";
    public static final String GROUP_ID_IO_MICRONAUT_PICOCLI = "io.micronaut.picocli";
    public static final String GROUP_ID_IO_MICRONAUT_DISCOVERY = "io.micronaut.discovery";
    public static final String GROUP_ID_IO_MICRONAUT_CONTROLPANEL = "io.micronaut.controlpanel";
    public static final String GROUP_ID_MICRONAUT_FLYWAY = "io.micronaut.flyway";

    private MicronautDependencyUtils() {

    }

    @NonNull
    public static Dependency.Builder coreDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT);
    }

    @NonNull
    public static Dependency.Builder awsDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_AWS);
    }

    @NonNull
    public static Dependency.Builder starterDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_STARTER);
    }

    @NonNull
    public static Dependency.Builder azureDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_AZURE);
    }

    @NonNull
    public static Dependency.Builder serdeDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_SERDE);
    }

    @NonNull
    public static Dependency.Builder securityDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_SECURITY);
    }

    @NonNull
    public static Dependency.Builder sessionDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_SESSION);
    }

    public static Dependency.Builder servletDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_SERVLET);
    }

    @NonNull
    public static Dependency.Builder testDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_TEST);
    }

    @NonNull
    public static Dependency.Builder r2dbcDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_R2DBC);
    }

    @NonNull
    public static Dependency.Builder tracingDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_TRACING);
    }

    @NonNull
    public static Dependency.Builder dataDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_DATA);
    }

    @NonNull
    public static Dependency.Builder sqlDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_SQL);
    }

    @NonNull
    public static Dependency.Builder kotlinDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_KOTLIN);
    }

    @NonNull
    public static Dependency.Builder groovyDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_GROOVY);
    }

    @NonNull
    private static Dependency.Builder micronautDependency(@NonNull String groupId) {
        return Dependency.builder()
                .groupId(groupId);
    }

    @NonNull
    public static Dependency.Builder cracDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_CRAC);
    }

    @NonNull
    public static Dependency.Builder gcpDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_GCP);
    }

    @NonNull
    public static Dependency.Builder ociDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_OCI);
    }

    @NonNull
    public static Dependency.Builder platformDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_PLATFORM);
    }

    @NonNull
    public static Dependency.Builder grpcDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_GRPC);
    }

    @NonNull
    public static Dependency.Builder jmsDependency() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_JMS);
    }

    public static Dependency.Builder neo4j() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_NEO4J);
    }

    @NonNull
    public static Dependency.Builder openapi() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_OPENAPI);
    }

    @NonNull
    public static Dependency.Builder validationDependency() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_VALIDATION);
    }

    @NonNull
    public static Dependency.Builder injectJava() {
        return coreDependency().artifactId(ARTIFACT_ID_MICRONAUT_INJECT_JAVA);
    }

    @NonNull
    public static Dependency.Builder picocliDependency() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_PICOCLI);
    }

    @NonNull
    public static Dependency.Builder discovery() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_DISCOVERY);
    }
    
    public static Dependency.Builder flywayDependency() {
        return micronautDependency(GROUP_ID_MICRONAUT_FLYWAY);
    }

    @NonNull
    public static Dependency.Builder moduleMavenAnnotationProcessor(@NonNull String groupId,
                                                                    @NonNull String artifactId,
                                                                    @NonNull String propertyName) {
        return moduleMavenAnnotationProcessor(groupId, artifactId, propertyName, false);
    }

    @NonNull
    public static Dependency.Builder moduleMavenAnnotationProcessor(@NonNull String groupId,
                                                                    @NonNull String artifactId,
                                                                    @NonNull String propertyName,
                                                                    boolean requiresPriority) {
        return Dependency.builder()
                .groupId(groupId)
                .artifactId(artifactId)
                .annotationProcessor(requiresPriority)
                .versionProperty(propertyName)
                .exclude(MICRONAUT_INJECT);
    }

    @NonNull
    public static Dependency.Builder controlPanelDependency() {
        return micronautDependency(GROUP_ID_IO_MICRONAUT_CONTROLPANEL).developmentOnly();
    }
}
