package io.github.evillrich.drex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the result of a document matching operation.
 * <p>
 * DrexMatchResult provides access to the extracted data from a successful
 * pattern match, including both structured data access and JSON string
 * representation. It also contains metadata about the matching process
 * such as lines processed and failure information.
 * <p>
 * Instances are immutable and thread-safe.
 *
 * @since 1.0
 * @see DrexMatcher
 */
public final class DrexMatchResult {
    
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    private final boolean success;
    private final Map<String, Object> data;
    private final int linesMatched;
    private final int linesProcessed;
    private final String failureReason;
    private final String jsonString; // Lazily computed

    /**
     * Creates a DrexMatchResult from an internal SimulationResult.
     * <p>
     * This constructor is package-private and used internally by DrexPattern
     * to wrap engine results in the public API.
     *
     * @param simulationResult the internal simulation result, must not be null
     * @throws IllegalArgumentException if simulationResult is null
     */
    DrexMatchResult(SimulationResult simulationResult) {
        Objects.requireNonNull(simulationResult, "SimulationResult cannot be null");
        
        this.success = simulationResult.isSuccess();
        this.data = simulationResult.isSuccess() 
            ? Collections.unmodifiableMap(simulationResult.getExtractedData())
            : Collections.emptyMap();
        this.linesMatched = simulationResult.getLinesMatched();
        this.linesProcessed = simulationResult.getLinesProcessed();
        this.failureReason = simulationResult.getFailureReason();
        
        // Compute JSON string representation
        this.jsonString = computeJsonString();
    }

    /**
     * Creates a failed DrexMatchResult.
     * <p>
     * This factory method is package-private and used internally to create
     * failure results without requiring access to engine classes.
     *
     * @param failureReason the reason for the failure, must not be null
     * @param linesProcessed the number of lines processed before failure
     * @return a failed DrexMatchResult, never null
     */
    public static DrexMatchResult failure(String failureReason, int linesProcessed) {
        // Create a failed result directly without using engine classes
        return new DrexMatchResult(false, Collections.emptyMap(), 0, linesProcessed, failureReason);
    }
    
    /**
     * Private constructor for creating results directly.
     */
    private DrexMatchResult(boolean success, Map<String, Object> data, int linesMatched, 
                           int linesProcessed, String failureReason) {
        this.success = success;
        this.data = Collections.unmodifiableMap(data);
        this.linesMatched = linesMatched;
        this.linesProcessed = linesProcessed;
        this.failureReason = failureReason;
        this.jsonString = computeJsonString();
    }

    /**
     * Returns whether the pattern matching was successful.
     *
     * @return true if the pattern matched successfully, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the extracted structured data from a successful match.
     * <p>
     * The returned map contains the structured data extracted according to
     * the pattern's binding configuration. For failed matches, returns an
     * empty map.
     *
     * @return an immutable map containing the extracted data, never null but may be empty
     */
    public Map<String, Object> getData() {
        return data;
    }

    /**
     * Returns the extracted data as a JSON string.
     * <p>
     * The JSON string represents the structured data extracted by the pattern.
     * For failed matches, returns an empty JSON object "{}".
     *
     * @return the JSON string representation of the extracted data, never null
     */
    public String getJsonString() {
        return jsonString;
    }

    /**
     * Returns the number of document lines that matched pattern elements.
     *
     * @return the number of lines matched, always non-negative
     */
    public int getLinesMatched() {
        return linesMatched;
    }

    /**
     * Returns the total number of document lines processed during matching.
     *
     * @return the number of lines processed, always non-negative
     */
    public int getLinesProcessed() {
        return linesProcessed;
    }

    /**
     * Returns the reason for matching failure, if any.
     *
     * @return the failure reason, or null if the match was successful
     */
    public String getFailureReason() {
        return failureReason;
    }

    /**
     * Returns whether any data was extracted during matching.
     * <p>
     * This is a convenience method that returns true if the match was successful
     * and produced non-empty extracted data.
     *
     * @return true if data was extracted, false otherwise
     */
    public boolean hasData() {
        return success && !data.isEmpty();
    }

    /**
     * Computes the JSON string representation of the extracted data.
     */
    private String computeJsonString() {
        if (!success || data.isEmpty()) {
            return "{}";
        }
        
        try {
            return JSON_MAPPER.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            // This should not happen with valid data from our engine,
            // but we'll return a safe fallback rather than throw
            return "{\"error\":\"Failed to serialize extracted data to JSON\"}";
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DrexMatchResult that = (DrexMatchResult) obj;
        return success == that.success &&
               linesMatched == that.linesMatched &&
               linesProcessed == that.linesProcessed &&
               Objects.equals(data, that.data) &&
               Objects.equals(failureReason, that.failureReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, data, linesMatched, linesProcessed, failureReason);
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("DrexMatchResult{success=true, linesProcessed=%d, linesMatched=%d, dataKeys=%s}", 
                               linesProcessed, linesMatched, data.keySet());
        } else {
            return String.format("DrexMatchResult{success=false, linesProcessed=%d, error='%s'}", 
                               linesProcessed, failureReason);
        }
    }
}