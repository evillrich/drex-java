# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Java implementation of Drex. The project is currently in its initial setup phase with no source code implemented yet.

## Build System

**Note: Build configuration is not yet set up.** The `.gitignore` file suggests this will be a Gradle-based project. When setting up the build:

1. Create a `build.gradle` or `build.gradle.kts` file
2. Define the Java version and dependencies
3. Set up the standard Gradle project structure:
   - `src/main/java/` for source code
   - `src/test/java/` for tests
   - `src/main/resources/` for resources

## Common Commands

Since the build system is not yet configured, here are the typical Gradle commands that will be used once set up:

- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Clean: `./gradlew clean`
- Run a single test: `./gradlew test --tests "TestClassName"`

## Development Environment

The project includes IntelliJ IDEA configuration (`.idea/` directory in `.gitignore`), suggesting IntelliJ IDEA as the primary IDE.

## Project Status

This is a newly initialized project. Key setup tasks that need to be completed:
1. Choose and configure build system (likely Gradle based on `.gitignore`)
2. Create standard Java project directory structure
3. Define package structure
4. Set up testing framework (JUnit, TestNG, etc.)
5. Add initial implementation classes