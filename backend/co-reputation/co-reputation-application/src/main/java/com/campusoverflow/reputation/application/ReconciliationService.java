package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.domain.LedgerRepository;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 适应度函数 FF-8：对账——每个账户的声誉必须等于其流水之和。 */
@Service
public class ReconciliationService {

    public record Mismatch(long userId, int accountReputation, long ledgerSum) {
    }

    private final ReputationAccountRepository accounts;
    private final LedgerRepository ledger;

    public ReconciliationService(ReputationAccountRepository accounts, LedgerRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Transactional(readOnly = true)
    public List<Mismatch> reconcile() {
        List<Mismatch> mismatches = new java.util.ArrayList<>();

        // 1. 查出所有账户
        List<ReputationAccount> allAccounts = accounts.findAll();

        // 2. 一次性查出所有用户的流水汇总 (不需要传参数)
        Map<Long, Long> ledgerSums = ledger.sumByUser();

        for (ReputationAccount account : allAccounts) {
            // 3. 从 Map 里拿出该用户的流水总和，如果没有流水默认是 0
            long ledgerSum = ledgerSums.getOrDefault(account.userId(), 0L);

            // 4. 对比账户余额与流水总和
            if (account.reputation() != ledgerSum) {
                // 如果对不上，记录一条异常
                mismatches.add(new Mismatch(account.userId(), account.reputation(), ledgerSum));
            }
        }

        return mismatches;
    }}