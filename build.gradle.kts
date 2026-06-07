plugins {
	alias(libs.plugins.java.library)
	alias(libs.plugins.maven.publish)
}

group = "pe.ask"
version = "1.1.0"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://jitpack.io") }
}

dependencies {
	api(platform(libs.spring.boot.dependencies))

	implementation(libs.commons.exception.core)

	compileOnly(libs.lombok)
	annotationProcessor(libs.lombok)
	testCompileOnly(libs.lombok)
	testAnnotationProcessor(libs.lombok)

	api(libs.spring.boot.starter.data.r2dbc)

	implementation(libs.r2dbc.postgresql)

	annotationProcessor(libs.spring.boot.configuration.processor)
	annotationProcessor(libs.spring.boot.autoconfigure.processor)

	testRuntimeOnly(libs.junit.platform.launcher)
}

publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			from(components["java"])
			pom {
				name.set("SQL Persistence Core")
				description.set("Core library for reactive SQL persistence, providing generic adapters, automatic mapping, and pagination helpers for Spring WebFlux.")
			}
		}
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
