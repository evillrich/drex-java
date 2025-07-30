package io.github.evillrich.drex;


import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * A matcher that executes document pattern matching using a DrexPattern.
 * <p>
 * DrexMatcher provides the main public API for executing pattern matching
 * against document text. It handles the internal complexity of NFA construction
 * and simulation, presenting a clean interface for pattern matching operations.
 * <p>
 * Instances are thread-safe and can be reused for multiple matching operations.
 * The underlying NFA is built once during construction and reused for all
 * matching operations.
 * <p>
 * Example usage:
 * <pre>
 * DrexPattern pattern = DrexPattern.builder()
 *     .name("InvoicePattern")
 *     .bindObject("invoice")
 *     .elements(...)
 *     .build();
 * 
 * DrexMatcher matcher = new DrexMatcher(pattern);
 * DrexMatchResult result = matcher.findMatch(documentLines);
 * 
 * if (result.isSuccess()) {
 *     String json = result.getJsonString();
 *     Map&lt;String, Object&gt; data = result.getData();
 * }
 * </pre>
 *
 * @since 1.0
 * @see DrexPattern
 * @see DrexMatchResult
 */
public final class DrexMatcher {
    
    private final DrexPattern pattern;

    /**
     * Creates a new DrexMatcher for the specified pattern.
     * <p>
     * The matcher prepares the pattern for efficient matching during construction,
     * so construction may throw exceptions if the pattern is invalid or
     * cannot be compiled.
     *
     * @param pattern the pattern to use for matching, must not be null
     * @throws IllegalArgumentException if pattern is null
     * @throws RuntimeException if the pattern cannot be compiled
     */
    public DrexMatcher(DrexPattern pattern) {
        this.pattern = Objects.requireNonNull(pattern, "Pattern cannot be null");
        
        try {
            // Compile the pattern for efficient matching
            this.pattern.compile();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compile pattern: " + e.getMessage(), e);
        }
    }

    /**
     * Returns the pattern used by this matcher.
     *
     * @return the DrexPattern, never null
     */
    public DrexPattern getPattern() {
        return pattern;
    }

    /**
     * Attempts to match the pattern against the specified document lines.
     * <p>
     * This method processes the document using the pattern's matching algorithm
     * and returns a result containing any extracted data and matching metadata.
     *
     * @param inputLines the lines of the document to match against, must not be null
     * @return the match result containing extracted data and metadata, never null
     * @throws IllegalArgumentException if inputLines is null
     */
    public DrexMatchResult findMatch(List<String> inputLines) {
        Objects.requireNonNull(inputLines, "Input lines cannot be null");
        
        try {
            // Delegate to the pattern's internal matching logic
            // This keeps the engine complexity hidden from the public API
            return pattern.findMatch(inputLines);
            
        } catch (Exception e) {
            // Convert any internal exceptions to a failed match result
            // This prevents internal engine exceptions from leaking to the public API
            return DrexMatchResult.failure("Internal matching error: " + e.getMessage(), 0);
        }
    }

    /**
     * Attempts to match the pattern against the specified document lines.
     * <p>
     * This is a convenience method that converts the array to a List and
     * delegates to {@link #findMatch(List)}.
     *
     * @param inputLines the lines of the document to match against, must not be null
     * @return the match result containing extracted data and metadata, never null
     * @throws IllegalArgumentException if inputLines is null
     */
    public DrexMatchResult findMatch(String[] inputLines) {
        Objects.requireNonNull(inputLines, "Input lines cannot be null");
        return findMatch(Arrays.asList(inputLines));
    }

    /**
     * Attempts to match the pattern against the specified document text.
     * <p>
     * This convenience method splits the document on newlines and delegates
     * to {@link #findMatch(List)}. The document is split using both \n and \r\n
     * line separators.
     *
     * @param document the document text to match against, must not be null
     * @return the match result containing extracted data and metadata, never null
     * @throws IllegalArgumentException if document is null
     */
    public DrexMatchResult findMatch(String document) {
        Objects.requireNonNull(document, "Document cannot be null");
        
        // Split on both \n and \r\n line separators
        String[] lines = document.split("\\r?\\n");
        return findMatch(Arrays.asList(lines));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DrexMatcher that = (DrexMatcher) obj;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }

    @Override
    public String toString() {
        return "DrexMatcher{" +
               "pattern=" + pattern.getName() +
               ", version=" + pattern.getVersion() +
               "}";
    }
}