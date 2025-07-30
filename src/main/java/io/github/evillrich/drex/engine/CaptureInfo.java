package io.github.evillrich.drex.engine;

import java.util.Objects;

/**
 * Records information about a captured value during pattern matching.
 * <p>
 * CaptureInfo stores both the captured text and metadata about where it was captured
 * from. This class is designed to support future position tracking features while
 * providing immediate value for debugging and validation.
 * <p>
 * For the MVP, this class primarily stores the path and values. Future versions
 * will extend this to include detailed position information for integration with
 * OCR and document analysis tools.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 */
public final class CaptureInfo {

    private final String path;
    private final String originalValue;
    private final String formattedValue;
    private final int lineNumber;
    private final int startPosition;
    private final int endPosition;

    /**
     * Creates a new capture information record.
     *
     * @param path the JSON path where this value was captured, must not be null
     * @param originalValue the raw captured text from the regex, may be null
     * @param formattedValue the value after formatter application, may be null
     * @param lineNumber the zero-based line number in the document where this was captured
     * @param startPosition the start position within the line (for future position tracking)
     * @param endPosition the end position within the line (for future position tracking)
     * @throws IllegalArgumentException if path is null, or if positions are invalid
     */
    public CaptureInfo(String path, String originalValue, String formattedValue,
                      int lineNumber, int startPosition, int endPosition) {
        this.path = Objects.requireNonNull(path, "path must not be null");
        this.originalValue = originalValue;
        this.formattedValue = formattedValue;
        
        if (lineNumber < 0) {
            throw new IllegalArgumentException("lineNumber must be non-negative: " + lineNumber);
        }
        this.lineNumber = lineNumber;
        
        if (startPosition < 0) {
            throw new IllegalArgumentException("startPosition must be non-negative: " + startPosition);
        }
        this.startPosition = startPosition;
        
        if (endPosition < startPosition) {
            throw new IllegalArgumentException("endPosition must be >= startPosition: " + 
                                             endPosition + " < " + startPosition);
        }
        this.endPosition = endPosition;
    }

    /**
     * Creates a capture info with minimal position information for MVP.
     * <p>
     * This convenience constructor is used when detailed position tracking is not yet needed.
     *
     * @param path the JSON path where this value was captured, must not be null
     * @param originalValue the raw captured text from the regex, may be null
     * @param formattedValue the value after formatter application, may be null
     * @param lineNumber the zero-based line number in the document where this was captured
     * @throws IllegalArgumentException if path is null or lineNumber is negative
     */
    public CaptureInfo(String path, String originalValue, String formattedValue, int lineNumber) {
        this(path, originalValue, formattedValue, lineNumber, 0, 0);
    }

    /**
     * Returns the JSON path where this value was captured.
     * <p>
     * The path uses dot notation for nested objects and bracket notation for arrays,
     * such as "invoice.items[0].name" or "eob.patient.memberId".
     *
     * @return the JSON path, never null
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the original captured text before any formatting.
     *
     * @return the raw captured value, may be null
     */
    public String getOriginalValue() {
        return originalValue;
    }

    /**
     * Returns the value after formatter application.
     * <p>
     * This is the value that actually gets stored in the JSON output.
     *
     * @return the formatted value, may be null
     */
    public String getFormattedValue() {
        return formattedValue;
    }

    /**
     * Returns the zero-based line number where this value was captured.
     *
     * @return the line number, always non-negative
     */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Returns the start position within the line.
     * <p>
     * This is intended for future position tracking features and may not be
     * accurate in the current MVP implementation.
     *
     * @return the start position, always non-negative
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * Returns the end position within the line.
     * <p>
     * This is intended for future position tracking features and may not be
     * accurate in the current MVP implementation.
     *
     * @return the end position, always >= startPosition
     */
    public int getEndPosition() {
        return endPosition;
    }

    /**
     * Returns whether this capture has position information.
     * <p>
     * Currently returns true if startPosition and endPosition are different,
     * indicating that position tracking was attempted.
     *
     * @return true if position information is available, false otherwise
     */
    public boolean hasPositionInfo() {
        return startPosition != endPosition;
    }

    /**
     * Returns whether formatting was applied to this capture.
     *
     * @return true if the formatted value differs from the original, false otherwise
     */
    public boolean wasFormatted() {
        return !Objects.equals(originalValue, formattedValue);
    }

    /**
     * Returns the length of the captured text.
     *
     * @return the length of the original value, or 0 if the value is null
     */
    public int getLength() {
        return originalValue != null ? originalValue.length() : 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CaptureInfo{");
        sb.append("path='").append(path).append('\'');
        sb.append(", originalValue='").append(originalValue).append('\'');
        
        if (wasFormatted()) {
            sb.append(", formattedValue='").append(formattedValue).append('\'');
        }
        
        sb.append(", line=").append(lineNumber);
        
        if (hasPositionInfo()) {
            sb.append(", pos=").append(startPosition).append("-").append(endPosition);
        }
        
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CaptureInfo that = (CaptureInfo) obj;
        return lineNumber == that.lineNumber &&
               startPosition == that.startPosition &&
               endPosition == that.endPosition &&
               Objects.equals(path, that.path) &&
               Objects.equals(originalValue, that.originalValue) &&
               Objects.equals(formattedValue, that.formattedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, originalValue, formattedValue, lineNumber, startPosition, endPosition);
    }
}