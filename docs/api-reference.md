# Drex Java API Reference

Complete reference for the Drex Java Fluent API.

> **Implementation Note**: The Drex Java project is currently in early development with minimal implementation. Most classes described in this API reference represent the planned architecture and may not be fully implemented yet. This documentation serves as the design specification for the final implementation.

## Core Classes

### DrexPattern

The root pattern class that represents a complete document extraction pattern.

```java
public class DrexPattern extends CompositePatternElement {
    public static DrexPatternBuilder builder();
}

public class DrexPatternBuilder {
    public DrexPatternBuilder version(String version);
    public DrexPatternBuilder name(String name);
    public DrexPatternBuilder comment(String comment);
    public DrexPatternBuilder bindObject(String bindObject);
    public DrexPatternBuilder editDistance(int editDistance);
    public DrexPatternBuilder elements(PatternElement... elements);
    public DrexPatternBuilder elements(List<PatternElement> elements);
    public DrexPattern build();
}
```

**Example:**
```java
DrexPattern pattern = DrexPattern.builder()
    .version("1.0")
    .name("InvoicePattern")
    .comment("Extract invoice data")
    .bindObject("invoice")
    .editDistance(1)
    .elements(
        Line.builder()
            .regex("Invoice #(\\d+)")
            .bindProperties(PropertyBinding.of("id"))
            .build()
    )
    .build();
```

---

## Pattern Elements

### Line

Matches a line against a regular expression pattern and extracts data via capture groups.

```java
public class Line extends LineElement {
    public static LineBuilder builder();
}

public class LineBuilder {
    public LineBuilder regex(String regex);
    public LineBuilder comment(String comment);
    public LineBuilder bindProperties(PropertyBinding... bindings);
    public LineBuilder bindProperties(List<PropertyBinding> bindings);
    public Line build();
}
```

**Example:**
```java
Line invoiceLine = Line.builder()
    .regex("Invoice #(\\w+) Date: ([\\d/]+) Total: \\$([\\d.]+)")
    .bindProperties(
        PropertyBinding.of("invoiceNumber"),
        PropertyBinding.of("date", "parseDate(MM/dd/yyyy)"),
        PropertyBinding.of("total", "currency()")
    )
    .comment("Extract invoice header information")
    .build();
```

### Anyline

Matches any single line, typically used to consume lines without extracting data.

```java
public class Anyline extends LineElement {
    public static AnylineBuilder builder();
}

public class AnylineBuilder {
    public AnylineBuilder comment(String comment);
    public AnylineBuilder bindProperties(PropertyBinding... bindings);
    public AnylineBuilder bindProperties(List<PropertyBinding> bindings);
    public Anyline build();
}
```

**Example:**
```java
// Skip unknown lines
Anyline skip = Anyline.builder()
    .comment("Skip blank or header lines")
    .build();

// Capture any line content (rare usage)
Anyline capture = Anyline.builder()
    .bindProperties(PropertyBinding.of("unknownContent"))
    .build();
```

### Group

Groups pattern elements for sequential matching and creates a nested JSON object.

```java
public class Group extends CompositePatternElement {
    public static GroupBuilder builder();
}

public class GroupBuilder {
    public GroupBuilder bindObject(String bindObject);
    public GroupBuilder comment(String comment);
    public GroupBuilder elements(PatternElement... elements);
    public GroupBuilder elements(List<PatternElement> elements);
    public Group build();
}
```

**Example:**
```java
Group customerInfo = Group.builder()
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
            .build(),
        Line.builder()
            .regex("Phone: ([\\d-]+)")
            .bindProperties(PropertyBinding.of("phone", "phone(format=e164)"))
            .build()
    )
    .build();
```

### Repeat

Repeats pattern elements with specified repetition mode and creates a JSON array.

```java
public class Repeat extends CompositePatternElement {
    public static RepeatBuilder builder();
}

public class RepeatBuilder {
    public RepeatBuilder bindArray(String bindArray);
    public RepeatBuilder mode(Repeat.Mode mode);
    public RepeatBuilder comment(String comment);
    public RepeatBuilder elements(PatternElement... elements);
    public RepeatBuilder elements(List<PatternElement> elements);
    public Repeat build();
}

public enum Repeat.Mode {
    ZERO_OR_MORE,  // *
    ONE_OR_MORE,   // +
    ZERO_OR_ONE    // ?
}
```

**Example:**
```java
Repeat lineItems = Repeat.builder()
    .bindArray("items")
    .mode(Repeat.Mode.ONE_OR_MORE)
    .comment("Extract all line items")
    .elements(
        Line.builder()
            .regex("(\\d+)\\s+(.+)\\s+\\$([\\d.]+)")
            .bindProperties(
                PropertyBinding.of("quantity"),
                PropertyBinding.of("description"),
                PropertyBinding.of("price", "currency()")
            )
            .build()
    )
    .build();
```

### Or

Tries alternative pattern elements until one succeeds, similar to alternation in regex.

```java
public class Or extends CompositePatternElement {
    public static OrBuilder builder();
}

public class OrBuilder {
    public OrBuilder comment(String comment);
    public OrBuilder elements(PatternElement... elements);
    public OrBuilder elements(List<PatternElement> elements);
    public Or build();
}
```

**Example:**
```java
Or totalFormats = Or.builder()
    .comment("Handle different total line formats")
    .elements(
        Line.builder()
            .regex("Total: \\$([\\d,]+\\.\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build(),
        Line.builder()
            .regex("Amount Due: \\$([\\d,]+\\.\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build(),
        Line.builder()
            .regex("Grand Total: ([\\d,]+\\.\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build(),
        Anyline.builder()
            .comment("Skip if no total found")
            .build()
    )
    .build();
```

---

## Property Binding

### PropertyBinding

Defines how to bind a regex capture group to a JSON property with optional formatting.

```java
public class PropertyBinding {
    public static PropertyBinding of(String property);
    public static PropertyBinding of(String property, String format);
    
    public String getProperty();
    public String getFormat();
}
```

**Examples:**
```java
// Simple property binding
PropertyBinding.of("customerName")

// With formatter
PropertyBinding.of("orderDate", "parseDate(MM/dd/yyyy)")
PropertyBinding.of("totalAmount", "currency()")
PropertyBinding.of("phoneNumber", "phone(format=e164)")

// Complex formatter with parameters
PropertyBinding.of("description", "trim()")
PropertyBinding.of("amount", "accounting()")  // For negative amounts in parentheses
```

### Built-in Formatters

#### Date Formatters
```java
PropertyBinding.of("date", "parseDate(MM/dd/yyyy)")     // "07/23/2024" → "2024-07-23"
PropertyBinding.of("date", "parseDate(dd/MM/yyyy)")     // "23/07/2024" → "2024-07-23"  
PropertyBinding.of("date", "parseDate(MMM dd, yyyy)")   // "Jul 23, 2024" → "2024-07-23"
PropertyBinding.of("date", "parseDate(yyyy-MM-dd)")     // "2024-07-23" → "2024-07-23"
```

#### Number Formatters
```java
PropertyBinding.of("amount", "currency()")              // " $1,234.56 " → "1234.56"
PropertyBinding.of("amount", "decimal(format=#,##0.00)") // "1,234.56" → "1234.56"
PropertyBinding.of("amount", "accounting()")            // "(1,234.56)" → "-1234.56"
```

#### Text Formatters
```java
PropertyBinding.of("name", "trim()")                    // "  John Doe  " → "John Doe"
PropertyBinding.of("code", "upperCase()")               // "abc123" → "ABC123"
PropertyBinding.of("phone", "phone(format=e164)")       // "(555) 123-4567" → "+15551234567"
```

#### OCR Repair Formatters
```java
PropertyBinding.of("amount", "autocorrect(currency)")   // "$l,5OO.56" → "1500.56"
PropertyBinding.of("date", "autocorrect(parseDate, MM/dd/yyyy)") // "O7/Z3/2O24" → "2024-07-23"
```

---

## Pattern Execution

### DrexEngine

Main engine for executing patterns against documents.

```java
public class DrexEngine {
    public static DrexEngine create();
    public static DrexEngineBuilder builder();
    
    public MatchResult match(DrexPattern pattern, String document);
}

public class DrexEngineBuilder {
    public DrexEngineBuilder enablePositionTracking(boolean enabled);
    public DrexEngineBuilder setEditDistance(int editDistance);
    public DrexEngineBuilder setTimeout(Duration timeout);
    public DrexEngine build();
}
```

**Example:**
```java
DrexEngine engine = DrexEngine.builder()
    .enablePositionTracking(true)
    .setTimeout(Duration.ofSeconds(30))
    .build();

MatchResult result = engine.match(pattern, document);
```

### MatchResult

Contains the results of pattern matching.

```java
public class MatchResult {
    public boolean isMatch();
    public String toJson();
    public JsonNode getJsonNode();
    public PositionMetadata getPositionMetadata();
    public MatchFailure getFailure();  // When isMatch() is false
}
```

**Example:**
```java
MatchResult result = engine.match(pattern, document);

if (result.isMatch()) {
    // Success - process extracted data
    JsonNode data = result.getJsonNode();
    String jsonString = result.toJson();
    
    // Get position information if tracking enabled
    PositionMetadata positions = result.getPositionMetadata();
    if (positions != null) {
        TextBounds bounds = positions.getPosition("invoice.total");
        System.out.println("Total found at line " + bounds.getLine());
    }
} else {
    // Handle match failure
    MatchFailure failure = result.getFailure();
    System.err.println("Pattern failed at line " + failure.getLineNumber() + 
                      ": " + failure.getReason());
}
```

### MatchFailure

Information about why a pattern failed to match.

```java
public class MatchFailure {
    public int getLineNumber();
    public String getReason();
    public PatternElement getFailedElement();
    public String getInputLine();
}
```

---

## Position Metadata

### PositionMetadata

Tracks the location of extracted values in the source document.

```java
public class PositionMetadata {
    public TextBounds getPosition(String jsonPath);
    public Map<String, TextBounds> getAllPositions();
    public String getOriginalText(String jsonPath);
    public String getFormattedText(String jsonPath);
}
```

### TextBounds

Represents the position of text in a document.

```java
public class TextBounds {
    public int getLine();      // 1-based line number
    public int getStart();     // 0-based character offset in line
    public int getEnd();       // 0-based character offset in line (exclusive)
    
    public String toString();  // Format: "line:start-end"
}
```

**Example:**
```java
PositionMetadata positions = result.getPositionMetadata();

// Get position of specific field
TextBounds totalBounds = positions.getPosition("invoice.total");
System.out.println("Total found at " + totalBounds); // "line:5:15-20"

// Get all positions
Map<String, TextBounds> allPositions = positions.getAllPositions();
for (Map.Entry<String, TextBounds> entry : allPositions.entrySet()) {
    String jsonPath = entry.getKey();
    TextBounds bounds = entry.getValue();
    String originalText = positions.getOriginalText(jsonPath);
    String formattedText = positions.getFormattedText(jsonPath);
    
    System.out.println(jsonPath + " at " + bounds + 
                      ": '" + originalText + "' → '" + formattedText + "'");
}
```

---

## Pattern Serialization

### PatternDeserializer

Converts JSON pattern definitions to Java objects.

```java
public class PatternDeserializer {
    public static DrexPattern deserialize(String json);
    public static DrexPattern deserialize(JsonNode jsonNode);
    public static DrexPattern deserializeFromFile(Path path);
}
```

### PatternSerializer

Converts Java pattern objects to JSON.

```java
public class PatternSerializer {
    public static String serialize(DrexPattern pattern);
    public static JsonNode serializeToJsonNode(DrexPattern pattern);
    public static void serializeToFile(DrexPattern pattern, Path path);
}
```

### PatternValidator

Validates patterns against the JSON schema.

```java
public class PatternValidator {
    public ValidationResult validate(DrexPattern pattern);
    public ValidationResult validate(String json);
    public ValidationResult validateFile(Path path);
}

public class ValidationResult {
    public boolean isValid();
    public List<ValidationError> getErrors();
    public List<ValidationWarning> getWarnings();
}

public class ValidationError {
    public String getMessage();
    public String getJsonPath();
    public String getErrorCode();
}
```

**Example:**
```java
// Load and validate pattern
String json = Files.readString(Paths.get("invoice-pattern.json"));
PatternValidator validator = new PatternValidator();
ValidationResult validation = validator.validate(json);

if (validation.isValid()) {
    DrexPattern pattern = PatternDeserializer.deserialize(json);
    // Use pattern
} else {
    for (ValidationError error : validation.getErrors()) {
        System.err.println("Error at " + error.getJsonPath() + ": " + error.getMessage());
    }
}
```

---

## Exception Handling

### DrexException Hierarchy

```java
public class DrexException extends RuntimeException {
    // Base exception for all Drex-related errors
}

public class PatternException extends DrexException {
    // Pattern compilation or validation errors
}

public class DocumentException extends DrexException {
    // Document processing errors
}

public class MatchException extends DrexException {
    // Pattern matching execution errors
}

public class SerializationException extends DrexException {
    // JSON serialization/deserialization errors
}
```

**Example Error Handling:**
```java
try {
    DrexPattern pattern = PatternDeserializer.deserialize(json);
    MatchResult result = engine.match(pattern, document);
    
    if (result.isMatch()) {
        return result.toJson();
    } else {
        throw new MatchException("Pattern did not match: " + result.getFailure().getReason());
    }
    
} catch (PatternException e) {
    logger.error("Invalid pattern: " + e.getMessage(), e);
    throw new IllegalArgumentException("Pattern compilation failed", e);
    
} catch (DocumentException e) {
    logger.error("Document processing error: " + e.getMessage(), e);
    throw new RuntimeException("Failed to process document", e);
    
} catch (SerializationException e) {
    logger.error("JSON serialization error: " + e.getMessage(), e);
    throw new RuntimeException("Failed to serialize pattern", e);
}
```