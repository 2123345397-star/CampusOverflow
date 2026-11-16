package com.campusoverflow.discovery.application;

import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationRepository;
import com.campusoverflow.identity.api.CourseSummary;
import com.campusoverflow.identity.api.IdentityApi;
import com.campusoverflow.identity.api.UserSummary;
import com.campusoverflow.qa.api.QaQueryApi;
import com.campusoverflow.qa.api.QuestionRef;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.page.PageResult;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/** 通知侧手写测试替身：不依赖 Spring / 数据库。 */
final class NotificationTestFixtures {

    private NotificationTestFixtures() {
    }

    static final class InMemoryNotifications implements NotificationRepository {
        private final Map<Long, Notification> store = new LinkedHashMap<>();
        private final AtomicLong sequence = new AtomicLong();

        @Override
        public boolean exists(String sourceEventId, long recipientId) {
            return store.values().stream()
                    .anyMatch(n -> n.sourceEventId().equals(sourceEventId) && n.recipientId() == recipientId);
        }

        @Override
        public Notification save(Notification notification) {
            if (notification.id() == null) {
                notification.assignId(sequence.incrementAndGet());
            }
            store.put(notification.id(), notification);
            return notification;
        }

        @Override
        public Optional<Notification> findById(long id) {
            return Optional.ofNullable(store.get(id));
        }

        @Override
        public PageResult<Notification> findByRecipient(long recipientId, PageRequest page) {
            List<Notification> all = store.values().stream()
                    .filter(n -> n.recipientId() == recipientId)
                    .toList();
            int from = Math.min(page.offset(), all.size());
            int to = Math.min(from + page.size(), all.size());
            return new PageResult<>(all.subList(from, to), all.size(), page.page(), page.size());
        }

        @Override
        public long countUnread(long recipientId) {
            return store.values().stream().filter(n -> n.recipientId() == recipientId && !n.read()).count();
        }

        @Override
        public int markAllRead(long recipientId) {
            int updated = 0;
            for (Notification n : store.values()) {
                if (n.recipientId() == recipientId && !n.read()) {
                    n.markRead(recipientId);
                    updated++;
                }
            }
            return updated;
        }

        List<Notification> all() {
            return new ArrayList<>(store.values());
        }

        List<Notification> byRecipient(long recipientId) {
            return store.values().stream().filter(n -> n.recipientId() == recipientId).toList();
        }
    }

    static final class StubIdentity implements IdentityApi {
        private final Map<Long, UserSummary> users = new HashMap<>();
        private final Map<String, UserSummary> byName = new HashMap<>();

        StubIdentity withUser(long id, String name) {
            UserSummary user = new UserSummary(id, name, com.campusoverflow.shared.security.Role.STUDENT, true,
                    "计算机工程学院");
            users.put(id, user);
            byName.put(name, user);
            return this;
        }

        @Override
        public Optional<UserSummary> findUser(long userId) {
            return Optional.ofNullable(users.get(userId));
        }

        @Override
        public Map<Long, UserSummary> findUsers(Collection<Long> userIds) {
            Map<Long, UserSummary> result = new HashMap<>();
            userIds.forEach(id -> findUser(id).ifPresent(u -> result.put(id, u)));
            return result;
        }

        @Override
        public Optional<UserSummary> findByDisplayName(String displayName) {
            return Optional.ofNullable(byName.get(displayName));
        }

        @Override
        public Optional<CourseSummary> findCourse(long courseId) {
            return Optional.of(new CourseSummary(courseId, "CS2001", "数据结构", "2026-秋", 9L));
        }

        @Override
        public boolean canManageCourse(long userId, Long courseId) {
            return false;
        }
    }

    static final class StubQa implements QaQueryApi {
        private final Map<Long, QuestionRef> questions = new HashMap<>();

        StubQa withQuestion(long id, long authorId, String title) {
            questions.put(id, new QuestionRef(id, authorId, 100L, title, null, false));
            return this;
        }

        @Override
        public Optional<QuestionRef> findQuestion(long questionId) {
            return Optional.ofNullable(questions.get(questionId));
        }
    }
}
