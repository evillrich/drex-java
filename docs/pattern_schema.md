# Drex Pattern JSON Schema

A Drex pattern must be a JSON object with:
- Optional `version`, `name`, and `comment` metadata fields.
- Optional `bindObject` at the pattern level to bind the entire matched document.
- Optional `editDistance` for fuzzy matching (default is 0 for exact matching).
- An `elements` array holding the actual matching elements.

Element binding properties:
- `group` elements support optional `bindObject` field for data extraction
- `repeat` elements support optional `bindArray` field for data extraction (produces arrays)
- `line` and `anyline` elements support optional `bindProperties` field for data extraction
- All elements support optional `comment` field for documentation

Composite elements (`group`, `repeat`, `or`) also contain `elements` arrays for their children.

See `pattern-schema.json` for the full machine-readable schema.

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