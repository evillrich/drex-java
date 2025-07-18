# Drex Pattern JSON Schema

A Drex pattern must be a JSON object with:
- Optional `version` and `name` metadata.
- An `elements` array holding the actual matching nodes.

Composite nodes (`group`, `repeat`, `or`) also contain `elements` arrays for their children.

See `pattern-schema.json` for the full machine-readable schema.

## Example
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "elements": [
    { "line": { "regex": "Header (.*)", "bind": "header" } },
    { "group": {
        "bind": "invoice",
        "elements": [
          { "line": { "regex": "Invoice #(\\d+)", "bind": "id" } }
        ]
    }}
  ]
}
```