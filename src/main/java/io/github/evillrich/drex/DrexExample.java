package io.github.evillrich.drex;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Example demonstrating the public DrexMatcher API.
 * <p>
 * This example shows how to use the clean public API without
 * needing to understand the internal engine complexity.
 */
public class DrexExample {
    
    public static void main(String[] args) {
        // Create a pattern that matches invoice lines
        DrexPattern invoicePattern = new DrexPattern(
            "1.0",                    // version
            "SimpleInvoice",          // name  
            "Extracts invoice data",  // comment
            "invoice",                // root object name
            0,                        // edit distance (no fuzzy matching)
            List.of(                  // pattern elements
                new Line(
                    "Invoice header",
                    "Invoice: (.+)",
                    PropertyBinding.of("number")
                ),
                new Line(
                    "Amount line", 
                    "Amount: \\$([0-9.]+)",
                    PropertyBinding.of("amount")
                ),
                new Line(
                    "Customer line",
                    "Customer: (.+)",
                    PropertyBinding.of("customer")
                )
            )
        );
        
        // Sample document
        List<String> documentLines = Arrays.asList(
            "Invoice: INV-12345",
            "Amount: $1,234.56", 
            "Customer: Acme Corp"
        );
        
        // Method 1: Direct matcher creation
        DrexMatcher matcher = new DrexMatcher(invoicePattern);
        DrexMatchResult result = matcher.findMatch(documentLines);
        
        // Method 2: Convenience method
        // DrexMatchResult result = invoicePattern.matcher().findMatch(documentLines);
        
        // Process results
        if (result.isSuccess()) {
            System.out.println("✅ Match successful!");
            System.out.println("Lines matched: " + result.getLinesMatched());
            System.out.println("Lines processed: " + result.getLinesProcessed());
            
            // Access structured data
            Map<String, Object> data = result.getData();
            System.out.println("Extracted data: " + data);
            
            // Access JSON string
            String json = result.getJsonString();
            System.out.println("JSON output: " + json);
            
        } else {
            System.out.println("❌ Match failed: " + result.getFailureReason());
            System.out.println("Lines processed: " + result.getLinesProcessed());
        }
    }
}