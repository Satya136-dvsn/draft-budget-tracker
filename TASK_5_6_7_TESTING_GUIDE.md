# Tasks 5, 6, 7 Testing Guide - Transactions, Budgets, and Goals

## Overview
This guide covers testing for:
- **Task 5**: Transaction Management (CRUD + Filtering)
- **Task 6**: Budget Management (CRUD + Progress Tracking)
- **Task 7**: Savings Goals Management (CRUD + Contributions)

## Prerequisites
1. Backend server running on `http://localhost:8080`
2. MySQL database running with budgetwise schema
3. Valid JWT token from login (use from previous tasks)
4. At least one category created (or use system categories)

---

## TASK 5: Transaction Management

### 1. Create Transaction (POST /api/transactions)

**Endpoint:** `POST http://localhost:8080/api/transactions`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body (Expense):**
```json
{
  "type": "EXPENSE",
  "amount": 150.50,
  "categoryId": 5,
  "description": "Lunch at restaurant",
  "transactionDate": "2024-11-15"
}
```

**Request Body (Income):**
```json
{
  "type": "INCOME",
  "amount": 5000.00,
  "categoryId": 1,
  "description": "Monthly salary",
  "transactionDate": "2024-11-01"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "type": "EXPENSE",
  "amount": 150.50,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "description": "Lunch at restaurant",
  "transactionDate": "2024-11-15",
  "isAnomaly": false,
  "createdAt": "2024-11-17T10:30:00",
  "updatedAt": "2024-11-17T10:30:00"
}
```

### 2. Get All Transactions (GET /api/transactions)

**Endpoint:** `GET http://localhost:8080/api/transactions?page=0&size=10&sortBy=transactionDate&sortDir=DESC`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "type": "EXPENSE",
      "amount": 150.50,
      "categoryId": 5,
      "categoryName": "Food & Dining",
      "description": "Lunch at restaurant",
      "transactionDate": "2024-11-15",
      "isAnomaly": false,
      "createdAt": "2024-11-17T10:30:00",
      "updatedAt": "2024-11-17T10:30:00"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 1,
  "totalPages": 1
}
```

### 3. Filter Transactions (GET /api/transactions with filters)

**Endpoint:** `GET http://localhost:8080/api/transactions?type=EXPENSE&categoryId=5&startDate=2024-11-01&endDate=2024-11-30&minAmount=100&maxAmount=200`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Query Parameters:**
- `type`: INCOME or EXPENSE
- `categoryId`: Category ID
- `startDate`: Start date (YYYY-MM-DD)
- `endDate`: End date (YYYY-MM-DD)
- `minAmount`: Minimum amount
- `maxAmount`: Maximum amount
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `sortBy`: Sort field (default: transactionDate)
- `sortDir`: ASC or DESC (default: DESC)

### 4. Get Transaction by ID (GET /api/transactions/{id})

**Endpoint:** `GET http://localhost:8080/api/transactions/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "type": "EXPENSE",
  "amount": 150.50,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "description": "Lunch at restaurant",
  "transactionDate": "2024-11-15",
  "isAnomaly": false,
  "createdAt": "2024-11-17T10:30:00",
  "updatedAt": "2024-11-17T10:30:00"
}
```

### 5. Update Transaction (PUT /api/transactions/{id})

**Endpoint:** `PUT http://localhost:8080/api/transactions/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "type": "EXPENSE",
  "amount": 175.00,
  "categoryId": 5,
  "description": "Lunch at restaurant - Updated amount",
  "transactionDate": "2024-11-15"
}
```

**Note:** Can only edit transactions within 30 days of creation.

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "type": "EXPENSE",
  "amount": 175.00,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "description": "Lunch at restaurant - Updated amount",
  "transactionDate": "2024-11-15",
  "isAnomaly": false,
  "createdAt": "2024-11-17T10:30:00",
  "updatedAt": "2024-11-17T10:35:00"
}
```

### 6. Delete Transaction (DELETE /api/transactions/{id})

**Endpoint:** `DELETE http://localhost:8080/api/transactions/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (204 No Content)**

---

## TASK 6: Budget Management

### 1. Create Budget (POST /api/budgets)

**Endpoint:** `POST http://localhost:8080/api/budgets`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body (Monthly Budget):**
```json
{
  "amount": 500.00,
  "categoryId": 5,
  "period": "MONTHLY",
  "startDate": "2024-11-01",
  "endDate": "2024-11-30",
  "alertThreshold": 80.00
}
```

**Request Body (Weekly Budget):**
```json
{
  "amount": 200.00,
  "categoryId": 6,
  "period": "WEEKLY",
  "startDate": "2024-11-11",
  "endDate": "2024-11-17",
  "alertThreshold": 75.00
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "amount": 500.00,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "period": "MONTHLY",
  "startDate": "2024-11-01",
  "endDate": "2024-11-30",
  "alertThreshold": 80.00,
  "spent": 150.50,
  "remaining": 349.50,
  "progressPercentage": 30.10,
  "createdAt": "2024-11-17T10:40:00",
  "updatedAt": "2024-11-17T10:40:00"
}
```

### 2. Get All Budgets (GET /api/budgets)

**Endpoint:** `GET http://localhost:8080/api/budgets`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Query Parameters:**
- `activeOnly`: true/false (default: false) - Only return active budgets

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "amount": 500.00,
    "categoryId": 5,
    "categoryName": "Food & Dining",
    "period": "MONTHLY",
    "startDate": "2024-11-01",
    "endDate": "2024-11-30",
    "alertThreshold": 80.00,
    "spent": 150.50,
    "remaining": 349.50,
    "progressPercentage": 30.10,
    "createdAt": "2024-11-17T10:40:00",
    "updatedAt": "2024-11-17T10:40:00"
  }
]
```

### 3. Get Active Budgets Only (GET /api/budgets?activeOnly=true)

**Endpoint:** `GET http://localhost:8080/api/budgets?activeOnly=true`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Returns only budgets where current date is between startDate and endDate**

### 4. Get Budget by ID (GET /api/budgets/{id})

**Endpoint:** `GET http://localhost:8080/api/budgets/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "amount": 500.00,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "period": "MONTHLY",
  "startDate": "2024-11-01",
  "endDate": "2024-11-30",
  "alertThreshold": 80.00,
  "spent": 150.50,
  "remaining": 349.50,
  "progressPercentage": 30.10,
  "createdAt": "2024-11-17T10:40:00",
  "updatedAt": "2024-11-17T10:40:00"
}
```

### 5. Update Budget (PUT /api/budgets/{id})

**Endpoint:** `PUT http://localhost:8080/api/budgets/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 600.00,
  "categoryId": 5,
  "period": "MONTHLY",
  "startDate": "2024-11-01",
  "endDate": "2024-11-30",
  "alertThreshold": 85.00
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "amount": 600.00,
  "categoryId": 5,
  "categoryName": "Food & Dining",
  "period": "MONTHLY",
  "startDate": "2024-11-01",
  "endDate": "2024-11-30",
  "alertThreshold": 85.00,
  "spent": 150.50,
  "remaining": 449.50,
  "progressPercentage": 25.08,
  "createdAt": "2024-11-17T10:40:00",
  "updatedAt": "2024-11-17T10:45:00"
}
```

### 6. Get Budget Alerts (GET /api/budgets/alerts)

**Endpoint:** `GET http://localhost:8080/api/budgets/alerts`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Returns budgets where progressPercentage >= alertThreshold**

**Expected Response (200 OK):**
```json
[
  {
    "id": 2,
    "amount": 300.00,
    "categoryId": 7,
    "categoryName": "Transportation",
    "period": "MONTHLY",
    "startDate": "2024-11-01",
    "endDate": "2024-11-30",
    "alertThreshold": 80.00,
    "spent": 250.00,
    "remaining": 50.00,
    "progressPercentage": 83.33,
    "createdAt": "2024-11-17T10:50:00",
    "updatedAt": "2024-11-17T10:50:00"
  }
]
```

### 7. Delete Budget (DELETE /api/budgets/{id})

**Endpoint:** `DELETE http://localhost:8080/api/budgets/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (204 No Content)**

---

## TASK 7: Savings Goals Management

### 1. Create Savings Goal (POST /api/goals)

**Endpoint:** `POST http://localhost:8080/api/goals`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Emergency Fund",
  "targetAmount": 10000.00,
  "currentAmount": 2000.00,
  "deadline": "2025-12-31"
}
```

**Request Body (Without initial amount):**
```json
{
  "name": "Vacation to Europe",
  "targetAmount": 5000.00,
  "deadline": "2025-06-30"
}
```

**Expected Response (201 Created):**
```json
{
  "id": 1,
  "name": "Emergency Fund",
  "targetAmount": 10000.00,
  "currentAmount": 2000.00,
  "deadline": "2025-12-31",
  "status": "ACTIVE",
  "progressPercentage": 20.00,
  "requiredMonthlySavings": 615.38,
  "createdAt": "2024-11-17T11:00:00",
  "updatedAt": "2024-11-17T11:00:00"
}
```

### 2. Get All Goals (GET /api/goals)

**Endpoint:** `GET http://localhost:8080/api/goals`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Query Parameters:**
- `activeOnly`: true/false (default: false) - Only return active goals

**Expected Response (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Emergency Fund",
    "targetAmount": 10000.00,
    "currentAmount": 2000.00,
    "deadline": "2025-12-31",
    "status": "ACTIVE",
    "progressPercentage": 20.00,
    "requiredMonthlySavings": 615.38,
    "createdAt": "2024-11-17T11:00:00",
    "updatedAt": "2024-11-17T11:00:00"
  },
  {
    "id": 2,
    "name": "Vacation to Europe",
    "targetAmount": 5000.00,
    "currentAmount": 0.00,
    "deadline": "2025-06-30",
    "status": "ACTIVE",
    "progressPercentage": 0.00,
    "requiredMonthlySavings": 714.29,
    "createdAt": "2024-11-17T11:05:00",
    "updatedAt": "2024-11-17T11:05:00"
  }
]
```

### 3. Get Active Goals Only (GET /api/goals?activeOnly=true)

**Endpoint:** `GET http://localhost:8080/api/goals?activeOnly=true`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Returns only goals with status = ACTIVE**

### 4. Get Goal by ID (GET /api/goals/{id})

**Endpoint:** `GET http://localhost:8080/api/goals/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Emergency Fund",
  "targetAmount": 10000.00,
  "currentAmount": 2000.00,
  "deadline": "2025-12-31",
  "status": "ACTIVE",
  "progressPercentage": 20.00,
  "requiredMonthlySavings": 615.38,
  "createdAt": "2024-11-17T11:00:00",
  "updatedAt": "2024-11-17T11:00:00"
}
```

### 5. Add Contribution to Goal (POST /api/goals/{id}/contribute)

**Endpoint:** `POST http://localhost:8080/api/goals/1/contribute`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "amount": 500.00
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Emergency Fund",
  "targetAmount": 10000.00,
  "currentAmount": 2500.00,
  "deadline": "2025-12-31",
  "status": "ACTIVE",
  "progressPercentage": 25.00,
  "requiredMonthlySavings": 576.92,
  "createdAt": "2024-11-17T11:00:00",
  "updatedAt": "2024-11-17T11:10:00"
}
```

**Note:** When currentAmount >= targetAmount, status automatically changes to COMPLETED

### 6. Update Goal (PUT /api/goals/{id})

**Endpoint:** `PUT http://localhost:8080/api/goals/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Emergency Fund - Updated",
  "targetAmount": 12000.00,
  "currentAmount": 2500.00,
  "deadline": "2025-12-31"
}
```

**Expected Response (200 OK):**
```json
{
  "id": 1,
  "name": "Emergency Fund - Updated",
  "targetAmount": 12000.00,
  "currentAmount": 2500.00,
  "deadline": "2025-12-31",
  "status": "ACTIVE",
  "progressPercentage": 20.83,
  "requiredMonthlySavings": 730.77,
  "createdAt": "2024-11-17T11:00:00",
  "updatedAt": "2024-11-17T11:15:00"
}
```

### 7. Delete Goal (DELETE /api/goals/{id})

**Endpoint:** `DELETE http://localhost:8080/api/goals/1`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
```

**Expected Response (204 No Content)**

---

## Integration Testing Scenarios

### Scenario 1: Budget Progress Updates Automatically

1. Create a budget for "Food & Dining" category (Nov 1-30, $500)
2. Verify budget shows spent = $0, progress = 0%
3. Create an expense transaction for "Food & Dining" ($150)
4. Get the budget again - verify spent = $150, progress = 30%
5. Create another expense transaction for "Food & Dining" ($200)
6. Get the budget again - verify spent = $350, progress = 70%
7. Update the first transaction amount to $250
8. Get the budget again - verify spent = $450, progress = 90%
9. Delete the second transaction
10. Get the budget again - verify spent = $250, progress = 50%

### Scenario 2: Budget Alert Threshold

1. Create a budget with alertThreshold = 80%
2. Add transactions until spent reaches 85%
3. Call GET /api/budgets/alerts
4. Verify the budget appears in the alerts list

### Scenario 3: Goal Completion

1. Create a goal with targetAmount = $1000, currentAmount = $800
2. Verify status = ACTIVE, progress = 80%
3. Add contribution of $200
4. Verify status = COMPLETED, progress = 100%
5. Try to add another contribution - should fail with error

### Scenario 4: Transaction Filtering

1. Create multiple transactions with different:
   - Types (INCOME/EXPENSE)
   - Categories
   - Dates
   - Amounts
2. Test each filter parameter individually
3. Test combinations of filters
4. Verify pagination works correctly

### Scenario 5: Overlapping Budget Prevention

1. Create a budget for category X (Nov 1-30)
2. Try to create another budget for category X (Nov 15-Dec 15)
3. Should fail with error about overlapping budgets

---

## Error Cases to Test

### Transaction Errors
- ❌ Amount = 0 or negative → "Amount must be greater than 0"
- ❌ Invalid category ID → "Category not found"
- ❌ Future transaction date → "Transaction date cannot be in the future"
- ❌ Edit transaction older than 30 days → "Cannot edit transactions older than 30 days"
- ❌ Access another user's transaction → "Transaction not found"

### Budget Errors
- ❌ End date before start date → "End date must be after start date"
- ❌ Overlapping budgets → "A budget already exists for this category and period"
- ❌ Invalid category ID → "Category not found"
- ❌ Alert threshold > 100 → Validation error
- ❌ Access another user's budget → "Budget not found"

### Goal Errors
- ❌ Deadline in the past → "Deadline must be in the future"
- ❌ Target amount = 0 or negative → "Target amount must be greater than 0"
- ❌ Contribute to completed goal → "Cannot contribute to inactive goal"
- ❌ Contribution amount = 0 or negative → "Contribution amount must be greater than 0"
- ❌ Access another user's goal → "Savings goal not found"

---

## Success Criteria

✅ All CRUD operations work for Transactions, Budgets, and Goals
✅ Transaction filtering works with all parameters
✅ Budget progress updates automatically when transactions change
✅ Budget alerts correctly identify budgets exceeding threshold
✅ Goal progress and required monthly savings calculate correctly
✅ Goal status changes to COMPLETED when target is reached
✅ Contributions can be added to active goals
✅ Overlapping budgets are prevented
✅ All validation rules are enforced
✅ Proper error messages for invalid requests
✅ Authorization works (users can only access their own data)
✅ Pagination works for transaction listing

---

## Next Steps

After successful testing:
1. ✅ Mark Tasks 5, 6, 7 as complete
2. 📝 Update PROJECT_STATUS.md
3. 📝 Create TASK_5_6_7_SUMMARY.md
4. 🚀 Move to Phase 2: Advanced Backend Services (Tasks 8-17)
