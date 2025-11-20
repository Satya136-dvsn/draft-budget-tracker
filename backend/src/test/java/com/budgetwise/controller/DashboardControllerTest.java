package com.budgetwise.controller;

import com.budgetwise.dto.DashboardSummaryDto;
import com.budgetwise.security.UserPrincipal;
import com.budgetwise.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DashboardService dashboardService;

    @BeforeEach
    public void setup() {
        // Setup UserPrincipal for authentication
        UserPrincipal userPrincipal = new UserPrincipal(
                1L,
                "test@example.com",
                "test@example.com",
                "password",
                "USER",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

        Authentication auth = new UsernamePasswordAuthenticationToken(userPrincipal, null,
                userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    public void testGetDashboardSummary_Success() throws Exception {
        // Mock service response
        DashboardSummaryDto summaryDto = DashboardSummaryDto.builder()
                .totalIncome(BigDecimal.valueOf(5000))
                .totalExpenses(BigDecimal.valueOf(2000))
                .balance(BigDecimal.valueOf(3000))
                .build();

        when(dashboardService.getDashboardSummary(anyLong())).thenReturn(summaryDto);

        // Perform request
        mockMvc.perform(get("/api/dashboard/summary"))
                .andExpect(status().isOk());
    }
}
