# Lab 1 · 身份绑定核查（角色卡 E4）

> 团队：TeamX　项目：CampusOverflow　对应实验：Lab 1（Git 协作首次演练）
> 对应角色卡任务：**E4 身份绑定核查** —— 每人 `git config user.email` 与 GitHub 邮箱一致，提交记录能对上人
> 负责人：**房国栋（Dev-2 前端与验证）**　验收标准：贡献图里 5 人都在，无 "unknown author"
> 复查日期：2026-10-29

---

## 1. 为什么必须查（本次就踩到了）

GitHub 判断"这次提交是谁"的唯一依据是 **commit author 的邮箱**，不是 `user.name`：

- 邮箱绑在**当前使用的账号**上 → 头像、用户名链接、贡献格、PR 的 commits 都能对上人；
- 邮箱没绑任何账号 → 显示 `unknown author` / 灰色头像，**贡献热力图不计数**；
- 邮箱绑在**另一个账号**上 → 提交会显示成那个账号，**你的课程账号贡献为 0** ← **本次属于这一种**。

### 本次真实发现（2026-10-29）

房国栋本地配置的提交身份是：

```
user.name  : baigui
user.email : W13636786666@163.com
```

该邮箱**确实已在 GitHub 验证**，但验证在账号 **`Baigui-Yexing`（id 314526884）** 上，而不是课程用的 **`2177023844`**。证据：仓库提交 `c796c11` 的页面作者链接指向 `Baigui-Yexing`。

> 教训：光看"能不能 push"看不出问题，必须点开提交页面看**作者链接指向哪个账号**。

---

## 2. 判定规则（四条全过才算绑定成功）

| # | 检查项 | 通过标准 |
| --- | --- | --- |
| 1 | 本地配置的邮箱 | `git config --global user.email` 的输出，等于目标 GitHub 账号 Settings → Emails 中**已验证**的邮箱 |
| 2 | 提交实际使用的邮箱 | `git log -1 --format='%an <%ae>'` 与第 1 项一致（改配置不会追溯历史提交） |
| 3 | **归属账号正确** | 打开该提交的 GitHub 页面，作者头像可点，且链接指向**你要用的那个账号** |
| 4 | PR 作者一致 | PR 页面左侧作者 = 第 3 项的账号 |

### 关于 noreply 邮箱（推荐做法）

一个已验证的邮箱**只能绑定一个 GitHub 账号**。所以"两个账号都用真实邮箱"做不到，最稳的做法是课程账号用专属 noreply：

```
<账号数字ID>+<账号登录名>@users.noreply.github.com
```

获取方式：用**课程账号**登录 GitHub → Settings → Emails → 勾选 *Keep my email addresses private*，页面会显示属于该账号的 noreply 地址，**原样复制**（数字 ID 是账号 ID，不等于登录名，别自己拼）。

---

## 3. 采集与审计命令

```bash
git config --global user.name
git config --global user.email
git log -1 --format='%an <%ae>'
git shortlog -sne --all
```

Windows 上 `git` 不在 PATH 时用绝对路径：

```powershell
& "D:\Program Files\Git\cmd\git.exe" shortlog -sne --all
```

仓库内脚本（自动比对并输出核查表）：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev-env/audit-authors.ps1 -GitHub 2177023844
```

```bash
bash scripts/dev-env/audit-authors.sh 2177023844
```

---

## 4. 核查结果表

| 成员 | 课程账号（团队登记） | 本地 `user.email` | 邮箱绑在哪个账号 | 结论 | 备注 |
| --- | --- | --- | --- | --- | --- |
| 房国栋 | `2177023844` | `W13636786666@163.com` | ⚠️ `Baigui-Yexing`（个人小号） | ⚠️ **已整改**（改用课程账号 noreply） | 历史提交 `57dd52c`、`0c8f980`、`c796c11` 等归属于小号 |
| 魏志国 | `2123345397-star` | ⬜ 待填 | ⬜ | ⬜ | |
| 陈明哲 | `cmz666-BOT` | ⬜ 待填 | ⬜ | ⬜ | 网页端提交的 committer 为 `noreply@github.com`，属正常 |
| 刘俊琒 | `Liujunfeng520` | ⬜ 待填 | ⬜ | ⬜ | |
| 青岑 | `qing963666` | ⬜ 待填 | ⬜ | ⬜ | |

图例：✅ 已绑定　⚠️ 需整改/已整改　⬜ 待核查

---

## 5. 整改步骤（房国栋 · 已执行）

**第 1 步：登录课程账号，取专属 noreply**

用 `2177023844` 登录 → Settings → Emails → 勾选 *Keep my email addresses private* → 复制形如 `NNNNNNN+2177023844@users.noreply.github.com` 的地址（本账号实测为 `333220585+2177023844@users.noreply.github.com`，其中 `333220585` 是账号 ID）。

**第 2 步：改本地配置（只影响以后的提交）**

```powershell
& $git config --global user.name  "房国栋"
& $git config --global user.email "<上一步复制的 noreply 地址>"
& $git config --global --list | Select-String user
```

**第 3 步：把本分支已有提交的作者改过来（分支刚推、无人基于它开发，可安全改写）**

```powershell
& $git rebase --exec "git commit --amend --no-edit --reset-author" origin/main
& $git -c http.proxy= -c https.proxy= push --force-with-lease
```

> 已在 `main` 上的历史提交**不要改**（会改写全队历史，PR 记录也会错乱）；只改自己未合并的分支。

**第 4 步：验证**

```powershell
& $git log -3 --format="%h %an <%ae>"
```

再打开 GitHub 上该提交页面，确认作者链接指向 `2177023844`。

---

## 6. 备选方案（不推荐）

如果坚持让 `2177023844` 也使用真实邮箱 `W13636786666@163.com`：

1. 先在 `Baigui-Yexing` 的 Settings → Emails 里**删除**该邮箱；
2. 再到 `2177023844` 里添加并完成验证；
3. 重跑第 5 节第 2~3 步。

代价：小号失去该邮箱（历史提交会变成 unknown author），且每次换设备都要维护账号切换。课程场景下 **noreply 更省事**。

---

## 7. 结论与去向

- **核心结论**：团队成员必须确认"提交归属的账号 = 课程登记账号"，只看邮箱是否验证过是不够的；
- 房国栋已整改：改用 `2177023844` 的 noreply，本分支 4 个提交的作者已重写；
- 其余 4 人：按下表补齐 `user.email` 与归属账号，全部 ✅ 后本任务关闭；
- 证据：`screenshots/identity-fangguodong.png`（`audit-authors.ps1` 输出 + `git log --format` 验证）。

---

## 8. 复查记录

| 日期 | 复查人 | 结论 |
| --- | --- | --- |
| 2026-10-28 | 房国栋 | 初查：发现提交作者为 `baigui <W13636786666@163.com>`，登记为"待确认" |
| 2026-10-29 | 房国栋 | 复核：该邮箱已验证，但绑定在 `Baigui-Yexing`；课程账号 `2177023844` 贡献为 0 → 改判"需整改" |
| 2026-10-29 | 房国栋 | 整改：改用课程账号 noreply `333220585+2177023844@users.noreply.github.com`，`rebase --exec --reset-author` 重写本分支提交 |
