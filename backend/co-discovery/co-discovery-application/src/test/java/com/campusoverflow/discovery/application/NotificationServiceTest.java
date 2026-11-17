package com.campusoverflow.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationType;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.domain.NotFoundException;
import com.campusoverflow.shared.page.PageRequest;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class NotificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-08T02:00:00Z");
    private static final Actor MING = new Actor(1, "小明", Role.STUDENT);
    private static final Actor HONG = new Actor(2, "小红", Role.STUDENT);

    private final NotificationTestFixtures.InMemoryNotifications repo =
            new NotificationTestFixtures.InMemoryNotifications();
    private final NotificationService service = new NotificationService(repo);

    @Test
    void listAndUnreadCountUseCurrentActor() {
        repo.save(Notification.create(1, NotificationType.ANSWER_RECEIVED, "你的问题有新回答", "/questions/7",
                "e1", NOW));
        repo.save(Notification.create(1, NotificationType.BADGE_AWARDED, "你获得了新徽章", "/me/reputation",
                "e2", NOW));
        repo.save(Notification.create(2, NotificationType.MENTIONED, "有人提到了你", "/questions/7",
                "e3", NOW));

        var page = service.list(MING, new PageRequest(1, 20));

        assertThat(page.total()).isEqualTo(2);
        assertThat(page.items()).extracting(NotificationView::type)
                .containsExactly("ANSWER_RECEIVED", "BADGE_AWARDED");
        assertThat(service.unreadCount(MING)).isEqualTo(2);
        assertThat(service.unreadCount(HONG)).isEqualTo(1);
    }

    @Test
    void markReadRejectsOtherUsersAndMissingNotifications() {
        Notification saved = repo.save(Notification.create(1, NotificationType.ANSWER_RECEIVED, "标题",
                "/questions/7", "e1", NOW));

        assertThatThrownBy(() -> service.markRead(HONG, saved.id())).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> service.markRead(MING, 999L)).isInstanceOf(NotFoundException.class);

        service.markRead(MING, saved.id());
        assertThat(repo.findById(saved.id()).orElseThrow().read()).isTrue();
        assertThat(service.unreadCount(MING)).isZero();
    }

    @Test
    void markAllReadReturnsUpdatedCountAndIsIdempotent() {
        repo.save(Notification.create(1, NotificationType.ANSWER_RECEIVED, "一", "/questions/1", "e1", NOW));
        repo.save(Notification.create(1, NotificationType.ANSWER_ACCEPTED, "二", "/questions/2", "e2", NOW));
        repo.save(Notification.create(2, NotificationType.MENTIONED, "别人的", "/questions/3", "e3", NOW));

        assertThat(service.markAllRead(MING)).isEqualTo(2);
        assertThat(service.markAllRead(MING)).isZero();
        assertThat(service.unreadCount(HONG)).isEqualTo(1);
    }
}
