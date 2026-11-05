package com.campusoverflow.qa.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.campusoverflow.shared.domain.BusinessRuleException;
import com.campusoverflow.shared.domain.ConflictException;
import com.campusoverflow.shared.domain.ForbiddenException;
import com.campusoverflow.shared.security.Actor;
import com.campusoverflow.shared.security.Role;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class QuestionTest {

    static final Instant NOW = Instant.parse("2026-10-01T08:00:00Z");
    static final Actor ASKER = new Actor(1, "提问者", Role.STUDENT);
    static final Actor OTHER = new Actor(2, "路人", Role.STUDENT);
    static final Actor TEACHER = new Actor(9, "王老师", Role.TEACHER);

    static Question persistedQuestion() {
        Question q = Question.post(ASKER.userId(), 100L, new Title("红黑树插入后如何判断旋转方向？"),
                MarkdownBody.ofQuestion("插入节点后出现连续红色节点，什么时候左旋、什么时候右旋？"),
                Tags.of(List.of("数据结构", "红黑树")), NOW);
        q.assignId(10);
        return q;
    }

    static Answer persistedAnswer(Question q, long authorId, long id) {
        Answer a = Answer.submit(q, authorId, MarkdownBody.ofAnswer("看叔叔节点的颜色和插入位置。"), NOW);
        a.assignId(id);
        return a;
    }

    @Nested
    class Posting {
        @Test
        void titleMustBeBetween10And150Chars() {
            assertThatThrownBy(() -> new Title("太短了")).isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("标题");
        }

        @Test
        void tagsAreNormalizedAndDeduplicated() {
            Tags tags = Tags.of(List.of("#Java", "java", " Spring-Boot "));
            assertThat(tags.names()).containsExactly("java", "spring-boot");
        }

        @Test
        void requiresOneToFiveTags() {
            assertThatThrownBy(() -> Tags.of(List.of())).isInstanceOf(BusinessRuleException.class);
            assertThatThrownBy(() -> Tags.of(List.of("a", "b", "c", "d", "e", "f")))
                    .isInstanceOf(BusinessRuleException.class);
        }

        @Test
        void rejectsIllegalTagCharacters() {
            assertThatThrownBy(() -> new TagName("<script>")).isInstanceOf(BusinessRuleException.class);
            assertThat(new TagName("C++").value()).isEqualTo("c++");
        }
    }

    /**
     * 边界值用例（Lab 4 课后第 3 天）：标题 9/10/150/151、正文 19/20/30000/30001、
     * 标签去重后 5/6 个、重复采纳、已删问题编辑。共 12 条，全部贴着不变式的两侧各取一个点。
     */
    @Nested
    class Boundaries {

        @Test
        void titleOfNineCodePointsIsRejected() {
            assertThatThrownBy(() -> new Title("一".repeat(9)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("10–150")
                    .hasMessageContaining("当前 9");
        }

        @Test
        void titleOfTenCodePointsIsAccepted() {
            assertThat(new Title("一".repeat(10)).value()).isEqualTo("一".repeat(10));
        }

        @Test
        void titleOf150CodePointsIsAccepted() {
            assertThat(new Title("标".repeat(150)).value()).hasSize(150);
        }

        @Test
        void titleOf151CodePointsIsRejected() {
            assertThatThrownBy(() -> new Title("标".repeat(151)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("当前 151");
        }

        @Test
        void questionBodyOf19CodePointsIsRejected() {
            assertThatThrownBy(() -> MarkdownBody.ofQuestion("正".repeat(19)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("20–30000");
        }

        @Test
        void questionBodyOf20CodePointsIsAccepted() {
            assertThat(MarkdownBody.ofQuestion("正".repeat(20)).value()).hasSize(20);
        }

        @Test
        void questionBodyOf30000CodePointsIsAccepted() {
            assertThat(MarkdownBody.ofQuestion("正".repeat(30_000)).value()).hasSize(30_000);
        }

        @Test
        void questionBodyOf30001CodePointsIsRejected() {
            assertThatThrownBy(() -> MarkdownBody.ofQuestion("正".repeat(30_001)))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("当前 30001");
        }

        @Test
        void exactlyFiveDistinctTagsAreAccepted() {
            Tags tags = Tags.of(List.of("java", "mysql", "redis", "spring", "数据结构"));

            assertThat(tags.names()).containsExactly("java", "mysql", "redis", "spring", "数据结构");
        }

        @Test
        void sixInputTagsCollapsingToFourDistinctAreAccepted() {
            // 1–5 的上限作用在“归一化去重之后”的集合上：Java / java / JAVA 只算一个
            Tags tags = Tags.of(List.of("Java", "java", "JAVA", "spring", "mysql", "redis"));

            assertThat(tags.names()).containsExactly("java", "spring", "mysql", "redis");
        }

        @Test
        void acceptingTheSameAnswerTwiceIsConflict() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, OTHER.userId(), 50);
            q.accept(a, ASKER, false);

            assertThatThrownBy(() -> q.accept(a, ASKER, false))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("已经采纳");
            assertThat(q.acceptedAnswerId()).isEqualTo(50L);
        }

        @Test
        void deletedQuestionCannotBeEdited() {
            Question q = persistedQuestion();
            q.softDelete(ASKER, false, NOW);

            assertThatThrownBy(() -> q.edit(ASKER, false, new Title("删除之后再改标题应该被拒绝"),
                    MarkdownBody.ofQuestion("这段正文用来验证已软删除的问题不允许继续编辑。"), q.tags(), NOW))
                    .isInstanceOf(BusinessRuleException.class)
                    .hasMessageContaining("已删除");
        }
    }

    @Nested
    class Accepting {
        @Test
        void askerCanAcceptAnAnswerOnce() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, OTHER.userId(), 50);
            q.accept(a, ASKER, false);
            assertThat(q.acceptedAnswerId()).isEqualTo(50L);
            assertThat(q.isAccepted(50)).isTrue();

            Answer b = persistedAnswer(q, 3, 51);
            assertThatThrownBy(() -> q.accept(b, ASKER, false)).isInstanceOf(ConflictException.class);
        }

        @Test
        void courseTeacherCanAcceptOnBehalfOfAsker() {
            Question q = persistedQuestion();
            q.accept(persistedAnswer(q, OTHER.userId(), 50), TEACHER, true);
            assertThat(q.acceptedAnswerId()).isEqualTo(50L);
        }

        @Test
        void otherStudentsCannotAccept() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, 3, 50);
            assertThatThrownBy(() -> q.accept(a, OTHER, false)).isInstanceOf(ForbiddenException.class);
        }

        @Test
        void answerMustBelongToQuestion() {
            Question q = persistedQuestion();
            Question another = persistedQuestion();
            Answer foreign = Answer.reconstitute(60L, 999L, 3, MarkdownBody.ofAnswer("这是别的问题下面的回答内容"), 0,
                    null, NOW, NOW, null, 0);
            assertThatThrownBy(() -> q.accept(foreign, ASKER, false)).isInstanceOf(BusinessRuleException.class);
            assertThat(another.acceptedAnswerId()).isNull();
        }

        @Test
        void deletedAnswerCannotBeAccepted() {
            Question q = persistedQuestion();
            Answer a = persistedAnswer(q, 3, 50);
            a.softDelete(new Actor(3, "回答者", Role.STUDENT), false, false, NOW);
            assertThatThrownBy(() -> q.accept(a, ASKER, false)).isInstanceOf(BusinessRuleException.class);
        }
    }

    @Nested
    class EditingAndDeleting {
        @Test
        void onlyAuthorOrManagerCanEdit() {
            Question q = persistedQuestion();
            assertThatThrownBy(() -> q.edit(OTHER, false, q.title(), q.body(), q.tags(), NOW))
                    .isInstanceOf(ForbiddenException.class);
            q.edit(TEACHER, true, new Title("红黑树插入后的旋转规则是什么？"), q.body(), q.tags(), NOW.plusSeconds(60));
            assertThat(q.title().value()).contains("旋转规则");
            assertThat(q.updatedAt()).isAfter(q.createdAt());
        }

        @Test
        void askerCannotDeleteAnsweredQuestionButManagerCan() {
            Question answered = Question.reconstitute(10L, ASKER.userId(), 100L, new Title("一个已经有回答的问题标题"),
                    MarkdownBody.ofQuestion("这个问题已经有了一个回答，所以提问者不能删除。"), Tags.of(List.of("java")),
                    null, 0, 1, 0, NOW, NOW, null, 0);
            assertThatThrownBy(() -> answered.softDelete(ASKER, false, NOW)).isInstanceOf(BusinessRuleException.class);
            answered.softDelete(TEACHER, true, NOW);
            assertThat(answered.isDeleted()).isTrue();
        }

        @Test
        void deletedQuestionCannotBeAnswered() {
            Question q = persistedQuestion();
            q.softDelete(ASKER, false, NOW);
            assertThatThrownBy(() -> Answer.submit(q, OTHER.userId(), MarkdownBody.ofAnswer("这是一个迟到了很久的回答内容"), NOW))
                    .isInstanceOf(BusinessRuleException.class);
        }
    }
}
