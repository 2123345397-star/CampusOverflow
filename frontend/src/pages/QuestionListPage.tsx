import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import { errorMessage } from '../errors';
import type { Page, QuestionSummary, SearchResult } from '../types';

const PAGE_SIZE = 20;

export function QuestionListPage() {
  const { me } = useAuth();
  const [data, setData] = useState<Page<QuestionSummary | SearchResult> | null>(null);
  const [tags, setTags] = useState<{ name: string; questionCount: number }[]>([]);
  const [keyword, setKeyword] = useState('');
  const [activeTag, setActiveTag] = useState<string | undefined>();
  const [sort, setSort] = useState('newest');
  const [unanswered, setUnanswered] = useState(false);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.popularTags().then(setTags).catch(() => undefined);
  }, []);

  // 筛选条件变化时回到第 1 页，避免停在越界页码上看到空白
  useEffect(() => {
    setPage(1);
  }, [keyword, activeTag, sort, unanswered]);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError('');
    const searching = keyword.trim() !== '' || activeTag || unanswered || sort !== 'newest';
    const promise = searching
      ? api.search(keyword.trim(), { tag: activeTag, sort, unanswered, page })
      : api.questions(page, undefined, PAGE_SIZE);
    promise
      .then((r) => { if (!cancelled) { setData(r); setLoading(false); } })
      .catch((e: unknown) => { if (!cancelled) { setError(errorMessage(e)); setLoading(false); } });
    return () => { cancelled = true; };
  }, [keyword, activeTag, sort, unanswered, page]);

  const totalPages = data ? Math.max(1, Math.ceil(data.total / data.size)) : 1;
  const busy = loading || (keyword.trim() !== '' && data === null);

  return (
    <div className="layout">
      <section>
        <div className="toolbar">
          <input placeholder="搜索问题（支持中文全文检索）" value={keyword}
                 onChange={(e) => setKeyword(e.target.value)} />
          <select value={sort} onChange={(e) => setSort(e.target.value)}>
            <option value="newest">最新</option>
            <option value="active">最近活跃</option>
            <option value="hot">得分最高</option>
            <option value="relevance">相关度</option>
          </select>
          <label className="check">
            <input type="checkbox" checked={unanswered} onChange={(e) => setUnanswered(e.target.checked)} />
            仅看未回答
          </label>
          {me && <Link className="primary" to="/ask">我要提问</Link>}
        </div>

        {activeTag && (
          <p className="muted">
            标签筛选：<b>{activeTag}</b> <button onClick={() => setActiveTag(undefined)}>清除</button>
          </p>
        )}

        {error && (
          <div>
            <p className="error">列表加载失败：{error}</p>
            <button onClick={() => setPage((p) => p)}>重试</button>
          </div>
        )}

        {/* 加载中：骨架屏，避免白屏 */}
        {busy && !error && (
          <ul className="qlist" aria-busy="true">
            {[0, 1, 2].map((i) => (
              <li key={i} className="skeleton-row">
                <div className="skeleton skeleton-line short" />
                <div className="skeleton skeleton-line" />
              </li>
            ))}
          </ul>
        )}

        {!busy && !error && data && data.items.length > 0 && (
          <ul className="qlist">
            {data.items.map((q) => (
              <li key={q.id}>
                <div className="stats">
                  <span><b>{q.score}</b> 票</span>
                  <span className={q.accepted ? 'accepted' : ''}><b>{q.answerCount}</b> 回答</span>
                  <span>{q.viewCount} 浏览</span>
                </div>
                <div className="qmain">
                  <Link className="qtitle" to={`/questions/${q.id}`}>{q.title}</Link>
                  <p className="excerpt">{q.excerpt}</p>
                  <div className="tagrow">
                    {q.tags.map((t) => <button key={t} className="tag" onClick={() => setActiveTag(t)}>{t}</button>)}
                    <span className="muted">
                      {q.author.displayName} · {new Date(q.createdAt).toLocaleString('zh-CN')}
                    </span>
                  </div>
                </div>
              </li>
            ))}
          </ul>
        )}

        {/* 空态：区分“还没有问题”和“筛不到结果” */}
        {!busy && !error && data && data.items.length === 0 && (
          <div className="empty">
            {keyword.trim() || activeTag || unanswered ? (
              <>
                <p className="muted">没有匹配的问题，换个关键词或清除筛选试试。</p>
                <button onClick={() => { setKeyword(''); setActiveTag(undefined); setUnanswered(false); setSort('newest'); }}>
                  清除全部筛选
                </button>
              </>
            ) : (
              <>
                <p className="muted">还没有人提问，来做第一个吧。</p>
                {me ? <Link className="primary" to="/ask">我要提问</Link> : <Link to="/login">登录后提问</Link>}
              </>
            )}
          </div>
        )}

        {/* 分页 */}
        {!busy && !error && data && data.total > data.size && (
          <div className="pager">
            <button disabled={page <= 1} onClick={() => setPage((p) => Math.max(1, p - 1))}>上一页</button>
            <span className="muted">第 {data.page} / {totalPages} 页 · 共 {data.total} 条</span>
            <button disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)}>下一页</button>
          </div>
        )}
      </section>

      <aside>
        <h3>热门标签</h3>
        <div className="tagrow">
          {tags.map((t) => (
            <button key={t.name} className="tag" onClick={() => setActiveTag(t.name)}>
              {t.name}<span className="count">{t.questionCount}</span>
            </button>
          ))}
          {tags.length === 0 && <p className="muted">暂无标签</p>}
        </div>
      </aside>
    </div>
  );
}
