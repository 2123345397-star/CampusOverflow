import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { api } from '../api';
import { useAuth } from '../auth';
import { errorKind, errorMessage } from '../errors';
import type { Answer, QuestionDetail } from '../types';

type PageState = 'loading' | 'ok' | 'notfound' | 'forbidden' | 'error';

export function QuestionDetailPage() {
  const { id } = useParams();
  const questionId = Number(id);
  const navigate = useNavigate();
  const { me } = useAuth();

  const [q, setQ] = useState<QuestionDetail | null>(null);
  const [state, setState] = useState<PageState>('loading');
  const [related, setRelated] = useState<{ id: number; title: string }[]>([]);
  const [draft, setDraft] = useState('');
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(false);

  const load = useCallback(() => {
    setState('loading');
    api.question(questionId)
      .then((data) => { setQ(data); setState('ok'); setError(''); })
      .catch((e: unknown) => {
        const kind = errorKind(e);
        setState(kind === 'notfound' ? 'notfound' : kind === 'forbidden' ? 'forbidden' : 'error');
        setError(errorMessage(e));
      });
  }, [questionId]);

  useEffect(() => {
    load();
    api.related(questionId).then(setRelated).catch(() => undefined);
  }, [load, questionId]);

  /** 所有写操作统一走这里：失败时把业务码翻成中文，成功后重新拉一次详情。 */
  const act = async (fn: () => Promise<unknown>) => {
    setError('');
    try {
      await fn();
      load();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  // ---- 四种状态界面：加载中 / 404 / 无权限 / 出错 ----
  if (state === 'loading') {
    return (
      <div className="layout">
        <article>
          <div className="skeleton skeleton-title" />
          <div className="skeleton skeleton-line" />
          <div className="skeleton skeleton-line short" />
          <div className="skeleton skeleton-block" />
        </article>
      </div>
    );
  }

  if (state === 'notfound') {
    return (
      <div className="state-page">
        <h1>问题不存在</h1>
        <p className="muted">{error || '这个问题可能已被作者删除，或者链接有误。'}</p>
        <Link className="primary" to="/">返回问题列表</Link>
      </div>
    );
  }

  if (state === 'forbidden') {
    return (
      <div className="state-page">
        <h1>没有查看权限</h1>
        <p className="muted">{error || '这个问题仅对课程成员开放。'}</p>
        <div className="actions">
          <Link className="primary" to="/courses">去课程页加入</Link>
          <Link to="/">返回列表</Link>
        </div>
      </div>
    );
  }

  if (state === 'error' || !q) {
    return (
      <div className="state-page">
        <h1>加载失败</h1>
        <p className="error">{error}</p>
        <button className="primary" onClick={load}>重试</button>
      </div>
    );
  }

  // ---- 权限全部来自后端 permissions，前端不写死角色 ----
  const { canEdit, canDelete, canAccept, canManage } = q.permissions;

  return (
    <div className="layout">
      <article>
        <h1>{q.title}</h1>
        <p className="muted">
          {q.author.displayName} · {new Date(q.createdAt).toLocaleString('zh-CN')} · {q.viewCount} 次浏览
          {q.course && <> · 课程 {q.course.name}</>}
        </p>

        {error && <p className="error">{error}</p>}

        {editing ? (
          <QuestionEditor
            question={q}
            onCancel={() => setEditing(false)}
            onSaved={() => { setEditing(false); load(); }}
            onError={setError}
          />
        ) : (
          <div className="post">
            <div className="votebox">
              <button disabled={!me} title={me ? '赞成' : '登录后可投票'}
                      onClick={() => act(() => api.vote('QUESTION', q.id, q.myVote === 1 ? 0 : 1))}>▲</button>
              <b>{q.score}</b>
              <button disabled={!me} title={me ? '反对' : '登录后可投票'}
                      onClick={() => act(() => api.vote('QUESTION', q.id, q.myVote === -1 ? 0 : -1))}>▼</button>
            </div>
            <div className="body">
              {/* 正文由后端渲染并经 commonmark + OWASP Sanitizer 双重净化 */}
              <div dangerouslySetInnerHTML={{ __html: q.bodyHtml }} />
              <div className="tagrow">{q.tags.map((t) => <span key={t} className="tag">{t}</span>)}</div>

              <div className="actions">
                {canEdit && <button onClick={() => setEditing(true)}>编辑问题</button>}
                {canDelete && (
                  <button className="danger" onClick={() => {
                    if (!confirm('删除后不可恢复，确定删除这个问题吗？')) return;
                    void act(async () => {
                      await api.deleteQuestion(q.id);
                      navigate('/');
                    });
                  }}>删除问题</button>
                )}
                {canManage && <BountyForm questionId={q.id} onDone={load} onError={setError} />}
              </div>

              <CommentList comments={q.comments} targetType="QUESTION" targetId={q.id} onDone={load} canComment={!!me} />
            </div>
          </div>
        )}

        <h2>{q.answerCount} 个回答</h2>
        {q.answers.length === 0 && (
          <p className="muted">还没有回答。第一个回答的人往往能拿到最高票。</p>
        )}
        {q.answers.map((a) => (
          <AnswerBlock key={a.id} answer={a} question={q} canAccept={canAccept} canManage={canManage}
                       canVote={!!me} onAct={act} onReload={load} onError={setError} />
        ))}

        {me ? (
          <section className="answer-form">
            <h3>写回答</h3>
            <textarea rows={8} value={draft} onChange={(e) => setDraft(e.target.value)}
                      placeholder="支持 Markdown，代码请使用 ``` 包裹" />
            <button className="primary" onClick={() => act(async () => {
              await api.answer(q.id, draft);
              setDraft('');
            })}>提交回答
            </button>
          </section>
        ) : <p className="muted"><Link to="/login">登录</Link>后可以回答与投票。</p>}
      </article>

      <aside>
        <h3>相关问题</h3>
        <ul className="plain">
          {related.map((r) => <li key={r.id}><Link to={`/questions/${r.id}`}>{r.title}</Link></li>)}
          {related.length === 0 && <li className="muted">暂无相关问题</li>}
        </ul>
      </aside>
    </div>
  );
}

/** 编辑问题：标题 / 正文 / 标签，保存后由父组件重新拉详情。 */
function QuestionEditor({ question, onCancel, onSaved, onError }: {
  question: QuestionDetail;
  onCancel: () => void;
  onSaved: () => void;
  onError: (message: string) => void;
}) {
  const [title, setTitle] = useState(question.title);
  const [body, setBody] = useState(question.body);
  const [tags, setTags] = useState(question.tags.join(' '));
  const [saving, setSaving] = useState(false);

  const save = async () => {
    setSaving(true);
    try {
      await api.editQuestion(question.id, {
        title,
        body,
        tags: tags.split(/[,，\s]+/).filter(Boolean),
      });
      onSaved();
    } catch (e) {
      onError(errorMessage(e));
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="answer-form">
      <h3>编辑问题</h3>
      <label>标题<input value={title} onChange={(e) => setTitle(e.target.value)} /></label>
      <label>正文<textarea rows={10} value={body} onChange={(e) => setBody(e.target.value)} /></label>
      <label>标签（1–5 个，空格或逗号分隔）<input value={tags} onChange={(e) => setTags(e.target.value)} /></label>
      <div className="actions">
        <button className="primary" onClick={save} disabled={saving}>{saving ? '保存中…' : '保存'}</button>
        <button onClick={onCancel} disabled={saving}>取消</button>
      </div>
    </div>
  );
}

/** 悬赏：用内联表单替代 prompt()，带范围校验与倒计时说明。 */
function BountyForm({ questionId, onDone, onError }: {
  questionId: number;
  onDone: () => void;
  onError: (message: string) => void;
}) {
  const [open, setOpen] = useState(false);
  const [points, setPoints] = useState('50');
  const [days, setDays] = useState('7');
  const [busy, setBusy] = useState(false);

  if (!open) return <button onClick={() => setOpen(true)}>发起悬赏</button>;

  const submit = async () => {
    setBusy(true);
    try {
      await api.bounty(questionId, Number(points), Number(days));
      setOpen(false);
      onDone();
    } catch (e) {
      onError(errorMessage(e));
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="inline-form">
      <label>积分（10–500）
        <input type="number" min={10} max={500} value={points} onChange={(e) => setPoints(e.target.value)} />
      </label>
      <label>有效天数（1–14）
        <input type="number" min={1} max={14} value={days} onChange={(e) => setDays(e.target.value)} />
      </label>
      <button className="primary" onClick={submit} disabled={busy}>{busy ? '提交中…' : '确定'}</button>
      <button onClick={() => setOpen(false)} disabled={busy}>取消</button>
    </div>
  );
}

function AnswerBlock({ answer, question, canAccept, canManage, canVote, onAct, onReload, onError }: {
  answer: Answer;
  question: QuestionDetail;
  canAccept: boolean;
  canManage: boolean;
  canVote: boolean;
  onAct: (fn: () => Promise<unknown>) => Promise<void>;
  onReload: () => void;
  onError: (message: string) => void;
}) {
  const [editing, setEditing] = useState(false);
  const [text, setText] = useState(answer.body);
  const accepted = question.acceptedAnswerId === answer.id;

  const save = async () => {
    try {
      await api.editAnswer(answer.id, text);
      setEditing(false);
      onReload();
    } catch (e) {
      onError(errorMessage(e));
    }
  };

  return (
    <div className={`post ${accepted ? 'is-accepted' : ''}`} id={`answer-${answer.id}`}>
      <div className="votebox">
        <button disabled={!canVote} onClick={() => onAct(() => api.vote('ANSWER', answer.id, answer.myVote === 1 ? 0 : 1))}>▲</button>
        <b>{answer.score}</b>
        <button disabled={!canVote} onClick={() => onAct(() => api.vote('ANSWER', answer.id, answer.myVote === -1 ? 0 : -1))}>▼</button>
        {accepted && <span className="check-mark" title="已采纳">✔</span>}
      </div>
      <div className="body">
        {editing ? (
          <>
            <textarea rows={8} value={text} onChange={(e) => setText(e.target.value)} />
            <div className="actions">
              <button className="primary" onClick={save}>保存</button>
              <button onClick={() => { setEditing(false); setText(answer.body); }}>取消</button>
            </div>
          </>
        ) : (
          <div dangerouslySetInnerHTML={{ __html: answer.bodyHtml }} />
        )}

        <p className="muted">
          {answer.author.displayName}
          {answer.endorsedBy && <span className="endorsed">教师认证 · {answer.endorsedBy.displayName}</span>}
          · {new Date(answer.createdAt).toLocaleString('zh-CN')}
        </p>

        <div className="actions">
          {canAccept && !question.acceptedAnswerId && (
            <button onClick={() => onAct(() => api.accept(question.id, answer.id))}>采纳此回答</button>
          )}
          {canManage && !answer.endorsedBy && (
            <button onClick={() => onAct(() => api.endorse(answer.id))}>教师认证</button>
          )}
          {/* 回答的编辑/删除同样由后端 canEdit 决定，前端不比较角色 */}
          {answer.canEdit && !editing && <button onClick={() => setEditing(true)}>编辑回答</button>}
          {answer.canEdit && (
            <button className="danger" onClick={() => {
              if (!confirm('确定删除这条回答吗？')) return;
              void onAct(() => api.deleteAnswer(answer.id));
            }}>删除回答</button>
          )}
        </div>

        <CommentList comments={answer.comments} targetType="ANSWER" targetId={answer.id} onDone={onReload} canComment={canVote} />
      </div>
    </div>
  );
}

function CommentList({ comments, targetType, targetId, onDone, canComment }: {
  comments: { id: number; author: { displayName: string }; body: string; createdAt: string }[];
  targetType: 'QUESTION' | 'ANSWER';
  targetId: number;
  onDone: () => void;
  canComment: boolean;
}) {
  const [text, setText] = useState('');
  const [open, setOpen] = useState(false);
  const [error, setError] = useState('');

  const submit = async () => {
    try {
      await api.comment(targetType, targetId, text);
      setText('');
      setOpen(false);
      setError('');
      onDone();
    } catch (e) {
      setError(errorMessage(e));
    }
  };

  return (
    <div className="comments">
      {comments.map((c) => (
        <div className="comment" key={c.id}>
          <span>{highlightMentions(c.body)}</span>
          <span className="muted"> — {c.author.displayName}</span>
        </div>
      ))}
      {error && <p className="error">{error}</p>}
      {canComment && (open ? (
        <div className="comment-form">
          <input value={text} onChange={(e) => setText(e.target.value)} placeholder="评论，可用 @昵称 提醒他人" />
          <button onClick={submit}>发表</button>
        </div>
      ) : <button className="link" onClick={() => setOpen(true)}>添加评论</button>)}
    </div>
  );
}

/** 评论里的 @昵称 高亮，和后端 MentionParser 的规则保持一致（@ 后 2–20 个非空白字符）。 */
function highlightMentions(text: string) {
  const parts = text.split(/(@[^\s@，。,.!?！？:：;；、]{2,20})/g);
  return parts.map((part, index) =>
    part.startsWith('@')
      ? <mark className="mention" key={index}>{part}</mark>
      : <span key={index}>{part}</span>,
  );
}
