# Drex Pattern JSON Schema

This document provides detailed explanations of the Drex pattern JSON schema. For the complete machine-readable schema, see `pattern-schema.json`.

## Pattern Root Object

A Drex pattern must be a JSON object with these properties:

### Required Properties

#### `elements` (array)
**Required**: Yes  
**Description**: Array of pattern elements that define the matching logic.  
**Type**: Array of [PatternElement](#pattern-elements)  
**Example**: 
```json
"elements": [
  {"line": {"regex": "Invoice #(\\d+)", "bindProperties": [{"property": "id"}]}},
  {"repeat": {"mode": "oneOrMore", "elements": [...]}}
]
```

### Optional Properties

#### `version` (string)
**Required**: No  
**Description**: Version identifier for the pattern, useful for pattern evolution and compatibility tracking.  
**Example**: `"version": "1.0"`

#### `name` (string)
**Required**: No  
**Description**: Human-readable name for the pattern, useful for documentation and debugging.  
**Example**: `"name": "InvoiceExtractor"`

#### `comment` (string)
**Required**: No  
**Description**: Documentation comment explaining the pattern's purpose.  
**Example**: `"comment": "Extracts invoice data including line items and totals"`

#### `bindObject` (string)
**Required**: No  
**Description**: If present, creates a JSON object at the root level and binds all extracted data under this key. Behaves like a top-level group bind.  
**Example**: `"bindObject": "invoice"` results in `{"invoice": {...extracted data...}}`

#### `editDistance` (integer)
**Required**: No  
**Default**: 0  
**Minimum**: 0  
**Description**: Maximum Levenshtein edit distance allowed for fuzzy matching. When > 0, enables fuzzy matching that can handle OCR errors and document variations.  
**Example**: `"editDistance": 1` allows up to 1 character insertion, deletion, or substitution.

---

## Pattern Elements

Pattern elements are the building blocks of Drex patterns. Each element is a JSON object with exactly one property that determines its type.

### Element Types

#### `line` - Line Matching Element

Matches a line against a regular expression pattern and extracts data via capture groups.

**Structure**:
```json
{
  "line": {
    "regex": "string (required)",
    "bindProperties": "array (optional)",
    "comment": "string (optional)"
  }
}
```

**Properties**:
- **`regex`** (required): Regular expression pattern. Cannot be empty. Uses standard Java regex syntax.
- **`bindProperties`** (optional): Array of [PropertyBinding](#property-binding) objects that map capture groups to JSON properties.
- **`comment`** (optional): Documentation comment.

**Example**:
```json
{
  "line": {
    "regex": "Invoice #(\\w+) Date: ([\\d/]+) Total: \\$([\\d.]+)",
    "bindProperties": [
      {"property": "invoiceNumber"},
      {"property": "date", "format": "parseDate(MM/dd/yyyy)"},
      {"property": "total", "format": "currency()"}
    ],
    "comment": "Extract invoice header information"
  }
}
```

#### `anyline` - Match Any Line Element

Matches any single line, similar to `.` in regex but for entire lines. Typically used to consume lines without extracting data.

**Structure**:
```json
{
  "anyline": {
    "bindProperties": "array (optional)",
    "comment": "string (optional)"
  }
}
```

**Properties**:
- **`bindProperties`** (optional): Rarely used since anyline typically consumes unwanted lines.
- **`comment`** (optional): Documentation comment.

**Example**:
```json
{"anyline": {"comment": "Skip blank line or header"}}
```

#### `group` - Grouping Element

Groups pattern elements for sequential matching and creates a nested JSON object context.

**Structure**:
```json
{
  "group": {
    "elements": "array (required)",
    "bindObject": "string (optional)",
    "comment": "string (optional)"
  }
}
```

**Properties**:
- **`elements`** (required): Array of child pattern elements to match sequentially.
- **`bindObject`** (optional): Creates a new JSON object under this key.
- **`comment`** (optional): Documentation comment.

**Example**:
```json
{
  "group": {
    "bindObject": "customer",
    "comment": "Customer information section",
    "elements": [
      {"line": {"regex": "Name: (.+)", "bindProperties": [{"property": "name"}]}},
      {"line": {"regex": "Phone: ([\\d-]+)", "bindProperties": [{"property": "phone"}]}}
    ]
  }
}
```

#### `repeat` - Repetition Element

Repeats pattern elements with specified repetition mode and creates a JSON array.

**Structure**:
```json
{
  "repeat": {
    "mode": "enum (required)",
    "elements": "array (required)",
    "bindArray": "string (optional)",
    "comment": "string (optional)"
  }
}
```

**Properties**:
- **`mode`** (required): Repetition mode. Valid values:
  - `"zeroOrMore"`: Like `*` in regex - matches 0 or more times
  - `"oneOrMore"`: Like `+` in regex - matches 1 or more times (fails if no matches)
  - `"zeroOrOne"`: Like `?` in regex - matches 0 or 1 time (optional)
- **`elements`** (required): Array of child pattern elements to repeat.
- **`bindArray`** (optional): Creates a JSON array under this key. Always produces an array output (empty array if no matches).
- **`comment`** (optional): Documentation comment.

**Example**:
```json
{
  "repeat": {
    "bindArray": "lineItems",
    "mode": "oneOrMore",
    "comment": "Extract all invoice line items",
    "elements": [
      {
        "line": {
          "regex": "(\\d+)\\s+(.+)\\s+\\$([\\d.]+)",
          "bindProperties": [
            {"property": "quantity"},
            {"property": "description"},
            {"property": "price", "format": "currency()"}
          ]
        }
      }
    ]
  }
}
```

#### `or` - Alternative Element

Tries alternative pattern elements until one succeeds, similar to alternation `|` in regex.

**Structure**:
```json
{
  "or": {
    "elements": "array (required)",
    "comment": "string (optional)"
  }
}
```

**Properties**:
- **`elements`** (required): Array of alternative pattern elements. Tried in order; first successful match is used.
- **`comment`** (optional): Documentation comment.

**Important**: `or` elements have no binding capabilities - they don't create JSON structure themselves. The chosen alternative element handles any data binding.

**Example**:
```json
{
  "or": {
    "comment": "Handle different total line formats",
    "elements": [
      {"line": {"regex": "Total: \\$([\\d.]+)", "bindProperties": [{"property": "total"}]}},
      {"line": {"regex": "Amount Due: \\$([\\d.]+)", "bindProperties": [{"property": "total"}]}},
      {"anyline": {"comment": "Skip if no total found"}}
    ]
  }
}
```

---

## Property Binding

PropertyBinding objects define how regex capture groups map to JSON properties with optional formatting.

**Structure**:
```json
{
  "property": "string (required)",
  "format": "string (optional)"
}
```

**Properties**:
- **`property`** (required): JSON property name to bind to. Supports both relative paths (`"total"`) and absolute paths (`"$.invoice.total"`).
- **`format`** (optional): Formatter function to normalize captured values using function call syntax.

### Binding Order
PropertyBinding objects in the `bindProperties` array are mapped to regex capture groups by index:
- First binding → Capture group 1
- Second binding → Capture group 2
- etc.

### Formatter Functions
Formatters use function call syntax with optional parameters:

**Date formatters**:
- `parseDate(MM/dd/yyyy)`: Converts `"07/23/2024"` → `"2024-07-23"`
- `parseDate(dd/MM/yyyy)`: Converts `"23/07/2024"` → `"2024-07-23"`

**Number formatters**:
- `currency()`: Converts `" $1,234.56 "` → `"1234.56"`
- `accounting()`: Converts `"(1,234.56)"` → `"-1234.56"`

**Text formatters**:
- `trim()`: Converts `"  text  "` → `"text"`
- `upperCase()`: Converts `"hello"` → `"HELLO"`

**OCR repair**:
- `autocorrect(currency)`: Converts `"$l,5OO.56"` → `"1500.56"`

---

## Schema Validation Rules

### Required Field Validation
- Root pattern must have `elements` array
- `line` elements must have non-empty `regex` 
- `repeat` elements must have `mode` and `elements`
- `group` and `or` elements must have `elements`

### Value Constraints
- `editDistance` must be ≥ 0
- `repeat.mode` must be one of: `zeroOrMore`, `oneOrMore`, `zeroOrOne`
- `regex` cannot be empty string

### Structure Constraints
- `elements` arrays cannot be empty
- PropertyBinding `property` field cannot be empty
- Only one element type property allowed per pattern element

## Example
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "comment": "Pattern to extract invoice information",
  "bindObject": "document",
  "elements": [
    { "line": { "regex": "Header (.*)", "bindProperties": [{"property": "header"}], "comment": "Match header line" } },
    { "group": {
        "bindObject": "invoice",
        "comment": "Group invoice details",
        "elements": [
          { "line": { "regex": "Invoice #(\\d+)", "bindProperties": [{"property": "id"}] } }
        ]
    }}
  ]
}
```