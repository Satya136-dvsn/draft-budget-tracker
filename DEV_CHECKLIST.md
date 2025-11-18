# Development Checklist

## Initial Setup (Task 1) ✅

- [x] Create backend Spring Boot project structure
- [x] Configure Maven dependencies (Web, Security, JPA, MySQL, WebSocket, JWT, etc.)
- [x] Create frontend React + Vite project structure
- [x] Install Material-UI (@mui/material, @emotion/react, @emotion/styled)
- [x] Install Framer Motion for animations
- [x] Install Recharts for data visualization
- [x] Configure MySQL database connection
- [x] Set up CORS configuration
- [x] Create Material-UI theme
- [x] Set up routing with React Router
- [x] Create project documentation

## Before Starting Development

### Database Setup
- [ ] Install MySQL 8.0+ if not already installed
- [ ] Start MySQL server
- [ ] Run `database/init.sql` to create `budgetwise` database
- [ ] Verify database connection

### Backend Setup
- [ ] Navigate to `backend` directory
- [ ] Update `application.properties` with your MySQL credentials
- [ ] Run `./mvnw clean install` (first time - downloads dependencies)
- [ ] Run `./mvnw spring-boot:run` to start backend
- [ ] Verify backend at http://localhost:8080/actuator/health

### Frontend Setup
- [ ] Navigate to `frontend` directory
- [ ] Run `npm install` (installs all dependencies including MUI, Framer Motion, Recharts)
- [ ] Run `npm run dev` to start frontend
- [ ] Verify frontend at http://localhost:3000
- [ ] Check browser console for errors (should be clean)

## Development Workflow

### Daily Development
- [ ] Start MySQL server
- [ ] Start backend server (`./mvnw spring-boot:run`)
- [ ] Start frontend server (`npm run dev`)
- [ ] Open browser to http://localhost:3000

### Before Committing Code
- [ ] Run backend tests: `./mvnw test`
- [ ] Run frontend tests: `npm test`
- [ ] Run frontend linter: `npm run lint`
- [ ] Check for console errors
- [ ] Test the feature manually

### Code Quality
- [ ] Follow Java naming conventions (backend)
- [ ] Follow React best practices (frontend)
- [ ] Write meaningful commit messages
- [ ] Add comments for complex logic
- [ ] Keep functions small and focused
- [ ] Handle errors appropriately

## Next Tasks (In Order)

### Task 2: Authentication System (Backend)
- [ ] 2.1: Create User Entity & Repository
- [ ] 2.2: Implement JWT Token Provider & Filters
- [ ] 2.3: Build Auth Controller (Login, Register, Refresh)
- [ ] 2.4: Implement Role-Based Access Control (RBAC)

### Task 3: User Profile Management (Backend)
- [ ] 3.1: Implement Profile Service
- [ ] 3.2: Create User Profile Controller

### Task 4: Category Management (Backend)
- [ ] 4.1: Create Category Entity & Repository
- [ ] 4.2: Implement Category Service
- [ ] 4.3: Create Category Controller

### Task 5: Transaction Management (Backend)
- [ ] 5.1: Create Transaction Entity & Repository
- [ ] 5.2: Implement Transaction Service
- [ ] 5.3: Create Transaction Controller

## Testing Checklist

### Backend Testing
- [ ] Unit tests for services
- [ ] Unit tests for repositories
- [ ] Integration tests for controllers
- [ ] Security tests for authentication
- [ ] Test coverage > 80%

### Frontend Testing
- [ ] Component tests with React Testing Library
- [ ] Hook tests
- [ ] Service tests (mocked API calls)
- [ ] Test coverage > 75%

## Deployment Checklist (Later)

### Pre-Deployment
- [ ] All tests passing
- [ ] No console errors
- [ ] Environment variables configured
- [ ] Database migrations ready
- [ ] Build production artifacts

### Production
- [ ] Backend JAR built: `./mvnw clean package`
- [ ] Frontend built: `npm run build`
- [ ] Database backed up
- [ ] SSL certificates configured
- [ ] Monitoring set up

## Troubleshooting

### Common Issues

**Backend won't start:**
- Check MySQL is running
- Verify database credentials in `application.properties`
- Check port 8080 is not in use
- Look at console logs for errors

**Frontend won't start:**
- Run `npm install` if dependencies missing
- Check port 3000 is not in use
- Clear npm cache: `npm cache clean --force`
- Delete `node_modules` and reinstall

**Database connection failed:**
- Verify MySQL is running: `mysql -u root -p`
- Check database exists: `SHOW DATABASES;`
- Verify credentials match `application.properties`

**CORS errors:**
- Check `CorsConfig.java` includes frontend URL
- Verify proxy in `vite.config.js`
- Check browser console for specific CORS error

## Useful Commands Reference

### Backend (Maven)
```bash
./mvnw clean install          # Build project
./mvnw spring-boot:run        # Run application
./mvnw test                   # Run tests
./mvnw clean package          # Build JAR
./mvnw dependency:tree        # Show dependencies
```

### Frontend (npm)
```bash
npm install                   # Install dependencies
npm run dev                   # Start dev server
npm run build                 # Build for production
npm run preview               # Preview production build
npm test                      # Run tests
npm run lint                  # Lint code
```

### Database (MySQL)
```bash
mysql -u root -p              # Connect to MySQL
SHOW DATABASES;               # List databases
USE budgetwise;               # Select database
SHOW TABLES;                  # List tables
DESCRIBE table_name;          # Show table structure
```

### Git
```bash
git status                    # Check status
git add .                     # Stage all changes
git commit -m "message"       # Commit changes
git push origin main          # Push to remote
git pull origin main          # Pull from remote
```

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev/)
- [Material-UI Documentation](https://mui.com/)
- [Framer Motion Documentation](https://www.framer.com/motion/)
- [Recharts Documentation](https://recharts.org/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

## Notes

- Keep this checklist updated as you progress
- Mark items complete as you finish them
- Add new items as needed
- Refer to `tasks.md` for detailed implementation steps

---

**Current Status**: Task 1 Complete ✅ - Ready for Task 2
**Next Step**: Implement Authentication System (Backend)
