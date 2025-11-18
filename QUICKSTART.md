# BudgetWise Quick Start Guide

## Step 1: Database Setup

### Option A: Using MySQL Workbench (Recommended)
1. Open MySQL Workbench
2. Connect to your MySQL server
3. Open the file `database/init.sql`
4. Execute the script (⚡ icon or Ctrl+Shift+Enter)
5. Verify the database was created

### Option B: Using Command Line
```bash
mysql -u root -p < database/init.sql
```

### Verify Database
```sql
SHOW DATABASES;
USE budgetwise;
```

## Step 2: Backend Setup

1. **Navigate to backend directory:**
   ```bash
   cd backend
   ```

2. **Update database credentials** (if needed):
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

3. **Install dependencies and run:**
   ```bash
   # Windows
   mvnw.cmd clean install
   mvnw.cmd spring-boot:run

   # Mac/Linux
   ./mvnw clean install
   ./mvnw spring-boot:run
   ```

4. **Verify backend is running:**
   - Open browser: http://localhost:8080
   - You should see a Whitelabel Error Page (this is normal - no endpoints yet)

## Step 3: Frontend Setup

1. **Navigate to frontend directory:**
   ```bash
   cd frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Start development server:**
   ```bash
   npm run dev
   ```

4. **Verify frontend is running:**
   - Open browser: http://localhost:3000
   - You should see the BudgetWise landing page with:
     - ✓ Material-UI Configured
     - ✓ Framer Motion Ready
     - ✓ Recharts Ready

## Step 4: Verify Installation

### Backend Health Check
```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### Frontend Check
- Navigate to http://localhost:3000
- Page should load without errors
- Check browser console (F12) - should be clean

## Common Issues

### Issue: MySQL Connection Failed
**Solution:** 
- Verify MySQL is running
- Check username/password in `application.properties`
- Ensure database `budgetwise` exists

### Issue: Port 8080 already in use
**Solution:**
- Stop other applications using port 8080
- Or change port in `application.properties`:
  ```properties
  server.port=8081
  ```

### Issue: Port 3000 already in use
**Solution:**
- Stop other applications using port 3000
- Or change port in `vite.config.js`:
  ```javascript
  server: { port: 3001 }
  ```

### Issue: npm install fails
**Solution:**
- Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and `package-lock.json`
- Run `npm install` again

## Next Steps

Now that your environment is set up, you can proceed with:

1. **Task 2**: Implement Authentication System
2. **Task 3**: User Profile Management
3. **Task 4**: Category Management

Refer to `.kiro/specs/budgetwise-complete-system/tasks.md` for the complete implementation plan.

## Development Workflow

### Running Both Servers
Open two terminal windows:

**Terminal 1 - Backend:**
```bash
cd backend
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm run dev
```

### Making Changes
- Backend changes: Server auto-restarts (Spring Boot DevTools)
- Frontend changes: Hot reload (Vite HMR)

### Stopping Servers
- Press `Ctrl+C` in each terminal

## Useful Commands

### Backend
```bash
# Run tests
./mvnw test

# Clean build
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Package for production
./mvnw clean package
```

### Frontend
```bash
# Development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Run tests
npm test

# Lint code
npm run lint
```

## Project URLs

- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **API Health**: http://localhost:8080/actuator/health
- **Swagger UI**: http://localhost:8080/swagger-ui.html (after implementation)

## Support

If you encounter any issues:
1. Check the console logs for error messages
2. Verify all prerequisites are installed
3. Ensure MySQL is running
4. Check that ports 3000 and 8080 are available

Happy coding! 🚀
