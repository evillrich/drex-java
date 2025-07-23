# Examples

Drex pattern examples

## Simple Invoice Example

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
  "comment": "Simple invoice extraction pattern",
  "elements": [
    { "group": {
        "bind": "invoice",
        "comment": "Group all invoice data",
        "elements": [
          { "line": { "regex": "Invoice #(\\d+)", "bind": "id", "comment": "Extract invoice number" } },
          { "repeat": {
              "bind": "items[]",
              "mode": "oneOrMore",
              "comment": "Extract line items",
              "elements": [
                { "line": { "regex": "(\\S+)\\s+(\\d+)\\s+([\\d\\.]+)", "bind": ["name","qty","price"], "comment": "Item name, quantity, price" } }
              ]
          }},
          { "or": {
              "comment": "Match total or skip line",
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

## Table Invoice Example

**Input Document:**
```
Invoice #67890
Date: 2024-01-15

Item	Quantity	Unit Price	Total
Laptop	1	899.99	899.99
Mouse	2	25.00	50.00
Keyboard	1	75.50	75.50

Subtotal: 1025.49
Tax: 82.04
Total: 1107.53
```

**Pattern (JSON):**
```json
{
  "version": "1.0",
  "name": "TableInvoicePattern",
  "comment": "Invoice pattern for tabular format",
  "elements": [
    { "group": {
        "bind": "invoice",
        "comment": "Main invoice container",
        "elements": [
          { "line": { "regex": "Invoice #(\\d+)", "bind": "id", "comment": "Invoice ID" } },
          { "line": { "regex": "Date: (\\d{4}-\\d{2}-\\d{2})", "bind": "date", "comment": "Invoice date in YYYY-MM-DD format" } },
          { "anyline": { "comment": "Skip blank line" } },
          { "line": { "regex": "Item\\tQuantity\\tUnit Price\\tTotal", "comment": "Table header" } },
          { "repeat": {
              "bind": "items[]",
              "mode": "oneOrMore",
              "comment": "Extract table rows",
              "elements": [
                { "line": { "regex": "([^\\t]+)\\t(\\d+)\\t([\\d\\.]+)\\t([\\d\\.]+)", "bind": ["item","quantity","unitPrice","total"], "comment": "Tab-separated item details" } }
              ]
          }},
          { "anyline": { "comment": "Skip blank line before totals" } },
          { "line": { "regex": "Subtotal: ([\\d\\.]+)", "bind": "subtotal" } },
          { "line": { "regex": "Tax: ([\\d\\.]+)", "bind": "tax" } },
          { "line": { "regex": "Total: ([\\d\\.]+)", "bind": "total", "comment": "Final total amount" } }
        ]
    }}
  ]
}
```

**Extracted Output:**
```json
{
  "invoice": {
    "id": "67890",
    "date": "2024-01-15",
    "items": [
      { "item": "Laptop", "quantity": "1", "unitPrice": "899.99", "total": "899.99" },
      { "item": "Mouse", "quantity": "2", "unitPrice": "25.00", "total": "50.00" },
      { "item": "Keyboard", "quantity": "1", "unitPrice": "75.50", "total": "75.50" }
    ],
    "subtotal": "1025.49",
    "tax": "82.04",
    "total": "1107.53"
  }
}
```
