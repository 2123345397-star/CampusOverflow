package com.campusoverflow.bootstrap.security;

import com.campusoverflow.identity.application.UserSessionRevoker;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

/**
 * 用 Spring Session 的 principal-name 索引撤销某用户的全部 Redis 会话。
 * 登录时 AuthController 会写入 PRINCIPAL_NAME_INDEX_NAME，索引才可用。
 */
@Component
public class SpringSessionUserSessionRevoker implements UserSessionRevoker {

    private final FindByIndexNameSessionRepository<? extends Session> sessions;

    public SpringSessionUserSessionRevoker(FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.sessions = sessions;
    }

    @Override
    public void revokeAll(String username) {
        sessions.findByPrincipalName(username).keySet().forEach(sessions::deleteById);
    }
}
