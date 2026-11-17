# T4-05 管理页证据

> 对应任务：Sprint 4 · T4-05 管理页（待认证学生、认证、建教职工、改角色）
> 采集地址：`http://127.0.0.1:5173/admin`
> 采集日期：2026-11-17

| 文件 | 场景 | 图中要点 |
| --- | --- | --- |
| [`admin-users-pending.png`](admin-users-pending.png) | 管理员登录后进入 `/admin` | 右上角 `ADMIN`、导航有「管理」；待认证学生列表 3 人，含认证按钮、角色下拉框、分页 |
| [`admin-forbidden.png`](admin-forbidden.png) | 学生登录后直接访问 `/admin` | 导航没有「管理」；页面显示「无权限访问 / 管理页仅管理员可用」 |

对应实现：`frontend/src/pages/AdminUsersPage.tsx`、`frontend/src/App.tsx` 的 `RequireAdmin` 路由守卫。
后端接口：`GET /api/v1/admin/users?verified=false`、`POST /api/v1/admin/users/{id}/verify`、
`POST /api/v1/admin/users`、`PUT /api/v1/admin/users/{id}/role`。

> 说明：两张图证明「管理员入口可见 + 非管理员路由守卫」；认证、建教职工、改角色的动作在彩排时现场演示。
