package com.campusoverflow.identity.application;

/**
 * 会话撤销端口：禁用账号后立即让其现有会话失效。
 *
 * <p>实现位于 bootstrap（Spring Session + Redis），应用层只依赖这个端口，
 * 不直接依赖具体会话存储。</p>
 */
public interface UserSessionRevoker {

    /** 撤销该用户名下的全部会话；没有会话时静默返回。 */
    void revokeAll(String username);
}
