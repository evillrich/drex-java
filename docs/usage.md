# Drex Usage Guide

Drex patterns can be defined as **JSON** documents or built dynamically using the **Java Fluent API**. This guide covers both approaches with practical examples.

## Pattern Structure

Every Drex pattern is a **JSON object** with:
- Optional `version`, `name`, and `comment` metadata
- Optional `bindObject` at the pattern level to bind the entire matched document
- Optional `editDistance` for fuzzy matching (default is 0 for exact matching)
- An `elements` array, which holds the actual matching logic

`elements` can include any combination of these pattern types:
- **`line`**: Matches regex patterns and extracts data via capture groups
- **`anyline`**: Matches any single line (typically for skipping unwanted content)
- **`group`**: Creates nested JSON objects with `bindObject`
- **`repeat`**: Handles repetitive patterns (zeroOrMore, oneOrMore, zeroOrOne) with `bindArray`
- **`or`**: Provides alternative matching paths (no binding - uses first successful match)

Composite elements (`group`, `repeat`, `or`) also contain `elements` arrays for their children.

---

## JSON Pattern Definition

### Basic Example

```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "comment": "Extract basic invoice information",
  "bindObject": "invoice",
  "elements": [
    { 
      "line": { 
        "regex": "Header (.*)", 
        "bindProperties": [{"property": "header"}] 
      } 
    },
    { 
      "group": {
        "bindObject": "details",
        "elements": [
          { 
            "line": { 
              "regex": "Invoice #(\\d+)", 
              "bindProperties": [{"property": "id"}] 
            } 
          },
          { 
            "repeat": {
              "bindArray": "items",
              "mode": "oneOrMore",
              "elements": [
                { 
                  "line": { 
                    "regex": "(\\S+)\\s+(\\d+)\\s+([\\d\\.]+)", 
                    "bindProperties": [
                      {"property": "name"},
                      {"property": "qty"},
                      {"property": "price"}
                    ] 
                  } 
                }
              ]
            }
          }
        ]
      }
    }
  ]
}
```

### Advanced Features

**Fuzzy Matching with Edit Distance:**
```json
{
  "version": "1.0",
  "name": "OCRInvoicePattern",
  "editDistance": 2,
  "bindObject": "invoice",
  "elements": [
    {
      "line": {
        "regex": "Invoice #(\\d+)",
        "bindProperties": [{"property": "invoiceNumber"}],
        "comment": "Handles OCR noise like 'lnvoice' instead of 'Invoice'"
      }
    }
  ]
}
```

**Property Binding with Formatters:**
```json
{
  "line": {
    "regex": "Date: (\\d{2}/\\d{2}/\\d{4}) Amount: \\$([\\d,]+\\.\\d{2})",
    "bindProperties": [
      {"property": "date", "format": "parseDate(MM/dd/yyyy)"},
      {"property": "amount", "format": "currency()"}
    ]
  }
}
```

---

## Java Fluent API

The Java API builds the same structure programmatically with type safety and IDE support.

### Basic Pattern Creation

```java
import io.github.evillrich.drex.*;

DrexPattern pattern = DrexPattern.builder()
    .version("1.0")
    .name("InvoicePattern")
    .comment("Extract basic invoice information")
    .bindObject("invoice")
    .elements(
        Line.builder()
            .regex("Header (.*)")
            .bindProperties(PropertyBinding.of("header"))
            .build(),
        Group.builder()
            .bindObject("details")
            .elements(
                Line.builder()
                    .regex("Invoice #(\\\\d+)")
                    .bindProperties(PropertyBinding.of("id"))
                    .build(),
                Repeat.builder()
                    .bindArray("items")
                    .mode(Repeat.Mode.ONE_OR_MORE)
                    .elements(
                        Line.builder()
                            .regex("(\\\\S+)\\\\s+(\\\\d+)\\\\s+([\\\\d\\\\.]+)")
                            .bindProperties(
                                PropertyBinding.of("name"),
                                PropertyBinding.of("qty"),
                                PropertyBinding.of("price")
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    )
    .build();
```

### PropertyBinding Creation

```java
// Simple property binding
PropertyBinding.of("propertyName")

// With formatter
PropertyBinding.of("amount", "currency()")
PropertyBinding.of("date", "parseDate(MM/dd/yyyy)")

// Multiple bindings
List<PropertyBinding> bindings = Arrays.asList(
    PropertyBinding.of("name"),
    PropertyBinding.of("quantity"),
    PropertyBinding.of("price", "currency()")
);
```

### Pattern Element Types

**Line Elements:**
```java
// Basic line matching
Line.builder()
    .regex("Total: ([\\\\d\\\\.]+)")
    .bindProperties(PropertyBinding.of("total"))
    .build()

// Line with multiple captures and formatters
Line.builder()
    .regex("Order #(\\\\w+) Date: ([\\\\d/]+) Customer: (.+)")
    .bindProperties(
        PropertyBinding.of("orderNumber"),
        PropertyBinding.of("orderDate", "parseDate(MM/dd/yyyy)"),
        PropertyBinding.of("customerName", "trim()")
    )
    .comment("Extract order header information")
    .build()
```

**Anyline Elements:**
```java
// Skip unknown lines
Anyline.builder()
    .comment("Skip blank or unknown lines")
    .build()

// Anyline with binding (rare)
Anyline.builder()
    .bindProperties(PropertyBinding.of("skippedContent"))
    .build()
```

**Group Elements:**
```java
Group.builder()
    .bindObject("customer")
    .comment("Customer information section")
    .elements(
        Line.builder()
            .regex("Name: (.+)")
            .bindProperties(PropertyBinding.of("name"))
            .build(),
        Line.builder()
            .regex("Address: (.+)")
            .bindProperties(PropertyBinding.of("address"))
            .build()
    )
    .build()
```

**Repeat Elements:**
```java
// One or more iterations
Repeat.builder()
    .bindArray("lineItems")
    .mode(Repeat.Mode.ONE_OR_MORE)
    .elements(
        Line.builder()
            .regex("(\\\\d+)\\\\s+(.+)\\\\s+\\\\$([\\\\d\\\\.]+)")
            .bindProperties(
                PropertyBinding.of("quantity"),
                PropertyBinding.of("description"),
                PropertyBinding.of("price", "currency()")
            )
            .build()
    )
    .build()

// Zero or more iterations
Repeat.builder()
    .bindArray("optionalNotes")
    .mode(Repeat.Mode.ZERO_OR_MORE)
    .elements(
        Line.builder()
            .regex("Note: (.+)")
            .bindProperties(PropertyBinding.of("note"))
            .build()
    )
    .build()

// Zero or one iteration
Repeat.builder()
    .bindArray("signature")
    .mode(Repeat.Mode.ZERO_OR_ONE)
    .elements(
        Line.builder()
            .regex("Signature: (.+)")
            .bindProperties(PropertyBinding.of("name"))
            .build()
    )
    .build()
```

**Or Elements:**
```java
Or.builder()
    .comment("Handle different total formats")
    .elements(
        Line.builder()
            .regex("Total: \\\\$([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build(),
        Line.builder()
            .regex("Amount Due: \\\\$([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build(),
        Line.builder()
            .regex("Final Total: ([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build()
    )
    .build()
```

### Advanced Pattern Features

**Fuzzy Matching:**
```java
DrexPattern fuzzPattern = DrexPattern.builder()
    .name("OCRTolerantPattern")
    .editDistance(2)  // Allow up to 2 character edits
    .bindObject("document")
    .elements(
        Line.builder()
            .regex("Invoice #(\\\\d+)")
            .bindProperties(PropertyBinding.of("invoiceNumber"))
            .comment("Handles OCR errors like 'lnvoice' -> 'Invoice'")
            .build()
    )
    .build();
```

**Nested Structures:**
```java
DrexPattern complexPattern = DrexPattern.builder()
    .name("ComplexDocumentPattern")
    .bindObject("document")
    .elements(
        Group.builder()
            .bindObject("header")
            .elements(
                Line.builder()
                    .regex("Document #(\\\\w+)")
                    .bindProperties(PropertyBinding.of("documentNumber"))
                    .build(),
                Line.builder()
                    .regex("Date: ([\\\\d/]+)")
                    .bindProperties(PropertyBinding.of("date", "parseDate(MM/dd/yyyy)"))
                    .build()
            )
            .build(),
        Repeat.builder()
            .bindArray("sections")
            .mode(Repeat.Mode.ONE_OR_MORE)
            .elements(
                Group.builder()
                    .elements(
                        Line.builder()
                            .regex("Section: (.+)")
                            .bindProperties(PropertyBinding.of("title"))
                            .build(),
                        Repeat.builder()
                            .bindArray("items")
                            .mode(Repeat.Mode.ZERO_OR_MORE)
                            .elements(
                                Line.builder()
                                    .regex("  - (.+)")
                                    .bindProperties(PropertyBinding.of("item"))
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()
    )
    .build();
```

---

## Pattern Execution

### Basic Usage

```java
// Execute pattern against a document
String document = """
    Invoice #12345
    Widget A 2 19.99
    Widget B 1 12.50
    Total: 32.49
    """;

MatchResult result = DrexEngine.match(pattern, document);

if (result.isMatch()) {
    String extractedJson = result.toJson();
    System.out.println(extractedJson);
} else {
    System.out.println("Pattern did not match document");
}
```

### Error Handling

```java
try {
    MatchResult result = DrexEngine.match(pattern, document);
    
    if (result.isMatch()) {
        JsonNode data = result.getJsonNode();
        // Process extracted data
    } else {
        MatchFailure failure = result.getFailure();
        System.err.println("Match failed at line " + failure.getLineNumber() + 
                          ": " + failure.getReason());
    }
} catch (PatternException e) {
    System.err.println("Pattern error: " + e.getMessage());
} catch (DocumentException e) {
    System.err.println("Document processing error: " + e.getMessage());
}
```

### Position Metadata

```java
// Enable position tracking for debugging/UI
DrexEngine engine = DrexEngine.builder()
    .enablePositionTracking(true)
    .build();

MatchResult result = engine.match(pattern, document);

if (result.isMatch()) {
    String data = result.toJson();
    PositionMetadata positions = result.getPositionMetadata();
    
    // Get position information for specific fields
    TextBounds invoiceIdBounds = positions.getPosition("invoice.id");
    System.out.println("Invoice ID found at line " + invoiceIdBounds.getLine() + 
                      ", chars " + invoiceIdBounds.getStart() + "-" + invoiceIdBounds.getEnd());
}
```

---

## Pattern Loading and Serialization

### From JSON Files

```java
// Load pattern from JSON file
String patternJson = Files.readString(Paths.get("patterns/invoice-pattern.json"));
DrexPattern pattern = PatternDeserializer.deserialize(patternJson);

// Validate against schema
PatternValidator validator = new PatternValidator();
ValidationResult validation = validator.validate(pattern);
if (!validation.isValid()) {
    for (ValidationError error : validation.getErrors()) {
        System.err.println("Validation error: " + error.getMessage());
    }
}
```

### Pattern Serialization

```java
// Convert Java pattern to JSON
DrexPattern pattern = DrexPattern.builder()
    .name("MyPattern")
    .elements(/* pattern elements */)
    .build();

String json = PatternSerializer.serialize(pattern);
System.out.println(json);

// Save to file
Files.writeString(Paths.get("my-pattern.json"), json);
```

---

## Best Practices

### Pattern Design
1. **Start simple**: Begin with basic patterns and add complexity incrementally
2. **Use comments**: Document complex regex patterns and business logic
3. **Test thoroughly**: Use the provided example files for unit testing
4. **Order matters**: In `or` elements, place more specific patterns first

### Performance Optimization
1. **Minimize backtracking**: Drex uses greedy matching to avoid performance issues
2. **Use specific patterns**: Avoid overly general regex that might match unintended content
3. **Position tracking**: Only enable when needed for debugging or UI features

### Error Handling
1. **Validate patterns**: Always validate against the schema before use
2. **Handle match failures**: Check `isMatch()` before accessing results
3. **Graceful degradation**: Design patterns to handle missing optional sections

### Testing Strategy
1. **Use example files**: Test with realistic documents from `docs/examples/`
2. **Test edge cases**: Include documents with missing sections, variations
3. **Fuzzy testing**: For OCR documents, test with intentionally corrupted input

---

## Troubleshooting

### Common Issues and Solutions

#### Regex Escaping in JSON vs Java

**Problem**: Regex patterns behave differently in JSON files vs Java strings.

**Solution**: 
- **JSON patterns**: Use single backslash `"regex": "Invoice #(\\d+)"`
- **Java patterns**: Use double backslash `regex("Invoice #(\\\\d+)")`

```java
// JSON pattern file
{"line": {"regex": "Total: \\$([\\d\\.]+)"}}

// Java Fluent API equivalent  
Line.builder().regex("Total: \\\\$([\\\\d\\\\.]+)")
```

#### Pattern Not Matching Expected Lines

**Problem**: Pattern works in testing but fails on real documents.

**Debugging steps**:
1. **Enable position tracking** to see where matching stops
2. **Add logging** to see which lines are being processed
3. **Test with minimal patterns** to isolate the issue
4. **Check line endings** - Windows (`\r\n`) vs Unix (`\n`)

```java
DrexEngine engine = DrexEngine.builder()
    .enablePositionTracking(true)
    .build();

MatchResult result = engine.match(pattern, document);
if (!result.isMatch()) {
    MatchFailure failure = result.getFailure();
    System.err.println("Failed at line " + failure.getLineNumber() + 
                      ": '" + failure.getInputLine() + "'");
    System.err.println("Reason: " + failure.getReason());
}
```

#### Greedy Repeat Consuming Too Much

**Problem**: `repeat` elements match more lines than intended.

**Solutions**:
1. **Use negative lookahead** in regex patterns
2. **Make patterns more specific** 
3. **Reorder elements** to handle edge cases

```java
// Instead of this generic pattern:
Line.builder().regex("(\\S+)\\s+(\\d+)\\s+([\\d.]+)")

// Use specific context:
Line.builder().regex("^(?!Total:)(\\S+)\\s+(\\d+)\\s+([\\d.]+)")
```

#### Performance Issues with Large Documents

**Problem**: Pattern matching takes too long on large files.

**Solutions**:
1. **Optimize regex patterns** - avoid complex lookaheads/lookbehinds
2. **Use more specific patterns** early in `or` elements
3. **Disable position tracking** if not needed
4. **Process documents in chunks** if possible

```java
// Efficient pattern ordering in 'or' elements
Or.builder()
    .elements(
        Line.builder().regex("Specific Pattern").build(),  // Most common first
        Line.builder().regex("Less Specific").build(),     // Less common second
        Anyline.builder().build()                          // Fallback last
    )
    .build()
```

#### Formatter Not Working as Expected

**Problem**: Formatters don't produce expected output.

**Debugging**:
1. **Test formatters individually** before using in patterns
2. **Check original vs formatted text** in position metadata
3. **Verify formatter parameters** match input format

```java
// Enable position tracking to see original vs formatted values
PositionMetadata positions = result.getPositionMetadata();
String original = positions.getOriginalText("invoice.date");
String formatted = positions.getFormattedText("invoice.date");
System.out.println("Original: '" + original + "' → Formatted: '" + formatted + "'");
```

#### JSON Schema Validation Errors

**Problem**: Pattern fails validation against schema.

**Common validation errors**:
1. **Missing required fields**: Every pattern needs `elements` array
2. **Invalid repeat modes**: Use `oneOrMore`, `zeroOrMore`, or `zeroOrOne`
3. **Empty regex**: Line elements require non-empty `regex` field
4. **Invalid property names**: Property names must be valid JSON keys

```java
PatternValidator validator = new PatternValidator();
ValidationResult validation = validator.validate(patternJson);

for (ValidationError error : validation.getErrors()) {
    System.err.println("Error at " + error.getJsonPath() + ": " + error.getMessage());
}
```

### Performance Considerations

#### Memory Usage
- **Position tracking**: Increases memory usage proportionally to document size
- **Large arrays**: Repeat elements with many iterations use more memory
- **Deep nesting**: Deeply nested groups increase stack usage

#### Processing Speed  
- **Edit distance**: Higher values slow down fuzzy matching significantly
- **Complex regex**: Avoid expensive regex features when possible
- **Pattern complexity**: Simpler patterns with fewer alternatives perform better

#### Optimization Tips
1. **Test with realistic data sizes** during development
2. **Profile memory usage** with large documents
3. **Use timeouts** to prevent runaway matching
4. **Consider pattern simplification** for high-volume processing