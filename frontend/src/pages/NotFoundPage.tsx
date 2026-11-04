import { Link } from 'react-router-dom';

/** 路由兜底页：地址写错或内容被删除时给出明确出口，不留白屏。 */
export function NotFoundPage() {
  return (
    <div className="state-page">
      <h1>页面不存在</h1>
      <p className="muted">链接可能已经失效，或者内容被作者删除。</p>
      <div className="actions">
        <Link className="primary" to="/">返回问题列表</Link>
        <Link to="/courses">去看看课程</Link>
      </div>
    </div>
  );
}
