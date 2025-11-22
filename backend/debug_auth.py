import requests
import json

BASE_URL = "http://localhost:8080/api"
EMAIL = "test@example.com"
PASSWORD = "password@123"

print(f"Attempting login with {EMAIL}...")
try:
    response = requests.post(f"{BASE_URL}/auth/login", json={"email": EMAIL, "password": PASSWORD})
    print(f"Status Code: {response.status_code}")
    print(f"Response Body: {response.text}")
except Exception as e:
    print(f"Error: {e}")
