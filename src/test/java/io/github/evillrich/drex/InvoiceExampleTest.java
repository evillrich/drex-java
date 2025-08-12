package io.github.evillrich.drex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for the invoice example from docs/examples/invoice.
 * Demonstrates using the fluent API to build patterns that match the JSON pattern structure.
 */
@DisplayName("Invoice Example Pattern Tests")
class InvoiceExampleTest {

    private DrexPattern invoicePattern;

    @BeforeEach
    void setUp() {
        // Build the invoice pattern using fluent API
        // This matches the structure in docs/examples/invoice/invoice-pattern.json
        invoicePattern = DrexPattern.builder()
            .version("1.0")
            .name("InvoicePattern")
            .comment("Simple invoice extraction pattern")
            .bindObject("invoice")
            .elements(
                // Invoice number line
                new Line(
                    "Extract invoice number",
                    "Invoice #(\\d+)",
                    PropertyBinding.of("id")
                ),
                
                // Repeat for line items
                new Repeat(
                    "Extract line items",
                    Repeat.Mode.ONE_OR_MORE,
                    "items",
                    new Line(
                        "Item name, quantity, price",
                        "(\\S+)\\s+(\\d+)\\s+([\\d\\.]+)",
                        PropertyBinding.of("name"),
                        PropertyBinding.of("qty"),
                        PropertyBinding.of("price")
                    )
                ),
                
                // Or element for total or anyline
                new Or(
                    "Match total or skip line",
                    new Line(
                        null,
                        "Total: ([\\d\\.]+)",
                        PropertyBinding.of("total")
                    ),
                    new Anyline(null)
                )
            )
            .build();
        
        // Compile the pattern
        invoicePattern.compile();
    }

    @Nested
    @DisplayName("Basic Invoice Matching")
    class BasicInvoiceMatching {
        
        @Test
        @DisplayName("Should match the sample invoice document")
        void shouldMatchSampleInvoiceDocument() {
            // Test with the sample invoice from docs/examples/invoice/invoice.txt
            List<String> invoiceLines = Arrays.asList(
                "Invoice #12345",
                "Pen 2 1.50",
                "Notebook 1 3.99",
                "Total: 6.99"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            // Verify match succeeded
            assertTrue(result.isSuccess(), "Pattern should match the invoice");
            assertTrue(result.hasData(), "Should extract data from invoice");
            assertEquals(4, result.getLinesProcessed(), "Should process all 4 lines");
            assertEquals(4, result.getLinesMatched(), "Should match all 4 lines");
            assertNull(result.getFailureReason(), "Should have no failure reason");
            
            // Verify extracted data structure
            Map<String, Object> data = result.getData();
            assertNotNull(data);
            assertTrue(data.containsKey("invoice"));
            
            @SuppressWarnings("unchecked")
            Map<String, Object> invoice = (Map<String, Object>) data.get("invoice");
            assertNotNull(invoice);
            
            // Verify invoice ID
            assertEquals("12345", invoice.get("id"));
            
            // Verify items array
            assertTrue(invoice.containsKey("items"));
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
            assertNotNull(items);
            assertEquals(2, items.size(), "Should have 2 items");
            
            // Verify first item (Pen)
            Map<String, Object> item1 = items.get(0);
            assertEquals("Pen", item1.get("name"));
            assertEquals("2", item1.get("qty"));
            assertEquals("1.50", item1.get("price"));
            
            // Verify second item (Notebook)
            Map<String, Object> item2 = items.get(1);
            assertEquals("Notebook", item2.get("name"));
            assertEquals("1", item2.get("qty"));
            assertEquals("3.99", item2.get("price"));
            
            // Verify total
            assertEquals("6.99", invoice.get("total"));
            
            // Verify JSON output format
            String json = result.getJsonString();
            assertNotNull(json);
            assertTrue(json.contains("\"id\":\"12345\""));
            assertTrue(json.contains("\"name\":\"Pen\""));
            assertTrue(json.contains("\"qty\":\"2\""));
            assertTrue(json.contains("\"price\":\"1.50\""));
            assertTrue(json.contains("\"name\":\"Notebook\""));
            assertTrue(json.contains("\"qty\":\"1\""));
            assertTrue(json.contains("\"price\":\"3.99\""));
            assertTrue(json.contains("\"total\":\"6.99\""));
        }
        
        @Test
        @DisplayName("Should read and match actual invoice.txt file")
        void shouldReadAndMatchActualInvoiceFile() throws IOException {
            // Read the actual invoice.txt file
            Path invoicePath = Paths.get("docs/examples/invoice/invoice.txt");
            if (Files.exists(invoicePath)) {
                List<String> invoiceLines = Files.readAllLines(invoicePath);
                
                DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
                
                assertTrue(result.isSuccess(), "Should match the actual invoice file");
                
                // Verify against expected output structure
                @SuppressWarnings("unchecked")
                Map<String, Object> invoice = (Map<String, Object>) result.getData().get("invoice");
                assertEquals("12345", invoice.get("id"));
                assertEquals("6.99", invoice.get("total"));
                
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
                assertEquals(2, items.size());
            }
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {
        
        @Test
        @DisplayName("Should handle invoice with multiple items")
        void shouldHandleInvoiceWithMultipleItems() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #98765",
                "Widget 5 2.50",
                "Gadget 3 4.99",
                "Doohickey 1 15.00",
                "Thingamajig 2 7.25",
                "Total: 57.72"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            assertTrue(result.isSuccess());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> invoice = (Map<String, Object>) result.getData().get("invoice");
            assertEquals("98765", invoice.get("id"));
            assertEquals("57.72", invoice.get("total"));
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
            assertEquals(4, items.size(), "Should have 4 items");
            
            // Verify all items were captured
            assertEquals("Widget", items.get(0).get("name"));
            assertEquals("Gadget", items.get(1).get("name"));
            assertEquals("Doohickey", items.get(2).get("name"));
            assertEquals("Thingamajig", items.get(3).get("name"));
        }
        
        @Test
        @DisplayName("Should handle invoice with single item")
        void shouldHandleInvoiceWithSingleItem() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #11111",
                "SingleItem 1 9.99",
                "Total: 9.99"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            assertTrue(result.isSuccess());
            
            @SuppressWarnings("unchecked")
            Map<String, Object> invoice = (Map<String, Object>) result.getData().get("invoice");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
            assertEquals(1, items.size(), "Should have exactly 1 item");
        }
        
        @Test
        @DisplayName("Should handle missing total line using anyline")
        void shouldHandleMissingTotalLine() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #55555",
                "Item1 2 5.00",
                "Item2 1 3.00",
                "Thank you for your business!"  // Not a total line, should match anyline
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            assertTrue(result.isSuccess(), "Should succeed even without total");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> invoice = (Map<String, Object>) result.getData().get("invoice");
            assertEquals("55555", invoice.get("id"));
            assertFalse(invoice.containsKey("total"), "Should not have total field");
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
            assertEquals(2, items.size());
        }
        
        @Test
        @DisplayName("Should handle decimal quantities")
        void shouldHandleDecimalQuantities() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #33333",
                "BulkItem 2.5 10.00",  // Decimal quantity
                "Total: 25.00"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            // This should fail because our regex expects integer quantities
            // The pattern uses (\\d+) for quantity, not ([\\d\\.]+)
            assertFalse(result.isSuccess(), "Should fail with decimal quantities");
        }
    }

    @Nested
    @DisplayName("Error Case Tests")
    class ErrorCaseTests {
        
        @Test
        @DisplayName("Should fail when invoice number is missing")
        void shouldFailWhenInvoiceNumberMissing() {
            List<String> invoiceLines = Arrays.asList(
                "Order Receipt",  // Not an invoice number line
                "Item1 1 5.00",
                "Total: 5.00"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            assertFalse(result.isSuccess(), "Should fail without invoice number");
            assertNotNull(result.getFailureReason());
            assertEquals(1, result.getLinesProcessed());
        }
        
        @Test
        @DisplayName("Should fail with empty document")
        void shouldFailWithEmptyDocument() {
            List<String> emptyDocument = Arrays.asList();
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(emptyDocument);
            
            assertFalse(result.isSuccess(), "Should fail with empty document");
            assertNotNull(result.getFailureReason());
            assertEquals(0, result.getLinesProcessed());
        }
        
        @Test
        @DisplayName("Should fail when no items match")
        void shouldFailWhenNoItemsMatch() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #77777",
                "This is not an item line",
                "Neither is this",
                "Total: 0.00"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            // Should fail because repeat mode is ONE_OR_MORE
            assertFalse(result.isSuccess(), "Should fail when no items match");
            assertTrue(result.getFailureReason().contains("No valid transition"));
        }
        
        @Test
        @DisplayName("Should handle malformed item lines")
        void shouldHandleMalformedItemLines() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #88888",
                "GoodItem 1 5.00",      // Valid item
                "BadItem missing price", // Invalid item format
                "Total: 5.00"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            // Should fail at the malformed line
            assertFalse(result.isSuccess(), "Should fail on malformed item");
            assertEquals(3, result.getLinesProcessed());
        }
        
        @Test
        @DisplayName("Should handle partial document match")
        void shouldHandlePartialDocumentMatch() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #99999",
                "Item1 2 10.00"
                // Document ends abruptly without total
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            // Pattern should fail as it can't complete
            assertFalse(result.isSuccess(), "Should fail with incomplete document");
            assertNotNull(result.getFailureReason());
            assertTrue(result.getFailureReason().contains("Pattern incomplete") ||
                      result.getFailureReason().contains("No valid transition"));
        }
    }

    @Nested
    @DisplayName("Format Variation Tests")
    class FormatVariationTests {
        
        @Test
        @DisplayName("Should handle different whitespace variations")
        void shouldHandleDifferentWhitespaceVariations() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #44444",
                "Item1    3    12.50",     // Multiple spaces
                "Item2\t1\t5.00",          // Tabs (matched by \\s+)
                "Item3  2  8.75",          // Mixed spacing
                "Total: 39.00"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            assertTrue(result.isSuccess(), "Should handle whitespace variations");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> invoice = (Map<String, Object>) result.getData().get("invoice");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) invoice.get("items");
            assertEquals(3, items.size(), "Should capture all 3 items");
        }
        
        @Test
        @DisplayName("Should handle currency symbols in prices")
        void shouldHandleCurrencySymbolsInPrices() {
            List<String> invoiceLines = Arrays.asList(
                "Invoice #66666",
                "Item1 1 $5.00",  // Dollar sign in price
                "Total: $5.00"
            );
            
            DrexMatchResult result = invoicePattern.matcher().findMatch(invoiceLines);
            
            // Should fail because pattern expects price to start with digit
            assertFalse(result.isSuccess(), "Should fail with currency symbols");
        }
    }
}