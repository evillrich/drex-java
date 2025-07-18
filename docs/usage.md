# Usage

Drex patterns can be defined as JSON or built dynamically using the Fluent API.

Each pattern is a **JSON object** with `elements` (an array of pattern nodes). 
Nodes can be `line`, `group`, `repeat`, `or`, or `anyline`. 
Composite nodes (`group`, `repeat`, `or`) also have `elements` for their children.

## JSON Example

```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "elements": [
    { "line": { "regex": "Header (.*)", "bind": "header" } },
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
          }}
        ]
    }}
  ]
}
```

## Fluent Java API

The API builds the same structure in code:
```java
Pattern pattern = Pattern.builder()
    .line("Header (.*)", b -> b.bind("header"))
    .group("invoice", g -> g
        .line("Invoice #(\\d+)", b -> b.bind("id"))
        .repeat("items[]", RepeatMode.ONE_OR_MORE, r -> r
            .line("(\\S+)\\s+(\\d+)\\s+([\\d\\.]+)", b -> b
                .bind("name")
                .bind("qty", "integer")
                .bind("price", "currency")
            )
        )
    )
    .build();
```
