# drex-java

Drex is a **document regex engine** that treats lines as tokens instead of characters.
It allows you to match patterns across semi-structured text documents (like invoices, logs, and purchase orders)
and extract structured **JSON output**. drex-java is the Java implementation of the Drex library.

Key features:
- **Line-based regex**: Process documents line by line, not character by character.
- **Structured JSON output**: Bind captured values into nested objects and arrays automatically.
- **Explainable extraction**: Patterns are explicit and easy to review or modify.
- **Dual authoring**: Write patterns as **JSON** or via a **Fluent Java API**.
- **Lean dependencies**: Only uses Jackson (JSON) and NetworkNT (JSON Schema validation).

## Quick Start

Add Drex to your project:

```gradle
dependencies {
    // JSON parsing and mapping
    implementation 'com.fasterxml.jackson.core:jackson-core:2.17.1'
    implementation 'com.fasterxml.jackson.core:jackson-databind:2.17.1'
    implementation 'com.fasterxml.jackson.core:jackson-annotations:2.17.1'

    // JSON Schema validation
    implementation 'com.networknt:json-schema-validator:1.0.90'
}
```
## Pattern Structure

Every Drex pattern is a **JSON object** with:
- Optional `version` and `name` metadata.
- An `elements` array, which holds the actual matching logic.

`elements` can include any combination of these node types:
- `line`
- `group`
- `repeat`
- `or`
- `anyline`

Composite nodes (`group`, `repeat`, `or`) also have their own `elements` arrays for their children.

## Basic Example

Given a document:
```
Invoice #12345
Pen 2 1.50
Notebook 1 3.99
Total: 6.99
```

And a Drex pattern (JSON):
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
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
      "or": {
        "elements": [
          { 
            "line": { 
              "regex": "Total: ([\\d\\.]+)", 
              "bindProperties": [{"property": "total"}] 
            } 
          },
          { "anyline": {} }
        ]
      }
    }
  ]
}
```

Produces structured output:
```json
{
  "invoice": {
    "id": "12345",
    "items": [
      { "name": "Pen", "qty": "2", "price": "1.50" },
      { "name": "Notebook", "qty": "1", "price": "3.99" }
    ],
    "total": "6.99"
  }
}
```

## Documentation

- **[Usage Guide](docs/usage.md)** - Pattern structure and API overview
- **[Examples](docs/examples.md)** - Common pattern examples  
- **[Binding & Formatting](docs/binding.md)** - How data extraction and formatting works
- **[Matching Algorithm](docs/matching.md)** - Understanding Drex's greedy, non-backtracking approach

