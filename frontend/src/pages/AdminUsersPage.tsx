import { useEffect, useState, type FormEvent } from 'react';
import { api, ApiError } from '../api';
import { useAuth } from '../auth';
import type { AdminUser, Page, Role } from '../types';

const PAGE_SIZE = 20;
const ALL_ROLES: Role[] = ['STUDENT', 'TA', 'TEACHER', 'ADMIN'];
const STAFF_ROLES: Role[] = ['TA', 'TEACHER', 'ADMIN'];

const ROLE_LABELS: Record<Role, string> = {
  STUDENT: '学生',
  TA: '助教',
  TEACHER: '教师',
  ADMIN: '管理员',
};

function roleLabel(role: Role): string {
  return ROLE_LABELS[role];
}

function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message;
  if (error instanceof Error) return error.message;
  return '操作失败，请稍后重试';
}

/**
 * T4-05 管理页：待认证学生、认证、创建教职工、调整角色。
 *
 * 后端接口（不是角色卡里写的 status=PENDING）：
 *   GET  /api/v1/admin/users?verified=false&page=1&size=20
 *   POST /api/v1/admin/users/{id}/verify
 *   POST /api/v1/admin/users
 *   PUT  /api/v1/admin/users/{id}/role
 */
export function AdminUsersPage() {
  const { me } = useAuth();
  const [verified, setVerified] = useState(false);
  const [page, setPage] = useState(1);
  const [data, setData] = useState<Page<AdminUser> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const reload = () => setReloadKey((key) => key + 1);

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api.adminUsers(page, PAGE_SIZE, verified)
      .then((result) => {
        if (active) setData(result);
      })
      .catch((e: unknown) => {
        if (active) {
          setData(null);
          setError(errorMessage(e));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [page, verified, reloadKey]);

  const switchTab = (nextVerified: boolean) => {
    setVerified(nextVerified);
    setPage(1);
    setNotice('');
  };

  const verify = async (user: AdminUser) => {
    setNotice('');
    setError('');
    try {
      await api.verifyUser(user.id);
      setNotice(`已认证 ${user.displayName}`);
      reload();
    } catch (e: unknown) {
      setError(errorMessage(e));
    }
  };

  const changeRole = async (user: AdminUser, role: Role) => {
    setNotice('');
    setError('');
    try {
      await api.changeRole(user.id, role);
      setNotice(`已将 ${user.displayName} 的角色改为${roleLabel(role)}`);
      reload();
    } catch (e: unknown) {
      setError(errorMessage(e));
    }
  };

  const totalPages = data ? Math.max(1, Math.ceil(data.total / data.size)) : 1;

  return (
    <div className="admin-page">
      <div className="page-head">
        <h1>用户管理</h1>
        <span className="muted">管理员操作</span>
      </div>

      <div className="tabs">
        <button className={!verified ? 'active' : ''} onClick={() => switchTab(false)}>待认证学生</button>
        <button className={verified ? 'active' : ''} onClick={() => switchTab(true)}>已认证用户</button>
      </div>

      {notice && <p className="ok">{notice}</p>}

      {error && (
        <div>
          <p className="error">{error}</p>
          <button onClick={reload}>重试</button>
        </div>
      )}

      {!error && loading && <p className="muted">加载中…</p>}

      {!error && !loading && data && data.items.length === 0 && (
        <div className="empty">
          <p className="muted">{verified ? '还没有已认证用户。' : '没有待认证学生。'}</p>
        </div>
      )}

      {!error && !loading && data && data.items.length > 0 && (
        <>
          <div className="table-wrap">
            <table className="board admin-table">
              <thead>
                <tr>
                  <th>姓名</th>
                  <th>账号</th>
                  <th>院系 / 班级</th>
                  <th>角色</th>
                  <th>注册时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                {data.items.map((user) => (
                  <AdminUserRow
                    key={user.id}
                    user={user}
                    currentUserId={me?.id}
                    onVerify={verify}
                    onChangeRole={changeRole}
                  />
                ))}
              </tbody>
            </table>
          </div>

          <div className="pager">
            <button disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>上一页</button>
            <span className="muted">第 {data.page} / {totalPages} 页，共 {data.total} 人</span>
            <button disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)}>下一页</button>
          </div>
        </>
      )}

      <CreateStaffForm
        onCreated={() => {
          setNotice('教职工账号已创建');
          setVerified(true);
          setPage(1);
          reload();
        }}
      />
    </div>
  );
}

function AdminUserRow({
  user,
  currentUserId,
  onVerify,
  onChangeRole,
}: {
  user: AdminUser;
  currentUserId?: number;
  onVerify: (user: AdminUser) => Promise<void>;
  onChangeRole: (user: AdminUser, role: Role) => Promise<void>;
}) {
  const [role, setRole] = useState<Role>(user.role);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    setRole(user.role);
  }, [user.role]);

  const isSelf = currentUserId === user.id;

  const run = async (action: () => Promise<void>) => {
    setBusy(true);
    try {
      await action();
    } finally {
      setBusy(false);
    }
  };

  return (
    <tr>
      <td>
        {user.displayName}
        {user.verified && <span className="verified" title="已认证">✔</span>}
      </td>
      <td>{user.maskedUsername}</td>
      <td>{[user.college, user.className].filter(Boolean).join(' / ') || '—'}</td>
      <td>{roleLabel(user.role)}</td>
      <td>{new Date(user.createdAt).toLocaleString('zh-CN')}</td>
      <td>
        <div className="admin-actions">
          {!user.verified && (
            <button disabled={busy} onClick={() => void run(() => onVerify(user))}>认证</button>
          )}
          {isSelf ? (
            <span className="muted">当前账号</span>
          ) : (
            <>
              <select
                value={role}
                onChange={(event) => setRole(event.target.value as Role)}
                disabled={busy}
                aria-label={`${user.displayName} 的角色`}
              >
                {ALL_ROLES.map((item) => <option key={item} value={item}>{roleLabel(item)}</option>)}
              </select>
              <button
                disabled={busy || role === user.role}
                onClick={() => void run(() => onChangeRole(user, role))}
              >
                改角色
              </button>
            </>
          )}
        </div>
      </td>
    </tr>
  );
}

function CreateStaffForm({ onCreated }: { onCreated: () => void }) {
  const [username, setUsername] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState<Role>('TEACHER');
  const [college, setCollege] = useState('');
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setBusy(true);
    setError('');
    try {
      await api.createStaff({
        username,
        displayName,
        email,
        password,
        role,
        college: college.trim() || undefined,
      });
      setUsername('');
      setDisplayName('');
      setEmail('');
      setPassword('');
      setRole('TEACHER');
      setCollege('');
      onCreated();
    } catch (e: unknown) {
      setError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <section className="admin-card">
      <h2>创建教职工账号</h2>
      <p className="muted">学生请走注册流程；这里只创建助教、教师或管理员。</p>

      <form className="admin-form" onSubmit={submit}>
        <label>
          工号
          <input value={username} onChange={(e) => setUsername(e.target.value)} required />
        </label>
        <label>
          姓名
          <input value={displayName} onChange={(e) => setDisplayName(e.target.value)} required />
        </label>
        <label>
          邮箱
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        </label>
        <label>
          初始密码
          <input type="password" minLength={8} value={password}
                 onChange={(e) => setPassword(e.target.value)} required />
        </label>
        <label>
          角色
          <select value={role} onChange={(e) => setRole(e.target.value as Role)}>
            {STAFF_ROLES.map((item) => <option key={item} value={item}>{roleLabel(item)}</option>)}
          </select>
        </label>
        <label>
          学院（可选）
          <input value={college} onChange={(e) => setCollege(e.target.value)} />
        </label>
        <div className="admin-form-actions">
          <button className="primary" type="submit" disabled={busy}>
            {busy ? '创建中…' : '创建账号'}
          </button>
        </div>
      </form>

      {error && <p className="error">创建失败：{error}</p>}
    </section>
  );
}
