# Advanced Features

## Node Types

Each Drex pattern consists of an `elements` array of nodes, which can be:
- `line`: Matches a line with a regex; capture groups can bind values.
- `group`: Container for sequential sub-nodes (has `elements`).
- `repeat`: Repeats a sub-node (`zeroOrMore`, `oneOrMore`, `zeroOrOne`).
- `or`: Alternation; tries sub-nodes in order (has `elements`).
- `anyline`: Matches any line (like `.`).

## Binding Model

- `group` or `repeat` with `bind` pushes a JSON object or array entry.
- Bind paths are relative to the current object context.

## Converters

Bind entries may specify converters, e.g.:
- `"integer"`
- `"currency"`
- `"date(yyyy-MM-dd)"`

Custom converters can be registered.
