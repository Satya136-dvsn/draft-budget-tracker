import requests
import json
from datetime import datetime

BASE_URL = "http://localhost:8080/api"
EMAIL = "test@example.com"
PASSWORD = "password@123"

def print_test(name, passed, details=""):
    status = "✓" if passed else "✗"
    print(f"{status} {name}")
    if details:
        print(f"   {details}")

# Authenticate
print("=" * 50)
print("AUTHENTICATION TEST")
print("=" * 50)
response = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
try:
    token = response.json()["accessToken"]
except KeyError:
    print(f"Login failed. Response: {response.json()}")
    exit(1)
headers = {"Authorization": f"Bearer {token}"}
print_test("Login successful", response.status_code == 200, f"Token: {token[:20]}...")

# Dashboard
print("\n" + "=" * 50)
print("DASHBOARD TEST")
print("=" * 50)
response = requests.get(f"{BASE_URL}/dashboard/summary", headers=headers)
dashboard = response.json()
print_test("Dashboard summary", response.status_code == 200)
print(f"   Income: ${dashboard.get('totalIncome', 0)}")
print(f"   Expenses: ${dashboard.get('totalExpenses', 0)}")
print(f"   Balance: ${dashboard.get('balance', 0)}")

# Transactions
print("\n" + "=" * 50)
print("TRANSACTIONS TEST")
print("=" * 50)
tx_data = {
    "type": "EXPENSE",
    "amount": 99.50,
    "categoryId": 1,
    "description": "Comprehensive Test Transaction",
    "transactionDate": "2025-11-20"
}
response = requests.post(f"{BASE_URL}/transactions", json=tx_data, headers=headers)
tx_id = response.json().get("id")
print_test("Create transaction", response.status_code == 200 or response.status_code == 201, f"ID: {tx_id}")

response = requests.get(f"{BASE_URL}/dashboard/summary", headers=headers)
new_expenses = response.json().get("totalExpenses", 0)
print_test("Dashboard cache evicted", new_expenses > dashboard.get("totalExpenses", 0), f"Expenses updated to ${new_expenses}")

# Categories
print("\n" + "=" * 50)
print("CATEGORIES TEST")
print("=" * 50)
response = requests.get(f"{BASE_URL}/categories", headers=headers)
categories = response.json()
print_test("List categories", response.status_code == 200, f"Found {len(categories)} categories")

# Try to delete system category (should fail)
response = requests.delete(f"{BASE_URL}/categories/1", headers=headers)
print_test("System category deletion blocked", response.status_code in [400, 403], "Correct error response")

# Budgets
print("\n" + "=" * 50)
print("BUDGETS TEST")
print("=" * 50)
budget_data = {
    "amount": 500,
    "categoryId": 1,
    "period": "MONTHLY",
    "startDate": "2025-11-01",
    "endDate": "2025-11-30",
    "alertThreshold": 80
}
response = requests.post(f"{BASE_URL}/budgets", json=budget_data, headers=headers)
budget_id = response.json().get("id")
print_test("Create budget", response.status_code in [200, 201], f"ID: {budget_id}")

contrib_data = {
    "amount": 75,
    "description": "Test Budget Contribution",
    "transactionDate": "2025-11-20"
}
response = requests.post(f"{BASE_URL}/budgets/{budget_id}/contribute", json=contrib_data, headers=headers)
print_test("Add budget contribution", response.status_code == 200)

response = requests.get(f"{BASE_URL}/dashboard/recent-transactions?limit=1", headers=headers)
latest_tx = response.json()[0]
print_test("Auto-transaction created", "contribution" in latest_tx.get("description", "").lower())

# Savings Goals
print("\n" + "=" * 50)
print("SAVINGS GOALS TEST")
print("=" * 50)
goal_data = {
    "name": "Comprehensive Test Goal",
    "targetAmount": 2000,
    "deadline": "2026-12-31"
}
response = requests.post(f"{BASE_URL}/savings-goals", json=goal_data, headers=headers)
goal_id = response.json().get("id")
print_test("Create savings goal", response.status_code in [200, 201], f"ID: {goal_id}")

contrib_data2 = {
    "amount": 150,
    "description": "Test Goal Contribution",
    "transactionDate": "2025-11-20"
}
response = requests.post(f"{BASE_URL}/savings-goals/{goal_id}/contribute", json=contrib_data2, headers=headers)
print_test("Add goal contribution", response.status_code == 200)

response = requests.get(f"{BASE_URL}/dashboard/recent-transactions?limit=1", headers=headers)
latest_tx = response.json()[0]
print_test("Goal auto-transaction (EXPENSE)", latest_tx.get("type") == "EXPENSE" and "Savings Goal" in latest_tx.get("description", ""))

response = requests.delete(f"{BASE_URL}/savings-goals/{goal_id}", headers=headers)
print_test("Delete savings goal", response.status_code in [200, 204])

response = requests.get(f"{BASE_URL}/dashboard/recent-transactions?limit=1", headers=headers)
latest_tx = response.json()[0]
print_test("Transaction converted to INCOME", latest_tx.get("type") == "INCOME" and "Deleted" in latest_tx.get("description", ""), f"Type: {latest_tx.get('type')}, Desc: {latest_tx.get('description', '')}")

# Analytics
print("\n" + "=" * 50)
print("ANALYTICS TEST")
print("=" * 50)
for months in [3, 6, 12]:
    response = requests.get(f"{BASE_URL}/dashboard/category-breakdown?months={months}", headers=headers)
    print_test(f"Category breakdown ({months} months)", response.status_code == 200)

print("\n" + "=" * 50)
print("VERIFICATION COMPLETE")
print("=" * 50)
