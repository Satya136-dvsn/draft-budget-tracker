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

print("Fetching 3 months data...")
res3 = requests.get(f"{BASE_URL}/dashboard/monthly-trends?months=3", headers=headers)
data3 = res3.json()
print(f"3 Months Data Points: {len(data3)}")

print("Fetching 6 months data...")
res6 = requests.get(f"{BASE_URL}/dashboard/monthly-trends?months=6", headers=headers)
data6 = res6.json()
print(f"6 Months Data Points: {len(data6)}")

if len(data3) == len(data6):
    print("FAIL: Data points are equal (likely cached incorrectly)")
else:
    print("PASS: Data points differ")
