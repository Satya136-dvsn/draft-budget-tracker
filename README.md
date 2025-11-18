# BudgetWise - AI-Driven Personal Finance Tracker

A comprehensive full-stack personal finance management application with AI-powered features, built with Spring Boot and React.

![BudgetWise](https://img.shields.io/badge/version-1.0.0-blue)
![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-green)
![React](https://img.shields.io/badge/React-18.2.0-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)

## 🌟 Features

### ✅ Completed (Phase 1-2)

#### Authentication & User Management
- User registration and login with JWT authentication
- Secure password encryption
- Role-based access control (USER, ADMIN)
- User profile management

#### Financial Tracking
- **Transactions**: Create, read, update, delete income and expenses
- **Categories**: System and custom categories with emoji icons
- **Budgets**: Set monthly budgets per category with progress tracking
- **Savings Goals**: Track savings goals with target amounts and deadlines

#### Dashboard & Analytics
- Real-time financial summary (income, expenses, balance)
- Category-wise spending breakdown
- Monthly trends and statistics
- Budget vs actual spending comparison

#### AI-Powered Features
- **Smart Categorization**: AI suggests categories for transactions
- **Spending Predictions**: ML-based future spending forecasts
- **Budget Advisor**: Personalized budget recommendations
- **Anomaly Detection**: Identifies unusual spending patterns
- **Chat Assistant**: Natural language financial queries

#### Advanced Features
- **Data Export**: Export transactions to CSV, Excel, PDF
- **Community Forum**: Discussion posts, comments, likes
- **Admin Dashboard**: User management, system statistics, audit logs
- **WebSocket Notifications**: Real-time updates
- **Caching**: Redis-based performance optimization

### ✅ Completed (Phase 3 - Frontend UI)

#### Frontend UI (React + Material-UI)
- ✅ **Batch 1**: Authentication UI (Login, Register, Protected Routes)
- ✅ **Batch 2**: Transactions & Categories Management
- ✅ **Batch 3**: Budgets & Goals UI
- 🔄 **Batch 4**: Dashboard & Charts (In Progress)
- 📋 **Batch 5**: AI Features UI (Planned)
- 📋 **Batch 6**: Forum & Admin UI (Planned)
- 📋 **Batch 7**: Polish & Deploy (Planned)

## 🏗️ Architecture

### Backend (Spring Boot)
```
backend/
├── src/main/java/com/budgetwise/
│   ├── controller/      # REST API endpoints
│   ├── service/         # Business logic
│   ├── repository/      # Data access layer
│   ├── entity/          # JPA entities
│   ├── dto/             # Data transfer objects
│   ├── security/        # JWT & authentication
│   ├── config/          # Configuration classes
│   └── exception/       # Exception handling
└── src/main/resources/
    ├── application.properties
    └── application-secrets.properties
```

### Frontend (React)
```
frontend/
├── src/
│   ├── components/      # Reusable UI components
│   ├── pages/           # Page components
│   ├── services/        # API service layer
│   ├── context/         # React context (Auth)
│   ├── routes/          # Route configuration
│   ├── theme/           # Material-UI theme
│   └── utils/           # Utility functions
└── public/
```

### Database (PostgreSQL)
```
database/
├── init.sql                    # Schema creation
└── insert_test_audit_logs.sql  # Sample data
```

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Node.js 18** or higher
- **PostgreSQL 15** or higher
- **Maven 3.8+**
- **Redis** (optional, for caching)
- **OpenAI API Key** (for AI features)

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/Satya136-dvsn/draft-budget-tracker.git
cd draft-budget-tracker
```

#### 2. Database Setup
```bash
# Create PostgreSQL database
psql -U postgres
CREATE DATABASE budgetwise;
\q

# Run initialization script
psql -U postgres -d budgetwise -f database/init.sql
```

#### 3. Backend Configuration
```bash
cd backend

# Create application-secrets.properties
cat > src/main/resources/application-secrets.properties << EOF
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/budgetwise
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT Secret (generate a secure random string)
jwt.secret=your_jwt_secret_key_here_minimum_256_bits

# OpenAI API Key (for AI features)
openai.api.key=your_openai_api_key_here

# Redis (optional)
spring.data.redis.host=localhost
spring.data.redis.port=6379
EOF

# Install dependencies and run
mvn clean install
mvn spring-boot:run
```

Backend will start on: **http://localhost:8080**

#### 4. Frontend Setup
```bash
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

Frontend will start on: **http://localhost:3000**

### Quick Start

1. **Register**: Go to http://localhost:3000/register
2. **Login**: Use your credentials
3. **Add Categories**: Create custom categories
4. **Add Transactions**: Track your income and expenses
5. **Set Budgets**: Create monthly budgets
6. **Explore AI Features**: Try smart categorization and predictions

## 📚 API Documentation

### Authentication
```
POST /api/auth/register    - Register new user
POST /api/auth/login       - Login user
```

### Transactions
```
GET    /api/transactions              - Get all transactions
POST   /api/transactions              - Create transaction
GET    /api/transactions/{id}         - Get transaction by ID
PUT    /api/transactions/{id}         - Update transaction
DELETE /api/transactions/{id}         - Delete transaction
GET    /api/transactions/recent       - Get recent transactions
```

### Categories
```
GET    /api/categories                - Get all categories
POST   /api/categories                - Create category
PUT    /api/categories/{id}           - Update category
DELETE /api/categories/{id}           - Delete category
GET    /api/categories/user           - Get user categories
```

### Budgets
```
GET    /api/budgets                   - Get all budgets
POST   /api/budgets                   - Create budget
PUT    /api/budgets/{id}              - Update budget
DELETE /api/budgets/{id}              - Delete budget
GET    /api/budgets/current           - Get current month budgets
```

### Savings Goals
```
GET    /api/savings-goals             - Get all savings goals
POST   /api/savings-goals             - Create savings goal
PUT    /api/savings-goals/{id}        - Update savings goal
DELETE /api/savings-goals/{id}        - Delete savings goal
POST   /api/savings-goals/{id}/contribute - Add contribution
```

### AI Features
```
POST   /api/ai/categorize             - Suggest category for transaction
POST   /api/ai/predict                - Predict future spending
GET    /api/ai/advisor                - Get budget recommendations
GET    /api/ai/anomalies              - Detect spending anomalies
POST   /api/chat/ask                  - Chat with AI assistant
```

### Dashboard
```
GET    /api/dashboard/summary         - Get financial summary
GET    /api/dashboard/breakdown       - Get category breakdown
GET    /api/dashboard/trends          - Get spending trends
```

## 🔧 Configuration

### Environment Variables

**Backend** (`application-secrets.properties`):
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/budgetwise
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT
jwt.secret=your_secret_key
jwt.expiration=86400000

# OpenAI
openai.api.key=sk-your-key-here

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

**Frontend** (`.env`):
```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## 🧪 Testing

### Backend Tests
```bash
cd backend
mvn test
```

### Frontend Tests
```bash
cd frontend
npm test
```

### API Testing with Postman
Import the Postman collection from `docs/postman/` directory.

## 📦 Deployment

### Backend (Spring Boot)
```bash
cd backend
mvn clean package
java -jar target/budgetwise-backend-1.0.0.jar
```

### Frontend (React)
```bash
cd frontend
npm run build
# Deploy the 'dist' folder to your hosting service
```

### Docker (Coming Soon)
```bash
docker-compose up
```

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL 15
- **Cache**: Redis
- **Security**: Spring Security + JWT
- **ORM**: Spring Data JPA
- **WebSocket**: STOMP
- **AI**: OpenAI GPT-4

### Frontend
- **Framework**: React 18.2.0
- **UI Library**: Material-UI (MUI)
- **Routing**: React Router v6
- **State Management**: React Context API
- **HTTP Client**: Axios
- **Charts**: Recharts
- **Animations**: Framer Motion
- **Build Tool**: Vite

### Database Schema
- Users & Authentication
- Transactions & Categories
- Budgets & Savings Goals
- Forum (Posts, Comments, Likes)
- Audit Logs
- System Categories

## 📊 Project Status

### Completed Features
- ✅ Backend API (100%)
- ✅ Database Schema (100%)
- ✅ Authentication System (100%)
- ✅ Transaction Management (100%)
- ✅ Budget & Goals (100%)
- ✅ AI Features (100%)
- ✅ Admin Dashboard (100%)
- ✅ WebSocket Support (100%)
- ✅ Frontend Auth UI (100%)
- ✅ Frontend Transactions UI (100%)
- ✅ Frontend Categories UI (100%)
- ✅ Frontend Budgets UI (100%)
- ✅ Frontend Savings Goals UI (100%)

### In Progress
- 🔄 Frontend Dashboard & Charts (50%)

### Planned
- 📋 Frontend AI Features UI
- 📋 Frontend Forum UI
- 📋 Frontend Admin UI

### Planned
- 📋 Mobile App (React Native)
- 📋 Docker Deployment
- 📋 CI/CD Pipeline
- 📋 Unit Tests (Backend)
- 📋 E2E Tests (Frontend)

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Satya Dvsn**
- GitHub: [@Satya136-dvsn](https://github.com/Satya136-dvsn)

## 🙏 Acknowledgments

- Spring Boot Team
- React Team
- Material-UI Team
- OpenAI for GPT-4 API
- All open-source contributors

## 📞 Support

For support, email your-email@example.com or open an issue on GitHub.

## 🗺️ Roadmap

### Version 1.1 (Q1 2026)
- Complete frontend UI
- Mobile responsive design
- Dark mode support
- Multi-currency support

### Version 2.0 (Q2 2026)
- Mobile app (iOS & Android)
- Bank account integration
- Receipt scanning (OCR)
- Investment tracking

### Version 3.0 (Q3 2026)
- Multi-user households
- Shared budgets
- Bill reminders
- Financial reports

---

**⭐ Star this repo if you find it helpful!**
