package com.budgetwise.service;

import com.budgetwise.dto.ChatResponseDto;
import com.budgetwise.entity.Transaction;
import com.budgetwise.entity.UserProfile;
import com.budgetwise.repository.BudgetRepository;
import com.budgetwise.repository.SavingsGoalRepository;
import com.budgetwise.repository.TransactionRepository;
import com.budgetwise.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatAssistantService {

    private final TransactionRepository transactionRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final UserProfileRepository userProfileRepository;

    public ChatResponseDto chat(String message, String conversationId, Long userId) {
        // Generate conversation ID if not provided
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        // Get user's financial context
        String context = buildFinancialContext(userId);

        // Generate response based on message
        String response = generateResponse(message.toLowerCase(), userId, context);

        return ChatResponseDto.builder()
                .response(response)
                .conversationId(conversationId)
                .context(context)
                .build();
    }

    private String buildFinancialContext(Long userId) {
        StringBuilder context = new StringBuilder();

        // Get current month transactions
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startOfMonth, endOfMonth);

        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int budgetCount = budgetRepository.countByUserId(userId);
        int goalCount = savingsGoalRepository.countByUserId(userId);

        context.append(String.format("Current month: Income $%.2f, Expenses $%.2f, ", 
                totalIncome, totalExpenses));
        context.append(String.format("Balance $%.2f. ", totalIncome.subtract(totalExpenses)));
        context.append(String.format("You have %d budgets and %d savings goals.", budgetCount, goalCount));

        return context.toString();
    }

    private String generateResponse(String message, Long userId, String context) {
        // Simple rule-based responses (in production, integrate with OpenAI API)
        
        if (message.contains("spending") || message.contains("expense")) {
            return generateSpendingResponse(userId);
        } else if (message.contains("saving") || message.contains("save")) {
            return generateSavingsResponse(userId);
        } else if (message.contains("budget")) {
            return generateBudgetResponse(userId);
        } else if (message.contains("goal")) {
            return generateGoalResponse(userId);
        } else if (message.contains("help") || message.contains("what can you do")) {
            return "I can help you with:\n" +
                    "• Analyzing your spending patterns\n" +
                    "• Tracking your savings goals\n" +
                    "• Managing your budgets\n" +
                    "• Providing financial advice\n" +
                    "• Detecting unusual transactions\n\n" +
                    "Try asking: 'How is my spending?' or 'Am I saving enough?'";
        } else {
            return "I'm your financial assistant! " + context + "\n\n" +
                    "Ask me about your spending, savings, budgets, or goals. " +
                    "For example: 'How much did I spend this month?' or 'Am I on track with my savings?'";
        }
    }

    private String generateSpendingResponse(Long userId) {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDate endOfMonth = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(
                userId, startOfMonth, endOfMonth);

        BigDecimal totalExpenses = transactions.stream()
                .filter(t -> t.getType() == Transaction.TransactionType.EXPENSE)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalExpenses.compareTo(BigDecimal.ZERO) == 0) {
            return "You haven't recorded any expenses this month yet. Start tracking your spending to get insights!";
        }

        return String.format("This month, you've spent $%.2f across %d transactions. " +
                "Would you like me to break this down by category?",
                totalExpenses, transactions.size());
    }

    private String generateSavingsResponse(Long userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        int goalCount = savingsGoalRepository.countByUserId(userId);

        if (goalCount == 0) {
            return "You don't have any savings goals set up yet. " +
                    "Setting goals can help you stay motivated! Would you like to create one?";
        }

        return String.format("You have %d active savings goals. " +
                "Keep up the good work! Regular contributions, even small ones, add up over time.",
                goalCount);
    }

    private String generateBudgetResponse(Long userId) {
        int budgetCount = budgetRepository.countByUserId(userId);

        if (budgetCount == 0) {
            return "You haven't set up any budgets yet. " +
                    "Budgets help you control spending and reach your financial goals. " +
                    "Would you like help creating one?";
        }

        return String.format("You have %d active budgets. " +
                "Budgets are a great way to stay on track. " +
                "Check your dashboard to see how you're doing!",
                budgetCount);
    }

    private String generateGoalResponse(Long userId) {
        int goalCount = savingsGoalRepository.countByUserId(userId);

        if (goalCount == 0) {
            return "You don't have any savings goals yet. " +
                    "Setting specific goals makes saving easier and more rewarding. " +
                    "What would you like to save for?";
        }

        return String.format("You have %d savings goals. " +
                "Stay focused and consistent - you're building a better financial future!",
                goalCount);
    }
}
