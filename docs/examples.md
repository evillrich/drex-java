# Examples

Drex pattern examples with top-level `elements`.

## Invoice Example

**Input Document:**
```
Invoice #12345
Pen 2 1.50
Notebook 1 3.99
Total: 6.99
```

**Pattern (JSON):**
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "elements": [
    { "group": {
        "bind": "invoice",
        "elements": [
          { "line": { "regex": "Invoice #(\\d+)", "bind": "id" } },
          { "repeat": {
              "bind": "items[]",
              "mode": "oneOrMore",
              "elements": [
                { "line": { "regex": "(\\S+)\\s+(\\d+)\\s+([\\d\\.]+)", "bind": ["name","qty","price"] } }
              ]
          }},
          { "or": {
              "elements": [
                { "line": { "regex": "Total: ([\\d\\.]+)", "bind": "total" } },
                { "anyline": {} }
              ]
          }}
        ]
    }}
  ]
}
```

**Extracted Output:**
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
