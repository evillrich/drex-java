# Drex Binding and Formatting Guide

This document explains how **Drex patterns** bind extracted values to JSON structures and normalize captured values using **formatters**.

Drex processes documents line by line (like a line-oriented regex) and outputs:
1. **Structured JSON** with all string values (no type conversion)
2. **Optional position metadata** in a separate file for debugging/UI workflows

---

## **Updated Binding Model**

### 1. Context Creation vs Value Assignment
We distinguish between **creating JSON structure** and **assigning values**:

- **`bindObject`**: Creates a new JSON object context (used by `pattern`, `group`, `or`)
- **`bindArray`**: Creates a new JSON array context (used by `repeat` only)
- **`bindProperties`**: Assigns values to the current context (used by `line`, `anyline`)

### 2. Path Types
- **Absolute paths**: `$.invoice.total` (from root)
- **Relative paths**: `total` (from current context)

### 3. Context Management Rules
- Composite elements (`group`, `repeat`, `or`) **only push new context if they have a bind property**
- If no bind property is present, children bind **directly into current context** (bubble up)
- This keeps pattern hierarchy and output hierarchy decoupled

### 4. Element Binding Behavior

**Pattern (Root)**:
```json
{"pattern": {"bindObject": "invoice"}} 
// Creates: {"invoice": {...}}
```

**Group (Nested Object)**:
```json
{"group": {"bindObject": "header"}}
// Creates object at: currentContext.header = {}
```

**Repeat (Array Creation)**:
```json
{"repeat": {"bindArray": "lineItems"}}
// Creates array at: currentContext.lineItems = []
// Each iteration creates a new object pushed to the array
```

**Or (Object - First Match)**:
```json
{"or": {"bindObject": "address"}}
// Creates object at: currentContext.address = {}
```

### 5. Line Element Binding
Lines extract multiple values from regex capture groups:

```json
{
  "line": {
    "regex": "Invoice #(\\d+) Date: (\\d{2}/\\d{2}/\\d{4}) Total: \\$([0-9.]+)",
    "bindProperties": [
      {"property": "number"},                                    // capture 1
      {"property": "date", "format": "iso_date(MM/dd/yyyy)"},   // capture 2  
      {"property": "total", "format": "currency()"}             // capture 3
    ]
  }
}
```

**Key principles**:
- Capture groups map to bindProperties array by index (first binding = capture 1, etc.)
- No need to specify capture numbers explicitly
- Single captures still use array format for consistency

---

## **Output Format**

### Structured JSON (Primary Output)
- **All values are strings** - no type conversion performed by Drex
- **Normalized strings** via formatters for downstream parsing
- **Clean, consumable structure** - nested objects/arrays as defined by patterns

### Position Metadata (Optional Separate File)
When position tracking is enabled, generates a separate file with location information:

```json
{
  "invoice.number": {
    "textBounds": {"line": 3, "start": 9, "end": 14},
    "originalText": "Invoice #12345",
    "formattedText": "12345"
  },
  "invoice.lineItems[0].description": {
    "textBounds": {"line": 5, "start": 0, "end": 8},
    "originalText": "Widget A",
    "formattedText": "Widget A"
  }
}
```

**Use cases for position metadata**:
- **Debugging** - "Why didn't this pattern match?"
- **UI workflows** - Highlight extracted fields for human correction
- **OCR correction** - Click on field to see source region
- **Audit trails** - Track extraction provenance

---

## **Formatters**

Formatters normalize captured values to **consistent strings**. They use function call syntax with parameters:

```json
{"property": "total", "format": "currency(repair=true)"}
{"property": "date", "format": "iso_date(MM/dd/yyyy)"}
```

**Key principles**:
- Formatters **never halt processing**
- Success → normalized string output
- Failure → **original captured string** output
- All output values remain strings (no type conversion)

### Function Call Syntax
```json
// Simple function
{"property": "amount", "format": "currency()"}

// With parameters  
{"property": "date", "format": "parseDate(MM/dd/yyyy)"}

// Multiple parameters
{"property": "phone", "format": "e164(country=US, strict=false)"}
```

### Built-In Formatters

**Dates**:
- `parseDate(MM/dd/yyyy)` – `"07/23/2024"` → `"2024-07-23"`
- `parseDate(dd/MM/yyyy)` – `"23/07/2024"` → `"2024-07-23"`
- `parseDate(MMM dd, yyyy)` – `"Jul 23, 2024"` → `"2024-07-23"`

**Numbers**:
- `currency()` – `" $1,234.56 "` → `"1234.56"`
- `decimal(format=#,##0.00)` – `"1,234.56"` → `"1234.56"`
- `accounting()` – `"(1,234.56)"` → `"-1234.56"`

**Text**:
- `trim()` – `"  hello  "` → `"hello"`
- `upperCase()` – `"hello world"` → `"HELLO WORLD"`
- `phone(format=e164)` – `"(555) 123-4567"` → `"+15551234567"`

**OCR Repair Functions**:
- `autocorrect(currency)` – `"$l,5OO.56"` → `"1500.56"` (fixes O→0, l→1)
- `autocorrect(parseDate, MM/dd/yyyy)` – `"O7/Z3/2O24"` → `"2024-07-23"`

---

## **Examples**

### Complete Invoice Pattern

```json
{
  "version": "1.0", 
  "name": "invoice-extractor",
  "bindObject": "invoice",
  "elements": [
    {
      "group": {
        "bindObject": "header", 
        "elements": [
          {
            "line": {
              "regex": "Invoice #(\\w+) Date: ([\\d/]+)",
              "bindProperties": [
                {"property": "number"},
                {"property": "date", "format": "parseDate(MM/dd/yyyy)"}
              ]
            }
          }
        ]
      }
    },
    {
      "repeat": {
        "bindArray": "lineItems",
        "mode": "oneOrMore",
        "elements": [
          {
            "line": {
              "regex": "(\\S+)\\s+(\\d+)\\s+\\$([\\d.]+)",
              "bindProperties": [
                {"property": "description"},
                {"property": "quantity", "format": "trim()"},
                {"property": "amount", "format": "currency()"}
              ]
            }
          }
        ]
      }
    },
    {
      "line": {
        "regex": "Total: \\$([\\d.]+)",
        "bindProperties": [{"property": "total", "format": "currency()"}]
      }
    }
  ]
}
```

**Input Document**:
```
Invoice #AB123 Date: 07/23/2024
Widget A 10 $19.99
Widget B 5 $12.50
Total: $32.49
```

**Output (extracted.json)**:
```json
{
  "invoice": {
    "header": {
      "number": "AB123",
      "date": "2024-07-23"
    },
    "lineItems": [
      {"description": "Widget A", "quantity": "10", "amount": "19.99"},
      {"description": "Widget B", "quantity": "5", "amount": "12.50"}
    ],
    "total": "32.49"
  }
}
```

**Position Metadata (extracted_positions.json)**:
```json
{
  "invoice.header.number": {
    "textBounds": {"line": 1, "start": 9, "end": 14},
    "originalText": "AB123",
    "formattedText": "AB123"
  },
  "invoice.header.date": {
    "textBounds": {"line": 1, "start": 21, "end": 31},
    "originalText": "07/23/2024", 
    "formattedText": "2024-07-23"
  },
  "invoice.lineItems[0].description": {
    "textBounds": {"line": 2, "start": 0, "end": 8},
    "originalText": "Widget A",
    "formattedText": "Widget A"
  }
}
```

---

## **Design Rationale**

### Why All String Output?
- **OCR compatibility** - Text may contain errors that prevent type conversion
- **Graceful degradation** - Failed formatting doesn't break extraction
- **Downstream choice** - Users decide when it's safe to convert to typed objects
- **Schema validation** - Can validate/convert strings after extraction

### Why Separate Position Files?
- **Clean primary output** - No metadata pollution in business data
- **Optional complexity** - Positions only when needed (debugging, UI)
- **Performance** - Can skip position tracking in production
- **Different use cases** - Batch processing vs interactive correction workflows

### Why Explicit bindObject/bindArray?
- **Clear intent** - Obvious what JSON structure is created
- **Type safety** - Only repeat creates arrays
- **Self-documenting** - Pattern shows output structure
- **Validation friendly** - Can enforce correct binding usage