import { useEffect, useState } from 'react';
import { BrowserRouter, Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './auth';
import { api } from './api';
import { QuestionListPage } from './pages/QuestionListPage';
import { QuestionDetailPage } from './pages/QuestionDetailPage';
import { AskPage } from './pages/AskPage';
import { LoginPage } from './pages/LoginPage';
import { LeaderboardPage } from './pages/LeaderboardPage';
import { ReputationPage } from './pages/ReputationPage';
import { NotificationsPage } from './pages/NotificationsPage';
import { CourseListPage } from './pages/courses/CourseListPage';
import { MyCoursesPage } from './pages/courses/MyCoursesPage';
import { NotFoundPage } from './pages/NotFoundPage';
import { AdminUsersPage } from './pages/AdminUsersPage';

function Header() {
  const { me, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!me) return;
    const load = () => api.unreadCount().then((r) => setUnread(r.count)).catch(() => undefined);
    load();
    const timer = setInterval(load, 30_000);
    return () => clearInterval(timer);
  }, [me]);

  return (
    <header className="topbar">
      <Link className="brand" to="/">CampusOverflow</Link>
      <nav>
        <Link to="/">问题</Link>
        <Link to="/courses">课程</Link>
        {me && <Link to="/courses/mine">我的课程</Link>}
        <Link to="/leaderboard">排行榜</Link>
        {me && <Link to="/notifications">通知{unread > 0 && <span className="badge">{unread}</span>}</Link>}
        {me?.role === 'ADMIN' && <Link to="/admin">管理</Link>}
      </nav>
      <div className="spacer" />
      {me ? (
        <div className="user">
          <span>{me.displayName}</span>
          <span className="role">{me.role}</span>
          <button onClick={() => logout().then(() => navigate('/'))}>退出</button>
        </div>
      ) : (
        <Link className="primary" to="/login">登录 / 注册</Link>
      )}
    </header>
  );
}

function RequireAuth({ children }: { children: JSX.Element }) {
  const { me, loading } = useAuth();
  if (loading) return <p className="muted">加载中…</p>;
  return me ? children : <Navigate to="/login" replace />;
}

/** 管理页只给 ADMIN；非管理员即使手输 URL 也只看到无权限页。 */
function RequireAdmin({ children }: { children: JSX.Element }) {
  const { me, loading } = useAuth();
  if (loading) return <p className="muted">加载中…</p>;
  if (!me) return <Navigate to="/login" replace />;
  if (me.role !== 'ADMIN') {
    return (
      <div className="state-page">
        <h1>无权限访问</h1>
        <p className="muted">管理页仅管理员可用。</p>
        <Link to="/">返回首页</Link>
      </div>
    );
  }
  return children;
}

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Header />
        <main>
          <Routes>
            <Route path="/" element={<QuestionListPage />} />
            <Route path="/questions/:id" element={<QuestionDetailPage />} />
            <Route path="/ask" element={<RequireAuth><AskPage /></RequireAuth>} />
            <Route path="/courses" element={<CourseListPage />} />
            <Route path="/courses/mine" element={<RequireAuth><MyCoursesPage /></RequireAuth>} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/leaderboard" element={<LeaderboardPage />} />
            <Route path="/users/:id/reputation" element={<ReputationPage />} />
            <Route path="/notifications" element={<RequireAuth><NotificationsPage /></RequireAuth>} />
            <Route path="/admin" element={<RequireAdmin><AdminUsersPage /></RequireAdmin>} />
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        </main>
      </AuthProvider>
    </BrowserRouter>
  );
}
