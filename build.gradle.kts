plugins {
	id("java-library")
	id("maven-publish")
}

group = "pe.ask"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	val springBootVersion = "4.0.5"
	api(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

	api("org.springframework.boot:spring-boot-starter-data-r2dbc")
	api("com.fasterxml.jackson.core:jackson-databind")

	implementation("org.postgresql:r2dbc-postgresql")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
	annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor:$springBootVersion")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				name.set("SQL Persistence Core")
				description.set("Librería core de persistencia reactiva para los microservicios")
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
