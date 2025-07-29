package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Represents a line pattern element that matches text using regular expressions.
 * <p>
 * Line elements are the primary mechanism for extracting data from document text.
 * They use regular expressions to match specific line patterns and capture groups
 * to extract data values, which are then bound to JSON properties through
 * PropertyBinding objects.
 * <p>
 * Each capture group in the regex corresponds positionally to a PropertyBinding:
 * capture group 1 maps to the first binding, group 2 to the second binding, etc.
 * The number of capture groups should match the number of property bindings.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see LineElement
 * @see PropertyBinding
 */
public final class Line implements LineElement {

    private final String comment;
    private final String regex;
    private final List<PropertyBinding> bindProperties;
    private final Pattern compiledPattern;

    /**
     * Creates a Line with validation and regex compilation.
     *
     * @param comment optional descriptive comment, may be null
     * @param regex the regular expression pattern to match, never null or empty
     * @param bindProperties the property bindings for captured groups, never null but may be empty
     * @throws IllegalArgumentException if regex is null/empty, bindProperties is null, 
     *                                  or bindProperties contains null values
     * @throws PatternCompilationException if the regex pattern is invalid
     */
    public Line(String comment, String regex, List<PropertyBinding> bindProperties) {
        if (regex == null || regex.trim().isEmpty()) {
            throw new IllegalArgumentException("regex must not be null or empty");
        }
        Objects.requireNonNull(bindProperties, "bindProperties must not be null");
        
        // Validate no null bindings
        for (int i = 0; i < bindProperties.size(); i++) {
            if (bindProperties.get(i) == null) {
                throw new IllegalArgumentException("Property binding at index " + i + " must not be null");
            }
        }
        
        // Trim regex and create immutable list
        this.comment = comment;
        this.regex = regex.trim();
        this.bindProperties = List.copyOf(bindProperties);
        
        // Compile the regex pattern during construction
        try {
            this.compiledPattern = Pattern.compile(this.regex);
        } catch (PatternSyntaxException e) {
            throw new PatternCompilationException("Invalid regex pattern '" + this.regex + "': " + e.getMessage(), e);
        }
    }

    /**
     * Creates a new Line with the specified regex and property bindings.
     *
     * @param comment optional descriptive comment, may be null
     * @param regex the regular expression pattern to match, must not be null or empty
     * @param bindProperties the property bindings for captured groups, must not be null but may be empty
     * @throws IllegalArgumentException if regex is null/empty, bindProperties is null, 
     *                                  or bindProperties contains null values
     * @throws PatternCompilationException if the regex pattern is invalid
     */
    public Line(String comment, String regex, PropertyBinding... bindProperties) {
        this(comment, regex, List.of(bindProperties));
    }

    /**
     * Returns the optional comment.
     *
     * @return the comment string, or null if no comment was provided
     */
    public String comment() {
        return comment;
    }

    /**
     * Returns the regular expression pattern.
     *
     * @return the regex pattern string, never null or empty
     */
    public String regex() {
        return regex;
    }

    /**
     * Returns the property bindings.
     *
     * @return an immutable list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> bindProperties() {
        return bindProperties;
    }

    /**
     * Returns the compiled pattern.
     *
     * @return the compiled Pattern object, never null
     */
    public Pattern compiledPattern() {
        return compiledPattern;
    }

    /**
     * Returns the number of property bindings.
     *
     * @return the number of bindings, zero or greater
     */
    public int getBindingCount() {
        return bindProperties.size();
    }

    /**
     * Returns true if this line has property bindings.
     *
     * @return true if there are bindings, false if empty
     */
    public boolean hasBindings() {
        return !bindProperties.isEmpty();
    }

    /**
     * Returns the number of capturing groups in the regex pattern.
     * <p>
     * This is useful for validating that the number of capture groups matches
     * the number of property bindings.
     *
     * @return the number of capturing groups in the compiled pattern
     */
    public int getCaptureGroupCount() {
        return compiledPattern.matcher("").groupCount();
    }

    @Override
    public void compile() {
        // Pattern is already compiled during construction - this is a no-op
        // but required by the interface for consistency
    }

    @Override
    public LineMatchResult match(String inputLine) {
        Objects.requireNonNull(inputLine, "inputLine must not be null");
        
        Matcher matcher = compiledPattern.matcher(inputLine);
        if (!matcher.find()) {
            return LineMatchResult.failure();
        }
        
        // Extract captured groups
        List<String> capturedGroups = new ArrayList<>();
        int groupCount = matcher.groupCount();
        for (int i = 1; i <= groupCount; i++) {
            capturedGroups.add(matcher.group(i));
        }
        
        return LineMatchResult.success(matcher.group(0), capturedGroups);
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitLine(this);
    }

    /**
     * Returns the property bindings that define how captured groups map to JSON properties.
     *
     * @return the list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> getBindProperties() {
        return bindProperties;
    }

    /**
     * Returns the regular expression pattern used for matching.
     *
     * @return the regex pattern string, never null or empty
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Returns the compiled regex pattern for efficient matching.
     *
     * @return the compiled Pattern object, never null
     */
    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     *
     * @return the comment string, or null if no comment was provided
     */
    @Override
    public String getComment() {
        return comment;
    }

    /**
     * Creates a builder-style pattern construction (backward compatibility).
     *
     * @return a new Line instance for fluent construction
     */
    public static Line builder() {
        return new Line(null, ".*", List.of());
    }

    /**
     * Sets the comment for this line pattern.
     *
     * @param comment optional descriptive comment, may be null
     * @return a new Line instance for method chaining
     */
    public Line comment(String comment) {
        return new Line(comment, this.regex, this.bindProperties);
    }

    /**
     * Sets the regular expression pattern for this line.
     *
     * @param regex the regular expression pattern to match, must not be null or empty
     * @return a new Line instance for method chaining
     */
    public Line regex(String regex) {
        return new Line(this.comment, regex, this.bindProperties);
    }

    /**
     * Sets the property bindings for captured groups.
     *
     * @param bindings the property bindings for captured groups, must not be null
     * @return a new Line instance for method chaining
     */
    public Line bindProperties(PropertyBinding... bindings) {
        return new Line(this.comment, this.regex, List.of(bindings));
    }

    /**
     * Sets the property bindings for captured groups.
     *
     * @param bindings the property bindings for captured groups, must not be null
     * @return a new Line instance for method chaining
     */
    public Line bindProperties(List<PropertyBinding> bindings) {
        return new Line(this.comment, this.regex, bindings);
    }

    /**
     * Completes the fluent construction.
     *
     * @return this Line instance
     */
    public Line build() {
        return this;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param obj the reference object with which to compare
     * @return true if this object is the same as the obj argument; false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Line line = (Line) obj;
        return Objects.equals(comment, line.comment) &&
               Objects.equals(regex, line.regex) &&
               Objects.equals(bindProperties, line.bindProperties);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(comment, regex, bindProperties);
    }

    /**
     * Returns a string representation of this Line.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString() {
        return "Line[" +
               (comment != null ? "comment=" + comment + ", " : "") +
               "regex=" + regex +
               ", bindProperties=" + bindProperties.size() + " bindings" +
               "]";
    }
}