# 课程域用户故事卡（US009–US012）

> 产出人：房国栋（Dev-2 前端与验证）　对应实验：Lab 2 需求建模工作坊
> 对应角色卡：**E1 课程页**（课程列表 / 我的课程 / 加入课程）、**E2 提问页选课**
> 归属迭代：Sprint 2（Sprint 1 已承诺 26 点，课程域不在其中，不挤占容量）
> 验收标准（Gherkin）：见 `docs/backlog/stories/US-09 ~ US-12`

---

## 1. 为什么补这四条

Lab 2 的故事地图骨架是"用户从未注册到成为活跃贡献者"，当前 `docs/backlog/stories/` 的 8 条故事（US-01~US-08）全部落在**问答主链路**上，缺少"课程"这一维度。而：

- 产品定位是**校园问答**，问题必须挂在某门课下，否则同班同学无法聚合；
- 角色卡 E1/E2 要交付的三个页面（课程列表、我的课程、加入课程）+ 提问页课程下拉，在 Backlog 里**没有对应故事**，验收时无据可依；
- 课程是"提问 → 回答 → 采纳"的上下文入口，属于 Walking Skeleton 的必要一环。

因此补充 4 条故事，优先级 P1，放在 Sprint 2。

## 2. 故事卡

| ID | 用户故事（作为 / 我想要 / 以便） | 优先级 | SP | 迭代 | Gherkin |
| --- | --- | --- | --- | --- | --- |
| US009 | 作为在校学生，我想要浏览课程目录，以便找到我关心的课程并在其中提问 | P1 | 5 | Sprint 2 | [`US-09-course-catalog.feature`](stories/US-09-course-catalog.feature) |
| US010 | 作为已加入课程的学生，我想要查看"我的课程"，以便快速回到某个课程继续提问 | P1 | 3 | Sprint 2 | [`US-10-my-courses.feature`](stories/US-10-my-courses.feature) |
| US011 | 作为在校学生，我想要加入一门课程，以便在该课程下提问并收到通知 | P1 | 5 | Sprint 2 | [`US-11-join-course.feature`](stories/US-11-join-course.feature) |
| US012 | 作为在校学生，我想要提问时选择课程，以便问题出现在正确的课程下 | P1 | 3 | Sprint 2 | [`US-12-ask-with-course.feature`](stories/US-12-ask-with-course.feature) |

合计 **16 点**，Sprint 2 另有问答主链路增量，需在 Planning 时二次校准容量。

## 3. INVEST 自评

| ID | Independent | Negotiable | Valuable | Estimable | Small | Testable | 结论 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| US009 | ✅ 只读列表，不依赖加入状态 | ✅ 卡片字段可裁剪 | ✅ 无目录就无法进入课程 | ✅ 接口契约明确 | ✅ 单页 + 一个 GET | ✅ 空列表/异常均有断言 | 通过 |
| US010 | ⚠️ 依赖 US011 产生数据，但可用 mock 独立开发 | ✅ | ✅ | ✅ | ✅ | ✅ | 通过（并行开发时用 mock） |
| US011 | ✅ | ✅ | ✅ 加入是后续一切的前提 | ✅ | ✅ | ✅ 含失败提示断言 | 通过 |
| US012 | ⚠️ 依赖 US011（下拉数据来源） | ✅ | ✅ 回答归属的根 | ✅ | ✅ | ✅ | 通过（可用"已加入课程"接口先行） |

> US010/US012 的依赖属于**数据依赖**而非**发布依赖**：后端契约（`GET /api/v1/courses`、`GET /api/v1/me/courses`）确定后，前端可用 mock 并行开发，符合"等不到就先用 mock"的约定。

## 4. 依赖与现状（2026-10-29 核对代码后更新）

**重要更正**：初稿写"后端没有 courses 接口，先用 mock"是错的。核对 `backend/co-identity` 后确认**课程后端已完整实现**，E1/E2 直接对接现成 API，不需要 mock。

### 4.1 现有接口（`CourseController`，`/api/v1/courses`）

| 端点 | 用途 | 关键返回 / 错误码 | 对应故事 |
| --- | --- | --- | --- |
| `GET /api/v1/courses` | 课程目录（登录时 `myRole` 字段告知我在这门课里的角色） | `CourseView{id,code,name,term,teacherId,teacherName,myRole}` | US009 |
| `GET /api/v1/courses/mine` | 我的课程 | 同上（`myRole` 非空） | US010 |
| `POST /api/v1/courses/{id}/join` | 加入课程 | `204`；`404 ID-4002` 课程不存在；`409 ID-2005` 你已加入该课程 | US011 |
| `POST /api/v1/courses` | 创建课程（教师/管理员） | `201 {id}`；`403 ID-3002`；`400 ID-1010/1011/1012`；`409 ID-2004` 同学期同代码 | 不在 E1/E2 范围 |
| `GET /api/v1/courses/{id}/members` | 课程成员 | `CourseMemberView{userId,displayName,role}` | 后续 |
| `PUT /api/v1/courses/{id}/members/{userId}` | 调整成员角色 | `204`；`403 ID-3003`；`404 ID-4003` | 后续 |

问答侧也已预留课程维度：`PostQuestionCommand(courseId, ...)`、`QuestionRef.courseId`、`QuestionPostedEvent.courseId`、`QuestionDetailView.courseRef`、`QaReadModel.listQuestions(courseId, page)`。

### 4.2 前后端差距（决定 Sprint 2 工作量）

| 缺口 | 影响哪些场景 | 建议 |
| --- | --- | --- |
| 前端**没有** `pages/courses/` 目录 | 全部 E1/E2 | 本次要写，见第 5 节 |
| `Course` 无 `archived/status` 字段 | US009"归档不可加入"、US010"归档后不出现在我的课程"、US011"已归档置灰" | 要么后端加状态，要么把这三个场景改为"课程不存在"；**需 PO 决策** |
| `CourseView` 无 `joinedAt` | US010"按加入时间倒序" | 前端暂按 `id` 倒序，或后端补字段 |
| 无"退出课程"用例 | 无（本 Sprint 不做） | 不纳入范围 |
| `Course` 无简介字段 | US009 卡片"一句话简介" | 用 `name + term + teacherName` 替代，不阻塞 |

### 4.3 前端要消费的字段（直接照抄，别自己造）

```ts
// GET /api/v1/courses 与 /api/v1/courses/mine 共用
type CourseView = {
  id: number; code: string; name: string; term: string;
  teacherId: number; teacherName: string;
  myRole: 'STUDENT' | 'TEACHER' | 'TEACHING_ASSISTANT' | null; // null = 未加入
};
```

## 5. 与前端页面的对应关系（E1 / E2 验收依据）

| 角色卡任务 | 页面（`frontend/src/pages/courses/`） | 覆盖故事 | 验收要点 |
| --- | --- | --- | --- |
| E1-a 课程列表 | `CourseListPage.tsx` | US009 | 卡片渲染、空状态、失败重试 |
| E1-b 我的课程 | `MyCoursesPage.tsx` | US010 | 排序、空态入口、未登录跳转 |
| E1-c 加入课程 | `CourseCard.tsx` + `useEnrollCourse` | US011 | **成功提示与失败提示都要有**（角色卡明确点名的坑） |
| E2 提问选课 | `pages/AskPage.tsx` 的课程下拉 | US012 | 只列已加入课程、默认选中、空态禁用提交 |

## 6. 与 Lab 2 提交物的关系

- 本文件 + 4 个 `.feature` 一并计入 Lab 2"至少 8 条 Gherkin 验收标准"（现有 US-01~US-08 已满足下限，本次扩展到 12 条）；
- 故事 ID 已同步追加进 [`product-backlog.md`](product-backlog.md)，保持"一条故事 = 一个 ID = 一份 Gherkin"的可追溯关系。
