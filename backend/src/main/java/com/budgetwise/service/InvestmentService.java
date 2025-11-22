package com.budgetwise.service;

import com.budgetwise.dto.InvestmentDto;
import com.budgetwise.dto.PortfolioSummaryDto;
import com.budgetwise.entity.Investment;
import com.budgetwise.exception.ResourceNotFoundException;
import com.budgetwise.repository.InvestmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    @Transactional
    public InvestmentDto createInvestment(InvestmentDto dto, Long userId) {
        Investment investment = new Investment();
        investment.setUserId(userId);
        investment.setName(dto.getName());
        investment.setType(dto.getType());
        investment.setQuantity(dto.getQuantity());
        investment.setBuyPrice(dto.getBuyPrice());
        investment.setCurrentPrice(dto.getCurrentPrice() != null ? dto.getCurrentPrice() : dto.getBuyPrice());
        investment.setPurchaseDate(dto.getPurchaseDate());
        investment.setSymbol(dto.getSymbol());
        investment.setNotes(dto.getNotes());

        Investment saved = investmentRepository.save(investment);
        return mapToDto(saved);
    }

    public List<InvestmentDto> getAllInvestments(Long userId) {
        return investmentRepository.findByUserIdOrderByPurchaseDateDesc(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public InvestmentDto getInvestmentById(Long id, Long userId) {
        Investment investment = investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        return mapToDto(investment);
    }

    @Transactional
    public InvestmentDto updateInvestment(Long id, InvestmentDto dto, Long userId) {
        Investment investment = investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        investment.setName(dto.getName());
        investment.setType(dto.getType());
        investment.setQuantity(dto.getQuantity());
        investment.setBuyPrice(dto.getBuyPrice());
        if (dto.getCurrentPrice() != null) {
            investment.setCurrentPrice(dto.getCurrentPrice());
        }
        investment.setPurchaseDate(dto.getPurchaseDate());
        investment.setSymbol(dto.getSymbol());
        investment.setNotes(dto.getNotes());

        Investment updated = investmentRepository.save(investment);
        return mapToDto(updated);
    }

    @Transactional
    public InvestmentDto updateCurrentPrice(Long id, BigDecimal currentPrice, Long userId) {
        Investment investment = investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));

        investment.setCurrentPrice(currentPrice);
        Investment updated = investmentRepository.save(investment);
        return mapToDto(updated);
    }

    @Transactional
    public void deleteInvestment(Long id, Long userId) {
        Investment investment = investmentRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found"));
        investmentRepository.delete(investment);
    }

    public PortfolioSummaryDto getPortfolioSummary(Long userId) {
        List<Investment> investments = investmentRepository.findByUserIdOrderByPurchaseDateDesc(userId);

        if (investments.isEmpty()) {
            return PortfolioSummaryDto.builder()
                    .totalInvested(BigDecimal.ZERO)
                    .currentValue(BigDecimal.ZERO)
                    .totalProfitLoss(BigDecimal.ZERO)
                    .totalProfitLossPercent(BigDecimal.ZERO)
                    .totalInvestments(0)
                    .assetAllocation(new HashMap<>())
                    .profitableInvestments(0)
                    .losingInvestments(0)
                    .build();
        }

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal currentValue = BigDecimal.ZERO;
        Map<String, BigDecimal> typeValues = new HashMap<>();
        int profitable = 0;
        int losing = 0;

        for (Investment inv : investments) {
            BigDecimal invested = inv.getBuyPrice().multiply(inv.getQuantity());
            BigDecimal current = (inv.getCurrentPrice() != null ? inv.getCurrentPrice() : inv.getBuyPrice())
                    .multiply(inv.getQuantity());

            totalInvested = totalInvested.add(invested);
            currentValue = currentValue.add(current);

            // Asset allocation by type
            String type = inv.getType().name();
            typeValues.put(type, typeValues.getOrDefault(type, BigDecimal.ZERO).add(current));

            // Count profitable vs losing
            if (current.compareTo(invested) > 0) {
                profitable++;
            } else if (current.compareTo(invested) < 0) {
                losing++;
            }
        }

        BigDecimal totalPL = currentValue.subtract(totalInvested);
        BigDecimal totalPLPercent = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            totalPLPercent = totalPL.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        // Calculate asset allocation percentages
        Map<String, BigDecimal> assetAllocation = new HashMap<>();
        if (currentValue.compareTo(BigDecimal.ZERO) > 0) {
            for (Map.Entry<String, BigDecimal> entry : typeValues.entrySet()) {
                BigDecimal percentage = entry.getValue()
                        .divide(currentValue, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                assetAllocation.put(entry.getKey(), percentage);
            }
        }

        return PortfolioSummaryDto.builder()
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .currentValue(currentValue.setScale(2, RoundingMode.HALF_UP))
                .totalProfitLoss(totalPL.setScale(2, RoundingMode.HALF_UP))
                .totalProfitLossPercent(totalPLPercent)
                .totalInvestments(investments.size())
                .assetAllocation(assetAllocation)
                .profitableInvestments(profitable)
                .losingInvestments(losing)
                .build();
    }

    private InvestmentDto mapToDto(Investment investment) {
        BigDecimal totalInvested = investment.getBuyPrice().multiply(investment.getQuantity());
        BigDecimal currentPrice = investment.getCurrentPrice() != null ? investment.getCurrentPrice()
                : investment.getBuyPrice();
        BigDecimal currentValue = currentPrice.multiply(investment.getQuantity());
        BigDecimal profitLoss = currentValue.subtract(totalInvested);

        BigDecimal profitLossPercent = BigDecimal.ZERO;
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            profitLossPercent = profitLoss.divide(totalInvested, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return InvestmentDto.builder()
                .id(investment.getId())
                .name(investment.getName())
                .type(investment.getType())
                .quantity(investment.getQuantity())
                .buyPrice(investment.getBuyPrice())
                .currentPrice(investment.getCurrentPrice())
                .purchaseDate(investment.getPurchaseDate())
                .symbol(investment.getSymbol())
                .notes(investment.getNotes())
                .createdAt(investment.getCreatedAt())
                .updatedAt(investment.getUpdatedAt())
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .currentValue(currentValue.setScale(2, RoundingMode.HALF_UP))
                .profitLoss(profitLoss.setScale(2, RoundingMode.HALF_UP))
                .profitLossPercent(profitLossPercent)
                .build();
    }
}
