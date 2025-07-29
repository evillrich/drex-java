# Getting Started with Drex

This guide will help you get up and running with Drex for document extraction in just a few minutes.

## What is Drex?

Drex is a **document regex engine** that processes documents line-by-line to extract structured JSON data. Unlike traditional regex that works character-by-character, Drex treats each line as a token, making it perfect for semi-structured documents like invoices, purchase orders, and log files.

**Key benefits:**
- **Line-oriented matching**: More intuitive for document structure
- **Structured output**: Automatically creates nested JSON objects and arrays
- **Fuzzy matching**: Handles OCR errors and document variations
- **Dual authoring**: Write patterns as JSON or Java code
- **Fast execution**: Greedy, non-backtracking algorithm

---

## Installation

### Gradle

Add Drex to your `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.evillrich.drex:drex-java:${drex.version}'
}
```

### Maven

Add to your `pom.xml`:

```xml
<dependency>
    <groupId>io.github.evillrich.drex</groupId>
    <artifactId>drex-java</artifactId>
    <version>${drex.version}</version>
</dependency>
```

> **Note**: Drex is currently in development. Check the project repository for the latest available version or build from source.

---

## Your First Pattern

Let's start with a simple example - extracting data from an invoice.

### Sample Document

```
Invoice #12345
Widget A 2 19.99
Widget B 1 12.50
Total: 32.49
```

### Step 1: Define the Pattern (JSON)

Create a file `invoice-pattern.json`:

```json
{
  "version": "1.0",
  "name": "SimpleInvoice",
  "bindObject": "invoice",
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
    },
    {
      "line": {
        "regex": "Total: ([\\d\\.]+)",
        "bindProperties": [{"property": "total"}]
      }
    }
  ]
}
```

### Step 2: Execute the Pattern (Java)

```java
import io.github.evillrich.drex.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class InvoiceExtractor {
    public static void main(String[] args) throws Exception {
        // Load pattern from JSON file
        String patternJson = Files.readString(Paths.get("invoice-pattern.json"));
        DrexPattern pattern = PatternDeserializer.deserialize(patternJson);
        
        // Load document
        String document = """
            Invoice #12345
            Widget A 2 19.99
            Widget B 1 12.50
            Total: 32.49
            """;
        
        // Execute pattern
        DrexEngine engine = DrexEngine.create();
        MatchResult result = engine.match(pattern, document);
        
        if (result.isMatch()) {
            System.out.println("Extracted data:");
            System.out.println(result.toJson());
        } else {
            System.err.println("Pattern did not match: " + 
                              result.getFailure().getReason());
        }
    }
}
```

### Step 3: Output

The extracted JSON will be:

```json
{
  "invoice": {
    "id": "12345",
    "items": [
      {"name": "Widget", "qty": "2", "price": "19.99"},
      {"name": "Widget", "qty": "1", "price": "12.50"}
    ],
    "total": "32.49"
  }
}
```

---

## Alternative: Java Fluent API

Instead of JSON, you can define patterns directly in Java:

```java
DrexPattern pattern = DrexPattern.builder()
    .version("1.0")
    .name("SimpleInvoice")
    .bindObject("invoice")
    .elements(
        // Extract invoice ID
        Line.builder()
            .regex("Invoice #(\\\\d+)")
            .bindProperties(PropertyBinding.of("id"))
            .build(),
        
        // Extract line items (one or more)
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
            .build(),
        
        // Extract total
        Line.builder()
            .regex("Total: ([\\\\d\\\\.]+)")
            .bindProperties(PropertyBinding.of("total"))
            .build()
    )
    .build();
```

---

## Understanding Pattern Elements

### Line Elements

Match specific text patterns and extract data:

```json
{
  "line": {
    "regex": "Customer: (.+) Phone: ([\\d-]+)",
    "bindProperties": [
      {"property": "customerName"},
      {"property": "phone"}
    ]
  }
}
```

### Repeat Elements

Handle multiple occurrences (like line items):

```json
{
  "repeat": {
    "bindArray": "items",
    "mode": "oneOrMore",
    "elements": [
      {"line": {"regex": "...", "bindProperties": [...]}}
    ]
  }
}
```

**Modes:**
- `oneOrMore`: At least one match required (like `+` in regex)
- `zeroOrMore`: Zero or more matches (like `*` in regex)  
- `zeroOrOne`: Optional section (like `?` in regex)

### Group Elements

Create nested JSON objects:

```json
{
  "group": {
    "bindObject": "customer",
    "elements": [
      {"line": {"regex": "Name: (.+)", "bindProperties": [{"property": "name"}]}},
      {"line": {"regex": "Address: (.+)", "bindProperties": [{"property": "address"}]}}
    ]
  }
}
```

### Or Elements

Try alternative patterns:

```json
{
  "or": {
    "elements": [
      {"line": {"regex": "Total: \\$([\\d.]+)", "bindProperties": [{"property": "total"}]}},
      {"line": {"regex": "Amount: \\$([\\d.]+)", "bindProperties": [{"property": "total"}]}},
      {"anyline": {}}
    ]
  }
}
```

### Anyline Elements

Skip unwanted lines:

```json
{"anyline": {"comment": "Skip blank lines or headers"}}
```

---

## Adding Formatters

Formatters normalize extracted values:

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

**Common formatters:**
- `currency()`: `"$1,234.56"` → `"1234.56"`
- `parseDate(MM/dd/yyyy)`: `"07/23/2024"` → `"2024-07-23"`
- `trim()`: `"  text  "` → `"text"`
- `upperCase()`: `"hello"` → `"HELLO"`

---

## Handling OCR Documents

For documents with OCR errors, enable fuzzy matching:

```json
{
  "version": "1.0",
  "name": "OCRInvoice",
  "editDistance": 1,
  "bindObject": "invoice",
  "elements": [
    {
      "line": {
        "regex": "Invoice #(\\d+)",
        "bindProperties": [{"property": "id"}],
        "comment": "Handles 'lnvoice' instead of 'Invoice'"
      }
    }
  ]
}
```

The `editDistance: 1` allows up to 1 character edit (insertion, deletion, or substitution) when matching.

---

## Testing Your Patterns

### Unit Testing

```java
@Test
void testInvoicePattern() throws Exception {
    // Load pattern
    String patternJson = Files.readString(Paths.get("src/test/resources/invoice-pattern.json"));
    DrexPattern pattern = PatternDeserializer.deserialize(patternJson);
    
    // Load test document
    String document = Files.readString(Paths.get("src/test/resources/invoice.txt"));
    
    // Execute and verify
    MatchResult result = DrexEngine.create().match(pattern, document);
    
    assertTrue(result.isMatch(), "Pattern should match");
    
    JsonNode data = result.getJsonNode();
    assertEquals("12345", data.get("invoice").get("id").asText());
    assertEquals(2, data.get("invoice").get("items").size());
}
```

### Validation

Always validate patterns before use:

```java
PatternValidator validator = new PatternValidator();
ValidationResult validation = validator.validate(patternJson);

if (!validation.isValid()) {
    for (ValidationError error : validation.getErrors()) {
        System.err.println("Validation error: " + error.getMessage());
    }
    return;
}
```

---

## Common Patterns

### Skip Unknown Lines

```json
{
  "repeat": {
    "bindArray": "data",
    "mode": "zeroOrMore",
    "elements": [
      {
        "or": {
          "elements": [
            {"line": {"regex": "Important: (.+)", "bindProperties": [{"property": "text"}]}},
            {"anyline": {}}
          ]
        }
      }
    ]
  }
}
```

### Optional Sections

```json
{
  "repeat": {
    "bindArray": "notes",
    "mode": "zeroOrOne",
    "elements": [
      {"line": {"regex": "Notes: (.+)", "bindProperties": [{"property": "note"}]}}
    ]
  }
}
```

### Multiple Format Support

```json
{
  "or": {
    "elements": [
      {"line": {"regex": "Total: \\$([\\d.]+)", "bindProperties": [{"property": "total"}]}},
      {"line": {"regex": "Total: ([\\d.]+) USD", "bindProperties": [{"property": "total"}]}},
      {"line": {"regex": "Amount: ([\\d.]+)", "bindProperties": [{"property": "total"}]}}
    ]
  }
}
```

---

## Next Steps

1. **Explore Examples**: Check out `docs/examples/` for more complex patterns
2. **Read the API Reference**: See `docs/api-reference.md` for complete API documentation
3. **Understand Binding**: Read `docs/binding.md` for advanced data binding techniques
4. **Learn Matching Behavior**: See `docs/matching.md` for pattern design best practices
5. **Try Real Documents**: Start with your own document types

## Need Help?

- **Documentation**: All guides are in the `docs/` directory
- **Examples**: Complete examples with test files in `docs/examples/`
- **Issues**: Report problems on the project issue tracker

Happy extracting! 🚀