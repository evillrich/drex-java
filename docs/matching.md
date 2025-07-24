# Drex Matching Algorithm

This document explains how Drex's matching algorithm works and how to design patterns that work effectively with its greedy, non-backtracking approach.

## Algorithm Overview

Drex uses a **greedy, non-backtracking** matching algorithm that processes documents line-by-line from top to bottom. When the matcher encounters a choice (like `repeat` or `or` elements), it makes a greedy decision and commits to that path without reconsidering if matching later fails.

### Key Characteristics:

1. **Linear Processing**: Documents are processed line-by-line, once, from start to finish
2. **Greedy Choices**: When faced with options, always takes the "more" option first
3. **No Backtracking**: Once a choice is made, the matcher never goes back to try alternatives
4. **Deterministic**: Same pattern + same document = same result every time

## How Element Types Behave

### Repeat Elements

For `repeat` elements, the matcher always tries the "more" option first:

- **`oneOrMore`**: Tries to match as many iterations as possible
- **`zeroOrMore`**: Tries to match as many iterations as possible (could be zero)
- **`zeroOrOne`**: Tries to match once first, falls back to zero only if the match fails

**Example:**
```json
{
  "repeat": {
    "mode": "oneOrMore",
    "elements": [
      {"line": {"regex": "(\\S+)\\s+(\\d+)\\s+([\\d.]+)"}}
    ]
  }
}
```

Given these lines:
```
Widget A 2 19.99
Widget B 1 12.50
Total: 32.49
```

The repeat will greedily consume the first two lines and stop when the third line doesn't match the pattern.

### Or Elements

For `or` elements, the matcher tries alternatives in order and takes the first successful match:

```json
{
  "or": {
    "elements": [
      {"line": {"regex": "Total: ([\\d.]+)"}},     // Tried first
      {"line": {"regex": "Subtotal: ([\\d.]+)"}}, // Tried second
      {"anyline": {}}                              // Fallback
    ]
  }
}
```

**Important**: Order matters! Put more specific patterns before general ones.

## Design Principles for Effective Patterns

### 1. Specific Before General

Always put more specific patterns before general ones in `or` elements:

**Good:**
```json
{"or": {"elements": [
  {"line": {"regex": "Invoice #(\\d+)"}},  // Specific
  {"line": {"regex": "Invoice (\\w+)"}},   // General
  {"anyline": {}}                          // Catch-all
]}}
```

**Problematic:**
```json
{"or": {"elements": [
  {"line": {"regex": "Invoice (\\w+)"}},   // Too general - catches everything
  {"line": {"regex": "Invoice #(\\d+)"}}   // Never reached
]}}
```

### 2. Use Context to Disambiguate

When patterns might be ambiguous, use surrounding context or more specific regex:

**Instead of:**
```json
{"line": {"regex": "([\\d.]+)"}}  // Matches any number
```

**Use:**
```json
{"line": {"regex": "Total: ([\\d.]+)"}}  // Matches only totals
```

### 3. Design for the Common Case

Structure patterns to handle the most common document format first:

```json
{
  "or": {
    "elements": [
      {"line": {"regex": "Total: \\$([\\d.]+)"}},    // Most common: $32.49
      {"line": {"regex": "Total: ([\\d.]+) USD"}},   // Less common: 32.49 USD  
      {"line": {"regex": "Total: ([\\d.]+)"}}        // Fallback: 32.49
    ]
  }
}
```

## Working with the Algorithm

### Advantages

1. **Predictable Performance**: O(n) time complexity, no exponential blowup
2. **Easy to Debug**: Match behavior is deterministic and easy to trace
3. **Suitable for Documents**: Business documents have predictable structure
4. **Fast Execution**: No backtracking overhead

### Limitations and Workarounds

#### Problem: Greedy Repeat Consuming Too Much

**Scenario**: A repeat element consumes lines that should be handled by subsequent elements.

**Solution**: Use more specific patterns or add negative lookahead context:

```json
// Instead of matching any line with 3 numbers:
{"line": {"regex": "(\\S+)\\s+(\\d+)\\s+([\\d.]+)"}}

// Be more specific about what constitutes a line item:
{"line": {"regex": "^(?!Total:)(\\S+)\\s+(\\d+)\\s+([\\d.]+)"}}
```

#### Problem: Optional Sections

**Scenario**: Optional document sections might not appear, causing patterns to fail.

**Solution**: Use `zeroOrOne` repeat mode or `or` with `anyline` fallback:

```json
{
  "repeat": {
    "mode": "zeroOrOne",
    "elements": [
      {"line": {"regex": "Special Instructions: (.*)"}}
    ]
  }
}
```

#### Problem: Variable Document Formats

**Scenario**: Documents come in multiple formats that can't be handled by a single pattern.

**Solution**: Create separate patterns for each format and try them in sequence (at the application level), or use more flexible `or` structures.

## Best Practices

### 1. Test Pattern Order
When using `or` elements, test that alternatives are tried in the correct order:

```json
{
  "or": {
    "elements": [
      {"line": {"regex": "Invoice #([A-Z]\\d+)"}},     // Letters+numbers  
      {"line": {"regex": "Invoice #(\\d+)"}},          // Numbers only
      {"line": {"regex": "Invoice ([\\w-]+)"}}         // Any word characters
    ]
  }
}
```

### 2. Use Comments for Complex Logic
Document why patterns are ordered a certain way:

```json
{
  "or": {
    "comment": "Try currency formats in order of specificity",
    "elements": [
      {"line": {"regex": "Total: \\$([\\d,]+\\.[\\d]{2})"}},  // $1,234.56
      {"line": {"regex": "Total: \\$([\\d,]+)"}},             // $1,234  
      {"line": {"regex": "Total: ([\\d.]+)"}}                 // 1234.56
    ]
  }
}
```

### 3. Validate with Edge Cases
Test patterns with:
- Minimal documents (shortest possible)
- Maximum documents (longest possible) 
- Missing optional sections
- Unusual formatting variations

## Performance Characteristics

The greedy, non-backtracking algorithm provides:

- **Time Complexity**: O(n × m) where n = number of lines, m = average pattern complexity
- **Space Complexity**: O(d) where d = maximum nesting depth of pattern elements
- **Memory Usage**: Constant per line (no backtracking state)

This makes Drex suitable for processing large documents and batch operations without performance concerns.

---

**Key Takeaway**: Design patterns that work *with* the greedy algorithm rather than against it. Use specificity, ordering, and context to guide the matcher toward the correct interpretation of your documents.


## Why This Works Well for Documents

Document patterns are naturally suited to this greedy approach because:

1. **Lines are typically unique** - Unlike characters in text, each line in a business document usually has a distinct purpose
2. **Structure is predictable** - Invoice headers come before line items, which come before totals
3. **Transitions are obvious** - It's usually clear when you've moved from one document section to another

This means most document patterns can be processed in a single pass without needing to backtrack, making them both fast and predictable.

## Fuzzy Matching

When fuzzy matching is enabled (`editDistance > 0`), Drex allows partial matches using Levenshtein edit distance (insertions, deletions, substitutions).

**Priority system:**
- Exact matches are always tried first
- Edit matches are only attempted when exact matches fail
- This preserves the greedy behavior while handling OCR errors and document variations

**Example with OCR noise:**
```
Pattern: {"line": {"regex": "Invoice #(\\d+)"}}
Input:   "lnvoice #12345"  // 'I' misread as 'l'
Result:  Matches with 1-edit if editDistance >= 1
```

**Note:** The interaction between fuzzy matching and `repeat` elements may create some ambiguity about when to end repetition vs. try an edit match. Test patterns with real documents to ensure expected behavior.