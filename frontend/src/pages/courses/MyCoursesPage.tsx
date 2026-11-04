import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../api';
import type { Course } from '../../types';
import { CourseCard } from './CourseCard';

/**
 * 我的课程页：只列出当前登录用户已加入的课程。
 * 需要登录（路由外层已用 RequireAuth 拦住），空态给出“去课程目录”的入口。
 */
export function MyCoursesPage() {
  const [courses, setCourses] = useState<Course[] | null>(null);
  const [error, setError] = useState('');

  const load = useCallback(() => {
    setCourses(null);
    setError('');
    api.myCourses()
      .then(setCourses)
      .catch((e: Error) => setError(e.message));
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="form-page">
      <div className="page-head">
        <h1>我的课程</h1>
        <Link to="/courses">去课程目录 →</Link>
      </div>

      {error && (
        <div>
          <p className="error">加载失败：{error}</p>
          <button onClick={load}>重试</button>
        </div>
      )}

      {!error && courses === null && <p className="muted">加载中…</p>}

      {!error && courses !== null && courses.length === 0 && (
        <div className="empty">
          <p className="muted">你还没有加入任何课程。</p>
          <Link className="primary" to="/courses">去课程目录看看</Link>
        </div>
      )}

      {!error && courses !== null && courses.length > 0 && (
        <ul className="course-list">
          {courses.map((c) => (
            <CourseCard key={c.id} course={c} authenticated onJoined={() => undefined} />
          ))}
        </ul>
      )}
    </div>
  );
}
