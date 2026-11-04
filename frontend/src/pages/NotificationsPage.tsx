import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import type { Notification } from '../types';

export function NotificationsPage() {
  const [items, setItems] = useState<Notification[] | null>(null);
  const [error, setError] = useState('');

  const load = () =>
    api.notifications()
      .then((p) => { setItems(p.items); setError(''); })
      .catch((e: Error) => { setItems([]); setError(e.message); });

  useEffect(() => { load(); }, []);

  const readOne = async (id: number) => {
    await api.readOne(id).catch(() => undefined);
    setItems((prev) => prev?.map((n) => (n.id === id ? { ...n, read: true } : n)) ?? prev);
  };

  const unread = items?.filter((n) => !n.read).length ?? 0;

  return (
    <div className="form-page">
      <div className="page-head">
        <h1>通知</h1>
        <span className="muted">{unread > 0 ? `${unread} 条未读` : '全部已读'}</span>
      </div>

      <div className="actions">
        <button onClick={() => api.readAll().then(load)} disabled={unread === 0}>全部标记为已读</button>
        <button onClick={load}>刷新</button>
      </div>

      {error && <p className="error">加载失败：{error}</p>}
      {items === null && <p className="muted">加载中…</p>}

      {items !== null && items.length === 0 && !error && (
        <div className="empty">
          <p className="muted">暂时没有通知。</p>
          <p className="muted">有人回答你的问题、采纳你的回答或在评论里 @ 你时，会在这里提醒。</p>
        </div>
      )}

      <ul className="plain">
        {items?.map((n) => (
          <li key={n.id} className={n.read ? 'muted' : ''}>
            <span className="notif-type">{typeLabel(n.type)}</span>
            <Link to={n.link} onClick={() => { if (!n.read) void readOne(n.id); }}>{n.title}</Link>
            <span className="muted"> · {new Date(n.createdAt).toLocaleString('zh-CN')}</span>
            {!n.read && (
              <button className="link-btn" onClick={() => readOne(n.id)}>标为已读</button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
}

function typeLabel(type: string): string {
  const map: Record<string, string> = {
    ANSWER_RECEIVED: '[回答]',
    ANSWER_ACCEPTED: '[采纳]',
    ANSWER_ENDORSED: '[认证]',
    MENTIONED: '[提及]',
    BOUNTY_AWARDED: '[悬赏]',
    BADGE_AWARDED: '[徽章]',
  };
  return map[type] ?? '[通知]';
}
