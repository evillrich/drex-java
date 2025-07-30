package io.github.evillrich.drex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the DrexMatchResult class using integration testing approach.
 * Since SimulationResult is package-private, we test DrexMatchResult through DrexMatcher.
 */
@DisplayName("DrexMatchResult")
class DrexMatchResultTest {

    private DrexMatcher createTestMatcher() {
        DrexPattern pattern = new DrexPattern(
            "1.0",
            "TestPattern",
            null,
            "data",
            0,
            List.of(
                new Line(
                    null,
                    "Test: (.+)",
                    PropertyBinding.of("message")
                )
            )
        );
        return new DrexMatcher(pattern);
    }

    @Test
    @DisplayName("Should provide successful match results correctly")
    void shouldProvideSuccessfulMatchResults() {
        DrexMatcher matcher = createTestMatcher();
        
        List<String> input = Arrays.asList("Test: Success");
        DrexMatchResult result = matcher.findMatch(input);
        
        // Verify success properties
        assertTrue(result.isSuccess());
        assertTrue(result.hasData());
        assertEquals(1, result.getLinesMatched());
        assertEquals(1, result.getLinesProcessed());
        assertNull(result.getFailureReason());
        
        // Verify JSON string
        String json = result.getJsonString();
        assertNotNull(json);
        assertTrue(json.contains("Success"));
        assertTrue(json.contains("message"));
        // Should be valid JSON (starts and ends with braces)
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
    }

    @Test
    @DisplayName("Should provide failed match results correctly")
    void shouldProvideFailedMatchResults() {
        DrexMatcher matcher = createTestMatcher();
        
        // Input that won't match the pattern
        List<String> input = Arrays.asList("NoMatch: Failed");
        DrexMatchResult result = matcher.findMatch(input);
        
        // Verify failure properties
        assertFalse(result.isSuccess());
        assertFalse(result.hasData());
        assertEquals(0, result.getLinesMatched());
        assertEquals(1, result.getLinesProcessed());
        assertNotNull(result.getFailureReason());
        
        // Verify empty data
        assertTrue(result.getData().isEmpty());
        assertEquals("{}", result.getJsonString());
    }

    @Test
    @DisplayName("Should handle empty input correctly")
    void shouldHandleEmptyInputCorrectly() {
        DrexMatcher matcher = createTestMatcher();
        
        List<String> emptyInput = Arrays.asList();
        DrexMatchResult result = matcher.findMatch(emptyInput);
        
        assertFalse(result.isSuccess());
        assertFalse(result.hasData());
        assertEquals(0, result.getLinesMatched());
        assertEquals(0, result.getLinesProcessed());
        assertTrue(result.getData().isEmpty());
        assertEquals("{}", result.getJsonString());
    }

    @Test
    @DisplayName("Should have informative toString")
    void shouldHaveInformativeToString() {
        DrexMatcher matcher = createTestMatcher();
        
        // Test successful result
        List<String> input = Arrays.asList("Test: Success");
        DrexMatchResult result = matcher.findMatch(input);
        
        String toString = result.toString();
        assertTrue(toString.contains("success=true"));
        assertTrue(toString.contains("linesProcessed=1"));
        assertTrue(toString.contains("linesMatched=1"));
        
        // Test failed result  
        List<String> failInput = Arrays.asList("NoMatch: Failed");
        DrexMatchResult failedResult = matcher.findMatch(failInput);
        
        String failToString = failedResult.toString();
        assertTrue(failToString.contains("success=false"));
        assertTrue(failToString.contains("linesProcessed=1"));
        assertTrue(failToString.contains("error="));
    }
}