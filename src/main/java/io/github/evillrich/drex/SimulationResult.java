package io.github.evillrich.drex;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the result of an NFA simulation.
 * <p>
 * Contains the outcome of pattern matching including whether the simulation
 * was successful, any extracted data, and metadata about the matching process.
 * SimulationResult instances are immutable and thread-safe.
 *
 * @since 1.0
 */
final class SimulationResult {
    private final boolean success;
    private final Map<String, Object> extractedData;
    private final int linesProcessed;
    private final int linesMatched;
    private final String failureReason;

    /**
     * Creates a successful simulation result.
     *
     * @param extractedData the structured data extracted during simulation, must not be null
     * @param linesProcessed the number of document lines processed
     * @param linesMatched the number of lines that matched patterns
     * @throws IllegalArgumentException if extractedData is null or line counts are negative
     */
    public SimulationResult(Map<String, Object> extractedData, int linesProcessed, int linesMatched) {
        this.success = true;
        this.extractedData = Collections.unmodifiableMap(new LinkedHashMap<>(
            Objects.requireNonNull(extractedData, "Extracted data cannot be null")));
        this.linesProcessed = validateLinesProcessed(linesProcessed);
        this.linesMatched = validateLinesMatched(linesMatched);
        this.failureReason = null;
    }

    /**
     * Creates a failed simulation result.
     *
     * @param failureReason the reason for simulation failure, must not be null
     * @param linesProcessed the number of lines processed before failure
     * @throws IllegalArgumentException if failureReason is null or linesProcessed is negative
     */
    public SimulationResult(String failureReason, int linesProcessed) {
        this.success = false;
        this.extractedData = Collections.emptyMap();
        this.linesProcessed = validateLinesProcessed(linesProcessed);
        this.linesMatched = 0;
        this.failureReason = Objects.requireNonNull(failureReason, "Failure reason cannot be null");
    }

    private static int validateLinesProcessed(int linesProcessed) {
        if (linesProcessed < 0) {
            throw new IllegalArgumentException("Lines processed cannot be negative: " + linesProcessed);
        }
        return linesProcessed;
    }

    private static int validateLinesMatched(int linesMatched) {
        if (linesMatched < 0) {
            throw new IllegalArgumentException("Lines matched cannot be negative: " + linesMatched);
        }
        return linesMatched;
    }

    /**
     * Returns whether the simulation was successful.
     *
     * @return true if the pattern matched successfully, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the extracted structured data from a successful simulation.
     *
     * @return the extracted data as a Map, never null but may be empty if simulation failed
     */
    public Map<String, Object> getExtractedData() {
        return extractedData;
    }

    /**
     * Returns the number of lines that matched patterns during simulation.
     *
     * @return the number of lines matched, always non-negative
     */
    public int getLinesMatched() {
        return linesMatched;
    }

    /**
     * Returns the number of document lines processed during simulation.
     *
     * @return the number of lines processed, always non-negative
     */
    public int getLinesProcessed() {
        return linesProcessed;
    }

    /**
     * Returns the failure reason from a failed simulation.
     *
     * @return the failure reason, or null if the simulation was successful
     */
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimulationResult that = (SimulationResult) obj;
        return success == that.success &&
               linesProcessed == that.linesProcessed &&
               linesMatched == that.linesMatched &&
               Objects.equals(extractedData, that.extractedData) &&
               Objects.equals(failureReason, that.failureReason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, extractedData, linesProcessed, linesMatched, failureReason);
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("SimulationResult{success=true, linesProcessed=%d, linesMatched=%d, dataKeys=%s}", 
                               linesProcessed, linesMatched, extractedData.keySet());
        } else {
            return String.format("SimulationResult{success=false, linesProcessed=%d, error='%s'}", 
                               linesProcessed, failureReason);
        }
    }
}