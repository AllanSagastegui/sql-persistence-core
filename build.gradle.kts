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
	implementation(platform("org.springframework.boot:spring-boot-dependencies:4.0.5"))

	implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
	implementation("com.fasterxml.jackson.core:jackson-databind")

	implementation("org.postgresql:r2dbc-postgresql")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
	annotationProcessor("org.springframework.boot:spring-boot-autoconfigure-processor")

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				name.set("SQL Persistence Core")
				description.set("")
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
