import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import type { LedgerEntry, ReputationProfile } from '../types';

/**
 * 个人声誉主页：徽章墙 + 积分流水（分页）+ 可用/冻结积分。
 *
 * 三个界面纪律（对应角色卡验收）：
 * - 拿不到数据时给出「不存在/加载失败 + 重试」，不白屏
 * - 他人主页不显示积分（后端对非本人返回 null），不猜、不用自己算
 * - 徽章墙补齐六种徽章的说明，未获得的置灰
 */
export function ReputationPage() {
  const { id } = useParams();
  const userId = Number(id);
  const { me } = useAuth();

  const [profile, setProfile] = useState<ReputationProfile | null>(null);
  const [profileError, setProfileError] = useState('');
  const [loadingProfile, setLoadingProfile] = useState(true);

  const loadProfile = useCallback(() => {
    setLoadingProfile(true);
    setProfileError('');
    api.reputation(userId)
      .then(setProfile)
      .catch((e: Error) => setProfileError(e.message))
      .finally(() => setLoadingProfile(false));
  }, [userId]);

  useEffect(() => { loadProfile(); }, [loadProfile]);

  const isSelf = me?.id === userId;

  if (loadingProfile) {
    return (
      <div className="form-page">
        <h1>声誉档案</h1>
        <p className="muted">加载中…</p>
      </div>
    );
  }

  if (profileError || !profile) {
    return (
      <div className="form-page">
        <h1>声誉档案</h1>
        <p className="error">{profileError || '无法加载该用户的信息'}</p>
        <div className="actions">
          <button className="primary" onClick={loadProfile}>重试</button>
          <Link to="/leaderboard">去看排行榜</Link>
        </div>
      </div>
    );
  }

  const badgeCodes = new Set(profile.badges.map((b) => b.code));

  return (
    <div className="form-page">
      <div className="page-head">
        <h1>{profile.displayName}</h1>
        <Link to="/leaderboard">排行榜 →</Link>
      </div>

      {/* ---- 声誉与积分：积分只有本人能看到 ---- */}
      <div className="rep-summary">
        <div>
          <span className="rep-value">{profile.reputation}</span>
          <span className="muted"> 声誉</span>
        </div>
        {isSelf ? (
          <>
            <div>
              <span className="rep-value">{profile.availablePoints ?? 0}</span>
              <span className="muted"> 可用积分</span>
            </div>
            <div>
              <span className="rep-value">{profile.frozenPoints ?? 0}</span>
              <span className="muted"> 冻结积分</span>
            </div>
          </>
        ) : (
          <p className="muted">积分仅本人可见</p>
        )}
      </div>

      {/* ---- 徽章墙 ---- */}
      <h2>徽章墙</h2>
      {profile.badges.length === 0 ? (
        <div className="empty">
          <p className="muted">还没有获得任何徽章。</p>
          <p className="muted">回答被采纳、声誉达到 100 时就会点亮第一枚徽章。</p>
        </div>
      ) : (
        <div className="badge-grid">
          {profile.badges.map((b) => (
            <div className="badge-card earned" key={b.code}>
              <span className="badge-icon">🏅</span>
              <b>{b.name}</b>
              <p className="muted">{b.description}</p>
              <span className="muted badge-date">
                {new Date(b.awardedAt).toLocaleDateString('zh-CN')} 获得
              </span>
            </div>
          ))}
        </div>
      )}

      {/* 未获得的徽章：置灰，告诉用户差距在哪（阈值来自后端枚举，前端不写死判断） */}
      <UnearnedBadges owned={badgeCodes} />

      {/* ---- 积分流水：只有本人能看，分页 ---- */}
      {isSelf ? <LedgerSection userId={userId} /> : (
        <p className="muted">积分流水仅本人可见。</p>
      )}
    </div>
  );
}

/** 六种徽章里尚未获得的，统一置灰展示 */
function UnearnedBadges({ owned }: { owned: Set<string> }) {
  const all: { code: string; name: string; desc: string }[] = [
    { code: 'FIRST_ANSWER', name: '初来乍到', desc: '提交第一个回答' },
    { code: 'FIRST_ACCEPTED', name: '初露锋芒', desc: '第一个回答被采纳' },
    { code: 'HELPER_10', name: '解惑达人', desc: '累计 10 个回答被采纳' },
    { code: 'TEACHER_ENDORSED', name: '名师认可', desc: '回答获得教师认证' },
    { code: 'REPUTATION_100', name: '小有名气', desc: '声誉达到 100' },
    { code: 'REPUTATION_1000', name: '德高望重', desc: '声誉达到 1000' },
  ];
  const missing = all.filter((b) => !owned.has(b.code));
  if (missing.length === 0) return null;

  return (
    <>
      <h3>待解锁</h3>
      <div className="badge-grid">
        {missing.map((b) => (
          <div className="badge-card" key={b.code}>
            <span className="badge-icon">🔒</span>
            <b>{b.name}</b>
            <p className="muted">{b.desc}</p>
          </div>
        ))}
      </div>
    </>
  );
}

/** 积分流水：时间、原因、±分值，分页 */
function LedgerSection({ userId }: { userId: number }) {
  const [items, setItems] = useState<LedgerEntry[] | null>(null);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [size, setSize] = useState(20);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    api.ledger(userId, page)
      .then((p) => {
        if (cancelled) return;
        setItems(p.items);
        setTotal(p.total);
        setSize(p.size);
      })
      .catch((e: Error) => { if (!cancelled) { setError(e.message); setItems([]); } })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [userId, page]);

  const totalPages = Math.max(1, Math.ceil(total / size));

  return (
    <>
      <h2>积分流水</h2>
      {error && (
        <div>
          <p className="error">流水加载失败：{error}</p>
          <button onClick={() => setPage((p) => p)}>重试</button>
        </div>
      )}
      {loading && !error && <p className="muted">加载中…</p>}
      {!loading && !error && items && items.length === 0 && (
        <div className="empty">
          <p className="muted">还没有积分变动记录。</p>
          <p className="muted">回答问题被采纳、获得投票时，这里会出现记录。</p>
        </div>
      )}
      {!loading && !error && items && items.length > 0 && (
        <>
          <table className="board">
            <thead>
              <tr><th>时间</th><th>原因</th><th>变动</th></tr>
            </thead>
            <tbody>
              {items.map((l) => (
                <tr key={l.id}>
                  <td className="muted">{new Date(l.createdAt).toLocaleString('zh-CN')}</td>
                  <td>{l.reasonLabel || l.reason}</td>
                  <td className={l.delta >= 0 ? 'delta-up' : 'delta-down'}>
                    {l.delta >= 0 ? `+${l.delta}` : l.delta}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {total > size && (
            <div className="pager">
              <button disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>上一页</button>
              <span className="muted">第 {page} / {totalPages} 页 · 共 {total} 条</span>
              <button disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)}>下一页</button>
            </div>
          )}
        </>
      )}
    </>
  );
}
