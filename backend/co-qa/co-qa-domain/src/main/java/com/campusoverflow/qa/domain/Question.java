package com.campusoverflow.qa.domain;

import java.time.LocalDateTime;
import java.util.UUID;

public class Question {
    private String id;
    private String title;
    private MarkdownBody body;
    private Tags tags;
    private String authorId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private int answerCount;
    private boolean accepted;
    private boolean deleted;
    private int version;

    public static Question post(String title, MarkdownBody body, Tags tags, String authorId) {
        Question q = new Question();
        q.id = UUID.randomUUID().toString();
        q.title = title;
        q.body = body;
        q.tags = tags;
        q.authorId = authorId;
        q.createdAt = LocalDateTime.now();
        q.updatedAt = q.createdAt;
        q.answerCount = 0;
        q.accepted = false;
        q.deleted = false;
        q.version = 0;
        return q;
    }

    public void edit(String title, MarkdownBody body, Tags tags, String operatorId, boolean isAdmin) {
        ensureNotDeleted();
        if (!this.authorId.equals(operatorId) && !isAdmin) {
            throw new RuntimeException("QA-1020: 只有作者或管理员可以编辑");
        }
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.updatedAt = LocalDateTime.now();
    }

    public void softDelete(String operatorId, boolean isAdmin) {
        ensureNotDeleted();
        if (!isAdmin) {
            if (!this.authorId.equals(operatorId)) {
                throw new RuntimeException("QA-1020: 只有作者或管理员可以删除");
            }
            if (this.answerCount > 0) {
                throw new RuntimeException("QA-1020: 已有回答的问题不能删除");
            }
        }
        this.deleted = true;
        this.updatedAt = LocalDateTime.now();
    }

    public void ensureOpenForAnswers() {
        ensureNotDeleted();
    }

    public void ensureNotDeleted() {
        if (this.deleted) {
            throw new RuntimeException("QA-1020: 资源已删除");
        }
    }
}
