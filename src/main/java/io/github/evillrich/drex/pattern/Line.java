package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
 * @see Builder
 */
public final class Line extends LineElement {

    private final String regex;
    private final List<PropertyBinding> bindProperties;
    private volatile Pattern compiledPattern;
    private volatile boolean compiled = false;

    /**
     * Constructs a new Line with the specified regex and property bindings.
     * <p>
     * Note: The regex pattern is not compiled during construction. Call {@link #compile()}
     * before attempting to use this Line for matching operations.
     *
     * @param comment optional descriptive comment, may be null
     * @param regex the regular expression pattern to match, must not be null or empty
     * @param bindProperties the property bindings for captured groups, must not be null but may be empty
     * @throws IllegalArgumentException if regex is null/empty, bindProperties is null, 
     *                                  or bindProperties contains null values
     */
    public Line(String comment, String regex, List<PropertyBinding> bindProperties) {
        super(comment);
        
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
        
        this.regex = regex.trim();
        this.bindProperties = Collections.unmodifiableList(new ArrayList<>(bindProperties));
    }

    /**
     * Constructs a new Line with the specified regex and property bindings.
     * <p>
     * Note: The regex pattern is not compiled during construction. Call {@link #compile()}
     * before attempting to use this Line for matching operations.
     *
     * @param comment optional descriptive comment, may be null
     * @param regex the regular expression pattern to match, must not be null or empty
     * @param bindProperties the property bindings for captured groups, must not be null but may be empty
     * @throws IllegalArgumentException if regex is null/empty, bindProperties is null, 
     *                                  or bindProperties contains null values
     */
    public Line(String comment, String regex, PropertyBinding... bindProperties) {
        this(comment, regex, Arrays.asList(bindProperties));
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
     * <p>
     * This pattern is compiled during the {@link #compile()} phase.
     *
     * @return the compiled Pattern object, or null if not yet compiled
     * @throws IllegalStateException if the pattern has not been compiled
     */
    public Pattern getCompiledPattern() {
        if (!compiled) {
            throw new IllegalStateException("Pattern must be compiled before accessing compiled pattern");
        }
        return compiledPattern;
    }

    /**
     * Returns the property bindings that define how captured groups map to JSON properties.
     * <p>
     * The returned list is immutable and contains no null elements. Each binding
     * corresponds positionally to a regex capture group.
     *
     * @return the list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> getBindProperties() {
        return bindProperties;
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
     * @throws IllegalStateException if the pattern has not been compiled
     */
    public int getCaptureGroupCount() {
        if (!compiled) {
            throw new IllegalStateException("Pattern must be compiled before accessing capture group count");
        }
        return compiledPattern.matcher("").groupCount();
    }

    @Override
    public void compile() {
        if (compiled) {
            return; // Already compiled - idempotent
        }
        
        try {
            compiledPattern = Pattern.compile(regex);
            compiled = true;
        } catch (PatternSyntaxException e) {
            throw new PatternCompilationException("Invalid regex pattern '" + regex + "': " + e.getMessage(), e);
        }
    }

    @Override
    public LineMatchResult match(String inputLine) {
        Objects.requireNonNull(inputLine, "inputLine must not be null");
        
        if (!compiled) {
            throw new IllegalStateException("Pattern must be compiled before matching");
        }
        
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        Line line = (Line) obj;
        return Objects.equals(regex, line.regex) &&
               Objects.equals(bindProperties, line.bindProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), regex, bindProperties);
    }

    @Override
    public String toString() {
        return "Line[" +
               "regex=" + regex +
               ", bindings=" + bindProperties.size() +
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }

    /**
     * Creates a new builder for constructing Line instances.
     *
     * @return a new Builder instance, never null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing Line instances using a fluent API.
     */
    public static final class Builder {
        private String comment;
        private String regex;
        private List<PropertyBinding> bindProperties;

        private Builder() {}

        /**
         * Sets an optional comment describing this line.
         *
         * @param comment the comment text, may be null
         * @return this builder for method chaining
         */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the regular expression pattern.
         *
         * @param regex the regex pattern, must not be null or empty
         * @return this builder for method chaining
         * @throws IllegalArgumentException if regex is null or empty
         */
        public Builder regex(String regex) {
            if (regex == null || regex.trim().isEmpty()) {
                throw new IllegalArgumentException("regex must not be null or empty");
            }
            this.regex = regex.trim();
            return this;
        }

        /**
         * Sets the property bindings.
         *
         * @param bindProperties the property bindings, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindProperties is null
         */
        public Builder bindProperties(List<PropertyBinding> bindProperties) {
            Objects.requireNonNull(bindProperties, "bindProperties must not be null");
            this.bindProperties = bindProperties;
            return this;
        }

        /**
         * Sets the property bindings.
         *
         * @param bindProperties the property bindings, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindProperties is null
         */
        public Builder bindProperties(PropertyBinding... bindProperties) {
            Objects.requireNonNull(bindProperties, "bindProperties must not be null");
            this.bindProperties = Arrays.asList(bindProperties);
            return this;
        }

        /**
         * Constructs a new Line with the configured properties.
         * <p>
         * Note: The regex pattern is not compiled during construction. Call {@link Line#compile()}
         * on the returned instance before attempting to use it for matching operations.
         *
         * @return a new Line instance, never null
         * @throws IllegalStateException if required properties are not set
         */
        public Line build() {
            if (regex == null) {
                throw new IllegalStateException("regex is required");
            }
            if (bindProperties == null) {
                bindProperties = Collections.emptyList();
            }

            return new Line(comment, regex, bindProperties);
        }
    }
}