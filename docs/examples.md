# Drex Pattern Examples

This document provides comprehensive examples of Drex patterns for different document types, including both JSON pattern definitions and Java Fluent API equivalents.

## Example Files

All examples include test files:
- **Input document** (.txt): The source document to be processed
- **Pattern file** (.json): The Drex pattern definition
- **Expected output** (.json): The expected extracted data

Test files are located in `docs/examples/` subdirectories.

---

## 1. Simple Invoice Example

**Location**: `docs/examples/invoice/`

### Input Document
```
Invoice #12345
Pen 2 1.50
Notebook 1 3.99
Total: 6.99
```

### JSON Pattern
```json
{
  "version": "1.0",
  "name": "InvoicePattern",
  "comment": "Simple invoice extraction pattern",
  "bindObject": "invoice",
  "elements": [
    { 
      "line": { 
        "regex": "Invoice #(\\d+)", 
        "bindProperties": [{"property": "id"}], 
        "comment": "Extract invoice number" 
      } 
    },
    { 
      "repeat": {
        "bindArray": "items",
        "mode": "oneOrMore",
        "comment": "Extract line items",
        "elements": [
          { 
            "line": { 
              "regex": "(\\S+)\\s+(\\d+)\\s+([\\d\\.]+)", 
              "bindProperties": [
                {"property": "name"},
                {"property": "qty"},
                {"property": "price"}
              ], 
              "comment": "Item name, quantity, price" 
            } 
          }
        ]
      }
    },
    { 
      "or": {
        "comment": "Match total or skip line",
        "elements": [
          { 
            "line": { 
              "regex": "Total: ([\\d\\.]+)", 
              "bindProperties": [{"property": "total"}] 
            } 
          },
          { "anyline": {} }
        ]
      }
    }
  ]
}
```

### Java Fluent API
```java
DrexPattern invoicePattern = DrexPattern.builder()
    .version("1.0")
    .name("InvoicePattern")
    .comment("Simple invoice extraction pattern")
    .bindObject("invoice")
    .elements(
        Line.builder()
            .regex("Invoice #(\\\\d+)")
            .bindProperties(PropertyBinding.of("id"))
            .comment("Extract invoice number")
            .build(),
        Repeat.builder()
            .bindArray("items")
            .mode(Repeat.Mode.ONE_OR_MORE)
            .comment("Extract line items")
            .elements(
                Line.builder()
                    .regex("(\\\\S+)\\\\s+(\\\\d+)\\\\s+([\\\\d\\\\.]+)")
                    .bindProperties(
                        PropertyBinding.of("name"),
                        PropertyBinding.of("qty"),
                        PropertyBinding.of("price")
                    )
                    .comment("Item name, quantity, price")
                    .build()
            )
            .build(),
        Or.builder()
            .comment("Match total or skip line")
            .elements(
                Line.builder()
                    .regex("Total: ([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("total"))
                    .build(),
                Anyline.builder().build()
            )
            .build()
    )
    .build();
```

### Expected Output
```json
{
  "invoice": {
    "id": "12345",
    "items": [
      { "name": "Pen", "qty": "2", "price": "1.50" },
      { "name": "Notebook", "qty": "1", "price": "3.99" }
    ],
    "total": "6.99"
  }
}
```

---

## 2. Tabular Invoice Example

**Location**: `docs/examples/table-invoice/`

### Input Document
```
Invoice #67890
Date: 2024-01-15

Item	Quantity	Unit Price	Total
Laptop	1	899.99	899.99
Mouse	2	25.00	50.00
Keyboard	1	75.50	75.50

Subtotal: 1025.49
Tax: 82.04
Total: 1107.53
```

### JSON Pattern
```json
{
  "version": "1.0",
  "name": "TableInvoicePattern",
  "comment": "Invoice pattern for tabular format",
  "bindObject": "invoice",
  "elements": [
    { 
      "line": { 
        "regex": "Invoice #(\\d+)", 
        "bindProperties": [{"property": "id"}], 
        "comment": "Invoice ID" 
      } 
    },
    { 
      "line": { 
        "regex": "Date: (\\d{4}-\\d{2}-\\d{2})", 
        "bindProperties": [{"property": "date"}], 
        "comment": "Invoice date in YYYY-MM-DD format" 
      } 
    },
    { "anyline": { "comment": "Skip blank line" } },
    { 
      "line": { 
        "regex": "Item\\tQuantity\\tUnit Price\\tTotal", 
        "comment": "Table header" 
      } 
    },
    { 
      "repeat": {
        "bindArray": "items",
        "mode": "oneOrMore",
        "comment": "Extract table rows",
        "elements": [
          { 
            "line": { 
              "regex": "([^\\t]+)\\t(\\d+)\\t([\\d\\.]+)\\t([\\d\\.]+)", 
              "bindProperties": [
                {"property": "item"},
                {"property": "quantity"},
                {"property": "unitPrice"},
                {"property": "total"}
              ], 
              "comment": "Tab-separated item details" 
            } 
          }
        ]
      }
    },
    { "anyline": { "comment": "Skip blank line before totals" } },
    { 
      "line": { 
        "regex": "Subtotal: ([\\d\\.]+)", 
        "bindProperties": [{"property": "subtotal"}] 
      } 
    },
    { 
      "line": { 
        "regex": "Tax: ([\\d\\.]+)", 
        "bindProperties": [{"property": "tax"}] 
      } 
    },
    { 
      "line": { 
        "regex": "Total: ([\\d\\.]+)", 
        "bindProperties": [{"property": "total"}], 
        "comment": "Final total amount" 
      } 
    }
  ]
}
```

### Java Fluent API
```java
DrexPattern tableInvoicePattern = DrexPattern.builder()
    .version("1.0")
    .name("TableInvoicePattern")
    .comment("Invoice pattern for tabular format")
    .bindObject("invoice")
    .elements(
        Line.builder()
            .regex("Invoice #(\\\\d+)")
            .bindProperties(PropertyBinding.of("id"))
            .comment("Invoice ID")
            .build(),
        Line.builder()
            .regex("Date: (\\\\d{4}-\\\\d{2}-\\\\d{2})")
            .bindProperties(PropertyBinding.of("date"))
            .comment("Invoice date in YYYY-MM-DD format")
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Line.builder()
            .regex("Item\\\\tQuantity\\\\tUnit Price\\\\tTotal")
            .comment("Table header")
            .build(),
        Repeat.builder()
            .bindArray("items")
            .mode(Repeat.Mode.ONE_OR_MORE)
            .comment("Extract table rows")
            .elements(
                Line.builder()
                    .regex("([^\\\\t]+)\\\\t(\\\\d+)\\\\t([\\\\d\\\\.]+)\\\\t([\\\\d\\\\.]+)")
                    .bindProperties(
                        PropertyBinding.of("item"),
                        PropertyBinding.of("quantity"),
                        PropertyBinding.of("unitPrice"),
                        PropertyBinding.of("total")
                    )
                    .comment("Tab-separated item details")
                    .build()
            )
            .build(),
        Anyline.builder()
            .comment("Skip blank line before totals")
            .build(),
        Line.builder()
            .regex("Subtotal: ([\\\\d\\\\.]+)")
            .bindProperties(PropertyBinding.of("subtotal"))
            .build(),
        Line.builder()
            .regex("Tax: ([\\\\d\\\\.]+)")
            .bindProperties(PropertyBinding.of("tax"))
            .build(),
        Line.builder()
            .regex("Total: ([\\\\d\\\\.]+)")
            .bindProperties(PropertyBinding.of("total"))
            .comment("Final total amount")
            .build()
    )
    .build();
```

### Expected Output
```json
{
  "invoice": {
    "id": "67890",
    "date": "2024-01-15",
    "items": [
      { "item": "Laptop", "quantity": "1", "unitPrice": "899.99", "total": "899.99" },
      { "item": "Mouse", "quantity": "2", "unitPrice": "25.00", "total": "50.00" },
      { "item": "Keyboard", "quantity": "1", "unitPrice": "75.50", "total": "75.50" }
    ],
    "subtotal": "1025.49",
    "tax": "82.04",
    "total": "1107.53"
  }
}
```

---

## 3. Purchase Order Example

**Location**: `docs/examples/purchase-order/`

### Input Document
```
PO Number: PO-2024-001234
Date: 2024-03-15
Vendor: Tech Supply Corp
Ship To: Acme Corporation, 123 Business Ave, New York, NY 10001

Line  Part Number    Description                 Qty    Unit Price    Total
1     LAP-4521      Dell Laptop i7 16GB         5      $1,200.00     $6,000.00
2     MON-2847      27" LED Monitor             10     $325.00       $3,250.00
3     KEY-1092      Wireless Keyboard           15     $45.99        $689.85
4     MOU-3384      Optical Mouse               15     $19.99        $299.85

Subtotal: $10,239.70
Shipping: $125.00
Tax: $829.57
Total: $11,194.27

Terms: Net 30
Ship Via: FedEx Ground
```

Demonstrates complex document structure with vendor information, line items, and multiple totals.

### Java Fluent API
```java
DrexPattern purchaseOrderPattern = DrexPattern.builder()
    .version("1.0")
    .name("PurchaseOrderPattern")
    .comment("Extract purchase order information including vendor, shipping, and line items")
    .bindObject("purchaseOrder")
    .elements(
        Line.builder()
            .regex("PO Number: (\\\\w+-\\\\d+-\\\\d+)")
            .bindProperties(PropertyBinding.of("poNumber"))
            .comment("Purchase order number")
            .build(),
        Line.builder()
            .regex("Date: (\\\\d{4}-\\\\d{2}-\\\\d{2})")
            .bindProperties(PropertyBinding.of("date", "parseDate(yyyy-MM-dd)"))
            .build(),
        Line.builder()
            .regex("Vendor: (.+)")
            .bindProperties(PropertyBinding.of("vendor"))
            .build(),
        Line.builder()
            .regex("Ship To: (.+)")
            .bindProperties(PropertyBinding.of("shipTo"))
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Line.builder()
            .regex("Line\\\\s+Part Number\\\\s+Description\\\\s+Qty\\\\s+Unit Price\\\\s+Total")
            .comment("Table header")
            .build(),
        Repeat.builder()
            .bindArray("lineItems")
            .mode(Repeat.Mode.ONE_OR_MORE)
            .comment("Extract purchase order line items")
            .elements(
                Line.builder()
                    .regex("(\\\\d+)\\\\s+([A-Z]+-\\\\d+)\\\\s+(.+?)\\\\s+(\\\\d+)\\\\s+\\\\$([\\\\d,]+\\\\.\\\\d{2})\\\\s+\\\\$([\\\\d,]+\\\\.\\\\d{2})")
                    .bindProperties(
                        PropertyBinding.of("lineNo"),
                        PropertyBinding.of("partNumber"),
                        PropertyBinding.of("description"),
                        PropertyBinding.of("quantity"),
                        PropertyBinding.of("unitPrice", "currency()"),
                        PropertyBinding.of("totalPrice", "currency()")
                    )
                    .build()
            )
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Line.builder()
            .regex("Subtotal: \\\\$([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("subtotal", "currency()"))
            .build(),
        Line.builder()
            .regex("Shipping: \\\\$([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("shipping", "currency()"))
            .build(),
        Line.builder()
            .regex("Tax: \\\\$([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("tax", "currency()"))
            .build(),
        Line.builder()
            .regex("Total: \\\\$([\\\\d,]+\\\\.\\\\d{2})")
            .bindProperties(PropertyBinding.of("total", "currency()"))
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Line.builder()
            .regex("Terms: (.+)")
            .bindProperties(PropertyBinding.of("terms"))
            .build(),
        Line.builder()
            .regex("Ship Via: (.+)")
            .bindProperties(PropertyBinding.of("shipVia"))
            .build()
    )
    .build();
```

### Expected Output
```json
{
  "purchaseOrder": {
    "poNumber": "PO-2024-001234",
    "date": "2024-03-15",
    "vendor": "Tech Supply Corp",
    "shipTo": "Acme Corporation, 123 Business Ave, New York, NY 10001",
    "lineItems": [
      {
        "lineNo": "1",
        "partNumber": "LAP-4521",
        "description": "Dell Laptop i7 16GB",
        "quantity": "5",
        "unitPrice": "1200.00",
        "totalPrice": "6000.00"
      },
      {
        "lineNo": "2",
        "partNumber": "MON-2847",
        "description": "27\" LED Monitor",
        "quantity": "10",
        "unitPrice": "325.00",
        "totalPrice": "3250.00"
      },
      {
        "lineNo": "3",
        "partNumber": "KEY-1092",
        "description": "Wireless Keyboard",
        "quantity": "15",
        "unitPrice": "45.99",
        "totalPrice": "689.85"
      },
      {
        "lineNo": "4",
        "partNumber": "MOU-3384",
        "description": "Optical Mouse",
        "quantity": "15",
        "unitPrice": "19.99",
        "totalPrice": "299.85"
      }
    ],
    "subtotal": "10239.70",
    "shipping": "125.00",
    "tax": "829.57",
    "total": "11194.27",
    "terms": "Net 30",
    "shipVia": "FedEx Ground"
  }
}
```

---

## 4. Explanation of Benefits (EOB) Example

**Location**: `docs/examples/eob/`

### Input Document
```
EXPLANATION OF BENEFITS

Patient: John A. Smith
Member ID: ABC123456789
Group Number: 12345
Date of Service: 03/15/2024

Provider: Metro Family Health
Provider ID: 1234567890
Service Location: In-Network

Claim Number: CLM2024031501

Service Details:
Service Date: 03/15/2024
Provider: Dr. Sarah Johnson, MD
Service Code: 99213
Description: Office Visit - Level 3
Billed: $185.00
Allowed: $142.50
Deductible: $0.00
Copay: $25.00
Coinsurance: $0.00
Paid: $117.50
Patient Responsibility: $25.00

Service Date: 03/15/2024
Provider: Metro Family Health Lab
Service Code: 80053
Description: Comprehensive Metabolic Panel
Billed: $95.00
Allowed: $68.40
Deductible: $0.00
Copay: $0.00
Coinsurance: $13.68
Paid: $54.72
Patient Responsibility: $13.68

Summary:
Total Billed: $280.00
Total Allowed: $210.90
Total Paid: $172.22
Total Patient Responsibility: $38.68

Deductible Applied: $0.00
Deductible Remaining: $500.00
```

Demonstrates nested groups, patient information, provider details, and multiple service entries with fuzzy matching enabled.

### Key Features
- **Fuzzy matching** with `editDistance: 1` for OCR noise tolerance
- **Nested groups** for patient and provider information
- **Complex repeat patterns** for multiple services
- **Currency formatting** for monetary values

### Java Fluent API
```java
DrexPattern eobPattern = DrexPattern.builder()
    .version("1.0")
    .name("EOBPattern")
    .comment("Extract Explanation of Benefits information including patient details and claims")
    .bindObject("eob")
    .editDistance(1)
    .elements(
        Line.builder()
            .regex("EXPLANATION OF BENEFITS")
            .comment("Header validation")
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Group.builder()
            .bindObject("patient")
            .comment("Patient information section")
            .elements(
                Line.builder()
                    .regex("Patient: (.+)")
                    .bindProperties(PropertyBinding.of("name"))
                    .build(),
                Line.builder()
                    .regex("Member ID: (\\\\w+)")
                    .bindProperties(PropertyBinding.of("memberId"))
                    .build(),
                Line.builder()
                    .regex("Group Number: (\\\\d+)")
                    .bindProperties(PropertyBinding.of("groupNumber"))
                    .build(),
                Line.builder()
                    .regex("Date of Service: (\\\\d{2}/\\\\d{2}/\\\\d{4})")
                    .bindProperties(PropertyBinding.of("serviceDate", "parseDate(MM/dd/yyyy)"))
                    .build()
            )
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Group.builder()
            .bindObject("provider")
            .comment("Provider information")
            .elements(
                Line.builder()
                    .regex("Provider: (.+)")
                    .bindProperties(PropertyBinding.of("name"))
                    .build(),
                Line.builder()
                    .regex("Provider ID: (\\\\d+)")
                    .bindProperties(PropertyBinding.of("id"))
                    .build(),
                Line.builder()
                    .regex("Service Location: (.+)")
                    .bindProperties(PropertyBinding.of("location"))
                    .build()
            )
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Line.builder()
            .regex("Claim Number: (\\\\w+)")
            .bindProperties(PropertyBinding.of("claimNumber"))
            .build(),
        Anyline.builder()
            .comment("Skip blank line")
            .build(),
        Line.builder()
            .regex("Service Details:")
            .comment("Service details header")
            .build(),
        Repeat.builder()
            .bindArray("services")
            .mode(Repeat.Mode.ONE_OR_MORE)
            .comment("Extract service details")
            .elements(
                Group.builder()
                    .comment("Single service entry")
                    .elements(
                        Line.builder()
                            .regex("Service Date: (\\\\d{2}/\\\\d{2}/\\\\d{4})")
                            .bindProperties(PropertyBinding.of("serviceDate", "parseDate(MM/dd/yyyy)"))
                            .build(),
                        Line.builder()
                            .regex("Provider: (.+)")
                            .bindProperties(PropertyBinding.of("provider"))
                            .build(),
                        Line.builder()
                            .regex("Service Code: (\\\\w+)")
                            .bindProperties(PropertyBinding.of("serviceCode"))
                            .build(),
                        Line.builder()
                            .regex("Description: (.+)")
                            .bindProperties(PropertyBinding.of("description"))
                            .build(),
                        Line.builder()
                            .regex("Billed: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("billed", "currency()"))
                            .build(),
                        Line.builder()
                            .regex("Allowed: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("allowed", "currency()"))
                            .build(),
                        Line.builder()
                            .regex("Deductible: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("deductible", "currency()"))
                            .build(),
                        Line.builder()
                            .regex("Copay: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("copay", "currency()"))
                            .build(),
                        Line.builder()
                            .regex("Coinsurance: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("coinsurance", "currency()"))
                            .build(),
                        Line.builder()
                            .regex("Paid: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("paid", "currency()"))
                            .build(),
                        Line.builder()
                            .regex("Patient Responsibility: \\\\$([\\\\d\\\\.]+)")
                            .bindProperties(PropertyBinding.of("patientResponsibility", "currency()"))
                            .build(),
                        Anyline.builder()
                            .comment("Skip blank line between services")
                            .build()
                    )
                    .build()
            )
            .build(),
        Line.builder()
            .regex("Summary:")
            .comment("Summary section header")
            .build(),
        Group.builder()
            .bindObject("summary")
            .comment("Summary totals")
            .elements(
                Line.builder()
                    .regex("Total Billed: \\\\$([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("totalBilled", "currency()"))
                    .build(),
                Line.builder()
                    .regex("Total Allowed: \\\\$([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("totalAllowed", "currency()"))
                    .build(),
                Line.builder()
                    .regex("Total Paid: \\\\$([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("totalPaid", "currency()"))
                    .build(),
                Line.builder()
                    .regex("Total Patient Responsibility: \\\\$([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("totalPatientResponsibility", "currency()"))
                    .build(),
                Anyline.builder()
                    .comment("Skip blank line")
                    .build(),
                Line.builder()
                    .regex("Deductible Applied: \\\\$([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("deductibleApplied", "currency()"))
                    .build(),
                Line.builder()
                    .regex("Deductible Remaining: \\\\$([\\\\d\\\\.]+)")
                    .bindProperties(PropertyBinding.of("deductibleRemaining", "currency()"))
                    .build()
            )
            .build()
    )
    .build();
```

### Expected Output
```json
{
  "eob": {
    "patient": {
      "name": "John A. Smith",
      "memberId": "ABC123456789",
      "groupNumber": "12345",
      "serviceDate": "2024-03-15"
    },
    "provider": {
      "name": "Metro Family Health",
      "id": "1234567890",
      "location": "In-Network"
    },
    "claimNumber": "CLM2024031501",
    "services": [
      {
        "serviceDate": "2024-03-15",
        "provider": "Dr. Sarah Johnson, MD",
        "serviceCode": "99213",
        "description": "Office Visit - Level 3",
        "billed": "185.00",
        "allowed": "142.50",
        "deductible": "0.00",
        "copay": "25.00",
        "coinsurance": "0.00",
        "paid": "117.50",
        "patientResponsibility": "25.00"
      },
      {
        "serviceDate": "2024-03-15",
        "provider": "Metro Family Health Lab",
        "serviceCode": "80053",
        "description": "Comprehensive Metabolic Panel",
        "billed": "95.00",
        "allowed": "68.40",
        "deductible": "0.00",
        "copay": "0.00",
        "coinsurance": "13.68",
        "paid": "54.72",
        "patientResponsibility": "13.68"
      }
    ],
    "summary": {
      "totalBilled": "280.00",
      "totalAllowed": "210.90",
      "totalPaid": "172.22",
      "totalPatientResponsibility": "38.68",
      "deductibleApplied": "0.00",
      "deductibleRemaining": "500.00"
    }
  }
}
```

---

## 5. Application Log Example

**Location**: `docs/examples/log-file/`

### Input Document
```
[2024-03-15 09:15:23] INFO Starting application server on port 8080
[2024-03-15 09:15:24] INFO Database connection established: jdbc:postgresql://localhost:5432/appdb
[2024-03-15 09:15:25] INFO Cache initialized with 1000 entries
[2024-03-15 09:16:12] INFO User login: user123 from IP 192.168.1.100
[2024-03-15 09:16:45] WARN Failed login attempt for user: admin from IP 10.0.0.50
    at com.example.auth.AuthService.authenticate(AuthService.java:45)
    at com.example.web.LoginController.doPost(LoginController.java:78)
[2024-03-15 09:17:22] INFO Order created: ORDER-2024-001 by user123, total: $145.99
[2024-03-15 09:18:03] ERROR Database connection lost
    at java.sql.DriverManager.getConnection(DriverManager.java:632)
    at com.example.db.ConnectionPool.getConnection(ConnectionPool.java:89)
    at com.example.service.OrderService.saveOrder(OrderService.java:156)
[2024-03-15 09:18:04] INFO Attempting database reconnection
[2024-03-15 09:18:06] INFO Database reconnected successfully
[2024-03-15 09:19:15] INFO Payment processed: $145.99 for ORDER-2024-001, method: CREDIT_CARD
[2024-03-15 09:20:30] WARN High memory usage detected: 85% of heap space used
[2024-03-15 09:21:45] ERROR Failed to send email notification
    at com.example.email.EmailService.sendEmail(EmailService.java:123)
    at com.example.service.NotificationService.notify(NotificationService.java:67)
[2024-03-15 09:22:10] INFO User logout: user123 session duration: 6m 58s
```

Demonstrates processing structured log files with different log levels and optional stack traces.

### Key Features
- **Multiple log levels** (INFO, WARN, ERROR)
- **Optional stack traces** using nested repeat patterns
- **Timestamp parsing** with formatters
- **Or patterns** for handling different line types

### Java Fluent API
```java
DrexPattern logPattern = DrexPattern.builder()
    .version("1.0")
    .name("ApplicationLogPattern")
    .comment("Extract application log entries with different message types and stack traces")
    .elements(
        Repeat.builder()
            .bindArray("logEntries")
            .mode(Repeat.Mode.ZERO_OR_MORE)
            .comment("Process all log entries")
            .elements(
                Or.builder()
                    .comment("Handle different types of log lines")
                    .elements(
                        Group.builder()
                            .comment("Standard log entry with optional stack trace")
                            .elements(
                                Line.builder()
                                    .regex("\\\\[(\\\\d{4}-\\\\d{2}-\\\\d{2} \\\\d{2}:\\\\d{2}:\\\\d{2})\\\\] (ERROR|WARN|INFO|DEBUG) (.+)")
                                    .bindProperties(
                                        PropertyBinding.of("timestamp", "parseDate(yyyy-MM-dd HH:mm:ss)"),
                                        PropertyBinding.of("level"),
                                        PropertyBinding.of("message")
                                    )
                                    .comment("Main log entry line")
                                    .build(),
                                Repeat.builder()
                                    .bindArray("stackTrace")
                                    .mode(Repeat.Mode.ZERO_OR_MORE)
                                    .comment("Capture stack trace lines if present")
                                    .elements(
                                        Line.builder()
                                            .regex("\\\\s+at (.+)\\\\((.+?):(\\\\d+)\\\\)")
                                            .bindProperties(
                                                PropertyBinding.of("method"),
                                                PropertyBinding.of("file"),
                                                PropertyBinding.of("lineNumber")
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .build(),
                        Line.builder()
                            .regex("\\\\s+at .+")
                            .comment("Skip orphaned stack trace lines")
                            .build()
                    )
                    .build()
            )
            .build()
    )
    .build();
```

### Expected Output
```json
{
  "logEntries": [
    {
      "timestamp": "2024-03-15 09:15:23",
      "level": "INFO",
      "message": "Starting application server on port 8080",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:15:24",
      "level": "INFO",
      "message": "Database connection established: jdbc:postgresql://localhost:5432/appdb",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:15:25",
      "level": "INFO",
      "message": "Cache initialized with 1000 entries",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:16:12",
      "level": "INFO",
      "message": "User login: user123 from IP 192.168.1.100",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:16:45",
      "level": "WARN",
      "message": "Failed login attempt for user: admin from IP 10.0.0.50",
      "stackTrace": [
        {
          "method": "com.example.auth.AuthService.authenticate",
          "file": "AuthService.java",
          "lineNumber": "45"
        },
        {
          "method": "com.example.web.LoginController.doPost",
          "file": "LoginController.java",
          "lineNumber": "78"
        }
      ]
    },
    {
      "timestamp": "2024-03-15 09:17:22",
      "level": "INFO",
      "message": "Order created: ORDER-2024-001 by user123, total: $145.99",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:18:03",
      "level": "ERROR",
      "message": "Database connection lost",
      "stackTrace": [
        {
          "method": "java.sql.DriverManager.getConnection",
          "file": "DriverManager.java",
          "lineNumber": "632"
        },
        {
          "method": "com.example.db.ConnectionPool.getConnection",
          "file": "ConnectionPool.java",
          "lineNumber": "89"
        },
        {
          "method": "com.example.service.OrderService.saveOrder",
          "file": "OrderService.java",
          "lineNumber": "156"
        }
      ]
    },
    {
      "timestamp": "2024-03-15 09:18:04",
      "level": "INFO",
      "message": "Attempting database reconnection",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:18:06",
      "level": "INFO",
      "message": "Database reconnected successfully",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:19:15",
      "level": "INFO",
      "message": "Payment processed: $145.99 for ORDER-2024-001, method: CREDIT_CARD",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:20:30",
      "level": "WARN",
      "message": "High memory usage detected: 85% of heap space used",
      "stackTrace": []
    },
    {
      "timestamp": "2024-03-15 09:21:45",
      "level": "ERROR",
      "message": "Failed to send email notification",
      "stackTrace": [
        {
          "method": "com.example.email.EmailService.sendEmail",
          "file": "EmailService.java",
          "lineNumber": "123"
        },
        {
          "method": "com.example.service.NotificationService.notify",
          "file": "NotificationService.java",
          "lineNumber": "67"
        }
      ]
    },
    {
      "timestamp": "2024-03-15 09:22:10",
      "level": "INFO",
      "message": "User logout: user123 session duration: 6m 58s",
      "stackTrace": []
    }
  ]
}
```

---

## Testing with Example Files

All examples include test files that can be used for unit testing:

```java
@Test
void testInvoicePatternExtraction() {
    // Load pattern
    String patternJson = Files.readString(
        Paths.get("docs/examples/invoice/invoice-pattern.json"));
    DrexPattern pattern = PatternDeserializer.deserialize(patternJson);
    
    // Load test document
    String document = Files.readString(
        Paths.get("docs/examples/invoice/invoice.txt"));
    
    // Execute pattern
    MatchResult result = DrexEngine.match(pattern, document);
    
    // Load expected output
    String expectedJson = Files.readString(
        Paths.get("docs/examples/invoice/invoice-output.json"));
    
    // Verify results
    assertJsonEquals(expectedJson, result.toJson());
}
```

## Pattern Design Notes

### Greedy Matching Considerations
- **Or patterns**: Place more specific patterns before general ones
- **Repeat patterns**: Will consume as many matches as possible
- **Context clues**: Use surrounding text to disambiguate similar patterns

### Formatter Usage
- **Currency values**: Use `currency()` to normalize monetary amounts
- **Dates**: Use `parseDate()` with appropriate format strings
- **Graceful degradation**: Formatters never halt processing on failure

### Fuzzy Matching
- **OCR noise tolerance**: Use `editDistance` for documents with OCR errors
- **Conservative values**: Start with `editDistance: 1` and increase if needed
- **Testing**: Verify fuzzy patterns with intentionally corrupted test data