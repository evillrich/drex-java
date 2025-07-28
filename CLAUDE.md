# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Drex is a **document regex engine** that processes documents line-by-line (not character-by-character) to extract structured JSON data from semi-structured text documents like invoices, purchase orders, and logs. This is the Java implementation using a greedy, non-backtracking matching algorithm with optional fuzzy matching via Levenshtein edit distance.

## Architecture

### Core Pattern Model
The pattern system follows a Visitor pattern with these key elements:

- **PatternElement** (abstract): Base class for all pattern elements
- **CompositePatternElement**: Elements that contain other elements (`group`, `repeat`, `or`)
- **LineElement**: Terminal elements that match actual text lines (`line`, `anyline`)
- **GroupingPatternElement**: Elements that create JSON object contexts (`DrexPattern`, `Group`)

### Pattern Types
- **Line**: Matches regex patterns and extracts data via capture groups
- **Anyline**: Matches any single line (typically for skipping unwanted content)
- **Group**: Creates nested JSON objects with `bindObject`
- **Repeat**: Handles repetitive patterns (zeroOrMore, oneOrMore, zeroOrOne) with `bindArray`
- **Or**: Provides alternative matching paths (no binding - uses first successful match)

### Binding System
- **`bindObject`**: Creates JSON objects (used by pattern root, groups)
- **`bindArray`**: Creates JSON arrays (used by repeat elements)  
- **`bindProperties`**: Maps regex capture groups to JSON properties (used by line/anyline)
- **PropertyBinding**: Defines property name and optional formatter function

### Matching Algorithm
Uses greedy, non-backtracking approach:
- Processes documents line-by-line, top to bottom
- For `repeat`: always tries "more" option first
- For `or`: tries alternatives in order, takes first match
- No backtracking once choice is made
- Fuzzy matching (if enabled) tries exact matches first, then edit matches

## Build System

**Gradle-based project** with Java 21:

### Common Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`  
- Run application: `./gradlew run`
- Clean: `./gradlew clean`
- Run single test: `./gradlew test --tests "TestClassName"`

### Dependencies
- **SLF4J**: Logging (compile-only)
- **Jackson**: JSON parsing and object mapping (jackson-core, jackson-databind, jackson-annotations)
- **NetworkNT JSON Schema Validator**: Pattern schema validation
- **JUnit 5**: Testing framework

## Package Structure

Base package: `io.github.evillrich.drex`

## Key Documentation

Pattern authoring guidance:
- **`docs/binding.md`**: Data binding and formatter system
- **`docs/matching.md`**: Matching algorithm behavior and pattern design principles  
- **`docs/examples.md`**: Common pattern examples
- **`pattern-schema.json`**: Machine-readable JSON schema for patterns
- **`docs/pattern_classes.mermaid`**: Class diagram of pattern object model

Design principles:
- All output values are strings (no type conversion in engine)
- Formatters normalize strings but never halt processing on failure  
- Optional separate position metadata file for debugging/UI workflows
- Patterns work with greedy algorithm (specific before general in `or` elements)

## Development Notes

The project currently has minimal implementation - mostly documentation and schema definitions. When implementing:

1. Follow the Visitor pattern shown in class diagrams
2. Implement pattern elements according to `pattern-schema.json`
3. Use `bindProperties` array format for line elements (not simple `bind` strings)
4. Remember `or` elements have no binding capabilities
5. Test patterns with the greedy matching behavior in mind