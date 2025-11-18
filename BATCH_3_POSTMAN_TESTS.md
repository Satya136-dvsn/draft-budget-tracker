# 🧪 Batch 3: Complete Postman Testing Guide
## Tasks 15-17: Community Forum, Admin, WebSocket

---

## 📋 Quick Reference

**Batch 3 Endpoints:**
1. `POST /api/forum/posts` - Create post
2. `GET /api/forum/posts` - List posts
3. `GET /api/forum/posts/{id}` - Get post
4. `POST /api/forum/posts/{id}/like` - Like post
5. `DELETE /api/forum/posts/{id}/like` - Unlike post
6. `POST /api/forum/posts/{postId}/comments` - Add comment
7. `GET /api/forum/posts/{postId}/comments` - Get comments
8. `GET /api/admin/stats` - System stats (ADMIN only)
9. `GET /api/admin/users` - List users (ADMIN only)
10. `GET /api/admin/audit-logs` - Audit logs (ADMIN only)

---

## 🏛️ Task 15: Community Forum

### 15.1 Create Post
**Endpoint:** `POST /api/forum/posts`

**Request:**
```
POST http://localhost:8080/api/forum/posts
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Best budgeting tips for beginners",
  "content": "I've been using this app for 6 months and here are my top tips: 1) Track every expense, 2) Set realistic budgets, 3) Review weekly",
  "tags": "tips,budgeting,beginners"
}
```

**Expected Response:**
```json
{
  "id": 1,
  "title": "Best budgeting tips for beginners",
  "content": "I've been using this app for 6 months...",
  "tags": "tips,budgeting,beginners",
  "likeCount": 0,
  "commentCount": 0,
  "userId": 1,
  "userName": "User 1",
  "createdAt": "2025-11-18T16:30:00",
  "updatedAt": "2025-11-18T16:30:00",
  "isLiked": false
}
```

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Post created", () => {
    const data = pm.response.json();
    pm.expect(data.id).to.be.a('number');
    pm.expect(data.likeCount).to.eql(0);
    pm.expect(data.commentCount).to.eql(0);
});
// Save post ID for later tests
pm.collectionVariables.set("postId", pm.response.json().id);
```

---

### 15.2 List Posts (Recent)
**Endpoint:** `GET /api/forum/posts`

**Request:**
```
GET http://localhost:8080/api/forum/posts?page=0&size=10&sort=recent
Authorization: Bearer <token>
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "title": "Best budgeting tips for beginners",
      "content": "I've been using this app...",
      "tags": "tips,budgeting,beginners",
      "likeCount": 0,
      "commentCount": 0,
      "userId": 1,
      "userName": "User 1",
      "createdAt": "2025-11-18T16:30:00",
      "isLiked": false
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

---

### 15.3 List Posts (Trending)
**Endpoint:** `GET /api/forum/posts?sort=trending`

**Request:**
```
GET http://localhost:8080/api/forum/posts?page=0&size=10&sort=trending
Authorization: Bearer <token>
```

**Description:** Returns posts sorted by engagement (likes + comments)

---

### 15.4 Get Single Post
**Endpoint:** `GET /api/forum/posts/{id}`

**Request:**
```
GET http://localhost:8080/api/forum/posts/1
Authorization: Bearer <token>
```

**Expected Response:**
```json
{
  "id": 1,
  "title": "Best budgeting tips for beginners",
  "content": "I've been using this app for 6 months...",
  "tags": "tips,budgeting,beginners",
  "likeCount": 0,
  "commentCount": 0,
  "userId": 1,
  "userName": "User 1",
  "createdAt": "2025-11-18T16:30:00",
  "updatedAt": "2025-11-18T16:30:00",
  "isLiked": false
}
```

---

### 15.5 Like Post
**Endpoint:** `POST /api/forum/posts/{id}/like`

**Request:**
```
POST http://localhost:8080/api/forum/posts/1/like
Authorization: Bearer <token>
```

**Expected Response:**
```
200 OK (empty body)
```

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
```

**Verify:** Get the post again - `likeCount` should be 1 and `isLiked` should be true

---

### 15.6 Unlike Post
**Endpoint:** `DELETE /api/forum/posts/{id}/like`

**Request:**
```
DELETE http://localhost:8080/api/forum/posts/1/like
Authorization: Bearer <token>
```

**Expected Response:**
```
200 OK (empty body)
```

**Verify:** Get the post again - `likeCount` should be 0 and `isLiked` should be false

---

### 15.7 Add Comment
**Endpoint:** `POST /api/forum/posts/{postId}/comments`

**Request:**
```
POST http://localhost:8080/api/forum/posts/1/comments
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "Great tips! I especially agree with tracking every expense."
}
```

**Expected Response:**
```json
{
  "id": 1,
  "postId": 1,
  "userId": 1,
  "content": "Great tips! I especially agree with tracking every expense.",
  "likeCount": 0,
  "createdAt": "2025-11-18T16:35:00"
}
```

**Test Assertions:**
```javascript
pm.test("Comment created", () => {
    const data = pm.response.json();
    pm.expect(data.id).to.be.a('number');
    pm.expect(data.postId).to.eql(1);
});
```

**Verify:** Get the post again - `commentCount` should be 1

---

### 15.8 Get Comments
**Endpoint:** `GET /api/forum/posts/{postId}/comments`

**Request:**
```
GET http://localhost:8080/api/forum/posts/1/comments?page=0&size=20
Authorization: Bearer <token>
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "postId": 1,
      "userId": 1,
      "content": "Great tips! I especially agree with tracking every expense.",
      "likeCount": 0,
      "createdAt": "2025-11-18T16:35:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 👨‍💼 Task 16: Admin Dashboard

**Note:** These endpoints require ADMIN role. Regular users will get 403 Forbidden.

### 16.1 Get System Statistics
**Endpoint:** `GET /api/admin/stats`

**Request:**
```
GET http://localhost:8080/api/admin/stats
Authorization: Bearer <admin-token>
```

**Expected Response:**
```json
{
  "totalUsers": 5,
  "totalTransactions": 150,
  "totalCategories": 20
}
```

**Test Assertions:**
```javascript
pm.test("Status is 200", () => pm.response.to.have.status(200));
pm.test("Has stats", () => {
    const data = pm.response.json();
    pm.expect(data).to.have.all.keys('totalUsers', 'totalTransactions', 'totalCategories');
    pm.expect(data.totalUsers).to.be.at.least(0);
});
```

---

### 16.2 List All Users
**Endpoint:** `GET /api/admin/users`

**Request:**
```
GET http://localhost:8080/api/admin/users?page=0&size=20
Authorization: Bearer <admin-token>
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "USER",
      "createdAt": "2025-11-17T10:00:00"
    },
    {
      "id": 2,
      "email": "admin@example.com",
      "firstName": "Admin",
      "lastName": "User",
      "role": "ADMIN",
      "createdAt": "2025-11-17T09:00:00"
    }
  ],
  "totalElements": 2,
  "totalPages": 1
}
```

---

### 16.3 Get Audit Logs
**Endpoint:** `GET /api/admin/audit-logs`

**Request:**
```
GET http://localhost:8080/api/admin/audit-logs?page=0&size=50
Authorization: Bearer <admin-token>
```

**Expected Response:**
```json
{
  "content": [
    {
      "id": 1,
      "adminUserId": 2,
      "actionType": "USER_VIEW",
      "targetUserId": 1,
      "targetResource": "User",
      "details": "Viewed user details",
      "ipAddress": "127.0.0.1",
      "createdAt": "2025-11-18T16:40:00"
    }
  ],
  "totalElements": 1,
  "totalPages": 1
}
```

---

## 🔌 Task 17: WebSocket Real-time Updates

### 17.1 WebSocket Connection (JavaScript Client)

**HTML/JavaScript Example:**
```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>WebSocket Test</h1>
    <div id="messages"></div>

    <script>
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);

            // Subscribe to dashboard updates
            stompClient.subscribe('/topic/user/1/dashboard', function(message) {
                console.log('Dashboard update:', message.body);
                document.getElementById('messages').innerHTML += 
                    '<p>Dashboard: ' + message.body + '</p>';
            });

            // Subscribe to alerts
            stompClient.subscribe('/topic/user/1/alerts', function(message) {
                console.log('Alert:', message.body);
                document.getElementById('messages').innerHTML += 
                    '<p>Alert: ' + message.body + '</p>';
            });

            // Subscribe to notifications
            stompClient.subscribe('/topic/user/1/notifications', function(message) {
                console.log('Notification:', message.body);
                document.getElementById('messages').innerHTML += 
                    '<p>Notification: ' + message.body + '</p>';
            });
        });
    </script>
</body>
</html>
```

### 17.2 Testing WebSocket

**Method 1: Browser Console**
1. Open the HTML file above in browser
2. Open browser console (F12)
3. Create a transaction via Postman
4. Watch for "Dashboard: refresh" message

**Method 2: Postman WebSocket (if supported)**
1. Create new WebSocket request
2. URL: `ws://localhost:8080/ws`
3. Connect and subscribe to topics

**Topics Available:**
- `/topic/user/{userId}/dashboard` - Dashboard refresh signals
- `/topic/user/{userId}/alerts` - Budget alerts
- `/topic/user/{userId}/notifications` - General notifications

---

## 🔄 Complete Test Workflow

### Phase 1: Forum Testing
```bash
1. POST /api/forum/posts (create 3 posts)
2. GET /api/forum/posts?sort=recent
3. GET /api/forum/posts?sort=trending
4. GET /api/forum/posts/1
5. POST /api/forum/posts/1/like
6. GET /api/forum/posts/1 (verify likeCount = 1)
7. POST /api/forum/posts/1/comments (add 2 comments)
8. GET /api/forum/posts/1/comments
9. GET /api/forum/posts/1 (verify commentCount = 2)
10. DELETE /api/forum/posts/1/like
```

### Phase 2: Admin Testing (ADMIN role required)
```bash
1. GET /api/admin/stats
2. GET /api/admin/users
3. GET /api/admin/audit-logs
```

### Phase 3: WebSocket Testing
```bash
1. Open WebSocket test HTML in browser
2. Create a transaction via Postman
3. Verify dashboard refresh message received
4. Create a budget alert scenario
5. Verify alert message received
```

---

## 📦 Postman Collection Structure

```
BudgetWise - Batch 3/
├── Task 15 - Forum/
│   ├── Create Post
│   ├── List Posts (Recent)
│   ├── List Posts (Trending)
│   ├── Get Post
│   ├── Like Post
│   ├── Unlike Post
│   ├── Add Comment
│   └── Get Comments
├── Task 16 - Admin/
│   ├── Get System Stats
│   ├── List All Users
│   └── Get Audit Logs
└── Task 17 - WebSocket/
    └── (Use browser-based testing)
```

---

## ✅ Success Criteria

All tests pass when:
- ✅ Posts can be created and retrieved
- ✅ Like/unlike updates counts correctly
- ✅ Comments are added and listed
- ✅ Trending algorithm works (sorts by engagement)
- ✅ Admin endpoints return data (with ADMIN role)
- ✅ Admin endpoints return 403 (without ADMIN role)
- ✅ WebSocket connects successfully
- ✅ Real-time messages are received

---

## 🎉 Batch 3 Complete!

**Total Endpoints Tested:** 10
- Forum: 7 endpoints
- Admin: 3 endpoints
- WebSocket: Real-time channels

**Next:** Phase 3 - Enterprise Frontend or Phase 4 - Production Readiness! 🚀
