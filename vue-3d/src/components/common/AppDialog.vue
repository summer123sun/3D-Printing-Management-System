<script setup lang="ts">
/**
 * 通用对话框 AppDialog（v2 紫系配色）
 *
 * 统一全站所有 el-dialog 样式：
 * - 圆角 16px + 大阴影
 * - 顶部彩色渐变标题栏（primary 紫 / success 翠绿 / warning 琥珀 / danger 红 / info 灰）
 * - 居中显示
 * - 弹窗进入/退出动画（scale + fade + 弹性回弹）
 * - 统一的 footer 按钮风格（带 loading + 主题色按钮 + hover 上浮）
 *
 * 用法：
 *   <AppDialog v-model="visible" title="分配打印机" icon="Printer" type="primary"
 *              @confirm="handleConfirm" :loading="submitting">
 *     <el-form>...</el-form>
 *   </AppDialog>
 */
import { computed } from 'vue'
import {
  Printer, Check, Warning, CircleClose, InfoFilled, Delete,
  User, Edit, Bell, Star,
} from '@element-plus/icons-vue'

type DialogType = 'primary' | 'success' | 'warning' | 'danger' | 'info'

interface Props {
  modelValue: boolean
  title: string
  icon?: string
  type?: DialogType
  width?: string
  confirmText?: string
  cancelText?: string
  loading?: boolean
  showFooter?: boolean
  beforeClose?: () => boolean | Promise<boolean>
  closeOnClickModal?: boolean
  closeOnPressEscape?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  icon: '',
  type: 'primary',
  width: '520px',
  confirmText: '确认',
  cancelText: '取消',
  loading: false,
  showFooter: true,
  beforeClose: undefined,
  closeOnClickModal: false,
  closeOnPressEscape: true,
})

const emit = defineEmits<{
  'update:modelValue': [val: boolean]
  'confirm': []
  'cancel': []
}>()

const iconMap: Record<string, any> = {
  Printer, Check, Warning, CircleClose, InfoFilled, Delete,
  User, Edit, Bell, Star,
  default: InfoFilled,
}

const iconComponent = computed(() => {
  if (!props.icon) return null
  return iconMap[props.icon] || iconMap.default
})

// v2 主题色（深海蓝金 — 与 variables.scss 同步）
const TYPE_COLORS: Record<DialogType, string> = {
  primary: '#0A2540',  // 深海蓝（主色：品牌权威感）
  success: '#00D4AA',  // 薄荷青（成功）
  warning: '#FFD700',  // 金色（警告 / 重要）
  danger:  '#FF4757',  // 红（危险）
  info:    '#6B7C93',  // 蓝灰
}

const dialogAccent = computed(() => TYPE_COLORS[props.type] || TYPE_COLORS.primary)

const handleClose = async () => {
  if (props.beforeClose) {
    const ok = await props.beforeClose()
    if (!ok) return
  }
  emit('update:modelValue', false)
  emit('cancel')
}

const handleConfirm = () => {
  emit('confirm')
}

const handleUpdate = (val: boolean) => {
  if (!val) handleClose()
  else emit('update:modelValue', val)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="title"
    :width="width"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    :show-close="false"
    center
    align-center
    class="app-dialog"
    :style="{ '--dialog-accent': dialogAccent }"
    @update:model-value="handleUpdate"
    @close="handleClose"
  >
    <template #header>
      <div class="app-dialog-header">
        <div class="app-dialog-icon">
          <el-icon :size="18"><component :is="iconComponent || InfoFilled" /></el-icon>
        </div>
        <span class="app-dialog-title">{{ title }}</span>
      </div>
    </template>

    <!-- 关闭键：绝对定位到弹窗真正的右上角（不嵌在标题栏里） -->
    <button class="app-dialog-close" @click="handleClose" aria-label="关闭">
      <el-icon :size="18"><CircleClose /></el-icon>
    </button>

    <div class="app-dialog-body">
      <slot />
    </div>

    <template v-if="showFooter" #footer>
      <div class="app-dialog-footer">
        <el-button class="app-btn-cancel" :disabled="loading" @click="handleClose">
          {{ cancelText }}
        </el-button>
        <el-button
          class="app-btn-confirm"
          :type="type === 'primary' ? 'primary' : type"
          :loading="loading"
          @click="handleConfirm"
        >
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.app-dialog-body {
  padding: 4px 0 8px;
  background: #ffffff;
}
.app-dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  .app-btn-cancel {
    padding: 10px 20px;
    border-radius: 10px;
    font-weight: 500;
    transition: all 0.2s ease;
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
    }
  }
  .app-btn-confirm {
    padding: 10px 24px;
    min-width: 88px;
    border-radius: 10px;
    font-weight: 500;
    transition: all 0.2s ease;
    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 14px color-mix(in srgb, var(--dialog-accent, #7c3aed) 30%, transparent);
    }
  }
}
</style>

<!-- 弹窗全局样式（必须非 scoped 才能覆盖 el-dialog） -->
<style lang="scss">
.app-dialog {
  // 弹窗本体：圆角 + 阴影 + 纯白底 + 相对定位（关闭键 absolute 用）
  .el-dialog {
    border-radius: 16px !important;
    overflow: visible !important;  // 关闭键要浮在弹窗外右上角
    background: #ffffff !important;
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15), 0 4px 12px rgba(0, 0, 0, 0.08) !important;
    position: relative !important;
    // 兜底最小宽度：EP 2.x 的 el-dialog 默认 min-width 600px，但当内部用
    // <el-form label-width="100px"> 的 flexbox 布局时，可能把弹窗压成 ~300px
    // （用户报修：维护记录弹窗塌陷）。这里强制 min-width 防止塌陷
    min-width: 540px !important;
    // 显式居中兜底（用户报修：弹窗没在屏幕居中）
    // EP 2.x 的居中靠 .el-overlay-dialog flexbox 实现，但若 .el-overlay-dialog
    // 被全局样式污染（display/justify-content 失效），弹窗会贴着顶部/左边
    // 这里用 position: fixed + transform 兜底居中
    position: fixed !important;
    top: 50% !important;
    left: 50% !important;
    right: auto !important;
    bottom: auto !important;
    transform: translate(-50%, -50%) !important;
    margin: 0 !important;
  }
  .el-dialog__body {
    overflow: visible !important;  // 防止关闭键被裁
  }
  // 遮罩：半透明黑（不磨砂）
  .el-dialog__wrapper {
    background-color: rgba(0, 0, 0, 0.5) !important;
  }

  // 隐藏 Element Plus 默认 header
  // ❌ 不要 height: 0 —— 会让 .app-dialog-header 的 56px 内容溢出遮挡 body
  // ✅ 只清 padding/margin/border，让 header flex item 自动撑开
  .el-dialog__header {
    padding: 0 !important;
    margin: 0 !important;
    border-bottom: none !important;
  }

  // 自定义 header（紫系渐变标题栏）
  .app-dialog-header {
    display: flex;
    align-items: center;
    gap: 12px;
    height: 56px;
    padding: 0 20px;
    background: linear-gradient(135deg,
      var(--dialog-accent, #7c3aed) 0%,
      color-mix(in srgb, var(--dialog-accent, #7c3aed) 75%, white 25%) 100%);
    color: white;
    margin: 0;
    border-top-left-radius: 16px;
    border-top-right-radius: 16px;
    position: relative;
  }
  .app-dialog-icon {
    width: 32px;
    height: 32px;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
  }
  .app-dialog-title {
    flex: 1;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }

  // 关闭键：绝对定位浮在弹窗真正的右上角（不受标题栏高度限制）
  .app-dialog-close {
    position: absolute !important;
    top: 12px;
    right: 12px;
    z-index: 10;
    width: 32px;
    height: 32px;
    background: rgba(255, 255, 255, 0.92);
    border: 1px solid rgba(10, 37, 64, 0.12);
    border-radius: 50%;  // 圆形（更精致）
    color: #0A2540;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 2px 8px rgba(10, 37, 64, 0.12);
    transition: all 0.25s cubic-bezier(0.34, 1.56, 0.64, 1);
    &:hover {
      background: #FF4757;
      border-color: #FF4757;
      color: #fff;
      transform: rotate(90deg) scale(1.1);
      box-shadow: 0 4px 12px rgba(255, 71, 87, 0.4);
    }
  }

  // body（不强制居中，slot 自己用 .result-content 之类的容器控制对齐）
  .el-dialog__body {
    padding: 28px 28px 20px !important;
    background: #ffffff !important;
    color: #303133;
  }

  // footer
  .el-dialog__footer {
    padding: 12px 28px 20px !important;
    border-top: 1px solid #f0f2f5;
    background: #fafbfc !important;
    border-bottom-left-radius: 16px;
    border-bottom-right-radius: 16px;
  }
}

// 弹窗进入/退出动画（覆盖 EP 默认的）
.app-dialog-dialog-fade-enter-active {
  animation: dialog-pop-in 0.32s cubic-bezier(0.34, 1.56, 0.64, 1) !important;
}
.app-dialog-dialog-fade-leave-active {
  animation: dialog-pop-out 0.2s cubic-bezier(0.65, 0, 0.35, 1) !important;
}
@keyframes dialog-pop-in {
  0% { opacity: 0; transform: scale(0.85) translateY(-10px); }
  100% { opacity: 1; transform: scale(1) translateY(0); }
}
@keyframes dialog-pop-out {
  0% { opacity: 1; transform: scale(1); }
  100% { opacity: 0; transform: scale(0.95); }
}

// 遮罩淡入淡出
.app-dialog-fade-enter-active,
.app-dialog-fade-leave-active {
  transition: opacity 0.25s ease !important;
}
.app-dialog-fade-enter-from,
.app-dialog-fade-leave-to {
  opacity: 0;
}
</style>
