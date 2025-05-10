plugins {
	java
	id("org.springframework.boot") version "3.4.5"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "org.tenpo"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

extra["springCloudVersion"] = "2024.0.1"

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-webflux")
	implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
	implementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")

	implementation("org.springdoc:springdoc-openapi-starter-webflux-api:2.8.8")
	implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.8.8")

	implementation("org.springframework.kafka:spring-kafka:3.3.5")
	implementation("io.projectreactor.kafka:reactor-kafka:1.3.23")

	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("org.postgresql:r2dbc-postgresql")
	implementation("org.liquibase:liquibase-core")

	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")

	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:junit-jupiter")
	testImplementation("com.redis:testcontainers-redis:2.2.4")
	testImplementation("org.testcontainers:postgresql:1.21.0")
	testImplementation("org.testcontainers:kafka:1.21.0")
	testImplementation("org.testcontainers:r2dbc:1.21.0")


	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("io.projectreactor:reactor-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}



tasks.withType<Test> {
	useJUnitPlatform()
}
