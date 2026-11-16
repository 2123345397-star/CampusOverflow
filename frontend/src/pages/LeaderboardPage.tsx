import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import type { LeaderboardEntry } from '../types';

/** 四个周期：后端 LeaderboardPeriod 枚举为 WEEK / MONTH / TERM / ALL */
const PERIODS = [['week', '本周'], ['month', '本月'], ['term', '本学期'], ['all', '总榜']] as const;

export function LeaderboardPage() {
  const { me } = useAuth();
  const [period, setPeriod] = useState<string>('week');
  const [rows, setRows] = useState<LeaderboardEntry[] | null>(null);
  const [error, setError] = useState('');

  const load = (p: string) => {
    setRows(null);
    setError('');
    api.leaderboard(p)
      .then(setRows)
      .catch((e: Error) => { setError(e.message); setRows([]); });
  };

  useEffect(() => { load(period); }, [period]);

  return (
    <div className="form-page">
      <div className="page-head">
        <h1>声誉排行榜</h1>
        {me && <Link to={`/users/${me.id}/reputation`}>我的声誉档案 →</Link>}
      </div>

      {/* 四个周期切换：周 / 月 / 学期 / 总 */}
      <div className="tabs">
        {PERIODS.map(([value, label]) => (
          <button key={value}
                  className={period === value ? 'active' : ''}
                  onClick={() => setPeriod(value)}>
            {label}
          </button>
        ))}
      </div>

      {error && (
        <div>
          <p className="error">排行榜加载失败：{error}</p>
          <button onClick={() => load(period)}>重试</button>
        </div>
      )}

      {!error && rows === null && <p className="muted">加载中…</p>}

      {/* 空榜占位：区分「周期内还没有数据」和「榜单从未产生」 */}
      {!error && rows !== null && rows.length === 0 && (
        <div className="empty">
          <p className="muted">
            {period === 'all' ? '还没有任何声誉数据。' : '这个周期还没有数据。'}
          </p>
          <p className="muted">
            {period === 'all' ? '回答问题被采纳后就会上榜。' : '切换到「总榜」看看历史排名。'}
          </p>
          {period !== 'all' && <button onClick={() => setPeriod('all')}>查看总榜</button>}
        </div>
      )}

      {!error && rows !== null && rows.length > 0 && (
        <table className="board">
          <thead><tr><th>名次</th><th>用户</th><th>声誉</th></tr></thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.userId} className={me?.id === r.userId ? 'is-me' : ''}>
                <td>{medal(r.rank)}</td>
                <td>
                  <Link to={`/users/${r.userId}/reputation`}>{r.displayName}</Link>
                  {r.verified && <span className="verified" title="已认证">✔</span>}
                </td>
                <td>{r.score}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}

function medal(rank: number): string {
  return rank === 1 ? '🥇' : rank === 2 ? '🥈' : rank === 3 ? '🥉' : String(rank);
}
