package com.campusoverflow.reputation.application;

import com.campusoverflow.reputation.api.ReputationApi;
import com.campusoverflow.reputation.api.ReputationSummary;
import com.campusoverflow.reputation.domain.ReputationAccount;
import com.campusoverflow.reputation.domain.ReputationAccountRepository;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ReputationApiImpl implements ReputationApi {

    private final ReputationAccountRepository accounts;

    public ReputationApiImpl(ReputationAccountRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public ReputationSummary summary(long userId) {
        ReputationAccount account = accounts.findByUserId(userId)
                .orElseGet(() -> ReputationAccount.reconstitute(userId, 0, 0, 0, 0));
        return new ReputationSummary(userId, account.reputation());
    }

    @Override
    public Map<Long, Integer> reputations(Collection<Long> userIds) {
        Map<Long, Integer> result = new java.util.HashMap<>();
        userIds.forEach(id -> result.put(id, 0));
        accounts.findAllByUserId(userIds).forEach(a -> result.put(a.userId(), a.reputation()));
        return result;
    }
}
