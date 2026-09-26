package com.cloudsherpa.service.unit.alerts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cloudsherpa.lib.entities.Budget;
import com.cloudsherpa.lib.entities.CurrencyEnum;
import com.cloudsherpa.lib.repositories.BudgetRepository;
import com.cloudsherpa.service.alerts.controller.BudgetsController;
import com.cloudsherpa.service.alerts.dto.BudgetResponse;
import com.cloudsherpa.service.alerts.dto.CreateBudgetRequest;
import com.cloudsherpa.service.alerts.dto.UpdateBudgetRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class BudgetsControllerTest {

  @Mock private BudgetRepository budgetRepository;

  private BudgetsController controller;

  @BeforeEach
  void setUp() {
    controller = new BudgetsController(budgetRepository);
  }

  @Test
  void createBudgetShouldReturnCreatedWhenValidTenantScope() {
    CreateBudgetRequest request = mock(CreateBudgetRequest.class);

    when(request.scope()).thenReturn("TENANT");
    when(request.amount()).thenReturn(new BigDecimal(1000.00));
    when(request.userId()).thenReturn(UUID.randomUUID());

    Budget savedBudget = new Budget();
    savedBudget.setBudgetId(UUID.randomUUID());
    when(budgetRepository.save(any(Budget.class))).thenReturn(savedBudget);

    ResponseEntity<BudgetResponse> response = controller.createBudget(request);

    assertEquals(HttpStatus.CREATED, response.getStatusCode());
    verify(budgetRepository).save(any(Budget.class));
  }

  @Test
  void createBudgetShouldReturnBadRequestForMissingScopeIdOnAccountScope() {
    CreateBudgetRequest request = mock(CreateBudgetRequest.class);
    when(request.scope()).thenReturn("ACCOUNT");
    when(request.scopeId()).thenReturn(null);

    ResponseEntity<BudgetResponse> response = controller.createBudget(request);

    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    verify(budgetRepository, never()).save(any(Budget.class));
  }

  @Test
  void listBudgetsShouldReturnAllWhenScopeIsNull() {
    when(budgetRepository.findAll()).thenReturn(List.of(new Budget()));

    ResponseEntity<List<BudgetResponse>> response = controller.listBudgets(null, null);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(1, response.getBody().size());
  }

  @Test
  void updateBudgetShouldModifySpecifiedFieldsAndReturnOk() {
    UUID budgetId = UUID.randomUUID();
    Budget existingBudget = new Budget();
    existingBudget.setAmount(new BigDecimal(500.00));

    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(existingBudget));
    when(budgetRepository.save(any(Budget.class))).thenReturn(existingBudget);

    UpdateBudgetRequest request = mock(UpdateBudgetRequest.class);
    when(request.amount()).thenReturn(new BigDecimal(750.00));
    when(request.currency()).thenReturn(CurrencyEnum.USD);

    ResponseEntity<BudgetResponse> response = controller.updateBudget(budgetId, request);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ArgumentCaptor<Budget> captor = ArgumentCaptor.forClass(Budget.class);
    verify(budgetRepository).save(captor.capture());
    assertEquals(new BigDecimal(750.00), captor.getValue().getAmount());
  }

  @Test
  void deleteBudgetShouldReturnNoContentWhenFound() {
    UUID budgetId = UUID.randomUUID();
    when(budgetRepository.findById(budgetId)).thenReturn(Optional.of(new Budget()));

    ResponseEntity<Void> response = controller.deleteBudget(budgetId);

    assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    verify(budgetRepository).deleteById(budgetId);
  }
}
