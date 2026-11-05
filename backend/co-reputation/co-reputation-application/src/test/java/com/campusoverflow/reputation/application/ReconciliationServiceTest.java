package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReconciliationServiceTest {

    @Mock
    private ReputationAccountRepository accounts;

    @Mock
    private LedgerRepository ledger;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @Test
    void shouldReturnEmptyListWhenAllAccountsMatchLedger() {
        // 修正1：使用 reconstitute 创建对象
        ReputationAccount account = ReputationAccount.reconstitute(1L, 15, 0, 0, 0L);

        when(accounts.findAll()).thenReturn(List.of(account));
        when(ledger.sumByUser()).thenReturn(Map.of(1L, 15L));

        var mismatches = reconciliationService.reconcile();

        assertThat(mismatches).isEmpty();
    }

    @Test
    void shouldReturnMismatchWhenAccountDoesNotMatchLedger() {
        // 修正2：使用 reconstitute 创建对象
        ReputationAccount account = ReputationAccount.reconstitute(1L, 15, 0, 0, 0L);

        when(accounts.findAll()).thenReturn(List.of(account));
        when(ledger.sumByUser()).thenReturn(Map.of(1L, 10L));

        var mismatches = reconciliationService.reconcile();

        assertThat(mismatches).hasSize(1);

        // 修正3：使用 getFirst() 消除 IDEA 警告
        assertThat(mismatches.getFirst().userId()).isEqualTo(1L);
        assertThat(mismatches.getFirst().accountReputation()).isEqualTo(15);
        assertThat(mismatches.getFirst().ledgerSum()).isEqualTo(10L);
    }
}