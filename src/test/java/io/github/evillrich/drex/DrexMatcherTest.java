package io.github.evillrich.drex;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the public DrexMatcher API.
 */
@DisplayName("DrexMatcher Public API")
class DrexMatcherTest {

    @Test
    @DisplayName("Should create matcher from pattern and execute basic matching")
    void shouldCreateMatcherAndExecuteBasicMatching() {
        // Create a simple pattern that matches lines with "Hello: <name>"
        DrexPattern pattern = new DrexPattern(
            "1.0",
            "HelloPattern", 
            "Test pattern",
            "greeting",
            0,
            List.of(
                new Line(
                    "Hello line",
                    "Hello: (.+)",
                    PropertyBinding.of("name")
                )
            )
        );

        // Create matcher
        DrexMatcher matcher = new DrexMatcher(pattern);
        
        // Verify matcher properties
        assertEquals(pattern, matcher.getPattern());
        
        // Test document
        List<String> documentLines = Arrays.asList("Hello: World");
        
        // Execute matching
        DrexMatchResult result = matcher.findMatch(documentLines);
        
        // Verify results
        assertTrue(result.isSuccess(), "Match should succeed");
        assertTrue(result.hasData(), "Should have extracted data");
        assertEquals(1, result.getLinesMatched());
        assertEquals(1, result.getLinesProcessed());
        assertNull(result.getFailureReason());
        
        // Verify extracted data
        Map<String, Object> data = result.getData();
        assertNotNull(data);
        assertTrue(data.containsKey("greeting"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> greeting = (Map<String, Object>) data.get("greeting");
        assertEquals("World", greeting.get("name"));
        
        // Verify JSON output
        String json = result.getJsonString();
        assertNotNull(json);
        assertTrue(json.contains("World"));
        assertTrue(json.contains("name"));
    }

    @Test
    @DisplayName("Should handle matching failure gracefully")
    void shouldHandleMatchingFailureGracefully() {
        // Create a pattern that expects "Hello:" but we'll give it "Goodbye:"
        DrexPattern pattern = new DrexPattern(
            "1.0",
            "HelloPattern",
            null,
            "greeting", 
            0,
            List.of(
                new Line(
                    null,
                    "Hello: (.+)",
                    PropertyBinding.of("name")
                )
            )
        );

        DrexMatcher matcher = new DrexMatcher(pattern);
        
        // Document that doesn't match
        List<String> documentLines = Arrays.asList("Goodbye: World");
        
        DrexMatchResult result = matcher.findMatch(documentLines);
        
        // Verify failure handling
        assertFalse(result.isSuccess(), "Match should fail");
        assertFalse(result.hasData(), "Should not have extracted data");
        assertEquals(0, result.getLinesMatched());
        assertEquals(1, result.getLinesProcessed());
        assertNotNull(result.getFailureReason());
        
        // Verify empty data
        assertTrue(result.getData().isEmpty());
        assertEquals("{}", result.getJsonString());
    }

    @Test
    @DisplayName("Should provide multiple input methods")
    void shouldProvideMultipleInputMethods() {
        DrexPattern pattern = new DrexPattern(
            "1.0",
            "TestPattern",
            null,
            "data",
            0,
            List.of(
                new Line(
                    null,
                    "Value: (.+)",
                    PropertyBinding.of("value")
                )
            )
        );

        DrexMatcher matcher = new DrexMatcher(pattern);
        
        // Test List<String> input
        List<String> listInput = Arrays.asList("Value: FromList");
        DrexMatchResult listResult = matcher.findMatch(listInput);
        assertTrue(listResult.isSuccess());
        
        // Test String[] input  
        String[] arrayInput = {"Value: FromArray"};
        DrexMatchResult arrayResult = matcher.findMatch(arrayInput);
        assertTrue(arrayResult.isSuccess());
        
        // Test String input (with newlines)
        String stringInput = "Value: FromString\nSecond line";
        DrexMatchResult stringResult = matcher.findMatch(stringInput);
        assertTrue(stringResult.isSuccess());
    }

    @Test
    @DisplayName("Should work with DrexPattern.matcher() convenience method")
    void shouldWorkWithConvenienceMethod() {
        DrexPattern pattern = new DrexPattern(
            "1.0",
            "ConveniencePattern",
            null,
            "test",
            0,
            List.of(
                new Line(
                    null,
                    "Test: (.+)",
                    PropertyBinding.of("message")
                )
            )
        );

        // Use convenience method
        DrexMatchResult result = pattern.matcher().findMatch(
            Arrays.asList("Test: Convenience works!")
        );
        
        assertTrue(result.isSuccess());
        assertTrue(result.hasData());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> testData = (Map<String, Object>) result.getData().get("test");
        assertEquals("Convenience works!", testData.get("message"));
    }

    @Test
    @DisplayName("Should handle null inputs gracefully")
    void shouldHandleNullInputsGracefully() {
        DrexPattern pattern = new DrexPattern(
            "1.0",
            "NullTestPattern",
            null,
            "test",
            0,
            List.of()
        );

        DrexMatcher matcher = new DrexMatcher(pattern);
        
        // Test null inputs
        assertThrows(IllegalArgumentException.class, () -> {
            matcher.findMatch((List<String>) null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            matcher.findMatch((String[]) null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            matcher.findMatch((String) null);
        });
    }

    @Test
    @DisplayName("Should construct DrexMatcher with null pattern throws exception")
    void shouldThrowExceptionForNullPattern() {
        assertThrows(IllegalArgumentException.class, () -> {
            new DrexMatcher(null);
        });
    }

    @Test
    @DisplayName("Should have proper toString representation")
    void shouldHaveProperToStringRepresentation() {
        DrexPattern pattern = new DrexPattern(
            "2.0",
            "TestPattern",
            null,
            "data",
            0,
            List.of()
        );

        DrexMatcher matcher = new DrexMatcher(pattern);
        String toString = matcher.toString();
        
        assertTrue(toString.contains("TestPattern"));
        assertTrue(toString.contains("2.0"));
    }
}