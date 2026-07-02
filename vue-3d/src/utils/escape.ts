/**
 * HTML 转义工具（v2.13）
 *
 * 修复 3 个 P0 XSS：
 *   1. project/detail 的"完成项目"/"取消项目"/"移除成员"确认弹窗
 *   2. login 的错误消息弹窗
 *   3. task/apply 的成功弹窗（onclick 字符串已改 @click，但消息体也有 HTML）
 *
 * 之前都用 dangerouslyUseHTMLString: true + 字符串拼接 ${userInput}，
 * 任何 user-controlled 字段（项目名 / 任务标题 / 错误消息）都直接拼到 HTML 里。
 *
 * 修法：
 *   - 所有 dangerouslyUseHTMLString 都用 escapeHtml() 转义 user-controlled 字段
 *   - 或改用 Vue 模板插槽（Element Plus ElMessageBox 支持 message 作为函数）
 *
 * 注意：escapeHtml 只防 attribute/text context 注入；
 * 复杂的 HTML 拼接建议改用 Vue 插槽（更安全也更可维护）。
 */

/**
 * 转义 HTML 特殊字符，用于在 dangerouslyUseHTMLString 弹窗中嵌入用户输入
 */
export const escapeHtml = (s: unknown): string => {
  if (s === null || s === undefined) return ''
  return String(s).replace(/[&<>"']/g, (c) => {
    const map: Record<string, string> = {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#39;',
    }
    return map[c] ?? c
  })
}
