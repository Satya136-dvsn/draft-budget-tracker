import requests
import json

BASE_URL = "http://localhost:8080/api"
EMAIL = "test@example.com"
PASSWORD = "password@123"

# Login
response = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
try:
    token = response.json()["accessToken"]
except KeyError:
    print("Login failed")
    exit(1)

headers = {"Authorization": f"Bearer {token}"}

print("Fetching AI Predictions...")
res = requests.get(f"{BASE_URL}/ai/predictions", headers=headers)
print(f"Status Code: {res.status_code}")

if res.status_code == 200:
    predictions = res.json()
    print(f"\nFound {len(predictions)} predictions:")
    for pred in predictions:
        print(f"\n  Category: {pred.get('categoryName')}")
        print(f"  Predicted Amount: ${pred.get('predictedAmount')}")
        print(f"  Historical Average: ${pred.get('historicalAverage')}")
        print(f"  Trend: {pred.get('trend')}")
        print(f"  Confidence: {pred.get('confidenceScore')}%")
else:
    print(f"Error: {res.text}")
