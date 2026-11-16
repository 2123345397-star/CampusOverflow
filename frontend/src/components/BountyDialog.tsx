import { useEffect, useRef, useState } from 'react';
import { api, ApiError } from '../api';
import type { Bounty } from '../types';

/**
 * 悬赏对话框：替换原来的 prompt()。
 *
 * - 表单：积分 10–500、天数 1–14，前端先校验，后端 422 再兜底
 * - 已有悬赏时展示倒计时（剩余时间），过期显示「已结束」，剩 1 小时内标红
 * - 定时器在卸载时清理，避免切页面后泄漏
 */
export function BountyDialog({ questionId, canOpen, onChanged }: {
  questionId: number;
  /** 后端 permissions.canManage 为 true 时才允许发起（教师在课程内才能悬赏） */
  canOpen: boolean;
  onChanged?: () => void;
}) {
  const [bounty, setBounty] = useState<Bounty | null>(null);
  const [loading, setLoading] = useState(true);
  const [open, setOpen] = useState(false);
  const [points, setPoints] = useState('50');
  const [days, setDays] = useState('7');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const [now, setNow] = useState(() => Date.now());
  const timer = useRef<number | null>(null);

  const load = () => {
    setLoading(true);
    api.bountyOf(questionId)
      .then(setBounty)
      .catch(() => setBounty(null))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [questionId]);

  // 每秒刷新一次「当前时间」，驱动倒计时重新计算
  useEffect(() => {
    if (!bounty || bounty.status !== 'OPEN') return;
    timer.current = window.setInterval(() => setNow(Date.now()), 1000);
    return () => {
      if (timer.current !== null) {
        window.clearInterval(timer.current);
        timer.current = null;
      }
    };
  }, [bounty]);

  const submit = async () => {
    const p = Number(points);
    const d = Number(days);
    // 前端先拦一道，减少无意义的 422
    if (!Number.isFinite(p) || p < 10 || p > 500) {
      setError('悬赏积分需在 10–500 之间');
      return;
    }
    if (!Number.isFinite(d) || d < 1 || d > 14) {
      setError('悬赏期限需在 1–14 天之间');
      return;
    }
    setBusy(true);
    setError('');
    try {
      await api.bounty(questionId, p, d);
      setOpen(false);
      setNow(Date.now());
      load();
      onChanged?.();
    } catch (e) {
      setError(describe(e));
    } finally {
      setBusy(false);
    }
  };

  if (loading) {
    return <p className="muted">悬赏信息加载中…</p>;
  }

  return (
    <div className="bounty-box">
      {bounty ? (
        <BountyStatus bounty={bounty} now={now} />
      ) : (
        <p className="muted">该问题还没有悬赏。</p>
      )}

      {canOpen && !open && (
        <button onClick={() => { setOpen(true); setError(''); }}>发起悬赏</button>
      )}

      {open && (
        <div className="inline-form">
          <label>积分（10–500）
            <input type="number" min={10} max={500} value={points}
                   onChange={(e) => setPoints(e.target.value)} />
          </label>
          <label>有效天数（1–14）
            <input type="number" min={1} max={14} value={days}
                   onChange={(e) => setDays(e.target.value)} />
          </label>
          <button className="primary" onClick={submit} disabled={busy}>
            {busy ? '提交中…' : '确定'}
          </button>
          <button onClick={() => setOpen(false)} disabled={busy}>取消</button>
        </div>
      )}

      {error && <p className="error">{error}</p>}
    </div>
  );
}

/** 悬赏状态行：状态 + 剩余时间倒计时 */
function BountyStatus({ bounty, now }: { bounty: Bounty; now: number }) {
  const expiresAt = new Date(bounty.expiresAt).getTime();
  const remain = expiresAt - now;
  const open = bounty.status === 'OPEN';

  let text: string;
  let cls = 'bounty-status';
  if (!open) {
    text = statusLabel(bounty.status);
    cls += ' is-closed';
  } else if (remain <= 0) {
    text = '已结束';
    cls += ' is-closed';
  } else {
    text = `剩余 ${formatRemain(remain)}`;
    // 剩 1 小时以内变红，提醒尽快结算
    if (remain <= 60 * 60 * 1000) cls += ' is-urgent';
  }

  return (
    <p className={cls}>
      💰 悬赏 <b>{bounty.points}</b> 分 · 发起人 {bounty.sponsorName} · {text}
    </p>
  );
}

function statusLabel(status: string): string {
  const map: Record<string, string> = {
    OPEN: '进行中',
    AWARDED: '已发放',
    CLOSED: '已关闭（积分已退回）',
    EXPIRED: '已过期（积分已退回）',
  };
  return map[status] ?? status;
}

/** 把毫秒差转成「3 天 5 小时」/「5 小时 12 分」/「12 分 30 秒」 */
function formatRemain(ms: number): string {
  const total = Math.floor(ms / 1000);
  const d = Math.floor(total / 86400);
  const h = Math.floor((total % 86400) / 3600);
  const m = Math.floor((total % 3600) / 60);
  const s = total % 60;
  if (d > 0) return `${d} 天 ${h} 小时`;
  if (h > 0) return `${h} 小时 ${m} 分`;
  if (m > 0) return `${m} 分 ${s} 秒`;
  return `${s} 秒`;
}

/** REP-1003 / REP-1004 / REP-1002 等错误码 → 中文提示 */
function describe(e: unknown): string {
  if (e instanceof ApiError) {
    switch (e.code) {
      case 'REP-1002': return '可用悬赏积分不足';
      case 'REP-1003': return '悬赏积分需在 10–500 之间';
      case 'REP-1004': return '悬赏期限需在 1–14 天之间';
      case 'QA-3001': return '只有课程教师/助教可以发起悬赏';
      case 'QA-1020': return '该问题已关闭，无法发起悬赏';
      default:
        if (e.status === 403) return '你没有权限发起悬赏';
        if (e.status === 404) return '问题不存在或已被删除';
        if (e.status === 422) return '提交内容不符合规则，请检查积分与天数';
        return e.message || '发起悬赏失败，请稍后重试';
    }
  }
  return '网络异常，请稍后重试';
}
