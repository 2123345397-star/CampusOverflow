package com.campusoverflow.discovery.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.campusoverflow.discovery.domain.notification.Notification;
import com.campusoverflow.discovery.domain.notification.NotificationType;
import com.campusoverflow.qa.api.event.AnswerAcceptedEvent;
import com.campusoverflow.qa.api.event.AnswerEndorsedEvent;
import com.campusoverflow.qa.api.event.AnswerSubmittedEvent;
import com.campusoverflow.qa.api.event.CommentPostedEvent;
import com.campusoverflow.reputation.api.event.BadgeAwardedEvent;
import com.campusoverflow.reputation.api.event.BountyAwardedEvent;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NotificationProjectorTest {

    private static final Instant NOW = Instant.parse("2026-10-08T02:00:00Z");

    private final NotificationTestFixtures.InMemoryNotifications notifications =
            new NotificationTestFixtures.InMemoryNotifications();
    private final NotificationTestFixtures.StubIdentity identity = new NotificationTestFixtures.StubIdentity()
            .withUser(1, "小明").withUser(2, "小红").withUser(9, "王老师");
    private final NotificationTestFixtures.StubQa qa = new NotificationTestFixtures.StubQa()
            .withQuestion(7, 1, "红黑树插入后为什么必须旋转？");

    private NotificationProjector projector;

    @BeforeEach
    void setUp() {
        projector = new NotificationProjector(notifications, identity, qa);
    }

    @Test
    void answerSubmittedNotifiesAskerOnce() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent("e1", NOW, 50, 7, 2, 1,
                "红黑树插入后为什么必须旋转？");

        projector.on(event);
        projector.on(event);

        List<Notification> list = notifications.byRecipient(1);
        assertThat(list).hasSize(1);
        assertThat(list.get(0).type()).isEqualTo(NotificationType.ANSWER_RECEIVED);
        assertThat(list.get(0).title()).contains("小红", "回答了你的问题");
        assertThat(list.get(0).link()).isEqualTo("/questions/7");
    }

    @Test
    void selfAnswerDoesNotNotify() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent("e2", NOW, 50, 7, 1, 1, "title");

        projector.on(event);

        assertThat(notifications.all()).isEmpty();
    }

    @Test
    void acceptedAndEndorsedNotifyAnswerAuthorAndSkipSelfActions() {
        AnswerAcceptedEvent accepted = new AnswerAcceptedEvent("e3", NOW, 50, 7, 2, 1, 1, "title");
        AnswerEndorsedEvent endorsed = new AnswerEndorsedEvent("e4", NOW, 50, 7, 2, 9, "title");
        AnswerAcceptedEvent selfAccepted = new AnswerAcceptedEvent("e5", NOW, 50, 7, 2, 1, 2, "title");
        AnswerEndorsedEvent selfEndorsed = new AnswerEndorsedEvent("e6", NOW, 50, 7, 2, 2, "title");

        projector.on(accepted);
        projector.on(accepted);
        projector.on(endorsed);
        projector.on(selfAccepted);
        projector.on(selfEndorsed);

        List<Notification> list = notifications.byRecipient(2);
        assertThat(list).extracting(Notification::type)
                .containsExactly(NotificationType.ANSWER_ACCEPTED, NotificationType.ANSWER_ENDORSED);
    }

    @Test
    void mentionNotifiesEachTargetOnceAndSkipsAuthor() {
        CommentPostedEvent event = new CommentPostedEvent("e7", NOW, 60, 7, 1,
                "感谢 @王老师，另外 @小红 @小红 你看看");

        projector.on(event);
        projector.on(event);

        assertThat(notifications.byRecipient(9)).hasSize(1);
        assertThat(notifications.byRecipient(2)).hasSize(1);
        assertThat(notifications.byRecipient(1)).isEmpty();
        assertThat(notifications.byRecipient(9).get(0).type()).isEqualTo(NotificationType.MENTIONED);
        assertThat(notifications.byRecipient(9).get(0).title()).contains("红黑树插入后为什么必须旋转？");
    }

    @Test
    void bountyAndBadgeNotificationsAreIdempotent() {
        BountyAwardedEvent bounty = new BountyAwardedEvent("e8", NOW, 1, 7, 9, 2, 50);
        BadgeAwardedEvent badge = new BadgeAwardedEvent("e9", NOW, 2, "FIRST_ACCEPTED", "初露锋芒");

        projector.on(bounty);
        projector.on(bounty);
        projector.on(badge);
        projector.on(badge);

        assertThat(notifications.byRecipient(2)).extracting(Notification::type)
                .containsExactly(NotificationType.BOUNTY_AWARDED, NotificationType.BADGE_AWARDED);
    }
}
