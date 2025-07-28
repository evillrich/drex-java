package io.github.evillrich.drex.engine;

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
public final class SimulationResult {
    private final boolean success;
    private final String extractedData;
    private final int linesProcessed;
    private final String errorMessage;

    /**
     * Creates a successful simulation result.
     *
     * @param extractedData the JSON data extracted during simulation, must not be null
     * @param linesProcessed the number of document lines processed
     * @throws IllegalArgumentException if extractedData is null or linesProcessed is negative
     */
    public SimulationResult(String extractedData, int linesProcessed) {
        this.success = true;
        this.extractedData = Objects.requireNonNull(extractedData, "Extracted data cannot be null");
        this.linesProcessed = validateLinesProcessed(linesProcessed);
        this.errorMessage = null;
    }

    /**
     * Creates a failed simulation result.
     *
     * @param errorMessage description of why the simulation failed, must not be null
     * @param linesProcessed the number of document lines processed before failure
     * @throws IllegalArgumentException if errorMessage is null or linesProcessed is negative
     */
    public SimulationResult(String errorMessage, int linesProcessed) {
        this.success = false;
        this.extractedData = null;
        this.linesProcessed = validateLinesProcessed(linesProcessed);
        this.errorMessage = Objects.requireNonNull(errorMessage, "Error message cannot be null");
    }

    private static int validateLinesProcessed(int linesProcessed) {
        if (linesProcessed < 0) {
            throw new IllegalArgumentException("Lines processed cannot be negative: " + linesProcessed);
        }
        return linesProcessed;
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
     * Returns the extracted JSON data from a successful simulation.
     *
     * @return the extracted data as JSON string, or null if the simulation failed
     */
    public String getExtractedData() {
        return extractedData;
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
     * Returns the error message from a failed simulation.
     *
     * @return the error message, or null if the simulation was successful
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SimulationResult that = (SimulationResult) obj;
        return success == that.success &&
               linesProcessed == that.linesProcessed &&
               Objects.equals(extractedData, that.extractedData) &&
               Objects.equals(errorMessage, that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(success, extractedData, linesProcessed, errorMessage);
    }

    @Override
    public String toString() {
        if (success) {
            return String.format("SimulationResult{success=true, linesProcessed=%d, dataLength=%d}", 
                               linesProcessed, extractedData != null ? extractedData.length() : 0);
        } else {
            return String.format("SimulationResult{success=false, linesProcessed=%d, error='%s'}", 
                               linesProcessed, errorMessage);
        }
    }
}