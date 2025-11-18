# 🚀 Dashboard API - Quick Postman Test

## Step 1: Login & Get Token
```
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "email": "john.doe@example.com",
  "password": "password123"
}
```
**Copy the `token` from response!**

---

## Step 2: Test Dashboard Endpoints

### 1️⃣ Dashboard Summary
```
GET http://localhost:8080/api/dashboard/summary
Authorization: Bearer <your-token>
```

### 2️⃣ Monthly Trends (6 months)
```
GET http://localhost:8080/api/dashboard/monthly-trends?months=6
Authorization: Bearer <your-token>
```

### 3️⃣ Category Breakdown
```
GET http://localhost:8080/api/dashboard/category-breakdown
Authorization: Bearer <your-token>
```

### 4️⃣ Recent Transactions
```
GET http://localhost:8080/api/dashboard/recent-transactions?limit=10
Authorization: Bearer <your-token>
```

---

## ✅ Expected Results

All endpoints should return **200 OK** with JSON data.

**See `TASK_8_DASHBOARD_TESTING.md` for detailed examples and test assertions!**
