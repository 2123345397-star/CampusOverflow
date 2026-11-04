package com.campusoverflow.discovery.domain.notification;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 解析评论中的 @昵称（昵称不含空格与 @，长度 2–20）。每条评论最多提醒 5 人，防止滥用。 */
public final class MentionParser {

    private static final Pattern MENTION = Pattern.compile("@([^\\s@，。,.!?！？:：;；、]{2,20})");
    public static final int MAX_MENTIONS = 5;

    private MentionParser() {
    }

    /**
     * 从评论正文中提取被提及的昵称：保持出现顺序、去重、最多 {@link #MAX_MENTIONS} 个。
     *
     * <p>昵称规则由 {@link #MENTION} 正则约束：@ 之后紧跟 2–20 个非空白、非 @、非标点的字符。
     * 因此邮箱地址（a@b）因为 @ 后不足 2 个字符不会命中；"@小明，" 命中后会自动丢掉逗号。</p>
     */
    public static Set<String> extract(String text) {
        Set<String> names = new LinkedHashSet<>();
        if (text == null || text.isBlank()) {
            return names;
        }
        Matcher matcher = MENTION.matcher(text);
        while (names.size() < MAX_MENTIONS && matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }
}
