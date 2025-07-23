# Drex Binding and Formatting Guide

This document explains how **Drex patterns** bind extracted values to JSON structures and normalize captured values using **formatters**.

Drex processes documents line by line (like a line-oriented regex) and outputs a structured JSON object that can be consumed directly or validated/deserialized into typed objects.

---

## **Binding Rules**

### 1. Scopes and Objects
- Any composite node (`pattern`, `group`, `repeat`, `or`) with a `bind` **creates a new JSON object scope**.
- If `bind` is missing, child nodes bind **directly into the parent scope** (bubble up).
- This keeps the **pattern hierarchy** and **output hierarchy** decoupled — you only get a new object when you explicitly ask for one.

### 2. Repeats Always Produce Arrays
- A `repeat` node must bind to a field with a `[]` suffix (e.g., `"items[]"`).
- Each iteration:
    - Creates a **new child object** (even for a single scalar field),
    - Pushes that object into the array.
- Arrays are **always present**:
    - `[]` when there are zero matches,
    - `[ { ... } ]` when one match,
    - `[ { ... }, { ... } ]` for multiple matches.

### 3. Lines Bind into Current Scope
- `line` and `anyline` nodes never create new objects; they **populate properties on the current scope**.
- The `bind` value can be:
    - **String** – maps the entire match (or first capture group) to a field.
    - **Array** – maps multiple capture groups by index to named fields.
    - **Object(s)** – each object specifies:
        - `path`: field name in JSON,
        - Optional `format`: a named formatter (see below).

### 4. Or Nodes Follow Group Rules
- If `bind` is present, the matched branch is wrapped in a new object.
- If not, its children bind directly into the parent scope.

### 5. Root Pattern
- Acts like a `group`:
    - If it has `bind`, that’s the root object.
    - If not, the top-level JSON object is populated by its children.

---

## **Formatters**

Formatters normalize captured values to **consistent strings** that can be:
- Easily deserialized into typed objects when clean,
- Or left as-is when OCR or noisy data prevents reliable normalization.

**Key principles:**
- A formatter **never halts processing**.
- If normalization succeeds → the normalized string is output.
- If normalization fails → the **original captured string** is output.

### Registry and Extensibility
- Drex uses a **formatter registry**, pre-populated with built-ins (dates, numbers, booleans, phone, text).
- Callers can:
    - Add new formatters,
    - Override built-ins,
    - Implement **repair + format** formatters for OCR-damaged inputs.
- Formatters can accept **parameters** (e.g., date formats) via a colon-delimited syntax:
  ```json
  { "path": "createdAt", "format": "iso_date:MM/DD/YYYY" }
  ```

---

## **Built-In Formatters**

### Dates
- `iso_date:MM/DD/YYYY` – `"07/23/2024"` → `"2024-07-23"`
- `iso_date:DD/MM/YYYY` – `"23/07/2024"` → `"2024-07-23"`
- `iso_date:MMM DD, YYYY` – `"Jul 23, 2024"` → `"2024-07-23"`

### Datetimes
- `iso_datetime:MM/DD/YYYY HH:mm` – `"07/23/2024 14:30"` → `"2024-07-23T14:30:00Z"`
- `iso_datetime:MM/DD/YYYY h:mm a` – `"07/23/2024 2:30 PM"` → `"2024-07-23T14:30:00Z"`

### Numbers
- `decimal:#,##0.00` – `"1,234.56"` → `"1234.56"`
- `decimal:(#,##0.00)` – `"(1,234.56)"` → `"-1234.56"` (accounting style)

### Booleans
- `boolean:yes/no` – `"Yes"` → `"true"`, `"No"` → `"false"`

### Phones
- `e164:(###) ###-####` – `"(555) 123-4567"` → `"+15551234567"`

### Text
- `uppercase:any` – `"hello world"` → `"HELLO WORLD"`
- `trim:any` – `"  hello  "` → `"hello"`

---

## **OCR Repair Formatters**
- These attempt simple character substitutions (`O → 0`, `Z → 2`, `l → 1`, etc.) before normalizing.
- Examples:
    - `decimal_repair:#,##0.00` – `"1,Z34.5O"` → `"1234.50"`
    - `iso_date_repair:MM/DD/YYYY` – `"O7/Z3/2O24"` → `"2024-07-23"`

---

## **Examples**

### Invoice with Repair Formatters

Pattern:
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "bind": "invoice",
  "tokens": [
    { "line": {
        "regex": "Invoice #(\\w+)",
        "bind": { "path": "id" }
    }},
    { "line": {
        "regex": "Date: ([\\d/ZO]+)",
        "bind": { "path": "createdAt", "format": "iso_date_repair:MM/DD/YYYY" }
    }},
    { "repeat": {
        "bind": "items[]",
        "tokens": [
          { "line": {
              "regex": "(\\S+)\\s+(\\S+)\\s+([\\dO\\.]+)",
              "bind": [
                { "path": "name" },
                { "path": "qty", "format": "decimal_repair:#,##0" },
                { "path": "price", "format": "decimal_repair:#,##0.00" }
              ]
          } }
        ]
    }},
    { "line": {
        "regex": "Total: ([\\dO,\\.\\(\\)]+)",
        "bind": { "path": "total", "format": "decimal_repair:(#,##0.00)" }
    }}
  ]
}
```

OCR’d Document:
```
Invoice #AB123
Date: O7/Z3/2O24
WidgetA 1O 19.OO
Total: (1,Z34.5O)
```

Output:
```json
{
  "invoice": {
    "id": "AB123",
    "createdAt": "2024-07-23",
    "items": [
      { "name": "WidgetA", "qty": "10", "price": "19.00" }
    ],
    "total": "-1234.50"
  }
}
```
If any repair fails (e.g., `"1OQ"`), the **raw captured string** (`"1OQ"`) is output unchanged.
