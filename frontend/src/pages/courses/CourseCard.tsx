import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, ApiError } from '../../api';
import type { Course } from '../../types';

interface Props {
  course: Course;
  /** 未登录时点“加入”先引导登录，不在无权限状态下发请求。 */
  authenticated: boolean;
  onJoined: (courseId: number) => void;
}

const ROLE_LABEL: Record<string, string> = {
  STUDENT: '学生',
  TEACHING_ASSISTANT: '助教',
  TEACHER: '教师',
};

export function CourseCard({ course, authenticated, onJoined }: Props) {
  const navigate = useNavigate();
  const [justJoined, setJustJoined] = useState(false);
  const [joining, setJoining] = useState(false);
  const [message, setMessage] = useState('');
  const [failed, setFailed] = useState(false);

  const joined = Boolean(course.myRole) || justJoined;

  const join = async () => {
    if (!authenticated) {
      navigate('/login');
      return;
    }
    setJoining(true);
    setMessage('');
    setFailed(false);
    try {
      await api.joinCourse(course.id);
      setJustJoined(true);
      setMessage(`已加入《${course.name}》`);
      onJoined(course.id);
    } catch (e) {
      // 失败时按钮必须恢复可点，不能一直转圈（角色卡点名的坑）
      const code = e instanceof ApiError ? e.code : '';
      if (code === 'ID-2005') {
        setJustJoined(true);
        setMessage('你已经加入过这门课程');
        onJoined(course.id);
      } else if (code === 'ID-4002') {
        setFailed(true);
        setMessage('课程不存在或已关闭，请刷新后重试');
      } else {
        setFailed(true);
        setMessage('加入失败，请稍后重试');
      }
    } finally {
      setJoining(false);
    }
  };

  return (
    <li className="course-card">
      <div className="course-head">
        <span className="tag">{course.code}</span>
        <b className="course-name">{course.name}</b>
        {joined && <span className="badge ok-badge">已加入</span>}
      </div>

      <p className="muted course-meta">
        {course.term} · 授课教师 {course.teacherName}
        {course.myRole ? ` · 我的角色：${ROLE_LABEL[course.myRole] ?? course.myRole}` : ''}
      </p>

      {message && <p className={failed ? 'error' : 'ok'}>{message}</p>}

      <div className="actions">
        {joined ? (
          <button disabled>已加入</button>
        ) : (
          <button className="primary" onClick={join} disabled={joining}>
            {joining ? '加入中…' : '加入课程'}
          </button>
        )}
        <button onClick={() => navigate(`/?courseId=${course.id}`)}>查看课程问题</button>
      </div>
    </li>
  );
}
