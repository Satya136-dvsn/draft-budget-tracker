# 🧪 Postman Testing Guide - BudgetWise API

## Task 2: Authentication System Testing

### Base URL
```
http://localhost:8080
```

---

## 1️⃣ Test: Register New User

### Request Details
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/register`
- **Headers**:
  ```
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Test123!@#"
}
```

### Expected Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### Validation Rules
- Username: 3-50 characters
- Email: Valid email format
- Password: Minimum 8 characters, must contain letter, number, and special character

### Test Cases

#### ✅ Valid Registration
```json
{
  "username": "testuser1",
  "email": "test1@example.com",
  "password": "Pass123!@#"
}
```
**Expected**: 200 OK with tokens

#### ❌ Duplicate Email
```json
{
  "username": "testuser2",
  "email": "john@example.com",
  "password": "Pass123!@#"
}
```
**Expected**: 400 Bad Request - "Email already in use"

#### ❌ Duplicate Username
```json
{
  "username": "johndoe",
  "email": "different@example.com",
  "password": "Pass123!@#"
}
```
**Expected**: 400 Bad Request - "Username already in use"

#### ❌ Weak Password
```json
{
  "username": "testuser3",
  "email": "test3@example.com",
  "password": "weak"
}
```
**Expected**: 400 Bad Request - Validation error

#### ❌ Invalid Email
```json
{
  "username": "testuser4",
  "email": "notanemail",
  "password": "Pass123!@#"
}
```
**Expected**: 400 Bad Request - "Email should be valid"

---

## 2️⃣ Test: Login User

### Request Details
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/login`
- **Headers**:
  ```
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "email": "john@example.com",
  "password": "Test123!@#"
}
```

### Expected Response (200 OK)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### Test Cases

#### ✅ Valid Login
```json
{
  "email": "john@example.com",
  "password": "Test123!@#"
}
```
**Expected**: 200 OK with tokens

#### ❌ Wrong Password
```json
{
  "email": "john@example.com",
  "password": "WrongPassword123!"
}
```
**Expected**: 401 Unauthorized - "Invalid email or password"

#### ❌ Non-existent Email
```json
{
  "email": "nonexistent@example.com",
  "password": "Test123!@#"
}
```
**Expected**: 401 Unauthorized - "Invalid email or password"

#### ❌ Missing Fields
```json
{
  "email": "john@example.com"
}
```
**Expected**: 400 Bad Request - Validation error

---

## 3️⃣ Test: Refresh Token

### Request Details
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/auth/refresh`
- **Headers**:
  ```
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "refreshToken": "YOUR_REFRESH_TOKEN_HERE"
}
```

### Expected Response (200 OK)
```json
{
  "accessToken": "NEW_ACCESS_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN",
  "tokenType": "Bearer",
  "expiresIn": 3600000,
  "user": {
    "id": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

### Test Cases

#### ✅ Valid Refresh Token
Use the refresh token from login/register response
**Expected**: 200 OK with new tokens

#### ❌ Invalid Refresh Token
```json
{
  "refreshToken": "invalid.token.here"
}
```
**Expected**: 400 Bad Request - "Invalid refresh token"

#### ❌ Expired Refresh Token
Use a token that's older than 7 days
**Expected**: 400 Bad Request - "Invalid refresh token"

---

## 4️⃣ Test: Protected Endpoint (Test Endpoint)

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/auth/test`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  ```

### Expected Response (200 OK)
```
Auth endpoint is working!
```

### Test Cases

#### ✅ With Valid Token
**Headers**:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```
**Expected**: 200 OK - "Auth endpoint is working!"

#### ❌ Without Token
No Authorization header
**Expected**: 403 Forbidden

#### ❌ With Invalid Token
**Headers**:
```
Authorization: Bearer invalid.token.here
```
**Expected**: 403 Forbidden

---

## 5️⃣ Test: Health Check (Public Endpoint)

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/actuator/health`
- **Headers**: None required

### Expected Response (200 OK)
```json
{
  "status": "UP"
}
```

---

## 📋 Postman Collection Setup

### Step-by-Step Setup

1. **Open Postman**

2. **Create New Collection**
   - Click "New" → "Collection"
   - Name: "BudgetWise API"
   - Description: "BudgetWise Backend API Testing"

3. **Add Environment Variables**
   - Click "Environments" → "Create Environment"
   - Name: "BudgetWise Local"
   - Variables:
     ```
     base_url = http://localhost:8080
     access_token = (leave empty - will be set automatically)
     refresh_token = (leave empty - will be set automatically)
     ```

4. **Create Requests**

### Request 1: Register
- Name: "Register User"
- Method: POST
- URL: `{{base_url}}/api/auth/register`
- Body → raw → JSON:
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Test123!@#"
}
```
- Tests (to save tokens):
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.accessToken);
    pm.environment.set("refresh_token", jsonData.refreshToken);
}
```

### Request 2: Login
- Name: "Login User"
- Method: POST
- URL: `{{base_url}}/api/auth/login`
- Body → raw → JSON:
```json
{
  "email": "john@example.com",
  "password": "Test123!@#"
}
```
- Tests (to save tokens):
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.accessToken);
    pm.environment.set("refresh_token", jsonData.refreshToken);
}
```

### Request 3: Refresh Token
- Name: "Refresh Token"
- Method: POST
- URL: `{{base_url}}/api/auth/refresh`
- Body → raw → JSON:
```json
{
  "refreshToken": "{{refresh_token}}"
}
```
- Tests (to save new tokens):
```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("access_token", jsonData.accessToken);
    pm.environment.set("refresh_token", jsonData.refreshToken);
}
```

### Request 4: Test Protected Endpoint
- Name: "Test Auth Endpoint"
- Method: GET
- URL: `{{base_url}}/api/auth/test`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  ```

### Request 5: Health Check
- Name: "Health Check"
- Method: GET
- URL: `{{base_url}}/actuator/health`

---

## Task 3: User Profile Management Testing

### 6️⃣ Test: Get User Profile

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/profile`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  Content-Type: application/json
  ```

### Expected Response (200 OK)
```json
{
  "id": 1,
  "monthlyIncome": null,
  "savingsTarget": null,
  "currency": "INR",
  "timezone": "Asia/Kolkata",
  "language": "en",
  "theme": "light",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": true,
  "notificationPush": true
}
```

**Note**: Default values are set for Indian users:
- Currency: INR (Indian Rupees)
- Timezone: Asia/Kolkata (IST - UTC+5:30)
- Date Format: dd/MM/yyyy (Indian standard)

### Test Cases

#### ✅ Get Profile with Valid Token
**Headers**:
```
Authorization: Bearer {{access_token}}
```
**Expected**: 200 OK with profile data

#### ❌ Get Profile without Token
No Authorization header
**Expected**: 403 Forbidden

---

### 7️⃣ Test: Update User Profile

### Request Details
- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/profile`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "monthlyIncome": 50000.00,
  "savingsTarget": 10000.00,
  "currency": "INR",
  "timezone": "Asia/Kolkata",
  "language": "en",
  "theme": "dark",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": true,
  "notificationPush": false
}
```

### Expected Response (200 OK)
```json
{
  "id": 1,
  "monthlyIncome": 50000.00,
  "savingsTarget": 10000.00,
  "currency": "INR",
  "timezone": "Asia/Kolkata",
  "language": "en",
  "theme": "dark",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": true,
  "notificationPush": false
}
```

### Test Cases

#### ✅ Update Full Profile
```json
{
  "monthlyIncome": 60000.00,
  "savingsTarget": 15000.00,
  "currency": "INR",
  "timezone": "Asia/Kolkata",
  "language": "en",
  "theme": "light",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": true,
  "notificationPush": true
}
```
**Expected**: 200 OK with updated profile

#### ✅ Partial Update (only some fields)
```json
{
  "monthlyIncome": 70000.00,
  "theme": "dark"
}
```
**Expected**: 200 OK with updated fields, others unchanged

#### ❌ Invalid Monthly Income (negative)
```json
{
  "monthlyIncome": -1000.00
}
```
**Expected**: 400 Bad Request - "Monthly income must be positive"

#### ❌ Invalid Theme
```json
{
  "theme": "rainbow"
}
```
**Expected**: 400 Bad Request - "Theme must be either 'light' or 'dark'"

---

### 8️⃣ Test: Update User Preferences Only

### Request Details
- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/profile/preferences`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "theme": "dark",
  "language": "hi",
  "timezone": "Asia/Kolkata",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": false,
  "notificationPush": true
}
```

### Expected Response (200 OK)
```json
{
  "id": 1,
  "monthlyIncome": 50000.00,
  "savingsTarget": 10000.00,
  "currency": "INR",
  "timezone": "Asia/Kolkata",
  "language": "hi",
  "theme": "dark",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": false,
  "notificationPush": true
}
```

### Test Cases

#### ✅ Update Theme Only
```json
{
  "theme": "light"
}
```
**Expected**: 200 OK with theme updated, financial data unchanged

#### ✅ Update Notifications
```json
{
  "notificationEmail": false,
  "notificationPush": false
}
```
**Expected**: 200 OK with notifications disabled

#### ✅ Update Language and Timezone
```json
{
  "language": "hi",
  "timezone": "Asia/Kolkata"
}
```
**Expected**: 200 OK with language and timezone updated

---

### 9️⃣ Test: Profile Test Endpoint (Public)

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/profile/test`
- **Headers**: None required

### Expected Response (200 OK)
```
Profile endpoint is working!
```

---

## 📋 Updated Postman Collection Setup

### Additional Requests for Task 3

### Request 6: Get Profile
- Name: "Get User Profile"
- Method: GET
- URL: `{{base_url}}/api/profile`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  ```

### Request 7: Update Profile
- Name: "Update User Profile"
- Method: PUT
- URL: `{{base_url}}/api/profile`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
  ```
- Body → raw → JSON:
```json
{
  "monthlyIncome": 50000.00,
  "savingsTarget": 10000.00,
  "currency": "INR",
  "timezone": "Asia/Kolkata",
  "language": "en",
  "theme": "dark",
  "dateFormat": "dd/MM/yyyy",
  "notificationEmail": true,
  "notificationPush": true
}
```

### Request 8: Update Preferences
- Name: "Update User Preferences"
- Method: PUT
- URL: `{{base_url}}/api/profile/preferences`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
  ```
- Body → raw → JSON:
```json
{
  "theme": "dark",
  "language": "en",
  "notificationEmail": true,
  "notificationPush": false
}
```

### Request 9: Profile Test
- Name: "Profile Test Endpoint"
- Method: GET
- URL: `{{base_url}}/api/profile/test`

---

## Task 4: Category Management Testing

### 10️⃣ Test: Get All Categories

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/categories`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  ```

### Expected Response (200 OK)
```json
[
  {
    "id": 1,
    "name": "Food & Dining",
    "type": "EXPENSE",
    "icon": "🍽️",
    "color": "#FF6B6B",
    "isSystem": true,
    "userId": null
  },
  {
    "id": 2,
    "name": "Groceries",
    "type": "EXPENSE",
    "icon": "🛒",
    "color": "#4ECDC4",
    "isSystem": true,
    "userId": null
  },
  ...
]
```

**Note**: Returns all system categories (23 total) plus any custom categories created by the user.

### Test Cases

#### ✅ Get All Categories with Valid Token
**Headers**:
```
Authorization: Bearer {{access_token}}
```
**Expected**: 200 OK with list of system + custom categories

#### ❌ Get Categories without Token
No Authorization header
**Expected**: 403 Forbidden

---

### 11️⃣ Test: Get Custom Categories Only

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/categories/custom`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  ```

### Expected Response (200 OK)
```json
[
  {
    "id": 24,
    "name": "My Custom Category",
    "type": "EXPENSE",
    "icon": "🎯",
    "color": "#FF5733",
    "isSystem": false,
    "userId": 1
  }
]
```

**Note**: Returns only user-created custom categories (empty array if none created yet).

---

### 12️⃣ Test: Create Custom Category

### Request Details
- **Method**: `POST`
- **URL**: `http://localhost:8080/api/categories`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "name": "Gym Membership",
  "type": "EXPENSE",
  "icon": "💪",
  "color": "#FF5733"
}
```

### Expected Response (201 Created)
```json
{
  "id": 24,
  "name": "Gym Membership",
  "type": "EXPENSE",
  "icon": "💪",
  "color": "#FF5733",
  "isSystem": false,
  "userId": 1
}
```

### Test Cases

#### ✅ Create Valid Custom Category
```json
{
  "name": "Online Subscriptions",
  "type": "EXPENSE",
  "icon": "📱",
  "color": "#3498DB"
}
```
**Expected**: 201 Created with category details

#### ✅ Create Income Category
```json
{
  "name": "Side Hustle",
  "type": "INCOME",
  "icon": "💸",
  "color": "#2ECC71"
}
```
**Expected**: 201 Created

#### ❌ Create Duplicate Category
```json
{
  "name": "Food & Dining",
  "type": "EXPENSE",
  "icon": "🍕",
  "color": "#E74C3C"
}
```
**Expected**: 400 Bad Request - "Category with name 'Food & Dining' already exists"

#### ❌ Create Category without Name
```json
{
  "type": "EXPENSE",
  "icon": "🎯",
  "color": "#FF5733"
}
```
**Expected**: 400 Bad Request - "Category name is required"

#### ❌ Create Category without Type
```json
{
  "name": "Test Category",
  "icon": "🎯",
  "color": "#FF5733"
}
```
**Expected**: 400 Bad Request - "Category type is required"

---

### 13️⃣ Test: Get Category by ID

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/categories/{id}`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  ```

### Expected Response (200 OK)
```json
{
  "id": 1,
  "name": "Food & Dining",
  "type": "EXPENSE",
  "icon": "🍽️",
  "color": "#FF6B6B",
  "isSystem": true,
  "userId": null
}
```

### Test Cases

#### ✅ Get System Category
`GET /api/categories/1`
**Expected**: 200 OK with category details

#### ✅ Get Own Custom Category
`GET /api/categories/24` (your custom category ID)
**Expected**: 200 OK with category details

#### ❌ Get Non-existent Category
`GET /api/categories/9999`
**Expected**: 400 Bad Request - "Category not found"

---

### 14️⃣ Test: Update Custom Category

### Request Details
- **Method**: `PUT`
- **URL**: `http://localhost:8080/api/categories/{id}`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  Content-Type: application/json
  ```

### Request Body (JSON)
```json
{
  "name": "Fitness & Gym",
  "type": "EXPENSE",
  "icon": "🏋️",
  "color": "#E67E22"
}
```

### Expected Response (200 OK)
```json
{
  "id": 24,
  "name": "Fitness & Gym",
  "type": "EXPENSE",
  "icon": "🏋️",
  "color": "#E67E22",
  "isSystem": false,
  "userId": 1
}
```

### Test Cases

#### ✅ Update Own Custom Category
```json
{
  "name": "Updated Category Name",
  "type": "EXPENSE",
  "icon": "✨",
  "color": "#9B59B6"
}
```
**Expected**: 200 OK with updated category

#### ❌ Update System Category
Try to update category ID 1 (Food & Dining)
**Expected**: 400 Bad Request - "System categories cannot be modified"

#### ❌ Update to Duplicate Name
```json
{
  "name": "Groceries",
  "type": "EXPENSE",
  "icon": "🛒",
  "color": "#4ECDC4"
}
```
**Expected**: 400 Bad Request - "Category with name 'Groceries' already exists"

---

### 15️⃣ Test: Delete Custom Category

### Request Details
- **Method**: `DELETE`
- **URL**: `http://localhost:8080/api/categories/{id}`
- **Headers**:
  ```
  Authorization: Bearer YOUR_ACCESS_TOKEN_HERE
  ```

### Expected Response (204 No Content)
No response body

### Test Cases

#### ✅ Delete Own Custom Category
`DELETE /api/categories/24` (your custom category ID)
**Expected**: 204 No Content

#### ❌ Delete System Category
`DELETE /api/categories/1` (Food & Dining)
**Expected**: 400 Bad Request - "System categories cannot be deleted"

#### ❌ Delete Non-existent Category
`DELETE /api/categories/9999`
**Expected**: 400 Bad Request - "Category not found"

---

### 16️⃣ Test: Category Test Endpoint (Public)

### Request Details
- **Method**: `GET`
- **URL**: `http://localhost:8080/api/categories/test`
- **Headers**: None required

### Expected Response (200 OK)
```
Category endpoint is working!
```

---

## 📋 Updated Postman Collection Setup

### Additional Requests for Task 4

### Request 10: Get All Categories
- Name: "Get All Categories"
- Method: GET
- URL: `{{base_url}}/api/categories`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  ```

### Request 11: Get Custom Categories
- Name: "Get Custom Categories"
- Method: GET
- URL: `{{base_url}}/api/categories/custom`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  ```

### Request 12: Create Category
- Name: "Create Custom Category"
- Method: POST
- URL: `{{base_url}}/api/categories`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
  ```
- Body → raw → JSON:
```json
{
  "name": "Gym Membership",
  "type": "EXPENSE",
  "icon": "💪",
  "color": "#FF5733"
}
```

### Request 13: Get Category by ID
- Name: "Get Category by ID"
- Method: GET
- URL: `{{base_url}}/api/categories/1`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  ```

### Request 14: Update Category
- Name: "Update Custom Category"
- Method: PUT
- URL: `{{base_url}}/api/categories/24`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  Content-Type: application/json
  ```
- Body → raw → JSON:
```json
{
  "name": "Fitness & Gym",
  "type": "EXPENSE",
  "icon": "🏋️",
  "color": "#E67E22"
}
```

### Request 15: Delete Category
- Name: "Delete Custom Category"
- Method: DELETE
- URL: `{{base_url}}/api/categories/24`
- Headers:
  ```
  Authorization: Bearer {{access_token}}
  ```

### Request 16: Category Test
- Name: "Category Test Endpoint"
- Method: GET
- URL: `{{base_url}}/api/categories/test`

---

## 🔍 Testing Workflow

### Complete Test Flow

#### Task 2: Authentication
1. **Health Check** → Verify backend is running
2. **Register** → Create a new user (save tokens)
3. **Login** → Login with created user (save tokens)
4. **Test Protected Endpoint** → Verify token works
5. **Refresh Token** → Get new tokens
6. **Test Protected Endpoint** → Verify new token works

#### Task 3: User Profile
7. **Profile Test** → Verify profile endpoint is accessible
8. **Get Profile** → Retrieve user profile (auto-created on registration)
9. **Update Profile** → Update financial information (income, savings target)
10. **Update Preferences** → Update UI preferences (theme, language, notifications)
11. **Get Profile** → Verify updates were saved

#### Task 4: Category Management
12. **Category Test** → Verify category endpoint is accessible
13. **Get All Categories** → View all system categories (23 pre-seeded)
14. **Get Custom Categories** → View only user-created categories
15. **Create Category** → Create a custom expense category
16. **Create Category** → Create a custom income category
17. **Get Category by ID** → Retrieve specific category details
18. **Update Category** → Modify custom category (system categories protected)
19. **Delete Category** → Remove custom category (system categories protected)
20. **Verify Protection** → Try to modify/delete system category (should fail)

### Expected Results

✅ All requests should return 200 OK (except intentional error tests)
✅ Tokens should be saved automatically
✅ Protected endpoints should work with valid tokens
✅ Invalid requests should return appropriate error messages

---

## 🐛 Common Issues & Solutions

### Issue 1: Connection Refused
**Error**: `Could not get any response`
**Solution**: 
- Check if backend is running: `http://localhost:8080/actuator/health`
- Verify port 8080 is not blocked

### Issue 2: 403 Forbidden
**Error**: `Access Denied`
**Solution**:
- Check if Authorization header is set correctly
- Verify token format: `Bearer YOUR_TOKEN`
- Ensure token hasn't expired (1 hour for access token)

### Issue 3: 400 Bad Request
**Error**: `Validation Failed`
**Solution**:
- Check request body format (must be valid JSON)
- Verify all required fields are present
- Check validation rules (password strength, email format, etc.)

### Issue 4: 401 Unauthorized
**Error**: `Invalid email or password`
**Solution**:
- Verify credentials are correct
- Check if user exists (register first)
- Ensure password meets requirements

---

## 📊 Success Criteria

After testing, you should have:

✅ Successfully registered at least one user
✅ Successfully logged in with that user
✅ Received valid JWT tokens
✅ Accessed protected endpoint with token
✅ Refreshed tokens successfully
✅ Verified error handling works correctly

---

## 🎯 Next Steps

### After Task 2 (Authentication):
1. ✅ Task 2 is verified and working
2. 🚀 Ready to proceed with Task 3: User Profile Management
3. 📝 Can check users table in MySQL to see registered users

### After Task 3 (User Profile):
1. ✅ Task 3 is verified and working
2. 🚀 Ready to proceed with Task 4: Category Management
3. 📝 Can check user_profiles table in MySQL to see profile data
4. ⚠️ **Note**: Frontend pages are not yet available - we're building backend APIs first (Phase 1 & 2). Frontend UI will be implemented in Phase 3 (Tasks 18-32)

### After Task 4 (Category Management):
1. ✅ Task 4 is verified and working
2. 🚀 Ready to proceed with Task 5: Transaction Management Core
3. 📝 Can check categories table in MySQL to see 23 system categories + custom categories
4. 💡 **System Categories**: 15 Expense categories + 8 Income categories auto-seeded
5. 🔒 **Protection**: System categories cannot be modified or deleted

---

## 💡 Pro Tips

1. **Save Tokens Automatically**: Use Postman Tests tab to save tokens to environment variables
2. **Use Variables**: Use `{{base_url}}` and `{{access_token}}` for easy switching between environments
3. **Organize Requests**: Group related requests in folders
4. **Document Tests**: Add descriptions to each request explaining what it tests
5. **Export Collection**: Save your Postman collection for future use

---

**Ready to test!** 🚀
