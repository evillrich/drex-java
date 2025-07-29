package io.github.evillrich.drex.pattern;

import java.util.ArrayList;
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
 * @param comment optional descriptive comment, may be null
 * @param regex the regular expression pattern to match, never null or empty
 * @param bindProperties the property bindings for captured groups, never null but may be empty
 * @param compiledPattern the compiled regex pattern, never null
 * @since 1.0
 * @see LineElement
 * @see PropertyBinding
 */
public record Line(
    String comment,
    String regex,
    List<PropertyBinding> bindProperties,
    Pattern compiledPattern
) implements LineElement {

    /**
     * Creates a Line record with validation and regex compilation.
     */
    public Line {
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
        regex = regex.trim();
        bindProperties = Collections.unmodifiableList(List.copyOf(bindProperties));
        
        // Compile the regex pattern during construction
        try {
            compiledPattern = Pattern.compile(regex);
        } catch (PatternSyntaxException e) {
            throw new PatternCompilationException("Invalid regex pattern '" + regex + "': " + e.getMessage(), e);
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
    public Line(String comment, String regex, List<PropertyBinding> bindProperties) {
        this(comment, regex, bindProperties, null); // compiledPattern will be set in compact constructor
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
     * <p>
     * This is a convenience method equivalent to accessing the {@code bindProperties} component directly.
     *
     * @return the list of property bindings, never null but may be empty
     */
    public List<PropertyBinding> getBindProperties() {
        return bindProperties;
    }

    /**
     * Returns the regular expression pattern used for matching.
     * <p>
     * This is a convenience method equivalent to accessing the {@code regex} component directly.
     *
     * @return the regex pattern string, never null or empty
     */
    public String getRegex() {
        return regex;
    }

    /**
     * Returns the compiled regex pattern for efficient matching.
     * <p>
     * This is a convenience method equivalent to accessing the {@code compiledPattern} component directly.
     *
     * @return the compiled Pattern object, never null
     */
    public Pattern getCompiledPattern() {
        return compiledPattern;
    }

    /**
     * Returns the optional comment associated with this pattern element.
     * <p>
     * This is a convenience method equivalent to accessing the {@code comment} component directly.
     *
     * @return the comment string, or null if no comment was provided
     */
    @Override
    public String getComment() {
        return comment;
    }
}