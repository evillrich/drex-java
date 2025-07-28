# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Drex is a **document regex engine** that processes documents line-by-line (not character-by-character) to extract structured JSON data from semi-structured text documents like invoices, purchase orders, and logs. This is the Java implementation using a greedy, non-backtracking matching algorithm with optional fuzzy matching via Levenshtein edit distance.

**This is a Java library designed to be embedded in applications**, not a standalone application.

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
- Generate JavaDoc: `./gradlew javadoc`
- Check code style: `./gradlew checkstyleMain checkstyleTest`

### Dependencies
- **SLF4J**: Logging (compile-only)
- **Jackson**: JSON parsing and object mapping (jackson-core, jackson-databind, jackson-annotations)
- **NetworkNT JSON Schema Validator**: Pattern schema validation
- **JUnit 5**: Testing framework

## Package Structure

Base package: `io.github.evillrich.drex`

Expected package organization:
- `io.github.evillrich.drex` - Main API classes
- `io.github.evillrich.drex.engine` - Core matching engine
- `io.github.evillrich.drex.pattern` - Pattern element implementations
- `io.github.evillrich.drex.binding` - Binding and formatting system
- `io.github.evillrich.drex.serialization` - JSON serialization/deserialization
- `io.github.evillrich.drex.exception` - Custom exceptions
- `io.github.evillrich.drex.util` - Utility classes

## Java Coding Standards

### Code Style Requirements
Follow **traditional Java coding standards** as defined by:
- Oracle Java Code Conventions (where applicable)
- Google Java Style Guide (for modern practices)
- Effective Java principles (Joshua Bloch)

### Specific Standards

#### Naming Conventions
- **Classes**: PascalCase (`DrexPattern`, `MatchResult`)
- **Methods**: camelCase (`buildPattern`, `matchDocument`)
- **Variables**: camelCase (`patternElement`, `bindingContext`)
- **Constants**: UPPER_SNAKE_CASE (`DEFAULT_EDIT_DISTANCE`, `MAX_PATTERN_DEPTH`)
- **Packages**: lowercase with dots (`io.github.evillrich.drex.pattern`)

#### Class Structure Order
1. Static constants
2. Instance variables (private first)
3. Constructors
4. Public methods
5. Package-private methods
6. Private methods
7. Static methods
8. Inner classes

#### Access Modifiers
- Use the most restrictive access level possible
- Prefer `package-private` over `public` for internal APIs
- Use `final` for immutable fields and classes that shouldn't be extended
- Use `static final` for constants

#### Method Design
- Keep methods focused and small (ideally < 20 lines)
- Use meaningful parameter names
- Validate parameters using `Objects.requireNonNull()` or similar
- Throw appropriate exceptions with descriptive messages

### JavaDoc Requirements

**ALL public methods on public classes MUST have JavaDoc comments.**

#### JavaDoc Standards
- Use complete sentences with proper punctuation
- Start with a brief summary sentence
- Use `@param` for all parameters
- Use `@return` for non-void methods
- Use `@throws` for declared exceptions
- Use `@since` for new API additions
- Use `@deprecated` with migration guidance if applicable

#### JavaDoc Template for Public Methods
```java
/**
 * Matches the given document text against this pattern and extracts structured data.
 * <p>
 * This method processes the document line-by-line using a greedy, non-backtracking
 * algorithm. If fuzzy matching is enabled, exact matches are tried first before
 * attempting edit distance matches.
 *
 * @param documentText the input document text to process, must not be null
 * @param options optional matching configuration, may be null for defaults
 * @return a MatchResult containing extracted data and metadata, never null
 * @throws PatternMatchException if the pattern fails to match the document
 * @throws IllegalArgumentException if documentText is null or empty
 * @since 1.0
 */
public MatchResult match(String documentText, MatchOptions options) {
    // implementation
}
```

#### JavaDoc for Classes
```java
/**
 * Represents a document pattern that can extract structured JSON data from
 * semi-structured text documents.
 * <p>
 * DrexPattern instances are immutable and thread-safe once constructed.
 * Use {@link DrexPattern.Builder} to create new patterns programmatically,
 * or {@link PatternDeserializer} to load patterns from JSON.
 *
 * @author Your Name
 * @since 1.0
 * @see DrexEngine
 * @see MatchResult
 */
public final class DrexPattern {
    // implementation
}
```

### Library Design Principles

#### API Design
- **Immutability**: Prefer immutable objects for thread safety
- **Builder Pattern**: Use for complex object construction
- **Fluent APIs**: Support method chaining where appropriate
- **Defensive Copying**: Copy mutable parameters/return values when needed
- **Null Safety**: Never return null from public APIs (use Optional or empty collections)

#### Exception Handling
- Create specific exception types for different error conditions
- Use checked exceptions for recoverable errors
- Use unchecked exceptions for programming errors
- Include helpful error messages with context

#### Threading
- Document thread safety guarantees in JavaDoc
- Make core classes thread-safe where practical
- Use `@ThreadSafe`, `@NotThreadSafe`, or `@Immutable` annotations

#### Performance Considerations
- Prefer composition over inheritance
- Use lazy initialization for expensive operations
- Consider object pooling for frequently created objects
- Profile and optimize hot paths

### Testing Standards

#### Test Structure
- One test class per production class
- Use descriptive test method names: `shouldExtractInvoiceItemsWhenPatternMatches()`
- Follow Given-When-Then structure in test methods
- Use `@DisplayName` for complex test scenarios

#### Test Coverage
- Aim for 80%+ line coverage on public APIs
- Test both happy path and error conditions
- Include edge cases and boundary conditions
- Test thread safety where applicable

#### Test Organization
```java
@DisplayName("DrexPattern matching behavior")
class DrexPatternTest {
    
    @Nested
    @DisplayName("When matching simple patterns")
    class SimplePatternMatching {
        // test methods
    }
    
    @Nested
    @DisplayName("When using fuzzy matching")
    class FuzzyMatching {
        // test methods
    }
}
```

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

## Implementation Guidelines

### When implementing new features:

1. **API First**: Design the public API with JavaDoc before implementation
2. **Test Driven**: Write tests before or alongside implementation
3. **Visitor Pattern**: Follow the established Visitor pattern for pattern elements
4. **Schema Compliance**: Ensure implementations match `pattern-schema.json`
5. **Immutability**: Make objects immutable where possible
6. **Error Handling**: Provide clear, actionable error messages
7. **Performance**: Consider performance implications of the greedy algorithm
8. **Documentation**: Update relevant documentation files

### Code Review Checklist
- [ ] All public methods have complete JavaDoc
- [ ] Code follows naming conventions
- [ ] Access modifiers are appropriately restrictive
- [ ] Parameters are validated with clear error messages
- [ ] Tests cover both success and failure scenarios
- [ ] Thread safety is documented and implemented
- [ ] No null returns from public APIs
- [ ] Exceptions provide helpful context

### Library Usage Patterns

Expected usage patterns for embedding in applications:

```java
// Pattern creation
DrexPattern pattern = DrexPattern.builder()
    .version("1.0")
    .name("InvoicePattern")
    .bindObject("invoice")
    .elements(/* pattern elements */)
    .build();

// Document matching
MatchResult result = DrexEngine.match(pattern, documentText);
if (result.isSuccess()) {
    String json = result.getExtractedData();
    // process extracted data
}
```

## Development Notes

The project currently has minimal implementation - mostly documentation and schema definitions. When implementing:

1. Follow the Visitor pattern shown in class diagrams
2. Implement pattern elements according to `pattern-schema.json`
3. Use `bindProperties` array format for line elements (not simple `bind` strings)
4. Remember `or` elements have no binding capabilities
5. Test patterns with the greedy matching behavior in mind
6. Ensure all public APIs are properly documented with JavaDoc
7. Follow the established package structure and naming conventions
8. Implement comprehensive error handling with descriptive messages