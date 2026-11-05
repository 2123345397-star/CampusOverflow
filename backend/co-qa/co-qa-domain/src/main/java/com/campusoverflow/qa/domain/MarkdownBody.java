package com.campusoverflow.qa.domain;

public class MarkdownBody {
    private final String text;

    private MarkdownBody(String text) {
        this.text = text;
    }

    public static MarkdownBody ofQuestion(String text) {
        int len = text.codePointCount(0, text.length());
        if (len < 20 || len > 30000) {
            throw new RuntimeException("QA-1020: 问题正文长度必须在 20-30000 之间");
        }
        return new MarkdownBody(text);
    }

    public static MarkdownBody ofAnswer(String text) {
        int len = text.codePointCount(0, text.length());
        if (len < 10 || len > 30000) {
            throw new RuntimeException("QA-1020: 回答正文长度必须在 10-30000 之间");
        }
        return new MarkdownBody(text);
    }
    
    public String getText() {
        return text;
    }
}
