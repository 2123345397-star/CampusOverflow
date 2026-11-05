package com.campusoverflow.qa.domain;

import com.campusoverflow.shared.domain.Guard;

/**
 * Markdown 正文。问题正文 20–30000 码点，回答正文 10–30000 码点。
 * 校验统一走 {@link Guard#requireText}，非法输入抛 BusinessRuleException（映射 422），不抛裸 RuntimeException。
 */
public record MarkdownBody(String value) {

    public static final int QUESTION_MIN = 20;
    public static final int ANSWER_MIN = 10;
    public static final int MAX = 30000;

    public MarkdownBody {
        value = Guard.requireNonNull(value, "QA-1002", "正文");
    }

    /** 问题正文：至少 20 个字符。 */
    public static MarkdownBody ofQuestion(String text) {
        return new MarkdownBody(Guard.requireText(text, QUESTION_MIN, MAX, "QA-1002", "问题正文"));
    }

    /** 回答正文：至少 10 个字符。 */
    public static MarkdownBody ofAnswer(String text) {
        return new MarkdownBody(Guard.requireText(text, ANSWER_MIN, MAX, "QA-1003", "回答正文"));
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
