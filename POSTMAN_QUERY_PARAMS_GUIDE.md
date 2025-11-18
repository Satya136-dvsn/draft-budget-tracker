# How to Use Query Parameters in Postman

## Quick Answer: YES, but Postman makes it easy!

You have **2 options** in Postman:

---

## Option 1: Use Postman's Params Tab (Recommended) ✅

This is the **easiest and cleanest** way!

### Steps:

1. **Set the base URL** (without parameters):
   ```
   GET http://localhost:8080/api/transactions
   ```

2. **Click the "Params" tab** (next to Authorization)

3. **Add your parameters** in the Key-Value table:

   | Key | Value | Description |
   |-----|-------|-------------|
   | `type` | `EXPENSE` | ✅ Check to enable |
   | `categoryId` | `5` | ✅ Check to enable |
   | `startDate` | `2024-11-01` | ✅ Check to enable |
   | `endDate` | `2024-11-30` | ✅ Check to enable |
   | `minAmount` | `100` | ✅ Check to enable |
   | `maxAmount` | `200` | ✅ Check to enable |
   | `page` | `0` | ✅ Check to enable |
   | `size` | `10` | ✅ Check to enable |
   | `sortBy` | `transactionDate` | ✅ Check to enable |
   | `sortDir` | `DESC` | ✅ Check to enable |

4. **Postman automatically builds the URL** for you:
   ```
   http://localhost:8080/api/transactions?type=EXPENSE&categoryId=5&startDate=2024-11-01&endDate=2024-11-30&minAmount=100&maxAmount=200&page=0&size=10&sortBy=transactionDate&sortDir=DESC
   ```

5. **Click Send**

### Benefits:
- ✅ Easy to enable/disable parameters (just check/uncheck)
- ✅ No typing errors
- ✅ Clean and organized
- ✅ Can save as collection with different parameter sets

---

## Option 2: Type the Full URL (Manual)

You can also type the complete URL with parameters directly:

```
GET http://localhost:8080/api/transactions?type=EXPENSE&categoryId=5&startDate=2024-11-01&endDate=2024-11-30&minAmount=100&maxAmount=200
```

But this is **harder to manage** and **error-prone**.

---

## Visual Guide: Postman Interface

```
┌─────────────────────────────────────────────────────────────┐
│ GET  http://localhost:8080/api/transactions                 │
├─────────────────────────────────────────────────────────────┤
│ Params | Authorization | Headers | Body | Pre-request | ... │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Query Params:                                                │
│  ┌──────────────┬─────────────────┬────────────────────┐    │
│  │ ☑ Key        │ Value           │ Description        │    │
│  ├──────────────┼─────────────────┼────────────────────┤    │
│  │ ☑ type       │ EXPENSE         │                    │    │
│  │ ☑ categoryId │ 5               │                    │    │
│  │ ☑ startDate  │ 2024-11-01      │                    │    │
│  │ ☑ endDate    │ 2024-11-30      │                    │    │
│  │ ☑ minAmount  │ 100             │                    │    │
│  │ ☑ maxAmount  │ 200             │                    │    │
│  │ ☑ page       │ 0               │                    │    │
│  │ ☑ size       │ 10              │                    │    │
│  │ ☑ sortBy     │ transactionDate │                    │    │
│  │ ☑ sortDir    │ DESC            │                    │    │
│  └──────────────┴─────────────────┴────────────────────┘    │
│                                                               │
│  [Send]                                                       │
└─────────────────────────────────────────────────────────────┘
```

---

## Common Scenarios

### Scenario 1: Get All Transactions (No Filters)

**URL**: `GET http://localhost:8080/api/transactions`

**Params Tab**: Leave empty or uncheck all

**Result**: Returns all transactions with default pagination (page 0, size 10)

---

### Scenario 2: Filter by Type Only

**URL**: `GET http://localhost:8080/api/transactions`

**Params Tab**:
| Key | Value | ✓ |
|-----|-------|---|
| `type` | `EXPENSE` | ☑ |

**Result**: Returns only EXPENSE transactions

---

### Scenario 3: Filter by Date Range

**URL**: `GET http://localhost:8080/api/transactions`

**Params Tab**:
| Key | Value | ✓ |
|-----|-------|---|
| `startDate` | `2024-11-01` | ☑ |
| `endDate` | `2024-11-30` | ☑ |

**Result**: Returns transactions from November 2024

---

### Scenario 4: Filter by Category and Amount

**URL**: `GET http://localhost:8080/api/transactions`

**Params Tab**:
| Key | Value | ✓ |
|-----|-------|---|
| `categoryId` | `5` | ☑ |
| `minAmount` | `100` | ☑ |
| `maxAmount` | `500` | ☑ |

**Result**: Returns transactions in category 5 between $100-$500

---

### Scenario 5: Pagination (Get Page 2)

**URL**: `GET http://localhost:8080/api/transactions`

**Params Tab**:
| Key | Value | ✓ |
|-----|-------|---|
| `page` | `1` | ☑ |
| `size` | `20` | ☑ |

**Result**: Returns page 2 with 20 items per page

---

### Scenario 6: Sort by Amount (Highest First)

**URL**: `GET http://localhost:8080/api/transactions`

**Params Tab**:
| Key | Value | ✓ |
|-----|-------|---|
| `sortBy` | `amount` | ☑ |
| `sortDir` | `DESC` | ☑ |

**Result**: Returns transactions sorted by amount (highest first)

---

## Pro Tips 💡

### 1. You Don't Need All Parameters!
Only add the parameters you want to use. All are **optional**:
- No parameters = Get all transactions
- Only `type` = Filter by type
- Only `page` and `size` = Just pagination

### 2. Save Different Filter Combinations
In Postman, you can save multiple requests:
- "Get All Transactions"
- "Get Expenses Only"
- "Get November Transactions"
- "Get High-Value Transactions"

### 3. Use Postman Variables
You can use variables for common values:
```
{{baseUrl}}/api/transactions?categoryId={{foodCategoryId}}
```

### 4. Test Without Filters First
Start simple:
1. First: `GET /api/transactions` (no filters)
2. Then: Add one filter at a time
3. Finally: Combine multiple filters

---

## Complete Example

### Step-by-Step in Postman:

1. **Create New Request**
   - Click "New" → "Request"
   - Name it "Filter Transactions"

2. **Set Method and URL**
   - Method: `GET`
   - URL: `http://localhost:8080/api/transactions`

3. **Add Authorization**
   - Click "Authorization" tab
   - Type: `Bearer Token`
   - Token: `YOUR_JWT_TOKEN`

4. **Add Query Parameters**
   - Click "Params" tab
   - Add parameters as shown above
   - Check/uncheck to enable/disable

5. **Send Request**
   - Click "Send"
   - View results in the response panel

---

## Response Example

When you filter transactions, you'll get:

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
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false
    }
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "first": true,
  "numberOfElements": 1
}
```

---

## Quick Reference

### All Available Query Parameters

| Parameter | Type | Example | Required? |
|-----------|------|---------|-----------|
| `type` | String | `EXPENSE` or `INCOME` | No |
| `categoryId` | Number | `5` | No |
| `startDate` | Date | `2024-11-01` | No |
| `endDate` | Date | `2024-11-30` | No |
| `minAmount` | Number | `100` | No |
| `maxAmount` | Number | `500` | No |
| `page` | Number | `0` (first page) | No |
| `size` | Number | `10` (items per page) | No |
| `sortBy` | String | `transactionDate`, `amount` | No |
| `sortDir` | String | `ASC` or `DESC` | No |

**All parameters are optional!** Use only what you need.

---

## Summary

**Answer**: Yes, you add query parameters in Postman, but it's easy!

**Best Method**: Use the **Params tab** in Postman
- ✅ No need to type the full URL
- ✅ Easy to enable/disable filters
- ✅ No syntax errors
- ✅ Clean and organized

**Remember**: All parameters are **optional**. Start with no filters, then add what you need!

---

Need help? Check `TASK_5_6_7_TESTING_GUIDE.md` for complete examples!
