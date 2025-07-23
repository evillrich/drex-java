# Drex Pattern JSON Schema

A Drex pattern must be a JSON object with:
- Optional `version`, `name`, and `comment` metadata fields.
- Optional `bind` at the pattern level to bind the entire matched document.
- Optional `editDistance` for fuzzy matching (default is 0 for exact matching).
- An `elements` array holding the actual matching elements.

All elements (`group`, `repeat`, `or`, `line`, `anyline`) support:
- Optional `comment` field for documentation
- Optional `bind` field for data extraction

Composite elements (`group`, `repeat`, `or`) also contain `elements` arrays for their children.

See `pattern-schema.json` for the full machine-readable schema.

## Example
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "comment": "Pattern to extract invoice information",
  "bind": "document",
  "elements": [
    { "line": { "regex": "Header (.*)", "bind": "header", "comment": "Match header line" } },
    { "group": {
        "bind": "invoice",
        "comment": "Group invoice details",
        "elements": [
          { "line": { "regex": "Invoice #(\\d+)", "bind": "id" } }
        ]
    }}
  ]
}
```