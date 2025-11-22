# BudgetWise Tracker - Dashboard + Analytics + AI Features

## Detailed Technical Explanation for Mentor Presentation

---

## 📊 1. Dashboard Summary (Income, Expense, Balance)

### Overview

The dashboard provides a real-time financial overview displaying total income, expenses, current balance, and savings rate for the current month.

### Frontend Implementation

**File:** `c:\budgetwise tracker\frontend\src\pages\Dashboard.jsx`

```javascript
// Dashboard Summary Cards (Lines 80-100)
<Grid container spacing={3}>
  <Grid item xs={12} sm={6} md={3}>
    <StatCard 
      title="Total Income" 
      value={summary?.totalIncome ?? 0} 
      subtitle="This Month" 
      icon={<IncomeIcon />} 
      color="success" 
    />
  </Grid>
  <Grid item xs={12} sm={6} md={3}>
    <StatCard 
      title="Total Expenses" 
      value={summary?.totalExpenses ?? 0} 
      subtitle="This Month" 
      icon={<ExpenseIcon />} 
      color="error" 
    />
  </Grid>
  <Grid item xs={12} sm={6} md={3}>
    <StatCard 
      title="Current Balance" 
      value={summary?.balance ?? 0} 
      subtitle="Available Now" 
      icon={<BalanceIcon />} 
      color="primary" 
    />
  </Grid>
  <Grid item xs={12} sm={6} md={3}>
    <StatCard 
      title="Savings Rate" 
      value={summary?.savingsRate != null ? `${summary.savingsRate.toFixed(1)}%` : '--'} 
      subtitle="Of Total Income" 
      icon={<SavingsIcon />} 
      color="secondary" 
    />
  </Grid>
</Grid>
```

### Backend Implementation

**File:** `c:\budgetwise tracker\backend\src\main\java\com\budgetwise\service\DashboardService.java`

```java
// Calculate Dashboard Summary (Lines 32-68)
@Cacheable(value = "dashboard_summary", key = "#userId")
public DashboardSummaryDto getDashboardSummary(Long userId) {
    // Get current month's date range
    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

    // Fetch all transactions for current month
    List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
        userId, startOfMonth, endOfMonth);

    // Calculate total income
    BigDecimal totalIncome = transactions.stream()
        .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calculate total expenses
    BigDecimal totalExpenses = transactions.stream()
        .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
        .map(Transaction::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calculate balance (income - expenses)
    BigDecimal balance = totalIncome.subtract(totalExpenses);

    // Calculate savings rate percentage
    Double savingsRate = totalIncome.compareTo(BigDecimal.ZERO) > 0
        ? balance.divide(totalIncome, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue()
        : 0.0;

    return DashboardSummaryDto.builder()
        .totalIncome(totalIncome)
        .totalExpenses(totalExpenses)
        .balance(balance)
        .savingsRate(savingsRate)
        .transactionCount(transactions.size())
        .budgetCount(budgetRepository.countByUserId(userId))
        .goalCount(savingsGoalRepository.countByUserId(userId))
        .build();
}
```

**API Endpoint:** `c:\budgetwise tracker\backend\src\main\java\com\budgetwise\controller\DashboardController.java`

```java
@GetMapping("/summary")
public ResponseEntity<DashboardSummaryDto> getDashboardSummary(
        @AuthenticationPrincipal UserPrincipal userPrincipal) {
    DashboardSummaryDto summary = dashboardService.getDashboardSummary(userPrincipal.getId());
    return ResponseEntity.ok(summary);
}
```

### How It Works

1. **Data Fetching:** Frontend calls `/api/dashboard/summary` endpoint
2. **Backend Processing:**
   - Filters transactions for current month
   - Separates income vs expenses using stream filtering
   - Calculates balance (income - expenses)
   - Computes savings rate as percentage: `(balance / income) × 100`
3. **Caching:** Uses `@Cacheable` to improve performance
4. **Display:** Shows 4 stat cards with animated fade-in effects

---

## 📈 2. Financial Charts (Pie Chart + Bar Chart)

### A. Pie Chart - Category Breakdown

**Frontend File:** `c:\budgetwise tracker\frontend\src\pages\Dashboard.jsx` (Lines 132-190)

```javascript
// Pie Chart for Spending by Category
<PieChart>
  <Pie
    data={categoryBreakdown}
    dataKey="amount"
    nameKey="categoryName"
    cx="50%"
    cy="50%"
    outerRadius={100}
    paddingAngle={2}
  >
    {categoryBreakdown.map((entry, index) => (
      <Cell
        key={entry.categoryId}
        fill={getCategoryColor(entry.categoryId, index)}
        stroke="#1a1a1a"
        strokeWidth={2}
      />
    ))}
  </Pie>
  <Tooltip
    content={({ active, payload }) => {
      if (active && payload && payload.length) {
        const data = payload[0].payload;
        return (
          <Box>
            <Typography>{data.categoryName}</Typography>
            <Typography>{formatCurrency(data.amount)}</Typography>
            <Typography>{data.percentage?.toFixed(1)}% of total</Typography>
          </Box>
        );
      }
      return null;
    }}
  />
</PieChart>
```

**Backend Implementation:**

```java
// Lines 110-166 in DashboardService.java
@Cacheable(value = "dashboard_breakdown", key = "#userId")
public List<CategoryBreakdownDto> getCategoryBreakdown(Long userId) {
    // Get current month transactions
    LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
    LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    
    List<Transaction> expenses = transactionRepository.findByUserIdAndTransactionDateBetween(
        userId, startOfMonth, endOfMonth).stream()
        .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
        .collect(Collectors.toList());

    // Group expenses by category
    Map<Long, List<Transaction>> groupedByCategory = expenses.stream()
        .filter(t -> t.getCategoryId() != null)
        .collect(Collectors.groupingBy(Transaction::getCategoryId));

    // Calculate amount and percentage for each category
    groupedByCategory.forEach((categoryId, categoryTransactions) -> {
        BigDecimal categoryAmount = categoryTransactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        Double percentage = (categoryAmount / totalExpenses) * 100;
        
        breakdown.add(CategoryBreakdownDto.builder()
            .categoryId(categoryId)
            .categoryName(categoryNames.get(categoryId))
            .amount(categoryAmount)
            .percentage(percentage)
            .transactionCount(categoryTransactions.size())
            .build());
    });
    
    return breakdown;
}
```

### B. Line Chart - Monthly Trends

**Frontend File:** `c:\budgetwise tracker\frontend\src\pages\Dashboard.jsx` (Lines 104-130)

```javascript
// Line Chart showing Income vs Expenses over 6 months
<LineChart data={monthlyTrends}>
  <CartesianGrid strokeDasharray="3 3" />
  <XAxis dataKey="month" />
  <YAxis />
  <Tooltip formatter={(value) => formatCurrency(value)} />
  <Legend />
  <Line 
    type="monotone" 
    dataKey="income" 
    name="Income" 
    stroke="#4CAF50" 
    strokeWidth={3} 
  />
  <Line 
    type="monotone" 
    dataKey="expenses" 
    name="Expenses" 
    stroke="#F44336" 
    strokeWidth={3} 
  />
</LineChart>
```

**Backend Implementation:**

```java
// Lines 71-108 in DashboardService.java
@Cacheable(value = "dashboard_trends", key = "#userId")
public List<MonthlyTrendDto> getMonthlyTrends(Long userId, Integer months) {
    List<MonthlyTrendDto> trends = new ArrayList<>();
    LocalDate endDate = LocalDate.now();

    for (int i = months - 1; i >= 0; i--) {
        YearMonth yearMonth = YearMonth.from(endDate.minusMonths(i));
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        List<Transaction> transactions = transactionRepository
            .findByUserIdAndTransactionDateBetween(userId, startOfMonth, endOfMonth);

        BigDecimal income = transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal expenses = transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        trends.add(MonthlyTrendDto.builder()
            .month(startOfMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")))
            .income(income)
            .expenses(expenses)
            .netSavings(income.subtract(expenses))
            .build());
    }
    return trends;
}
```

---

## 📊 3. Monthly and Category-wise Analytics

### Analytics Page

**File:** `c:\budgetwise tracker\frontend\src\pages\Analytics.jsx`

This page provides deeper analytics with three main charts:

### A. Trend Analysis Chart (Area Chart)

**Component File:** `c:\budgetwise tracker\frontend\src\components\analytics\TrendAnalysisChart.jsx`

```javascript
<AreaChart data={data}>
  <defs>
    <linearGradient id="colorIncome" x1="0" y1="0" x2="0" y2="1">
      <stop offset="5%" stopColor="#4CAF50" stopOpacity={0.3} />
      <stop offset="95%" stopColor="#4CAF50" stopOpacity={0} />
    </linearGradient>
    <linearGradient id="colorExpenses" x1="0" y1="0" x2="0" y2="1">
      <stop offset="5%" stopColor="#F44336" stopOpacity={0.3} />
      <stop offset="95%" stopColor="#F44336" stopOpacity={0} />
    </linearGradient>
  </defs>
  <Area 
    type="monotone" 
    dataKey="income" 
    name="Income" 
    stroke="#4CAF50" 
    fill="url(#colorIncome)" 
  />
  <Area 
    type="monotone" 
    dataKey="expenses" 
    name="Expenses" 
    stroke="#F44336" 
    fill="url(#colorExpenses)" 
  />
</AreaChart>
```

**Features:**

- Gradient fill for visual appeal
- Shows income vs expenses trends over time
- Time range selector (3 months, 6 months, 1 year)

### B. Cash Flow Chart (Bar Chart)

**Component File:** `c:\budgetwise tracker\frontend\src\components\analytics\CashFlowChart.jsx`

```javascript
<BarChart data={data}>
  <CartesianGrid strokeDasharray="3 3" />
  <XAxis dataKey="month" />
  <YAxis tickFormatter={(value) => `₹${value / 1000}k`} />
  <Tooltip />
  <Legend />
  <ReferenceLine y={0} stroke="#000" />
  <Bar 
    dataKey="netSavings" 
    name="Net Cash Flow" 
    fill="#1976d2" 
    radius={[4, 4, 0, 0]} 
  />
</BarChart>
```

**Features:**

- Shows net cash flow (income - expenses) per month
- Reference line at zero to distinguish positive/negative flow
- Rounded bar corners for modern look

---

## 🤖 4. AI Chatbot

### Frontend Implementation

**File:** `c:\budgetwise tracker\frontend\src\pages\AIChat.jsx`

```javascript
const handleSend = async () => {
    // Add user message
    const userMessage = {
        role: 'user',
        content: input,
        timestamp: new Date(),
    };
    setMessages((prev) => [...prev, userMessage]);

    try {
        // Call backend API
        const response = await chatService.sendMessage(input, conversationId);

        // Store conversation ID for context
        if (response.data.conversationId && !conversationId) {
            setConversationId(response.data.conversationId);
        }

        // Add AI response
        const assistantMessage = {
            role: 'assistant',
            content: response.data.message,
            timestamp: new Date(),
        };
        setMessages((prev) => [...prev, assistantMessage]);
    } catch (error) {
        console.error('Chat error:', error);
    }
};
```

### Backend Implementation

**File:** `c:\budgetwise tracker\backend\src\main\java\com\budgetwise\service\OpenAIService.java`

```java
public String getFinancialAdvice(String userContext, String question) {
    try {
        String prompt = buildPrompt(userContext, question);
        
        // Build OpenAI API request
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4");
        requestBody.addProperty("prompt", prompt);
        requestBody.addProperty("max_tokens", 500);
        requestBody.addProperty("temperature", 0.7);
        
        Request request = new Request.Builder()
            .url(apiConfig.getOpenaiApiUrl() + "/completions")
            .addHeader("Authorization", "Bearer " + apiConfig.getOpenaiApiKey())
            .post(RequestBody.create(gson.toJson(requestBody)))
            .build();
        
        // Execute and parse response
        Response response = httpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            JsonObject json = gson.fromJson(response.body().string());
            return json.getAsJsonArray("choices")
                .get(0).getAsJsonObject()
                .get("text").getAsString();
        }
    } catch (IOException e) {
        return getFallbackAdvice(question);
    }
}

private String buildPrompt(String userContext, String question) {
    return "You are a financial advisor. User data:\n" +
        userContext + "\n\n" +
        "Question: " + question + "\n\n" +
        "Provide helpful financial advice:";
}

private String getFallbackAdvice(String question) {
    if (question.toLowerCase().contains("save")) {
        return "Follow the 50/30/20 rule: 50% needs, 30% wants, 20% savings.";
    } else if (question.toLowerCase().contains("budget")) {
        return "Track expenses and set realistic category budgets.";
    }
    return "Review spending and reduce unnecessary expenses.";
}
```

**Features:**

- Context-aware responses using conversation ID
- User financial data injected into prompts
- Fallback system when API unavailable
- Quick suggestion chips for common questions

---

## 🔮 5. AI Expense Prediction

### Backend Implementation

**File:** `c:\budgetwise tracker\backend\src\main\java\com\budgetwise\service\PredictionService.java`

```java
@Cacheable(value = "predictions", key = "#userId")
public List<PredictionDto> predictNextMonthExpenses(Long userId) {
    // Get last 6 months of data
    LocalDate startDate = LocalDate.now().minusMonths(6);
    
    List<Transaction> expenses = transactionRepository
        .findByUserIdAndTransactionDateBetween(userId, startDate, LocalDate.now())
        .stream()
        .filter(t -> t.getType() == EXPENSE)
        .collect(Collectors.toList());

    // Group by category
    Map<Long, List<Transaction>> byCategory = expenses.stream()
        .collect(Collectors.groupingBy(Transaction::getCategoryId));

    List<PredictionDto> predictions = new ArrayList<>();
    byCategory.forEach((categoryId, transactions) -> {
        if (transactions.size() >= 3) {  // Need minimum data points
            predictions.add(predictForCategory(categoryId, transactions));
        }
    });
    
    return predictions;
}

private PredictionDto predictForCategory(Long categoryId, List<Transaction> transactions) {
    // Calculate monthly totals
    Map<String, BigDecimal> monthlyTotals = transactions.stream()
        .collect(Collectors.groupingBy(
            t -> t.getDate().getYear() + "-" + t.getDate().getMonth(),
            Collectors.reducing(ZERO, Transaction::getAmount, BigDecimal::add)
        ));

    // Linear regression analysis
    SimpleRegression regression = new SimpleRegression();
    List<BigDecimal> amounts = new ArrayList<>(monthlyTotals.values());
    
    for (int i = 0; i < amounts.size(); i++) {
        regression.addData(i, amounts.get(i).doubleValue());
    }

    // Predict next month
    double predicted = regression.predict(amounts.size());
    BigDecimal predictedAmount = BigDecimal.valueOf(Math.max(0, predicted));

    // Calculate confidence (R-squared)
    double rSquared = regression.getRSquare();
    Double confidence = Math.min(100, rSquared * 100);

    // Determine trend
    String trend = determineTrend(amounts);

    return PredictionDto.builder()
        .categoryId(categoryId)
        .predictedAmount(predictedAmount)
        .confidenceScore(confidence)
        .trend(trend)
        .build();
}

private String determineTrend(List<BigDecimal> amounts) {
    BigDecimal first = amounts.get(0);
    BigDecimal last = amounts.get(amounts.size() - 1);
    BigDecimal percentChange = (last - first) / first * 100;
    
    if (percentChange > 10) return "INCREASING";
    else if (percentChange < -10) return "DECREASING";
    else return "STABLE";
}
```

### How Prediction Works

1. **Data Collection:** Last 6 months of expenses grouped by category
2. **Linear Regression:** Uses Apache Commons Math library
   - `y = mx + b` where x = month, y = amount
3. **Prediction:** Next month = `m * (n+1) + b`
4. **Confidence Score:** Based on R² (0-100%)
   - R² = 1.0 → 100% confidence (perfect fit)
   - R² = 0.5 → 50% confidence
5. **Trend Detection:**
   - >10% increase → INCREASING
   - <-10% decrease → DECREASING
   - Otherwise → STABLE

---

## 🔄 Data Flow

```
Frontend (React)
    ↓
API Services (dashboardService.js, chatService.js)
    ↓
REST Controllers (DashboardController, AIController)
    ↓
Business Logic (DashboardService, PredictionService, OpenAIService)
    ↓
Data Layer (TransactionRepository, CategoryRepository)
    ↓
Database (PostgreSQL)
```

---

## 🎯 Technologies

**Frontend:**

- React - UI framework
- Recharts - Charts library
- Material-UI - Components
- Axios - HTTP client

**Backend:**

- Spring Boot - Framework
- Apache Commons Math - Statistics
- OkHttp - HTTP client
- Spring Cache - Performance

**AI:**

- OpenAI GPT-4 - Chatbot
- Linear Regression - Predictions

---

## 📝 Summary

✅ **Dashboard:** Real-time income, expenses, balance, savings rate  
✅ **Charts:** Pie, line, area, bar charts  
✅ **Analytics:** Monthly trends, category breakdown, cash flow  
✅ **AI Chat:** Context-aware financial advice  
✅ **Predictions:** ML-based expense forecasting
