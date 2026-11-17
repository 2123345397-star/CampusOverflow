# T4-05 管理页证据

> 对应任务：Sprint 4 · T4-05 管理页（待认证学生、认证、建教职工、改角色）
> 采集地址：`http://127.0.0.1:5173/admin`
> 采集日期：2026-11-17

| 文件 | 场景 | 图中要点 |
| --- | --- | --- |
| [`admin-users-pending.png`](admin-users-pending.png) | 管理员登录后进入 `/admin` | 右上角 `ADMIN`、导航有「管理」；待认证学生列表 3 人，含认证按钮、角色下拉框、分页 |
| [`admin-forbidden.png`](admin-forbidden.png) | 学生登录后直接访问 `/admin` | 导航没有「管理」；页面显示「无权限访问 / 管理页仅管理员可用」 |
| [`admin-verify.png`](admin-verify.png) | 对 `TestStudent` 点「认证」 | 绿色提示“已认证 TestStudent”；待认证列表从 3 人变 2 人 |
| [`admin-create-staff.png`](admin-create-staff.png) | 创建教职工账号 `T9001` | 绿色提示“教职工账号已创建”；已认证用户出现“测试教师 / 教师” |
| [`admin-change-role.png`](admin-change-role.png) | 将 `测试教师` 从教师改为助教 | 绿色提示“已将 测试教师 的角色改为助教”；角色列显示“助教” |

对应实现：`frontend/src/pages/AdminUsersPage.tsx`、`frontend/src/App.tsx` 的 `RequireAdmin` 路由守卫。
后端接口：`GET /api/v1/admin/users?verified=false`、`POST /api/v1/admin/users/{id}/verify`、
`POST /api/v1/admin/users`、`PUT /api/v1/admin/users/{id}/role`。

> 说明：前两张证明「管理员入口可见 + 非管理员路由守卫」；后三张分别证明认证、建教职工、改角色三个动作可用。
> 动作截图采集于 2026-11-17，使用 demo 账号 `admin01`；`TestStudent` 已认证、`T9001` 已创建为演示数据。
