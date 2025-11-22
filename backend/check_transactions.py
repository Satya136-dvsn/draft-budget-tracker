import requests
import json

BASE_URL = "http://localhost:8080/api"
EMAIL = "test@example.com"
PASSWORD = "password@123"

# Login
response = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
token = response.json()["accessToken"]
headers = {"Authorization": f"Bearer {token}"}

print("Fetching all transactions...")
res = requests.get(f"{BASE_URL}/transactions", headers=headers)
print(f"Status Code: {res.status_code}")

if res.status_code == 200:
    data = res.json()
    
    # Handle both array and paginated response
    transactions = data if isinstance(data, list) else data.get('content', [])
    
    print(f"\nTotal transactions returned: {len(transactions)}")
    print("\nTransactions list:")
    for i, tx in enumerate(transactions, 1):
        print(f"{i}. {tx.get('transactionDate')} - {tx.get('description')} - {tx.get('type')} - ₹{tx.get('amount')}")
else:
    print(f"Error: {res.text}")
