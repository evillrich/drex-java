package io.github.evillrich.drex.pattern;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of matching a single line against a line pattern element.
 * <p>
 * LineMatchResult contains information about whether the match was successful,
 * the captured text groups, and any extracted property values. This record is
 * used by LineElement implementations to return matching results.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @param matched true if the line was successfully matched, false otherwise
 * @param matchedText the complete matched text, or null if the match failed
 * @param capturedGroups the captured regex groups, never null but may be empty
 * @since 1.0
 * @see LineElement
 * @see Line
 * @see Anyline
 */
public record LineMatchResult(
    boolean matched,
    String matchedText,
    List<String> capturedGroups
) {

    /**
     * Creates a LineMatchResult with validation and immutable list copying.
     */
    public LineMatchResult {
        Objects.requireNonNull(capturedGroups, "capturedGroups must not be null");
        
        // Validate that successful matches have non-null matchedText
        if (matched && matchedText == null) {
            throw new IllegalArgumentException("matchedText must not be null for successful matches");
        }
        
        // Ensure immutability by copying the list
        capturedGroups = Collections.unmodifiableList(List.copyOf(capturedGroups));
    }

    /**
     * Creates a successful match result with the specified matched text and captured groups.
     *
     * @param matchedText the complete matched text, must not be null
     * @param capturedGroups the captured regex groups, must not be null but may be empty
     * @return a successful LineMatchResult, never null
     * @throws IllegalArgumentException if matchedText or capturedGroups is null
     */
    public static LineMatchResult success(String matchedText, List<String> capturedGroups) {
        Objects.requireNonNull(matchedText, "matchedText must not be null");
        return new LineMatchResult(true, matchedText, capturedGroups);
    }

    /**
     * Creates a successful match result with the specified matched text and captured groups.
     *
     * @param matchedText the complete matched text, must not be null
     * @param capturedGroups the captured regex groups, must not be null but may be empty
     * @return a successful LineMatchResult, never null
     * @throws IllegalArgumentException if matchedText or capturedGroups is null
     */
    public static LineMatchResult success(String matchedText, String... capturedGroups) {
        Objects.requireNonNull(matchedText, "matchedText must not be null");
        Objects.requireNonNull(capturedGroups, "capturedGroups must not be null");
        return new LineMatchResult(true, matchedText, List.of(capturedGroups));
    }

    /**
     * Creates a failed match result.
     *
     * @return a failed LineMatchResult, never null
     */
    public static LineMatchResult failure() {
        return new LineMatchResult(false, null, Collections.emptyList());
    }

    /**
     * Returns true if the line was successfully matched.
     * <p>
     * This is a convenience method equivalent to accessing the {@code matched} component directly.
     *
     * @return true if matched, false otherwise
     */
    public boolean isMatched() {
        return matched;
    }

    /**
     * Returns the number of captured groups.
     *
     * @return the number of captured groups, zero or greater
     */
    public int getCaptureCount() {
        return capturedGroups.size();
    }

    /**
     * Returns the captured text for the specified group index.
     *
     * @param groupIndex the zero-based group index
     * @return the captured text for the group, or null if no capture at that index
     * @throws IndexOutOfBoundsException if groupIndex is negative or >= getCaptureCount()
     */
    public String getCapturedGroup(int groupIndex) {
        if (groupIndex < 0 || groupIndex >= capturedGroups.size()) {
            throw new IndexOutOfBoundsException("Group index " + groupIndex + 
                " is out of bounds for " + capturedGroups.size() + " captured groups");
        }
        return capturedGroups.get(groupIndex);
    }

    /**
     * Returns a string representation of this LineMatchResult.
     * <p>
     * For failed matches, returns a simple representation. For successful matches,
     * includes the matched text and capture count.
     */
    @Override
    public String toString() {
        if (!matched) {
            return "LineMatchResult[matched=false]";
        }
        return "LineMatchResult[" +
               "matched=true" +
               ", matchedText=" + matchedText +
               ", captureCount=" + capturedGroups.size() +
               "]";
    }
}