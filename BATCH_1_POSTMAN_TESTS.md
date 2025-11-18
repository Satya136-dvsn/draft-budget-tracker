# 🧪 Batch 1: Complete Postman Testing Guide
## Tasks 8-11: Dashboard & AI Services

---

## 📋 Table of Contents
1. [Setup & Authentication](#setup)
2. [Task 8: Dashboard APIs](#task-8)
3. [Task 9: Predictive Analysis](#task-9)
4. [Task 10: Budget Advisor](#task-10)
5. [Task 11: Auto-Categorization](#task-11)
6. [Complete Test Workflow](#workflow)

---

## 🔐 Setup & Authentication {#setup}

### Step 1: Login to Get JWT Token
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```

**Save the token from response!**

### Step 2: Set Authorization Header
For all subsequent requests:
```
Authorization: Bearer <your-jwt-token>
```

---

## 📊 Task 8: Dashboard Aggregation APIs {#task-8}

### 8.1 Get Dashboard Summary
**Endpoint:** `GET /api/dashboard/summary`

**Request:**
```
GET http://localhost:8080/api/dashboard/summary
Authorization: Bearer <token>
```

**Expected Response:**
```json
{
  "totalIncome": 5000.00,
  "totalExpenses": 3200.50,
  "balance": 1799.50,
  "savingsRate": 35.99,
  "transactionCount": 28,
  "budgetCount": 5,
  "goalCount": 3
}
```

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Has all fields", () => {
    const data = pm.response.json();
    pm.expect(data).to.have.all.keys(
        'totalIncome', 'totalExpenses', 'balance', 
        'savingsRate', 'transactionCount', 'budgetCount', 'goalCount'
    );
});
pm.test("Balance is correct", () => {
    const data = pm.response.json();
    pm.expect(data.balance).to.eql(data.totalIncome - data.totalExpenses);
});
```

---

### 8.2 Get Monthly Trends
**Endpoint:** `GET /api/dashboard/monthly-trends`

**Request:**
```
GET http://localhost:8080/api/dashboard/monthly-trends?months=6
Authorization: Bearer <token>
```

**Expected Response:**
```json
[
  {
    "month": "Jun 2025",
    "income": 5000.00,
    "expenses": 3000.00,
    "netSavings": 2000.00
  },
  {
    "month": "Jul 2025",
    "income": 5200.00,
    "expenses": 3100.00,
    "netSavings": 2100.00
  }
]
```

**Test Assertions:**
```javascript
pm.test("Returns array", () => {
    pm.expect(pm.response.json()).to.be.an('array');
});
pm.test("Net savings calculated correctly", () => {
    const data = pm.response.json();
    data.forEach(month => {
        pm.expect(month.netSavings).to.eql(month.income - month.expenses);
    });
});
```

---

### 8.3 Get Category Breakdown
**Endpoint:** `GET /api/dashboard/category-breakdown`

**Request:**
```
GET http://localhost:8080/api/dashboard/category-breakdown
Authorization: Bearer <token>
```

**Expected Response:**
```json
[
  {
    "categoryId": 8,
    "categoryName": "Rent",
    "amount": 1200.00,
    "percentage": 37.50,
    "transactionCount": 1
  },
  {
    "categoryId": 5,
    "categoryName": "Food & Dining",
    "amount": 850.50,
    "percentage": 26.58,
    "transactionCount": 15
  }
]
```

**Test Assertions:**
```javascript
pm.test("Sorted by amount descending", () => {
    const data = pm.response.json();
    for (let i = 0; i < data.length - 1; i++) {
        pm.expect(data[i].amount).to.be.at.least(data[i + 1].amount);
    }
});
```

---

### 8.4 Get Recent Transactions
**Endpoint:** `GET /api/dashboard/recent-transactions`

**Request:**
```
GET http://localhost:8080/api/dashboard/recent-transactions?limit=10
Authorization: Bearer <token>
```

**Expected Response:**
```json
[
  {
    "id": 45,
    "amount": 85.50,
    "type": "EXPENSE",
    "categoryId": 5,
    "categoryName": "Food & Dining",
    "description": "Dinner at restaurant",
    "date": "2025-11-18",
    "createdAt": "2025-11-18T19:30:00"
  }
]
```

---

## 🤖 Task 9: AI Predictive Analysis {#task-9}

### 9.1 Get Expense Predictions
**Endpoint:** `GET /api/ai/predictions`

**Description:** Predicts next month's expenses by category using linear regression on last 6 months of data.

**Request:**
```
GET http://localhost:8080/api/ai/predictions
Authorization: Bearer <token>
```

**Expected Response:**
```json
[
  {
    "categoryId": 5,
    "categoryName": "Food & Dining",
    "predictedAmount": 875.25,
    "historicalAverage": 850.00,
    "confidenceScore": 85.5,
    "trend": "INCREASING"
  },
  {
    "categoryId": 7,
    "categoryName": "Transportation",
    "predictedAmount": 420.00,
    "historicalAverage": 450.00,
    "confidenceScore": 78.2,
    "trend": "DECREASING"
  },
  {
    "categoryId": 9,
    "categoryName": "Utilities",
    "predictedAmount": 105.00,
    "historicalAverage": 105.00,
    "confidenceScore": 92.1,
    "trend": "STABLE"
  }
]
```

**Response Fields:**
- `predictedAmount`: ML prediction for next month
- `historicalAverage`: Average of last 6 months
- `confidenceScore`: 0-100 based on R-squared value
- `trend`: INCREASING, DECREASING, or STABLE

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Returns predictions array", () => {
    pm.expect(pm.response.json()).to.be.an('array');
});
pm.test("Each prediction has required fields", () => {
    const data = pm.response.json();
    if (data.length > 0) {
        pm.expect(data[0]).to.have.all.keys(
            'categoryId', 'categoryName', 'predictedAmount',
            'historicalAverage', 'confidenceScore', 'trend'
        );
    }
});
pm.test("Confidence scores are valid", () => {
    const data = pm.response.json();
    data.forEach(pred => {
        pm.expect(pred.confidenceScore).to.be.at.least(0);
        pm.expect(pred.confidenceScore).to.be.at.most(100);
    });
});
pm.test("Trends are valid", () => {
    const data = pm.response.json();
    const validTrends = ['INCREASING', 'DECREASING', 'STABLE'];
    data.forEach(pred => {
        pm.expect(validTrends).to.include(pred.trend);
    });
});
```

**Notes:**
- Requires at least 3 months of transaction data per category
- Uses Apache Commons Math SimpleRegression
- Cached for 24 hours

---

## 💡 Task 10: Smart Budget Advisor {#task-10}

### 10.1 Get Personalized Budget Advice
**Endpoint:** `GET /api/ai/advice`

**Description:** Analyzes spending patterns and provides personalized recommendations.

**Request:**
```
GET http://localhost:8080/api/ai/advice
Authorization: Bearer <token>
```

**Expected Response:**
```json
[
  {
    "category": "Savings",
    "recommendation": "Your savings rate is below 10%. Try to save at least 10-20% of your income.",
    "currentSpending": 3200.50,
    "recommendedSpending": 4000.00,
    "percentageOfIncome": 8.5,
    "priority": "HIGH",
    "actionItem": "Review your expenses and identify areas to cut back"
  },
  {
    "category": "Food & Dining",
    "recommendation": "Food & Dining spending is 25.5% of your income. Consider reducing to 20% or less.",
    "currentSpending": 1275.00,
    "recommendedSpending": 1000.00,
    "percentageOfIncome": 25.5,
    "priority": "MEDIUM",
    "actionItem": "Reduce Food & Dining spending by 275.00"
  },
  {
    "category": "Shopping",
    "recommendation": "Shopping spending is 22.0% of your income. Consider reducing to 20% or less.",
    "currentSpending": 1100.00,
    "recommendedSpending": 1000.00,
    "percentageOfIncome": 22.0,
    "priority": "MEDIUM",
    "actionItem": "Reduce Shopping spending by 100.00"
  }
]
```

**Response Fields:**
- `category`: Category or aspect being analyzed
- `recommendation`: Personalized advice
- `currentSpending`: Current spending amount
- `recommendedSpending`: Suggested spending limit
- `percentageOfIncome`: Current spending as % of income
- `priority`: HIGH, MEDIUM, or LOW
- `actionItem`: Specific action to take

**Analysis Rules:**
- ✅ Flags savings rate < 10%
- ✅ Flags categories > 20% of income (except Rent)
- ✅ Returns top 5 recommendations
- ✅ Sorted by priority then amount

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Returns advice array", () => {
    pm.expect(pm.response.json()).to.be.an('array');
});
pm.test("Max 5 recommendations", () => {
    pm.expect(pm.response.json().length).to.be.at.most(5);
});
pm.test("Valid priorities", () => {
    const data = pm.response.json();
    const validPriorities = ['HIGH', 'MEDIUM', 'LOW'];
    data.forEach(advice => {
        pm.expect(validPriorities).to.include(advice.priority);
    });
});
pm.test("Sorted by priority", () => {
    const data = pm.response.json();
    const priorityOrder = { 'HIGH': 1, 'MEDIUM': 2, 'LOW': 3 };
    for (let i = 0; i < data.length - 1; i++) {
        pm.expect(priorityOrder[data[i].priority])
            .to.be.at.most(priorityOrder[data[i + 1].priority]);
    }
});
```

**Test Scenarios:**
1. ✅ User with low savings rate → HIGH priority advice
2. ✅ User with high category spending → MEDIUM priority advice
3. ✅ User with balanced budget → Positive feedback
4. ✅ New user without income → Profile setup advice

---

## 🏷️ Task 11: Auto-Categorization {#task-11}

### 11.1 Suggest Category for Transaction
**Endpoint:** `POST /api/ai/categorize`

**Description:** Suggests category based on transaction description using keyword matching.

**Request:**
```
POST http://localhost:8080/api/ai/categorize
Authorization: Bearer <token>
Content-Type: application/json

{
  "description": "Starbucks coffee",
  "merchantName": "Starbucks"
}
```

**Expected Response:**
```json
{
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "confidence": 85.0,
  "reason": "Matched keywords in description: 'Starbucks coffee'"
}
```

**More Examples:**

**Example 2: Uber ride**
```json
Request: { "description": "Uber ride to airport" }
Response: {
  "categoryId": 7,
  "categoryName": "Transportation",
  "confidence": 90.0,
  "reason": "Matched keywords in description: 'Uber ride to airport'"
}
```

**Example 3: Amazon purchase**
```json
Request: { "description": "Amazon.com purchase" }
Response: {
  "categoryId": 12,
  "categoryName": "Shopping",
  "confidence": 80.0,
  "reason": "Matched keywords in description: 'Amazon.com purchase'"
}
```

**Example 4: Rent payment**
```json
Request: { "description": "Monthly rent payment" }
Response: {
  "categoryId": 8,
  "categoryName": "Rent",
  "confidence": 95.0,
  "reason": "Matched keywords in description: 'Monthly rent payment'"
}
```

**Example 5: Unknown**
```json
Request: { "description": "XYZ Corp payment" }
Response: {
  "categoryId": 16,
  "categoryName": "Other Expense",
  "confidence": 30.0,
  "reason": "No specific keywords matched, using default category"
}
```

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Has required fields", () => {
    const data = pm.response.json();
    pm.expect(data).to.have.all.keys(
        'categoryId', 'categoryName', 'confidence', 'reason'
    );
});
pm.test("Confidence is valid", () => {
    const data = pm.response.json();
    pm.expect(data.confidence).to.be.at.least(0);
    pm.expect(data.confidence).to.be.at.most(100);
});
```

---

### 11.2 Learn from Correction
**Endpoint:** `POST /api/ai/categorize/learn`

**Description:** Stores user corrections to improve future suggestions.

**Request:**
```
POST http://localhost:8080/api/ai/categorize/learn?description=Starbucks&categoryId=5
Authorization: Bearer <token>
```

**Expected Response:**
```
200 OK (empty body)
```

**Test Assertion:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
```

---

## 🔄 Complete Test Workflow {#workflow}

### Phase 1: Setup (Do Once)
1. ✅ Start Spring Boot server
2. ✅ Login and save JWT token
3. ✅ Create test data (transactions, budgets, goals)

### Phase 2: Dashboard Tests (Task 8)
```bash
1. GET /api/dashboard/summary
2. GET /api/dashboard/monthly-trends?months=6
3. GET /api/dashboard/category-breakdown
4. GET /api/dashboard/recent-transactions?limit=10
```

### Phase 3: AI Predictions (Task 9)
```bash
1. GET /api/ai/predictions
   - Verify predictions for each category
   - Check confidence scores
   - Validate trends
```

### Phase 4: Budget Advice (Task 10)
```bash
1. GET /api/ai/advice
   - Check for savings rate advice
   - Verify category recommendations
   - Validate priority sorting
```

### Phase 5: Auto-Categorization (Task 11)
```bash
1. POST /api/ai/categorize
   - Test with "Starbucks coffee"
   - Test with "Uber ride"
   - Test with "Amazon purchase"
   - Test with unknown description
   
2. POST /api/ai/categorize/learn
   - Submit correction
```

---

## 📦 Postman Collection Structure

```
BudgetWise - Batch 1/
├── Auth/
│   └── Login
├── Task 8 - Dashboard/
│   ├── Get Summary
│   ├── Get Monthly Trends (6 months)
│   ├── Get Monthly Trends (3 months)
│   ├── Get Category Breakdown
│   └── Get Recent Transactions
├── Task 9 - Predictions/
│   └── Get Expense Predictions
├── Task 10 - Budget Advisor/
│   └── Get Personalized Advice
└── Task 11 - Categorization/
    ├── Suggest Category - Starbucks
    ├── Suggest Category - Uber
    ├── Suggest Category - Amazon
    ├── Suggest Category - Unknown
    └── Learn from Correction
```

---

## ✅ Success Criteria

All tests pass when:
- ✅ All endpoints return 200 OK
- ✅ Response structures match expected format
- ✅ Calculations are accurate
- ✅ Predictions have valid confidence scores
- ✅ Advice is prioritized correctly
- ✅ Categorization matches keywords
- ✅ No server errors in console

---

## 🎉 Batch 1 Complete!

**Total Endpoints Tested:** 11
- Dashboard: 4 endpoints
- Predictions: 1 endpoint
- Budget Advisor: 1 endpoint
- Categorization: 2 endpoints

**Next:** Task 12 - Anomaly Detection 🔍
