package io.github.evillrich.drex.pattern;

import java.util.List;
import java.util.Objects;

/**
 * Represents the root of a document pattern that can extract structured JSON data.
 * <p>
 * DrexPattern is the top-level pattern element that defines a complete document
 * processing pattern. It contains metadata about the pattern (version, name) and
 * the root collection of pattern elements that define the matching logic.
 * <p>
 * DrexPattern instances are immutable and thread-safe once constructed.
 * Use {@link Builder} to create new patterns programmatically.
 *
 * @since 1.0
 * @see PatternElement
 * @see Builder
 */
public final class DrexPattern extends GroupingPatternElement {

    private final String version;
    private final String name;
    private final int editDistance;

    /**
     * Constructs a new DrexPattern with the specified properties.
     *
     * @param version the pattern version string, must not be null or empty
     * @param name the pattern name, must not be null or empty
     * @param comment optional descriptive comment, may be null
     * @param bindObject the name of the root JSON object, must not be null or empty
     * @param editDistance the maximum edit distance for fuzzy matching, must be non-negative
     * @param elements the child pattern elements, must not be null but may be empty
     * @throws IllegalArgumentException if version, name, or bindObject are null/empty,
     *                                  if editDistance is negative, or if elements contains null values
     */
    public DrexPattern(String version, String name, String comment, String bindObject, 
                       int editDistance, List<PatternElement> elements) {
        super(comment, bindObject, elements);
        
        if (version == null || version.trim().isEmpty()) {
            throw new IllegalArgumentException("version must not be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("name must not be null or empty");
        }
        if (editDistance < 0) {
            throw new IllegalArgumentException("editDistance must be non-negative");
        }
        
        this.version = version.trim();
        this.name = name.trim();
        this.editDistance = editDistance;
    }

    /**
     * Returns the version string of this pattern.
     * <p>
     * Pattern versions are used for compatibility tracking and schema evolution.
     *
     * @return the version string, never null or empty
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the name of this pattern.
     * <p>
     * Pattern names provide human-readable identification and are used in
     * error messages and debugging output.
     *
     * @return the pattern name, never null or empty
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the maximum edit distance allowed for fuzzy matching.
     * <p>
     * Edit distance determines how many character insertions, deletions, or
     * substitutions are allowed when matching text patterns. A value of 0
     * requires exact matches, while higher values enable fuzzy matching
     * for handling OCR errors or text variations.
     *
     * @return the edit distance, zero or greater
     */
    public int getEditDistance() {
        return editDistance;
    }

    /**
     * Returns true if fuzzy matching is enabled for this pattern.
     *
     * @return true if editDistance is greater than 0, false otherwise
     */
    public boolean isFuzzyMatchingEnabled() {
        return editDistance > 0;
    }

    @Override
    public <T> T accept(PatternVisitor<T> visitor) {
        Objects.requireNonNull(visitor, "visitor must not be null");
        return visitor.visitDrexPattern(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        DrexPattern that = (DrexPattern) obj;
        return editDistance == that.editDistance &&
               Objects.equals(version, that.version) &&
               Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), version, name, editDistance);
    }

    @Override
    public String toString() {
        return "DrexPattern[" +
               "name=" + name + 
               ", version=" + version +
               ", bindObject=" + getBindObject() +
               ", elements=" + getElementCount() +
               (editDistance > 0 ? ", editDistance=" + editDistance : "") +
               (getComment() != null ? ", comment=" + getComment() : "") +
               "]";
    }

    /**
     * Creates a new builder for constructing DrexPattern instances.
     *
     * @return a new Builder instance, never null
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing DrexPattern instances using a fluent API.
     * <p>
     * The builder allows step-by-step construction of complex patterns with
     * validation and default values.
     */
    public static final class Builder {
        private String version;
        private String name;
        private String comment;
        private String bindObject;
        private int editDistance = 0;
        private List<PatternElement> elements;

        private Builder() {}

        /**
         * Sets the pattern version.
         *
         * @param version the version string, must not be null or empty
         * @return this builder for method chaining
         * @throws IllegalArgumentException if version is null or empty
         */
        public Builder version(String version) {
            if (version == null || version.trim().isEmpty()) {
                throw new IllegalArgumentException("version must not be null or empty");
            }
            this.version = version.trim();
            return this;
        }

        /**
         * Sets the pattern name.
         *
         * @param name the pattern name, must not be null or empty
         * @return this builder for method chaining
         * @throws IllegalArgumentException if name is null or empty
         */
        public Builder name(String name) {
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("name must not be null or empty");
            }
            this.name = name.trim();
            return this;
        }

        /**
         * Sets an optional comment describing this pattern.
         *
         * @param comment the comment text, may be null
         * @return this builder for method chaining
         */
        public Builder comment(String comment) {
            this.comment = comment;
            return this;
        }

        /**
         * Sets the root object binding name.
         *
         * @param bindObject the JSON object name, must not be null or empty
         * @return this builder for method chaining
         * @throws IllegalArgumentException if bindObject is null or empty
         */
        public Builder bindObject(String bindObject) {
            if (bindObject == null || bindObject.trim().isEmpty()) {
                throw new IllegalArgumentException("bindObject must not be null or empty");
            }
            this.bindObject = bindObject.trim();
            return this;
        }

        /**
         * Sets the maximum edit distance for fuzzy matching.
         *
         * @param editDistance the edit distance, must be non-negative
         * @return this builder for method chaining
         * @throws IllegalArgumentException if editDistance is negative
         */
        public Builder editDistance(int editDistance) {
            if (editDistance < 0) {
                throw new IllegalArgumentException("editDistance must be non-negative");
            }
            this.editDistance = editDistance;
            return this;
        }

        /**
         * Sets the child pattern elements.
         *
         * @param elements the pattern elements, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if elements is null
         */
        public Builder elements(List<PatternElement> elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            this.elements = elements;
            return this;
        }

        /**
         * Sets the child pattern elements.
         *
         * @param elements the pattern elements, must not be null
         * @return this builder for method chaining
         * @throws IllegalArgumentException if elements is null
         */
        public Builder elements(PatternElement... elements) {
            Objects.requireNonNull(elements, "elements must not be null");
            this.elements = List.of(elements);
            return this;
        }

        /**
         * Constructs a new DrexPattern with the configured properties.
         *
         * @return a new DrexPattern instance, never null
         * @throws IllegalStateException if required properties are not set
         */
        public DrexPattern build() {
            if (version == null) {
                throw new IllegalStateException("version is required");
            }
            if (name == null) {
                throw new IllegalStateException("name is required");
            }
            if (bindObject == null) {
                throw new IllegalStateException("bindObject is required");
            }
            if (elements == null) {
                throw new IllegalStateException("elements is required");
            }

            return new DrexPattern(version, name, comment, bindObject, editDistance, elements);
        }
    }
}