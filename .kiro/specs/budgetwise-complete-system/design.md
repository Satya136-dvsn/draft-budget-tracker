# Design Document

## Overview

BudgetWise is a full-stack financial management platform built using a modern monolithic architecture with Spring Boot backend and React frontend. The system provides a seamless, real-time user experience with all services integrated within a single Spring Boot application. The design emphasizes simplicity, maintainability, performance, and professional UI/UX standards.

### Technology Stack

**Frontend:**
- React 18 with Hooks and Functional Components
- Vite (Fast build tool with HMR)
- Custom Design System + Material-UI patterns
- React Router 6 for navigation
- Axios for HTTP requests with interceptors
- SockJS + STOMP for WebSocket connections
- Chart.js and Recharts for data visualizations
- React Hook Form for form handling
- React Context API for state management
- Internationalization (i18n) support (EN, ES, FR)

**Backend:**
- Java 21 (LTS) with Spring Boot 3.5.3
- Spring Web (RESTful APIs)
- Spring Security 6.x (JWT authentication with RS256)
- Spring Data JPA (Hibernate 6.x ORM)
- Spring WebSocket (STOMP protocol for real-time features)
- Spring Cache (with Redis support)
- Apache Commons Math (for ML algorithms)
- iText 7.2.5 (PDF generation)
- Maven for build management

**Database:**
- MySQL 8.4 (Production database)
- H2 Database (Testing and development)
- HikariCP for connection pooling
- Flyway for database migrations
- Comprehensive indexing strategy for performance

**AI/ML:**
- Java-based machine learning using Apache Commons Math
- Optional external API integration (OpenAI, HuggingFace) via RestTemplate

**Development Tools:**
- Git for version control
- IntelliJ IDEA or VS Code
- Postman for API testing
- MySQL Workbench for database management

**Testing:**
- JUnit 5 + Mockito + Spring Boot Test (backend unit & integration tests)
- Vitest + React Testing Library (frontend unit tests)
- Cypress (E2E testing)
- H2 Database (integration testing)
- jest-axe (Accessibility testing)
- Target: 80%+ code coverage

**Deployment & DevOps:**
- Spring Boot JAR deployment (executable JAR)
- Docker & Docker Compose (optional for development)
- Nginx for reverse proxy and static file serving
- GitHub Actions for CI/CD pipeline
- Linux server (Ubuntu/CentOS) or cloud platform (AWS, Azure, DigitalOcean)
- Prometheus + Grafana for monitoring (ready)
- ELK Stack for logging (ready)

### Key Design Principles

1. **Monolithic Backend with Modular Frontend**: Spring Boot backend with React frontend
2. **Real-time Updates**: WebSocket and polling mechanisms for instant data synchronization
3. **Responsive Design**: Mobile-first approach with React and CSS frameworks
4. **Security First**: JWT authentication, encrypted data, and secure communication
5. **AI Integration**: Embedded Java ML and external AI services for intelligent features
6. **Scalability**: Optimized for single-server deployment with scaling options
7. **Error Resilience**: Comprehensive error handling and graceful degradation

## Architecture

### High-Level Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client Layer                             │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  React Frontend (Port 3000)                              │  │
│  │  - React 18 with Hooks                                   │  │
│  │  - Material-UI / Ant Design / Tailwind CSS              │  │
│  │  - Redux/Context API for State Management               │  │
│  │  - Real-time WebSocket Client                            │  │
│  │  - Chart.js / Recharts Visualizations                   │  │
│  │  - Axios for API calls                                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓ HTTPS/WSS
┌─────────────────────────────────────────────────────────────────┐
│                      Backend Layer                               │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Spring Boot Application (Port 8080)                     │  │
│  │  - REST API Controllers                                  │  │
│  │  - Spring Security + JWT                                 │  │
│  │  - WebSocket Support                                     │  │
│  │  - Business Logic Services                               │  │
│  │  - JPA/Hibernate for ORM                                │  │
│  │  - Embedded AI/ML (Java-based)                          │  │
│  │  - Community Forum APIs                                  │  │
│  │                                                          │  │
│  │  Modules:                                                │  │
│  │  - Authentication & Authorization                        │  │
│  │  - Transaction Management                                │  │
│  │  - Budget & Savings Goals                               │  │
│  │  - AI Services (Prediction, Categorization, Anomaly)    │  │
│  │  - Community Forum (Posts, Comments, Likes)             │  │
│  │  - Export & Backup                                       │  │
│  │  - Admin Management                                      │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                      Data Layer                                  │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  MySQL Database (Port 3306)                              │  │
│  │  - User accounts and profiles                            │  │
│  │  - Transactions and categories                           │  │
│  │  - Budgets and savings goals                            │  │
│  │  - Community forum data (posts, comments, likes)        │  │
│  │  - Notifications and audit logs                          │  │
│  │  - AI model parameters and statistics                   │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Application Configuration

```
┌──────────────────────────────────────┐
│  Spring Boot Configuration           │
│  - application.properties/yml        │
│  - Environment-specific configs      │
│  - Database connection pooling       │
│  - CORS configuration                │
│  - WebSocket configuration           │
└──────────────────────────────────────┘
```

## Components and Interfaces

### 1. Frontend (React 18)

#### Component Structure

```
src/
├── components/
│   ├── common/
│   │   ├── Navbar.jsx
│   │   ├── Sidebar.jsx
│   │   ├── Footer.jsx
│   │   ├── Loading.jsx
│   │   ├── ErrorBoundary.jsx
│   │   ├── ProtectedRoute.jsx
│   │   ├── Breadcrumb.jsx
│   │   ├── NotificationBell.jsx
│   │   └── UserProfileDropdown.jsx
│   ├── ui/ (Reusable UI Components)
│   │   ├── Button.jsx (Primary, Secondary, Outlined, Text variants)
│   │   ├── Card.jsx (Elevated, Outlined, Interactive variants)
│   │   ├── Modal.jsx (Confirmation, Form, Info variants)
│   │   ├── Input.jsx (Text, Number, Email, Password)
│   │   ├── Select.jsx (Single, Multi-select)
│   │   ├── Checkbox.jsx
│   │   ├── Radio.jsx
│   │   ├── DatePicker.jsx
│   │   ├── Table.jsx (Sortable, Filterable, Paginated)
│   │   ├── Alert.jsx (Success, Error, Warning, Info)
│   │   ├── Badge.jsx
│   │   ├── Chip.jsx
│   │   ├── Tooltip.jsx
│   │   ├── Skeleton.jsx
│   │   ├── Progress.jsx (Linear, Circular)
│   │   ├── Tabs.jsx
│   │   ├── Accordion.jsx
│   │   └── Stepper.jsx
│   ├── auth/
│   │   ├── Login.jsx (Enterprise design with animations)
│   │   ├── Register.jsx (Multi-step wizard)
│   │   ├── ForgotPassword.jsx
│   │   ├── ResetPassword.jsx
│   │   ├── Profile.jsx
│   │   └── ProfileSetup.jsx
│   ├── landing/
│   │   ├── HomePage.jsx
│   │   ├── HeroSection.jsx
│   │   ├── FeaturesSection.jsx
│   │   ├── TestimonialsSection.jsx
│   │   ├── PricingSection.jsx
│   │   └── CTASection.jsx
│   ├── dashboard/
│   │   ├── Dashboard.jsx (Enterprise layout with widgets)
│   │   ├── SummaryCards.jsx (Real-time data)
│   │   ├── ChartsSection.jsx (Interactive charts)
│   │   ├── AIInsights.jsx
│   │   ├── QuickActions.jsx
│   │   ├── RecentActivity.jsx
│   │   └── NotificationCenter.jsx
│   ├── transactions/
│   │   ├── TransactionList.jsx (Advanced data table)
│   │   ├── TransactionForm.jsx
│   │   ├── TransactionFilters.jsx (Advanced filtering)
│   │   ├── TransactionItem.jsx
│   │   ├── BulkActions.jsx
│   │   └── InlineEdit.jsx
│   ├── budgets/
│   │   ├── BudgetList.jsx
│   │   ├── BudgetForm.jsx
│   │   ├── BudgetCard.jsx (Visual progress)
│   │   ├── BudgetProgress.jsx
│   │   ├── BudgetForecast.jsx
│   │   ├── BudgetTemplates.jsx
│   │   └── AlertConfiguration.jsx
│   ├── goals/
│   │   ├── GoalsList.jsx
│   │   ├── GoalForm.jsx
│   │   ├── GoalCard.jsx
│   │   ├── MilestoneTracker.jsx
│   │   ├── ProgressVisualization.jsx
│   │   ├── AchievementCelebration.jsx
│   │   └── GoalRecommendations.jsx
│   ├── analytics/
│   │   ├── AnalyticsDashboard.jsx
│   │   ├── TrendAnalysis.jsx
│   │   ├── ComparativeView.jsx
│   │   ├── DrillDownChart.jsx
│   │   └── CustomDateRange.jsx
│   ├── reports/
│   │   ├── ReportsPage.jsx
│   │   ├── ReportTemplates.jsx
│   │   ├── CustomReportBuilder.jsx
│   │   ├── ScheduledReports.jsx
│   │   └── ExportOptions.jsx
│   ├── settings/
│   │   ├── SettingsPage.jsx (Tabbed interface)
│   │   ├── ProfileSettings.jsx
│   │   ├── SecuritySettings.jsx
│   │   ├── NotificationSettings.jsx
│   │   ├── PreferencesSettings.jsx
│   │   └── AccountManagement.jsx
│   ├── charts/
│   │   ├── BarChart.jsx
│   │   ├── PieChart.jsx
│   │   ├── LineChart.jsx
│   │   ├── DonutChart.jsx
│   │   ├── AreaChart.jsx
│   │   ├── ChartCard.jsx
│   │   └── ChartExport.jsx
│   ├── community/
│   │   ├── ForumHome.jsx
│   │   ├── PostList.jsx
│   │   ├── PostDetail.jsx
│   │   ├── PostForm.jsx
│   │   ├── CommentSection.jsx
│   │   └── TrendingTopics.jsx
│   └── admin/
│       ├── AdminDashboard.jsx
│       ├── UserManagement.jsx
│       ├── CategoryManagement.jsx
│       └── SystemStats.jsx
├── services/
│   ├── api.js
│   ├── authService.js
│   ├── transactionService.js
│   ├── budgetService.js
│   ├── savingsService.js
│   ├── aiService.js
│   ├── communityService.js
│   ├── exportService.js
│   └── websocketService.js
├── context/
│   ├── AuthContext.jsx
│   ├── ThemeContext.jsx
│   └── NotificationContext.jsx
├── hooks/
│   ├── useAuth.js
│   ├── useWebSocket.js
│   ├── useDebounce.js
│   └── useLocalStorage.js
├── utils/
│   ├── formatters.js
│   ├── validators.js
│   ├── constants.js
│   └── helpers.js
├── pages/
│   ├── HomePage.jsx
│   ├── DashboardPage.jsx
│   ├── TransactionsPage.jsx
│   ├── BudgetsPage.jsx
│   ├── SavingsPage.jsx
│   ├── CommunityPage.jsx
│   ├── AdminPage.jsx
│   └── NotFoundPage.jsx
└── App.jsx
```

#### Key Features

**Enterprise Design System**

The application implements a comprehensive design system for consistency and professional appearance:

*Design Tokens:*
- Colors: Primary (#1976d2), Secondary (#dc004e), Success (#4caf50), Warning (#ff9800), Error (#f44336), Neutral (gray scale)
- Typography: 
  - Font Families: Roboto (body), Montserrat (headings), Fira Code (monospace)
  - Font Sizes: 12px, 14px, 16px, 18px, 20px, 24px, 32px, 48px
  - Font Weights: 300 (light), 400 (regular), 500 (medium), 600 (semibold), 700 (bold)
  - Line Heights: 1.2 (tight), 1.5 (normal), 1.75 (relaxed)
- Spacing: 4px, 8px, 16px, 24px, 32px, 48px, 64px (based on 4px grid)
- Elevation: 0dp, 1dp, 2dp, 4dp, 6dp, 8dp, 12dp, 16dp, 24dp (Material Design shadows)
- Border Radius: 4px (small), 8px (medium), 16px (large), 50% (circular)
- Transitions: 150ms (fast), 300ms (normal), 500ms (slow) with ease-in-out timing

*Color Palette:*
- Light Mode:
  - Primary: #1976d2 (blue)
  - Secondary: #dc004e (pink)
  - Success: #4caf50 (green)
  - Warning: #ff9800 (orange)
  - Error: #f44336 (red)
  - Background: #ffffff, #f5f5f5
  - Text: #212121 (primary), #757575 (secondary)
- Dark Mode:
  - Primary: #90caf9 (light blue)
  - Secondary: #f48fb1 (light pink)
  - Success: #81c784 (light green)
  - Warning: #ffb74d (light orange)
  - Error: #e57373 (light red)
  - Background: #121212, #1e1e1e
  - Text: #ffffff (primary), #b0b0b0 (secondary)

*Component Variants:*
- Buttons: Primary (filled), Secondary (outlined), Text (no background), Icon (circular)
- Cards: Elevated (with shadow), Outlined (with border), Interactive (hover effects)
- Inputs: Standard, Filled, Outlined with floating labels
- Tables: Simple, Striped, Bordered, Hoverable with sorting and pagination

**Responsive Design**
- Breakpoints: 320px (mobile), 768px (tablet), 1024px (desktop), 1920px (large desktop)
- Flexbox and CSS Grid for layouts
- Material-UI / Ant Design / Tailwind CSS for styling
- Touch-optimized controls for mobile (minimum 44x44px touch targets)
- Mobile-first approach with progressive enhancement
- Responsive typography with fluid font sizes
- Adaptive navigation (hamburger menu on mobile, sidebar on desktop)

**Real-time Updates**
- WebSocket connection for live data using SockJS/STOMP
- Automatic reconnection on disconnect
- Optimistic UI updates with rollback on error
- Polling fallback (5-second interval)

**State Management**
- React Context API or Redux for global state
- Custom hooks for local state management
- Local storage for offline capability and persistence
- Session management with JWT refresh
- Axios interceptors for API calls

**UI Libraries**
- Material-UI (MUI) for component library
- Recharts or Chart.js for data visualizations
- React Router for navigation
- Formik or React Hook Form for form handling
- React Toastify for notifications

### 2. Backend Service (Spring Boot)

#### Package Structure

```
com.budgetwise.backend/
├── config/
│   ├── SecurityConfig.java
│   ├── WebSocketConfig.java
│   ├── CorsConfig.java
│   └── DatabaseConfig.java
├── controller/
│   ├── AuthController.java
│   ├── UserController.java
│   ├── TransactionController.java
│   ├── BudgetController.java
│   ├── SavingsGoalController.java
│   ├── CategoryController.java
│   ├── ExportController.java
│   └── AdminController.java
├── service/
│   ├── AuthService.java
│   ├── UserService.java
│   ├── TransactionService.java
│   ├── BudgetService.java
│   ├── SavingsGoalService.java
│   ├── CategoryService.java
│   ├── ExportService.java
│   ├── NotificationService.java
│   └── WebSocketService.java
├── repository/
│   ├── UserRepository.java
│   ├── TransactionRepository.java
│   ├── BudgetRepository.java
│   ├── SavingsGoalRepository.java
│   └── CategoryRepository.java
├── model/
│   ├── User.java
│   ├── Transaction.java
│   ├── Budget.java
│   ├── SavingsGoal.java
│   ├── Category.java
│   └── Notification.java
├── dto/
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── TransactionDTO.java
│   ├── BudgetDTO.java
│   └── DashboardSummaryDTO.java
├── security/
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   └── UserDetailsServiceImpl.java
└── exception/
    ├── GlobalExceptionHandler.java
    ├── ResourceNotFoundException.java
    └── UnauthorizedException.java
```

#### API Endpoints

**Authentication**
- POST `/api/auth/register` - User registration
- POST `/api/auth/login` - User login
- POST `/api/auth/refresh` - Refresh JWT token
- POST `/api/auth/logout` - User logout

**User Management**
- GET `/api/users/profile` - Get user profile
- PUT `/api/users/profile` - Update user profile
- GET `/api/users/preferences` - Get user preferences
- PUT `/api/users/preferences` - Update preferences

**Transactions**
- GET `/api/transactions` - List transactions (paginated, filtered)
- POST `/api/transactions` - Create transaction
- GET `/api/transactions/{id}` - Get transaction details
- PUT `/api/transactions/{id}` - Update transaction
- DELETE `/api/transactions/{id}` - Delete transaction
- GET `/api/transactions/summary` - Get transaction summary

**Budgets**
- GET `/api/budgets` - List budgets
- POST `/api/budgets` - Create budget
- GET `/api/budgets/{id}` - Get budget details
- PUT `/api/budgets/{id}` - Update budget
- DELETE `/api/budgets/{id}` - Delete budget
- GET `/api/budgets/{id}/progress` - Get budget progress

**Savings Goals**
- GET `/api/savings-goals` - List savings goals
- POST `/api/savings-goals` - Create savings goal
- GET `/api/savings-goals/{id}` - Get goal details
- PUT `/api/savings-goals/{id}` - Update goal
- DELETE `/api/savings-goals/{id}` - Delete goal

**Categories**
- GET `/api/categories` - List all categories
- POST `/api/categories` - Create custom category
- PUT `/api/categories/{id}` - Update category
- DELETE `/api/categories/{id}` - Delete category

**Export**
- GET `/api/export/csv` - Export transactions as CSV
- GET `/api/export/pdf` - Export report as PDF
- POST `/api/export/cloud` - Backup to cloud storage

**Admin**
- GET `/api/admin/users` - List all users
- GET `/api/admin/users/{id}/transactions` - View user transactions
- GET `/api/admin/categories` - Manage categories
- GET `/api/admin/statistics` - System statistics

**WebSocket**
- `/ws/notifications` - Real-time notifications
- `/ws/updates` - Real-time data updates

### 3. AI Service (Embedded in Spring Boot)

#### Structure (Java-based AI within Spring Boot)

```
com.budgetwise.backend/
├── ai/
│   ├── model/
│   │   ├── LinearRegression.java
│   │   ├── ExpensePredictor.java
│   │   ├── TransactionCategorizer.java
│   │   └── AnomalyDetector.java
│   ├── service/
│   │   ├── PredictionService.java
│   │   ├── CategorizationService.java
│   │   ├── AnomalyDetectionService.java
│   │   ├── BudgetAdvisorService.java
│   │   └── ChatAssistantService.java
│   ├── dto/
│   │   ├── PredictionResult.java
│   │   ├── CategorizationResult.java
│   │   ├── AnomalyResult.java
│   │   └── AdviceResult.java
│   └── repository/
│       ├── ModelParametersRepository.java
│       └── UserStatisticsRepository.java
```

#### AI Endpoints (within main Spring Boot app)

- POST `/api/ai/predict` - Predict next month expenses
- POST `/api/ai/categorize` - Auto-categorize transaction
- POST `/api/ai/detect-anomaly` - Detect anomalous transactions
- POST `/api/ai/advice` - Get budget advice
- POST `/api/ai/chat` - Chat with AI assistant (via external API)

#### ML Models (Java Implementation)

**1. Expense Predictor**
- Algorithm: Simple Linear Regression / Moving Average (Java implementation)
- Input: Historical transaction data (6-12 months)
- Output: Predicted next month expense with confidence interval
- Training: On-demand per user using Java math libraries
- Storage: MySQL for model parameters and statistics

**2. Transaction Categorizer**
- Algorithm: Keyword matching with weighted scoring
- Input: Transaction description
- Output: Category with confidence score
- Training: Pre-trained with common patterns, learns from corrections
- Storage: MySQL for keyword mappings and learned patterns

**3. Anomaly Detector**
- Algorithm: Statistical (mean + 2σ) using Java
- Input: Transaction amount and category history
- Output: Anomaly flag with severity score
- Training: Per user, per category
- Storage: MySQL for statistics

**4. Budget Advisor**
- Algorithm: Rule-based system in Java
- Input: Spending patterns, income, savings rate
- Output: Actionable recommendations
- External API: Optional OpenAI/HuggingFace integration via REST

**5. Chat Assistant**
- Algorithm: External LLM API integration
- Input: User question + financial summary
- Output: Personalized advice
- External API: OpenAI GPT-4 or HuggingFace (via RestTemplate/WebClient)
- Fallback: Rule-based responses when API unavailable

### 4. Community Service (Integrated in Spring Boot)

#### Structure (within Spring Boot backend)

```
com.budgetwise.backend/
├── community/
│   ├── model/
│   │   ├── Post.java
│   │   ├── Comment.java
│   │   └── Like.java
│   ├── repository/
│   │   ├── PostRepository.java
│   │   ├── CommentRepository.java
│   │   └── LikeRepository.java
│   ├── service/
│   │   ├── PostService.java
│   │   ├── CommentService.java
│   │   ├── LikeService.java
│   │   └── TrendingService.java
│   ├── controller/
│   │   └── CommunityController.java
│   └── dto/
│       ├── PostDTO.java
│       ├── CommentDTO.java
│       └── TrendingTopicDTO.java
```

#### API Endpoints (within main Spring Boot app)

- GET `/api/community/posts` - List posts (paginated, filtered)
- POST `/api/community/posts` - Create post
- GET `/api/community/posts/{id}` - Get post details
- PUT `/api/community/posts/{id}` - Update post
- DELETE `/api/community/posts/{id}` - Delete post
- POST `/api/community/posts/{id}/comments` - Add comment
- POST `/api/community/posts/{id}/like` - Like/unlike post
- GET `/api/community/trending` - Get trending topics

## Data Models

### Backend Service (MySQL)

**User Table**
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(20) DEFAULT 'USER',
    monthly_income DECIMAL(15,2),
    savings_target DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Transaction Table**
```sql
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id),
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    transaction_date DATE NOT NULL,
    is_anomaly BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, transaction_date),
    INDEX idx_category (category_id)
);
```

**Category Table**
```sql
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL, -- INCOME, EXPENSE
    icon VARCHAR(50),
    color VARCHAR(20),
    is_system BOOLEAN DEFAULT FALSE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(name, user_id)
);
```

**Budget Table**
```sql
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id),
    amount DECIMAL(15,2) NOT NULL,
    period VARCHAR(20) DEFAULT 'MONTHLY', -- WEEKLY, MONTHLY, YEARLY
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    alert_threshold DECIMAL(5,2) DEFAULT 80.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_period (user_id, start_date, end_date)
);
```

**Savings Goal Table**
```sql
CREATE TABLE savings_goals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    target_amount DECIMAL(15,2) NOT NULL,
    current_amount DECIMAL(15,2) DEFAULT 0,
    deadline DATE,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status)
);
```

**Notification Table**
```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_unread (user_id, is_read, created_at)
);
```

### Community Service (MySQL - Integrated)

**Post Table**
```sql
CREATE TABLE posts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(255),
    title VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    tags VARCHAR(500),
    likes_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_created (created_at DESC)
);
```

**Comment Table**
```sql
CREATE TABLE comments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT REFERENCES posts(id) ON DELETE CASCADE,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    user_name VARCHAR(255),
    content TEXT NOT NULL,
    likes_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post (post_id),
    INDEX idx_user (user_id)
);
```

**Like Table**
```sql
CREATE TABLE likes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(20) NOT NULL, -- POST, COMMENT
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_like (user_id, target_id, target_type),
    INDEX idx_target (target_id, target_type)
);
```

### AI Service (MySQL - Integrated)

**User Statistics Table**
```sql
CREATE TABLE user_statistics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES categories(id),
    mean_amount DECIMAL(15,2),
    std_dev DECIMAL(15,2),
    transaction_count INT,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_category (user_id, category_id)
);
```

**Prediction History Table**
```sql
CREATE TABLE prediction_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT REFERENCES users(id) ON DELETE CASCADE,
    predicted_amount DECIMAL(15,2),
    actual_amount DECIMAL(15,2),
    prediction_date DATE,
    accuracy_score DECIMAL(5,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, prediction_date)
);
```

## Error Handling

### Error Response Format

```json
{
  "timestamp": "2025-11-13T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Transaction amount must be positive",
  "path": "/api/transactions",
  "validationErrors": [
    {
      "field": "amount",
      "message": "must be greater than 0"
    }
  ]
}
```

### Error Categories

1. **Validation Errors (400)**: Invalid input data
2. **Authentication Errors (401)**: Invalid or expired JWT
3. **Authorization Errors (403)**: Insufficient permissions
4. **Not Found Errors (404)**: Resource doesn't exist
5. **Conflict Errors (409)**: Duplicate resource
6. **Server Errors (500)**: Internal server issues
7. **Service Unavailable (503)**: External service down

### Error Handling Strategy

- **Frontend**: Display user-friendly messages, log technical details
- **Backend**: Log all errors with stack traces, return sanitized messages
- **AI Service**: Graceful degradation, fallback to rule-based logic
- **Community Service**: Queue failed operations for retry

## Advanced Features (Enterprise Edition)

### Banking Integration
**Purpose:** Connect external bank accounts for automatic transaction synchronization

**Components:**
- **BankIntegrationController**: API endpoints for bank connections
- **BankIntegrationService**: Manage bank account linking and sync
- **BankAccount Entity**: Store bank account information
- **BankConnection Entity**: Store connection credentials (encrypted)

**Features:**
- Multiple bank account support
- Automatic transaction import
- Balance synchronization
- Connection status monitoring
- Secure credential storage (encrypted)

**Security:**
- OAuth 2.0 for bank authentication
- Encrypted storage of access tokens
- Regular token refresh
- Audit logging for all bank operations

### Bill Management & Reminders
**Purpose:** Track recurring bills and send payment reminders

**Components:**
- **BillController**: CRUD operations for bills
- **BillReminderService**: Scheduled reminder system
- **BillReminderScheduler**: Cron-based scheduler
- **Bill Entity**: Store bill information
- **BillPayment Entity**: Track payment history

**Features:**
- Recurring bill tracking (weekly, monthly, quarterly, yearly)
- Payment reminders (email, push notifications)
- Payment history
- Bill calendar view
- Overdue alerts
- Cash flow projection based on upcoming bills

**Notifications:**
- 3 days before due date
- On due date
- Overdue notifications
- Payment confirmation

### Investment Tracking
**Purpose:** Monitor investment portfolios and market performance

**Components:**
- **InvestmentController**: Portfolio management APIs
- **InvestmentService**: Investment calculations and analytics
- **PortfolioAnalyticsService**: Performance metrics
- **MarketDataService**: Real-time market data integration
- **Investment Entity**: Store investment holdings

**Features:**
- Stock and cryptocurrency tracking
- Portfolio performance analytics
- Asset allocation visualization
- Market data integration (Alpha Vantage API)
- Investment goals tracking
- Dividend tracking
- Capital gains/losses calculation

**Integrations:**
- Alpha Vantage API for stock prices
- Cryptocurrency APIs for crypto prices
- Real-time price updates

### Debt Management & Optimization
**Purpose:** Track debts and provide optimization strategies

**Components:**
- **DebtController**: Debt management APIs
- **DebtOptimizationController**: Optimization strategies
- **DebtService**: Debt tracking and calculations
- **DebtOptimizationService**: Payoff strategies
- **Debt Entity**: Store debt information

**Features:**
- Multiple debt types (credit cards, loans, mortgages, student loans)
- Debt payoff calculators
- Optimization strategies (avalanche, snowball)
- Interest tracking
- Payment schedules
- Debt consolidation analysis
- Payoff projections

**Strategies:**
- Avalanche method (highest interest first)
- Snowball method (smallest balance first)
- Custom payment plans
- Minimum payment tracking

### Retirement Planning
**Purpose:** Plan and track retirement savings

**Components:**
- **RetirementPlanningController**: Retirement planning APIs
- **RetirementPlanningService**: Retirement calculations
- **RetirementPlan Entity**: Store retirement plans

**Features:**
- 401(k) and IRA tracking
- Retirement calculators
- Contribution optimization
- Employer match tracking
- Retirement projections
- Social Security integration
- Required minimum distributions (RMD)
- Retirement income planning

**Calculations:**
- Future value projections
- Required monthly savings
- Retirement readiness score
- Income replacement ratio
- Life expectancy considerations

### Tax Planning
**Purpose:** Optimize tax strategies and track deductions

**Components:**
- **TaxPlanningController**: Tax planning APIs
- **TaxPlanningService**: Tax calculations
- **TaxPlan Entity**: Store tax information

**Features:**
- Tax calculation tools
- Deduction tracking
- Tax optimization strategies
- Multiple filing status support
- Capital gains/losses tracking
- Tax document preparation
- Estimated tax payments
- Tax bracket analysis

**Supported Deductions:**
- Mortgage interest
- State and local taxes
- Charitable contributions
- Medical expenses
- Retirement contributions
- HSA contributions

### Multi-Currency Support
**Purpose:** Support international users with multiple currencies

**Components:**
- **CurrencyController**: Currency management APIs
- **CurrencyService**: Currency operations
- **ExchangeRateService**: Exchange rate management
- **Currency Entity**: Store currency information
- **ExchangeRate Entity**: Store exchange rates

**Features:**
- 20+ supported currencies
- Real-time exchange rates
- Currency conversion
- Multi-currency transactions
- Currency-specific formatting
- Historical exchange rates
- Base currency selection per user

**Supported Currencies:**
- USD, EUR, GBP, JPY, CAD, AUD, CHF, CNY, INR, KRW
- MXN, BRL, RUB, ZAR, SGD, HKD, NOK, SEK, DKK, PLN

### Financial Health Analysis
**Purpose:** Provide comprehensive financial wellness assessment

**Components:**
- **FinancialHealthController**: Health analysis APIs
- **FinancialHealthService**: Health score calculations

**Features:**
- Comprehensive health score (0-100)
- Debt-to-income ratio
- Savings rate analysis
- Emergency fund calculator
- Financial wellness tips
- Trend analysis
- Personalized recommendations
- Benchmark comparisons

**Health Score Factors:**
- Savings rate
- Debt-to-income ratio
- Emergency fund adequacy
- Budget adherence
- Investment diversification
- Retirement readiness

### Scenario Analysis
**Purpose:** Model financial what-if scenarios

**Components:**
- **ScenarioAnalysisController**: Scenario modeling APIs
- **ScenarioAnalysisService**: Scenario calculations

**Features:**
- What-if scenario modeling
- Financial projections
- Goal impact analysis
- Risk assessment
- Sensitivity analysis
- Monte Carlo simulations
- Retirement scenarios
- Major purchase planning

**Scenario Types:**
- Income changes
- Major purchases (home, car)
- Job loss
- Retirement timing
- Investment returns
- Debt payoff strategies

### Real-Time Collaboration
**Purpose:** Enable real-time updates across all features

**Components:**
- **WebSocketConfig**: WebSocket configuration
- **WebSocketService**: Real-time message broadcasting
- **RealTimeController**: Real-time status endpoints

**Features:**
- Real-time transaction updates
- Live budget progress
- Instant notifications
- Multi-device synchronization
- Collaborative budgeting (family/team)
- Live chat support
- Real-time market data

**WebSocket Topics:**
- `/topic/transactions/{userId}`
- `/topic/budgets/{userId}`
- `/topic/notifications/{userId}`
- `/topic/market-data`

## Testing Strategy

### Unit Testing

**Backend (JUnit 5 + Mockito)**
- Service layer: Business logic validation
- Repository layer: Database operations
- Security: JWT generation and validation
- Coverage target: 80%

**Frontend (Jest + React Testing Library)**
- Component rendering and behavior
- Custom hooks
- Service methods
- User interactions
- Coverage target: 75%

**AI Services (JUnit 5)**
- ML model predictions (Java-based)
- Categorization accuracy
- Anomaly detection algorithms
- Coverage target: 80%

**Community Services (JUnit 5)**
- API endpoints
- Database operations
- Business logic
- Coverage target: 80%

### Integration Testing

- API endpoint testing with TestRestTemplate or MockMvc
- Database integration with H2 in-memory database or Testcontainers (MySQL)
- WebSocket connection testing with STOMP test client
- Service layer integration testing

### End-to-End Testing

- User flows with Cypress or Selenium
- Critical paths: Registration → Transaction → Budget → Visualization
- Cross-browser testing: Chrome, Firefox, Safari, Edge
- Mobile responsive testing: iOS Safari, Android Chrome
- React component integration testing

### Performance Testing

- Load testing with JMeter (1000 concurrent users)
- Response time targets: < 1s for API calls, < 3s for dashboard load
- Database query optimization
- Caching strategy validation

## Security Considerations

### Authentication & Authorization

- JWT tokens with 1-hour expiration
- Refresh tokens with 7-day expiration
- Role-based access control (USER, ADMIN)
- Password hashing with BCrypt (strength 12)

### Data Protection

- TLS 1.3 for all communications
- AES-256 encryption for sensitive data at rest
- SQL injection prevention with parameterized queries
- XSS prevention with input sanitization
- CSRF protection with tokens

### API Security

- Rate limiting: 100 requests/minute per user
- CORS configuration for allowed origins
- API key validation for external services
- Request/response logging for audit

### Compliance

- GDPR compliance: Data export, deletion, consent
- PCI DSS considerations for payment data (future)
- Regular security audits
- Vulnerability scanning

## Deployment Architecture

### Development Environment

- Local Spring Boot application (port 8080)
- React development server with hot reload (port 3000)
- Local MySQL database
- Environment-specific configuration files
- Mock external APIs for testing

### Production Environment

- Single server or cloud instance (AWS EC2, DigitalOcean, etc.)
- Spring Boot application as JAR or WAR
- Nginx as reverse proxy and static file server
- MySQL database with automated backups
- SSL/TLS certificates (Let's Encrypt)
- CDN for static assets (optional: CloudFlare)
- Monitoring with Spring Boot Actuator + Prometheus + Grafana
- Logging with Logback/SLF4J to files or ELK stack

### CI/CD Pipeline

1. Code commit triggers build (GitHub Actions)
2. Run backend unit tests (Maven/Gradle)
3. Run frontend unit tests (npm test)
4. Build Spring Boot JAR
5. Build React production bundle
6. Run integration tests
7. Deploy to staging server
8. Run E2E tests (Cypress)
9. Manual approval
10. Deploy to production server
11. Health check validation (Spring Boot Actuator)

## Performance Optimization

### Frontend

- Lazy loading for feature modules
- Virtual scrolling for large lists
- Image optimization and lazy loading
- Service worker for offline capability
- Bundle size optimization (< 500KB initial)

### Backend

- Database connection pooling (HikariCP)
- Query optimization with indexes
- Caching with Redis (session, frequently accessed data)
- Async processing for heavy operations
- Pagination for large datasets

### AI Service

- Model caching in memory
- Batch prediction for multiple users
- Async processing with Celery (optional)
- Result caching for repeated queries

### Database

- Proper indexing on frequently queried columns
- Partitioning for large tables (transactions by date)
- Regular VACUUM and ANALYZE
- Read replicas for reporting queries

## Monitoring and Observability

### Metrics

- Request rate, error rate, latency (RED metrics)
- CPU, memory, disk usage
- Database connection pool stats
- Cache hit/miss rates
- AI model prediction accuracy

### Logging

- Structured logging (JSON format)
- Log levels: DEBUG, INFO, WARN, ERROR
- Correlation IDs for request tracing
- Centralized logging with ELK

### Alerting

- High error rate (> 5%)
- Slow response time (> 3s)
- Service down
- Database connection issues
- Disk space low (< 20%)

## Future Enhancements

1. **Mobile Apps**: Native iOS and Android applications
2. **Bank Integration**: Automatic transaction import via Plaid/Yodlee
3. **Bill Reminders**: Automated payment reminders
4. **Investment Tracking**: Portfolio management features
5. **Multi-currency Support**: International transactions
6. **Family Accounts**: Shared budgets and goals
7. **Advanced AI**: Deep learning models for better predictions
8. **Voice Assistant**: Integration with Alexa/Google Assistant
9. **Gamification**: Achievements and rewards for savings milestones
10. **Financial Education**: In-app tutorials and courses
