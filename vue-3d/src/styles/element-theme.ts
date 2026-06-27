/**
 * Element Plus 主题色覆盖（v2 深海蓝金）
 *
 * Element Plus 2.x 用 CSS 变量驱动主题（--el-color-primary 等）
 * 这里只覆盖主色 + 辅色，亮/暗变体由 EP 自动计算（mix）
 * 如果需要完全控制，可以改 SCSS 变量 + 改 vite.config.ts
 *
 * 颜色方案（与 variables.scss 同步）：
 * - primary: #0A2540 (深海蓝 — 品牌主色)
 * - success: #00D4AA (薄荷青 — 成功/链接/按钮)
 * - warning: #FFD700 (金色 — 警告/重要数据)
 * - danger:  #FF4757 (红 — 危险/删除)
 * - info:    #6B7C93 (蓝灰 — 信息)
 */
const THEME_COLORS = {
  primary: '#0A2540',
  success: '#00D4AA',
  warning: '#FFD700',
  danger:  '#FF4757',
  info:    '#6B7C93',
} as const

export function setElementPlusTheme() {
  if (typeof document === 'undefined') return
  const style = document.createElement('style')
  style.id = 'ep-theme-override'
  // 生成 9 个 light 变体（primary + 8/9/7/5/4/3/2/1）和 3 个 dark 变体（7/8/9）
  let css = ':root {\n'
  for (const [name, color] of Object.entries(THEME_COLORS)) {
    const lightShades = generateLightShades(color)
    const darkShades = generateDarkShades(color)
    for (const [shade, value] of Object.entries(lightShades)) {
      css += `  --el-color-${name}-light-${shade}: ${value};\n`
    }
    for (const [shade, value] of Object.entries(darkShades)) {
      css += `  --el-color-${name}-dark-${shade}: ${value};\n`
    }
    css += `  --el-color-${name}: ${color};\n`
  }
  css += '}\n'
  style.textContent = css
  document.head.appendChild(style)
}

/**
 * 生成亮色变体（与白色混合 1~9 档）
 * 1 = 80%白（最浅），9 = 原色（最深）
 */
function generateLightShades(color: string): Record<number, string> {
  return {
    1: colorMix(color, '#ffffff', 0.9),
    2: colorMix(color, '#ffffff', 0.8),
    3: colorMix(color, '#ffffff', 0.65),
    4: colorMix(color, '#ffffff', 0.5),
    5: colorMix(color, '#ffffff', 0.35),
    6: colorMix(color, '#ffffff', 0.2),
    7: colorMix(color, '#ffffff', 0.1),
    8: colorMix(color, '#000000', 0.1),
    9: color,
  }
}

/**
 * 生成暗色变体（与黑色混合 2~4 档）
 */
function generateDarkShades(color: string): Record<number, string> {
  return {
    2: colorMix(color, '#000000', 0.2),
  }
}

/**
 * 用临时 DOM 元素 + 浏览器原生 color-mix 计算混合色
 * （fallback：用纯算式）
 */
function colorMix(c1: string, c2: string, weight: number): string {
  const rgb1 = hexToRgb(c1)
  const rgb2 = hexToRgb(c2)
  if (!rgb1 || !rgb2) return c1
  const r = Math.round(rgb1[0] * (1 - weight) + rgb2[0] * weight)
  const g = Math.round(rgb1[1] * (1 - weight) + rgb2[1] * weight)
  const b = Math.round(rgb1[2] * (1 - weight) + rgb2[2] * weight)
  return `#${[r, g, b].map((v) => v.toString(16).padStart(2, '0')).join('')}`
}

function hexToRgb(hex: string): [number, number, number] | null {
  const m = hex.replace('#', '').match(/^([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i)
  if (!m) return null
  return [parseInt(m[1]!, 16), parseInt(m[2]!, 16), parseInt(m[3]!, 16)]
}
