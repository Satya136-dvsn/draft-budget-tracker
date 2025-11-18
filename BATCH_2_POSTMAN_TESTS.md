# 🧪 Batch 2: Complete Postman Testing Guide
## Tasks 12-14: Anomaly Detection, Chat Assistant, Data Export

---

## 📋 Quick Reference

**Batch 2 Endpoints:**
1. `GET /api/anomalies` - Detect unusual transactions
2. `POST /api/anomalies/{id}/mark-expected` - Mark anomaly as expected
3. `POST /api/chat` - Chat with AI assistant
4. `GET /api/export/transactions` - Export transactions CSV
5. `GET /api/export/all-data` - Export all data CSV

---

## 🔍 Task 12: Anomaly Detection

### 12.1 Detect Anomalies
**Endpoint:** `GET /api/anomalies`

**Description:** Detects unusual spending patterns using statistical analysis (z-score > 2).

**Request:**
```
GET http://localhost:8080/api/anomalies
Authorization: Bearer <token>
```

**Expected Response:**
```json
[
  {
    "transactionId": 45,
    "description": "Expensive dinner",
    "amount": 500.00,
    "categoryName": "Category 5",
    "date": "2025-11-15",
    "categoryAverage": 85.50,
    "standardDeviation": 45.20,
    "zScore": 9.17,
    "severity": "HIGH",
    "reason": "Amount is 9.2 standard deviations above average"
  },
  {
    "transactionId": 42,
    "description": "Large grocery shop",
    "amount": 250.00,
    "categoryName": "Category 6",
    "date": "2025-11-10",
    "categoryAverage": 120.00,
    "standardDeviation": 30.00,
    "zScore": 4.33,
    "severity": "HIGH",
    "reason": "Amount is 4.3 standard deviations above average"
  }
]
```

**Response Fields:**
- `zScore`: Number of standard deviations from mean
- `severity`: HIGH (z > 3), MEDIUM (z > 2)
- `categoryAverage`: Mean spending for this category
- `standardDeviation`: Spread of spending

**Requirements:**
- Needs at least 10 transactions total
- Needs at least 5 transactions per category
- Analyzes last 3 months of data

**Test Scenarios:**
1. ✅ No data → Returns empty array `[]`
2. ✅ Normal spending → Returns empty array
3. ✅ One unusual transaction → Returns that anomaly
4. ✅ Multiple anomalies → Sorted by severity then z-score

---

### 12.2 Mark Anomaly as Expected
**Endpoint:** `POST /api/anomalies/{transactionId}/mark-expected`

**Description:** Mark a flagged transaction as expected (not anomalous).

**Request:**
```
POST http://localhost:8080/api/anomalies/45/mark-expected
Authorization: Bearer <token>
```

**Expected Response:**
```
200 OK (empty body)
```

---

## 💬 Task 13: Chat Assistant

### 13.1 Chat with AI Assistant
**Endpoint:** `POST /api/chat`

**Description:** Context-aware financial assistant that answers questions about your finances.

**Request:**
```
POST http://localhost:8080/api/chat
Authorization: Bearer <token>
Content-Type: application/json

{
  "message": "How is my spending this month?",
  "conversationId": null
}
```

**Expected Response:**
```json
{
  "response": "This month, you've spent $3,200.50 across 28 transactions. Would you like me to break this down by category?",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "context": "Current month: Income $5000.00, Expenses $3200.50, Balance $1799.50. You have 2 budgets and 5 savings goals."
}
```

**More Example Conversations:**

**Example 2: Savings Question**
```json
Request: {
  "message": "Am I saving enough?",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000"
}

Response: {
  "response": "You have 5 active savings goals. Keep up the good work! Regular contributions, even small ones, add up over time.",
  "conversationId": "550e8400-e29b-41d4-a716-446655440000",
  "context": "Current month: Income $5000.00, Expenses $3200.50, Balance $1799.50. You have 2 budgets and 5 savings goals."
}
```

**Example 3: Budget Question**
```json
Request: {
  "message": "Tell me about my budgets"
}

Response: {
  "response": "You have 2 active budgets. Budgets are a great way to stay on track. Check your dashboard to see how you're doing!",
  "conversationId": "...",
  "context": "..."
}
```

**Example 4: Help**
```json
Request: {
  "message": "What can you help me with?"
}

Response: {
  "response": "I can help you with:\n• Analyzing your spending patterns\n• Tracking your savings goals\n• Managing your budgets\n• Providing financial advice\n• Detecting unusual transactions\n\nTry asking: 'How is my spending?' or 'Am I saving enough?'",
  "conversationId": "...",
  "context": "..."
}
```

**Supported Topics:**
- Spending analysis
- Savings tracking
- Budget management
- Goal progress
- General help

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Has response", () => {
    const data = pm.response.json();
    pm.expect(data.response).to.be.a('string');
    pm.expect(data.response.length).to.be.above(0);
});
pm.test("Has conversation ID", () => {
    const data = pm.response.json();
    pm.expect(data.conversationId).to.be.a('string');
});
```

---

## 📥 Task 14: Data Export

### 14.1 Export Transactions CSV
**Endpoint:** `GET /api/export/transactions`

**Description:** Export transactions to CSV file with optional date range.

**Request 1: All Transactions**
```
GET http://localhost:8080/api/export/transactions
Authorization: Bearer <token>
```

**Request 2: Date Range**
```
GET http://localhost:8080/api/export/transactions?startDate=2025-01-01&endDate=2025-11-30
Authorization: Bearer <token>
```

**Expected Response:**
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="transactions.csv"`
- Body: CSV file download

**CSV Format:**
```csv
ID,Date,Type,Amount,Category ID,Description,Created At
1,2025-11-18,EXPENSE,85.50,5,"Dinner at restaurant",2025-11-18 19:30:00
2,2025-11-17,EXPENSE,50.00,7,"Gas station",2025-11-17 14:20:00
3,2025-11-16,INCOME,5000.00,1,"Monthly salary",2025-11-16 09:00:00
```

---

### 14.2 Export All Data CSV
**Endpoint:** `GET /api/export/all-data`

**Description:** Export complete financial data (transactions, budgets, goals) to CSV.

**Request:**
```
GET http://localhost:8080/api/export/all-data
Authorization: Bearer <token>
```

**Expected Response:**
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="budgetwise-data.csv"`
- Body: CSV file with all data

**CSV Format:**
```csv
=== TRANSACTIONS ===
ID,Date,Type,Amount,Category ID,Description
1,2025-11-18,EXPENSE,85.50,5,"Dinner"
2,2025-11-17,EXPENSE,50.00,7,"Gas"

=== BUDGETS ===
ID,Category ID,Amount,Period,Start Date,End Date,Spent
1,5,500.00,MONTHLY,2025-11-01,2025-11-30,285.50

=== SAVINGS GOALS ===
ID,Name,Target Amount,Current Amount,Deadline,Status
1,"Emergency Fund",10000.00,2500.00,2026-12-31,ACTIVE
```

**Test in Postman:**
1. Send request
2. Check response headers for `Content-Disposition`
3. Save response to file
4. Open CSV in Excel/Notepad

---

## 🔄 Complete Test Workflow

### Phase 1: Anomaly Detection
```bash
# Need test data first - create varied transactions
POST /api/transactions (create 10+ transactions with varying amounts)

# Then detect anomalies
GET /api/anomalies

# Mark one as expected
POST /api/anomalies/45/mark-expected
```

### Phase 2: Chat Assistant
```bash
# Start conversation
POST /api/chat
Body: {"message": "How is my spending?"}

# Continue conversation
POST /api/chat
Body: {
  "message": "Tell me about my savings",
  "conversationId": "<from-previous-response>"
}

# Ask for help
POST /api/chat
Body: {"message": "What can you do?"}
```

### Phase 3: Data Export
```bash
# Export transactions
GET /api/export/transactions

# Export with date range
GET /api/export/transactions?startDate=2025-01-01&endDate=2025-11-30

# Export all data
GET /api/export/all-data
```

---

## 📦 Postman Collection Structure

```
BudgetWise - Batch 2/
├── Task 12 - Anomaly Detection/
│   ├── Detect Anomalies
│   └── Mark as Expected
├── Task 13 - Chat Assistant/
│   ├── Chat - Spending Question
│   ├── Chat - Savings Question
│   ├── Chat - Budget Question
│   └── Chat - Help
└── Task 14 - Data Export/
    ├── Export Transactions (All)
    ├── Export Transactions (Date Range)
    └── Export All Data
```

---

## ✅ Success Criteria

All tests pass when:
- ✅ Anomaly detection returns valid z-scores
- ✅ Chat responses are contextual and helpful
- ✅ CSV exports download successfully
- ✅ CSV files open correctly in Excel
- ✅ All data is properly formatted
- ✅ No server errors

---

## 🎉 Batch 2 Complete!

**Total Endpoints:** 5
- Anomaly Detection: 2 endpoints
- Chat Assistant: 1 endpoint
- Data Export: 2 endpoints

**Next:** Batch 3 - Community Forum & Admin Features! 🚀
