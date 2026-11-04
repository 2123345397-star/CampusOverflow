# Lab 1 · 身份绑定核查（角色卡 E4）

> 团队：TeamX　项目：CampusOverflow　对应实验：Lab 1（Git 协作首次演练）
> 对应角色卡任务：**E4 身份绑定核查** —— 每人 `git config user.email` 与 GitHub 邮箱一致，提交记录能对上人
> 负责人：**房国栋（Dev-2 前端与验证）**　验收标准：贡献图里 5 人都在，无 "unknown author"
> 复查日期：2026-10-28

---

## 1. 为什么必须查（不是形式主义）

GitHub 判断"这次提交是谁"的唯一依据是 **commit author 的邮箱**，不是 `user.name`：

- 邮箱在**提交者本人的 GitHub 账号里验证过** → 头像、用户名链接、贡献格、PR 页面的 "commits" 都能对上人；
- 邮箱没有绑定 → 提交显示为 `unknown author` / 灰色头像，**贡献热力图不计数**，老师按贡献统计评分时等于白干；
- 用错邮箱还可能把别人的邮箱写成自己的，涉及学术诚信。

角色卡里那句"提交记录显示未知作者 → 先查 E4 邮箱绑定"，说的就是这个坑。

## 2. 判定规则（三条全过才算绑定成功）

| # | 检查项 | 通过标准 |
| --- | --- | --- |
| 1 | 本地配置的邮箱 | `git config --global user.email` 的输出，与 GitHub 账号 Settings → Emails 中**已验证**的某个邮箱完全一致（区分大小写之外的字符必须一致） |
| 2 | 提交实际使用的邮箱 | `git log -1 --format='%an <%ae>'` 与第 1 项一致；改过配置后仍可能是旧邮箱，必须看实际提交 |
| 3 | GitHub 侧的归属 | 提交在网页上显示作者头像并可点进 `https://github.com/<login>`；PR 作者与 commit 作者是同一账号 |

### 关于 noreply 邮箱（推荐做法）

若不想公开私人邮箱，GitHub 提供两种等价形式，二选一填进 `user.email` 即可：

```
2177023844+2177023844@users.noreply.github.com     # 推荐：带账号 ID 前缀，永不变
2177023844@users.noreply.github.com                # 旧式，部分老账号才有
```

获取方式：GitHub → Settings → Emails → 勾选 *Keep my email addresses private*，页面会直接给出属于你的 noreply 地址。

> 用 noreply 的好处：换邮箱、换电脑都不用改配置；坏处：本地 `git log` 里看不出真实邮箱（对课程提交反而更合适）。

## 3. 采集命令（每人本机执行，结果贴进第 4 节）

```bash
git config --global user.name
git config --global user.email
git log -1 --format='%an <%ae>'
```

一次性看全仓库所有作者及提交数：

```bash
git shortlog -sne --all
```

Windows 上若 `git` 不在 PATH（本仓库已知情况），用绝对路径：

```powershell
& "D:\Program Files\Git\cmd\git.exe" shortlog -sne --all
```

仓库内脚本可自动比对并输出核查表：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/dev-env/audit-authors.ps1 -GitHub 2177023844
```

```bash
bash scripts/dev-env/audit-authors.sh 2177023844
```

## 4. 核查结果表

| 成员 | GitHub 账号 | 本地 `user.name` | 本地 `user.email` | 是否账号已验证邮箱 | 结论 | 备注 |
| --- | --- | --- | --- | --- | --- | --- |
| 房国栋 | `2177023844` | `baigui` | `W13636786666@163.com` | ⚠️ 待确认（该邮箱未在 GitHub 账号内验证） | ⚠️ **需整改** | 仓库现有提交作者为 `baigui <W13636786666@163.com>`（如 `57dd52c`、`0c8f980`），若该邮箱未绑定，贡献不会被计入 |
| 魏志国 | `2123345397-star` | ⬜ | ⬜ | ⬜ | ⬜ | |
| 陈明哲 | `cmz666-BOT` | ⬜ | ⬜ | ⬜ | ⬜ | 有提交由 GitHub 网页端产生，committer 为 `GitHub <noreply@github.com>`，属正常 |
| 刘俊琒 | `Liujunfeng520` | ⬜ | ⬜ | ⬜ | ⬜ | |
| 青岑 | `qing963666` | ⬜ | ⬜ | ⬜ | ⬜ | |

图例：✅ 已绑定　⚠️ 需整改　⬜ 待核查

## 5. 整改步骤（房国栋 · 已定位）

**第 1 步：确认/添加邮箱到 GitHub 账号**

GitHub → Settings → Emails：若 `W13636786666@163.com` 不在列表中，添加并完成验证邮件；若不想公开，直接改用第 2 节的 noreply 地址。

**第 2 步：改本地配置（只影响以后的提交）**

```bash
# 方案 A：绑定真实邮箱（已在该账号验证）
git config --global user.name  "房国栋"
git config --global user.email "W13636786666@163.com"

# 方案 B：用 noreply（推荐，永不过期）
git config --global user.name  "房国栋"
git config --global user.email "2177023844+2177023844@users.noreply.github.com"

# 立即生效并校验
git config --global --list | findstr user
```

> `user.name` 建议用真实姓名或 GitHub 用户名，避免出现 `PC`、`Administrator`、`baigui` 这类看不出是谁的名字。

**第 3 步（可选）：修正自己分支上尚未合并的历史提交**

⚠️ 只对自己**尚未被他人基于它开发**的分支做，且改完要 `push --force-with-lease`；**已在 `main` 上的历史不要改**（会改写全队历史）。

```bash
# 修正最近一次提交的作者
git commit --amend --reset-author --no-edit

# 批量修正本分支全部提交的邮箱（谨慎）
git rebase -i --root   # 在编辑器里把每条 pick 改成 edit 后逐条 amend --reset-author
# 或一次性改写（仅限个人分支）：
git filter-branch --env-filter '
  export GIT_AUTHOR_EMAIL="2177023844+2177023844@users.noreply.github.com"
  export GIT_COMMITTER_EMAIL="$GIT_AUTHOR_EMAIL"
  export GIT_AUTHOR_NAME="房国栋"
  export GIT_COMMITTER_NAME="$GIT_AUTHOR_NAME"
' -- --all
```

**第 4 步：验证**

```bash
git log -1 --format='%an <%ae>'
```

再到 GitHub 上打开该提交，确认作者头像/用户名链接指向 `2177023844`。历史 `main` 上的旧提交若仍显示 unknown author，属于既成事实，在 Lab 1 说明里注明"后续提交已绑定"即可。

## 6. 结论与去向

- 现状：**5 人中至少 1 人（房国栋）邮箱未与 GitHub 账号对齐**，需要先整改再让老师统计贡献；
- 整改完成后，把第 4 节表格全部填成 ✅，截图（GitHub 提交页显示头像 + 本地 `git config` 输出）存到 `docs/lab1-submission/screenshots/identity-<拼音>.png`；
- 与 [`env-matrix.md`](env-matrix.md)（E3 环境）配套提交，两项一起构成角色卡"每个人都是真的在干活"的证据链。

## 7. 复查记录

| 日期 | 复查人 | 结论 |
| --- | --- | --- |
| 2026-10-28 | 房国栋 | 发现 `baigui <W13636786666@163.com>` 未绑定账号，已发起整改 |
