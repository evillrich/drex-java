package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the result of matching a single line against a line pattern element.
 * <p>
 * LineMatchResult contains information about whether the match was successful,
 * the captured text groups, and any extracted property values. This class is
 * used by LineElement implementations to return matching results.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see LineElement
 * @see Line
 * @see Anyline
 */
public final class LineMatchResult {

    private final boolean matched;
    private final String matchedText;
    private final List<String> capturedGroups;

    /**
     * Creates a successful match result with captured groups.
     *
     * @param matchedText the complete matched text, must not be null
     * @param capturedGroups the captured regex groups, must not be null but may be empty
     * @throws IllegalArgumentException if matchedText or capturedGroups is null
     */
    private LineMatchResult(String matchedText, List<String> capturedGroups) {
        Objects.requireNonNull(matchedText, "matchedText must not be null");
        Objects.requireNonNull(capturedGroups, "capturedGroups must not be null");
        
        this.matched = true;
        this.matchedText = matchedText;
        this.capturedGroups = Collections.unmodifiableList(new ArrayList<>(capturedGroups));
    }

    /**
     * Creates a failed match result.
     */
    private LineMatchResult() {
        this.matched = false;
        this.matchedText = null;
        this.capturedGroups = Collections.emptyList();
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
        return new LineMatchResult(matchedText, capturedGroups);
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
        Objects.requireNonNull(capturedGroups, "capturedGroups must not be null");
        return new LineMatchResult(matchedText, List.of(capturedGroups));
    }

    /**
     * Creates a failed match result.
     *
     * @return a failed LineMatchResult, never null
     */
    public static LineMatchResult failure() {
        return new LineMatchResult();
    }

    /**
     * Returns true if the line was successfully matched.
     *
     * @return true if matched, false otherwise
     */
    public boolean isMatched() {
        return matched;
    }

    /**
     * Returns the complete matched text.
     * <p>
     * This is only available for successful matches.
     *
     * @return the matched text, or null if the match failed
     */
    public String getMatchedText() {
        return matchedText;
    }

    /**
     * Returns the captured regex groups.
     * <p>
     * The returned list is immutable and contains the captured text for each
     * regex group. For successful matches, this list may be empty if the pattern
     * has no capture groups. For failed matches, this list is always empty.
     *
     * @return the list of captured groups, never null but may be empty
     */
    public List<String> getCapturedGroups() {
        return capturedGroups;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        LineMatchResult that = (LineMatchResult) obj;
        return matched == that.matched &&
               Objects.equals(matchedText, that.matchedText) &&
               Objects.equals(capturedGroups, that.capturedGroups);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matched, matchedText, capturedGroups);
    }

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