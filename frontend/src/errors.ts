import { ApiError } from './api';

/**
 * 业务错误码 → 中文提示。
 *
 * 原则：前端只做「码 → 文案」的映射，不判断角色、不猜测原因；
 * 码由后端 Problem Details 的 code 字段给出（见 docs/错误码.md）。
 */
const BY_CODE: Record<string, string> = {
  // Q&A
  'QA-1001': '标题长度不合法（10–150 字）',
  'QA-1002': '正文内容不合法（至少 20 字）',
  'QA-1004': '标签不能为空或格式不正确',
  'QA-1005': '每个问题需要 1–5 个标签',
  'QA-1015': '不能给自己的内容投票',
  'QA-1020': '该问题已关闭，不能再进行操作',
  'QA-2002': '该问题已经采纳过答案，不能重复采纳',
  'QA-3001': '只有提问者或课程教师/助教可以采纳回答',
  'QA-4001': '问题不存在或已被删除',

  // Identity
  'ID-1004': '密码需为 8–64 位且同时包含字母与数字',
  'ID-2001': '学号/工号、昵称或邮箱已被使用',
  'ID-2005': '你已经加入过这门课程',
  'ID-3001': '只有管理员可以执行该操作',
  'ID-3002': '只有教师或管理员可以创建课程',
  'ID-4002': '课程不存在或已关闭',

  // Reputation
  'REP-1002': '可用悬赏积分不足',
  'REP-1003': '悬赏积分需在 10–500 之间',
  'REP-1004': '悬赏期限需在 1–14 天之间',
  'REP-3002': '只能查看自己的声誉流水',

  // Discovery
  'DIS-1001': '每个关键词至少 2 个字符',
  'DIS-3001': '只能标记自己的通知为已读',
  'DIS-4001': '通知不存在',

  // 认证与平台
  'AUTH-401': '登录已过期，请重新登录',
  'AUTH-403-CSRF': '安全校验失败，请刷新页面后重试',
  'AUTH-429': '操作太频繁，请稍后再试',
  'SYS-409-CONCURRENT': '内容刚刚被其他人修改，请刷新后重试',
  'SYS-409-DUPLICATE': '重复提交，请勿连点',
};

/** 后端没给 code（或 code 未收录）时，退回到按 HTTP 状态给一句人话。 */
const BY_STATUS: Record<number, string> = {
  400: '请求参数有误，请检查后重试',
  401: '登录已过期，请重新登录',
  403: '你没有权限执行这个操作',
  404: '内容不存在或已被删除',
  409: '操作冲突，请刷新后重试',
  422: '操作不符合业务规则',
  429: '操作太频繁，请稍后再试',
  500: '服务器开小差了，请稍后重试',
};

export function errorMessage(e: unknown): string {
  if (e instanceof ApiError) {
    return BY_CODE[e.code] ?? BY_STATUS[e.status] ?? e.message;
  }
  if (e instanceof Error) {
    return e.message || '网络异常，请检查网络后重试';
  }
  return '未知错误，请稍后重试';
}

/** 页面级状态判定：区分「不存在」与「无权限」，好让界面给出不同出口。 */
export function errorKind(e: unknown): 'notfound' | 'forbidden' | 'other' {
  if (e instanceof ApiError) {
    if (e.status === 404) return 'notfound';
    if (e.status === 403) return 'forbidden';
  }
  return 'other';
}
