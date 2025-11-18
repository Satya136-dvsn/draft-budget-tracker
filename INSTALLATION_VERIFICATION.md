# Installation Verification Guide

## ✅ Pre-Installation Checklist

Before running the application, verify these items are installed:

### Required Software
- [ ] **Java 17 or higher**
  ```bash
  java -version
  # Should show: java version "17.x.x" or higher
  ```

- [ ] **Node.js 18 or higher**
  ```bash
  node --version
  # Should show: v18.x.x or higher
  ```

- [ ] **MySQL 8.0 or higher**
  ```bash
  mysql --version
  # Should show: mysql Ver 8.0.x or higher
  ```

- [ ] **Maven 3.6+ (or use included wrapper)**
  ```bash
  mvn --version
  # Or use ./mvnw (Maven wrapper included)
  ```

## 📦 Critical Dependencies Verification

### Backend Dependencies (pom.xml)
✅ **Confirmed Present:**
- Spring Boot Web 3.2.0
- Spring Boot Security 3.2.0
- Spring Boot Data JPA 3.2.0
- Spring Boot Validation 3.2.0
- Spring Boot WebSocket 3.2.0
- MySQL Connector (runtime)
- JWT (io.jsonwebtoken) 0.12.3
- Lombok (optional)
- Apache Commons Math 3.6.1
- Apache POI 5.2.5
- Spring Boot Actuator
- Spring Boot Test
- Spring Security Test

### Frontend Dependencies (package.json)
✅ **Critical Dependencies Confirmed:**
- ✅ **@mui/material**: ^5.15.0 (Material-UI)
- ✅ **@emotion/react**: ^11.11.1 (MUI peer dependency)
- ✅ **@emotion/styled**: ^11.11.0 (MUI peer dependency)
- ✅ **framer-motion**: ^10.16.16 (Animations)
- ✅ **recharts**: ^2.10.3 (Data Visualization)

✅ **Additional Dependencies:**
- react: ^18.2.0
- react-dom: ^18.2.0
- react-router-dom: ^6.20.1
- @mui/icons-material: ^5.15.0
- axios: ^1.6.2
- @tanstack/react-query: ^5.14.2
- date-fns: ^3.0.6
- react-hook-form: ^7.49.2
- sockjs-client: ^1.6.1
- @stomp/stompjs: ^7.0.0

## 🗄️ Database Setup Verification

### Step 1: Start MySQL
```bash
# Windows
net start MySQL80

# Mac
brew services start mysql

# Linux
sudo systemctl start mysql
```

### Step 2: Connect to MySQL
```bash
mysql -u root -p
# Enter your password
```

### Step 3: Create Database
```sql
CREATE DATABASE budgetwise;
SHOW DATABASES;
USE budgetwise;
```

### Step 4: Verify Connection
```sql
SELECT DATABASE();
-- Should show: budgetwise
```

## 🔧 Backend Verification

### Step 1: Navigate to Backend
```bash
cd backend
```

### Step 2: Update Credentials (if needed)
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.username=root
spring.datasource.password=your_password
```

### Step 3: Build Project (First Time)
```bash
# Windows
mvnw.cmd clean install

# Mac/Linux
./mvnw clean install
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: XX s
```

### Step 4: Start Backend
```bash
# Windows
mvnw.cmd spring-boot:run

# Mac/Linux
./mvnw spring-boot:run
```

**Expected Output:**
```
Started BudgetWiseApplication in X.XXX seconds
```

### Step 5: Verify Backend
Open browser or use curl:
```bash
curl http://localhost:8080/actuator/health
```

**Expected Response:**
```json
{"status":"UP"}
```

## 🎨 Frontend Verification

### Step 1: Navigate to Frontend
```bash
cd frontend
```

### Step 2: Install Dependencies
```bash
npm install
```

**Expected Output:**
```
added XXX packages in XXs
```

**Verify Critical Packages:**
```bash
npm list @mui/material
npm list framer-motion
npm list recharts
```

All should show installed versions.

### Step 3: Start Frontend
```bash
npm run dev
```

**Expected Output:**
```
VITE v5.0.8  ready in XXX ms

➜  Local:   http://localhost:3000/
➜  Network: use --host to expose
```

### Step 4: Verify Frontend
Open browser: http://localhost:3000

**Expected Display:**
- Large "BudgetWise" heading
- "AI-Driven Personal Finance Tracker" subtitle
- Backend and Frontend URLs
- Three green checkmarks:
  - ✓ Material-UI Configured
  - ✓ Framer Motion Ready
  - ✓ Recharts Ready

### Step 5: Check Browser Console
Press F12 to open Developer Tools

**Expected:**
- No red errors
- No warnings about missing dependencies
- Clean console

## 🧪 Full System Verification

### Test 1: Backend Health
```bash
curl http://localhost:8080/actuator/health
```
✅ Should return: `{"status":"UP"}`

### Test 2: Frontend Loading
- Navigate to http://localhost:3000
- ✅ Page loads without errors
- ✅ Material-UI styling visible
- ✅ Inter font loaded

### Test 3: API Proxy
Open browser console and run:
```javascript
fetch('/api/test')
  .then(r => console.log('Proxy working'))
  .catch(e => console.log('Proxy error:', e))
```
✅ Should attempt connection to backend (404 is OK - endpoint doesn't exist yet)

### Test 4: Hot Reload
1. Edit `frontend/src/App.jsx`
2. Change any text
3. Save file
✅ Browser should auto-refresh

### Test 5: Backend Auto-Restart
1. Edit `backend/src/main/java/com/budgetwise/BudgetWiseApplication.java`
2. Add a comment
3. Save file
✅ Backend should auto-restart (if DevTools enabled)

## 📊 Verification Checklist

### Infrastructure
- [ ] Java 17+ installed and verified
- [ ] Node.js 18+ installed and verified
- [ ] MySQL 8.0+ installed and running
- [ ] Maven available (or wrapper works)

### Database
- [ ] MySQL server running
- [ ] Database `budgetwise` created
- [ ] Connection credentials correct
- [ ] Can connect via MySQL Workbench or CLI

### Backend
- [ ] Dependencies downloaded (first `mvnw` run)
- [ ] Project builds successfully
- [ ] Backend starts without errors
- [ ] Runs on port 8080
- [ ] Health endpoint responds
- [ ] No errors in console

### Frontend
- [ ] Dependencies installed (`npm install` completed)
- [ ] Material-UI installed and verified
- [ ] Framer Motion installed and verified
- [ ] Recharts installed and verified
- [ ] Frontend starts without errors
- [ ] Runs on port 3000
- [ ] Landing page displays correctly
- [ ] No console errors in browser
- [ ] Hot reload works

### Integration
- [ ] Both servers running simultaneously
- [ ] API proxy configured correctly
- [ ] CORS working (no CORS errors)
- [ ] Can access both URLs

## 🚨 Troubleshooting

### Backend Issues

**Issue: Port 8080 in use**
```properties
# Change in application.properties
server.port=8081
```

**Issue: MySQL connection refused**
- Verify MySQL is running
- Check credentials in `application.properties`
- Verify database exists: `SHOW DATABASES;`

**Issue: Dependencies not downloading**
```bash
# Clear Maven cache
mvnw clean
# Try again
mvnw clean install
```

### Frontend Issues

**Issue: npm install fails**
```bash
# Clear cache
npm cache clean --force
# Delete node_modules
rm -rf node_modules package-lock.json
# Reinstall
npm install
```

**Issue: Port 3000 in use**
```javascript
// Change in vite.config.js
server: { port: 3001 }
```

**Issue: Module not found errors**
```bash
# Reinstall specific package
npm install @mui/material @emotion/react @emotion/styled
npm install framer-motion recharts
```

### Database Issues

**Issue: Access denied**
```sql
-- Reset password
ALTER USER 'root'@'localhost' IDENTIFIED BY 'newpassword';
FLUSH PRIVILEGES;
```

**Issue: Database doesn't exist**
```sql
CREATE DATABASE budgetwise;
```

## ✅ Success Criteria

All of the following should be true:

1. ✅ Backend running on http://localhost:8080
2. ✅ Frontend running on http://localhost:3000
3. ✅ Health endpoint returns `{"status":"UP"}`
4. ✅ Landing page displays with 3 green checkmarks
5. ✅ No errors in backend console
6. ✅ No errors in browser console
7. ✅ MySQL database `budgetwise` exists
8. ✅ All critical npm packages installed
9. ✅ Hot reload works on frontend
10. ✅ Can access both URLs in browser

## 🎉 Installation Complete!

If all checks pass, your installation is complete and you're ready to start development!

**Next Steps:**
1. Read `QUICKSTART.md` for development workflow
2. Check `DEV_CHECKLIST.md` for daily development tasks
3. Review `tasks.md` for Task 2: Authentication System
4. Start coding! 🚀

---

**Need Help?**
- Check console logs for specific errors
- Verify all prerequisites are installed
- Ensure ports 3000 and 8080 are available
- Review `QUICKSTART.md` for detailed setup steps
