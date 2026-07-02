<script setup lang="ts">
/**
 * 提交打印申请（**B** - 社员端）
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import HeroBanner from '@/components/member/HeroBanner.vue'
import MemberCard from '@/components/member/MemberCard.vue'
import StlUploader from '@/components/task/apply/StlUploader.vue'
import ParamForm from '@/components/task/apply/ParamForm.vue'
import { useTaskStore } from '@/stores/task'
import { useMemberStyle } from '@/composables/useMemberStyle'
import { Priority } from '@/types/task'
import type { TaskApplyDTO } from '@/types/task'

const router = useRouter()
const taskStore = useTaskStore()
const { isMember, isNewbie } = useMemberStyle()

const stlPath = ref<string>('')

const form = reactive<TaskApplyDTO>({
  title: '',
  modelName: '',
  stlFilePath: '',
  materialType: 'PLA',
  color: undefined,
  layerHeight: 0.2,
  infillRate: 20,
  needSupport: 0,
  priority: Priority.NORMAL,
  estWeight: undefined,
  estTime: undefined,
  projectId: undefined,
})

const submitting = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const handleSubmit = async () => {
  // ✅ 所有校验失败都用 alert 弹窗（最显眼，绝不漏看）
  if (!stlPath.value) {
    await ElMessageBox.alert(
      '<div style="text-align:center;padding:8px 0">📁 请先上传 STL 文件再提交</div>',
      '提示',
      { confirmButtonText: '知道了', type: 'warning', center: true, dangerouslyUseHTMLString: true }
    )
    return
  }
  if (!form.title || !form.modelName) {
    await ElMessageBox.alert(
      '<div style="text-align:center;padding:8px 0">📝 请填写任务标题和模型名称</div>',
      '提示',
      { confirmButtonText: '知道了', type: 'warning', center: true, dangerouslyUseHTMLString: true }
    )
    return
  }

  submitting.value = true
  try {
    form.stlFilePath = stlPath.value
    const taskId = await taskStore.apply(form)
    // ✅ 成功后：先弹醒目的居中弹窗 + 右上角通知（双保险，避免漏看）
    ElNotification.success({
      title: '✅ 提交成功',
      message: `任务 ${taskId} 已提交，等待审批`,
      duration: 4000,
      type: 'success',
    })

    // 5 秒倒计时自动跳转
    let secondsLeft = 5
    countdownTimer = setInterval(() => {
      secondsLeft--
      const el = document.getElementById('countdown-text')
      if (el) {
        if (secondsLeft > 0) {
          el.textContent = `${secondsLeft} 秒后自动跳转到任务详情…`
        } else {
          el.textContent = '正在跳转…'
        }
      }
      if (secondsLeft <= 0) {
        if (countdownTimer) clearInterval(countdownTimer)
        // 关闭弹窗（用 ESC 关闭 trigger catch 分支）+ 跳转
        ElMessageBox.close()  // 关闭当前弹窗
        router.push(`/task/${taskId}`)
      }
    }, 1000)

    try {
      await ElMessageBox.alert(
        `<div class="apply-success-content">
          <div class="success-icon-wrap">
            <div class="success-icon-circle">
              <svg viewBox="0 0 52 52" class="success-icon-svg">
                <circle class="success-icon-circle-bg" cx="26" cy="26" r="25" fill="none"/>
                <path class="success-icon-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
              </svg>
            </div>
          </div>
          <h2 class="success-title">任务已成功提交！</h2>
          <p class="success-subtitle">提交时间：${new Date().toLocaleString('zh-CN', { hour12: false })}</p>

          <div class="success-taskid-card">
            <div class="success-taskid-label">任务编号</div>
            <div class="success-taskid-value" onclick="navigator.clipboard.writeText('${taskId}'); this.classList.add('copied'); setTimeout(()=>this.classList.remove('copied'), 1200)" title="点击复制">
              ${taskId}
              <span class="success-taskid-copy">📋 点击复制</span>
            </div>
          </div>

          <div class="success-next-steps">
            <div class="success-step">
              <div class="success-step-num">1</div>
              <div class="success-step-text">
                <div class="success-step-title">技术骨干审批</div>
                <div class="success-step-desc">预计 <b>1-2 天</b> 内完成</div>
              </div>
            </div>
            <div class="success-step">
              <div class="success-step-num">2</div>
              <div class="success-step-text">
                <div class="success-step-title">进入打印队列</div>
                <div class="success-step-desc">审批通过后自动排队</div>
              </div>
            </div>
            <div class="success-step">
              <div class="success-step-num">3</div>
              <div class="success-step-text">
                <div class="success-step-title">打印完成取件</div>
                <div class="success-step-desc">会通知你取件签到</div>
              </div>
            </div>
          </div>

          <div class="success-countdown">
            <span id="countdown-text">5 秒后自动跳转到任务详情…</span>
          </div>
        </div>`,
        '',
        {
          confirmButtonText: '📄 立即查看详情',
          cancelButtonText: '继续提交流程',
          showCancelButton: true,
          type: 'success',
          center: true,
          dangerouslyUseHTMLString: true,
          customClass: 'apply-success-dialog',
          showClose: false,
          closeOnClickModal: false,
          closeOnPressEscape: false,
        }
      )
      // 用户点了「立即查看详情」
      if (countdownTimer) clearInterval(countdownTimer)
      router.push(`/task/${taskId}`)
    } catch {
      // 用户点了「继续提交流程」或 X
      if (countdownTimer) clearInterval(countdownTimer)
    }
  } catch (e: unknown) {
    console.error('[提交] 失败：', e)
    const errMsg = e instanceof Error ? e.message : String(e)
    // ❌ 失败时弹错误弹窗
    try {
      await ElMessageBox.alert(
        `<div class="apply-error-content">
          <div class="error-icon-wrap">
            <div class="error-icon-circle">
              <svg viewBox="0 0 52 52" class="error-icon-svg">
                <circle class="error-icon-circle-bg" cx="26" cy="26" r="25" fill="none"/>
                <path class="error-icon-cross" fill="none" d="M16 16 L36 36 M36 16 L16 36"/>
              </svg>
            </div>
          </div>
          <h2 class="error-title">提交失败</h2>
          <p class="error-desc">${errMsg || '请检查网络或稍后再试'}</p>
        </div>`,
        '',
        {
          confirmButtonText: '知道了',
          type: 'error',
          center: true,
          dangerouslyUseHTMLString: true,
          customClass: 'apply-error-dialog',
        }
      )
    } catch {
      // 用户关掉了弹窗
    }
    ElNotification.error({
      title: '提交失败',
      message: errMsg || '请检查网络或稍后再试',
      duration: 5000,
    })
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="task-apply-page">
    <!-- 成员端 HeroBanner -->
    <HeroBanner
      v-if="isMember"
      title="提交打印任务"
      subtitle="把你的创意变成实体 · 上传 STL 文件，一键启动"
      illustration="cta-apply"
      :is-newbie="isNewbie"
      newbie-tip="不确定参数怎么填？默认的 PLA + 0.2mm 层厚 + 20% 填充率就够日常用了。"
    />

    <!-- 后台端 PageHeader -->
    <PageHeader v-else title="提交打印申请" :show-back="true" @back="router.back()" />

    <MemberCard v-if="isMember" padding="32px">
      <!-- 第一步：上传 STL -->
      <h3 class="step-title">① 上传 STL 文件</h3>
      <StlUploader v-model="stlPath" />

      <el-divider />

      <!-- 第二步：填写参数 -->
      <h3 class="step-title">② 填写打印参数</h3>
      <ParamForm v-model="form" />

      <el-divider />

      <!-- 提交 -->
      <div class="action-bar">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" size="large" round :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
      </div>
    </MemberCard>

    <el-card v-else>
      <!-- 第一步：上传 STL -->
      <h3 class="step-title">① 上传 STL 文件</h3>
      <StlUploader v-model="stlPath" />

      <el-divider />

      <!-- 第二步：填写参数 -->
      <h3 class="step-title">② 填写打印参数</h3>
      <ParamForm v-model="form" />

      <el-divider />

      <!-- 提交 -->
      <div class="action-bar">
        <el-button @click="router.back()">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          提交申请
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.task-apply-page {
  padding: 0;
}
.step-title {
  margin: 0 0 $spacing-medium;
  font-size: $font-size-large;
  font-weight: 500;
  color: $brand-color;
}
.action-bar {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-small;
}
</style>

<!-- 弹窗全局样式（非 scoped，因为 Element Plus 把 dialog 渲染在 body 末尾） -->
<style lang="scss">
/* ============ 提交成功弹窗 ============ */
.apply-success-dialog {
  .el-message-box__header {
    display: none;
  }
  .el-message-box__content {
    padding: 32px 32px 16px !important;
  }
  .el-message-box__btns {
    padding: 0 32px 28px !important;
    .el-button--primary {
      background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
      border: none;
      padding: 12px 28px;
      font-size: 15px;
      font-weight: 500;
      box-shadow: 0 4px 12px rgba(103, 194, 58, 0.3);
      transition: all 0.2s;
      &:hover {
        transform: translateY(-1px);
        box-shadow: 0 6px 16px rgba(103, 194, 58, 0.4);
      }
    }
  }
}

.apply-success-content {
  text-align: center;
  padding: 0;
}

/* 顶部对勾动画 */
.success-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.success-icon-circle {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(103, 194, 58, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}
.success-icon-svg {
  width: 40px;
  height: 40px;
  stroke: white;
  stroke-width: 4;
  stroke-linecap: round;
  stroke-linejoin: round;
  fill: none;
}
.success-icon-circle-bg {
  stroke: rgba(255, 255, 255, 0.4);
  stroke-width: 2;
  stroke-dasharray: 166;
  stroke-dashoffset: 166;
  animation: success-circle 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}
.success-icon-check {
  stroke-dasharray: 48;
  stroke-dashoffset: 48;
  animation: success-check 0.4s 0.5s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}
@keyframes success-circle {
  to { stroke-dashoffset: 0; }
}
@keyframes success-check {
  to { stroke-dashoffset: 0; }
}

/* 标题 */
.success-title {
  margin: 0 0 8px;
  font-size: 22px;
  font-weight: 600;
  color: #303133;
  animation: fadeInUp 0.5s 0.7s both;
}
.success-subtitle {
  margin: 0 0 20px;
  font-size: 13px;
  color: var(--text-secondary);
  animation: fadeInUp 0.5s 0.8s both;
}
@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 任务编号卡片 */
.success-taskid-card {
  background: linear-gradient(135deg, #f0f9eb 0%, #e1f3d8 100%);
  border: 1px dashed #b3e19d;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 20px;
  animation: fadeInUp 0.5s 0.9s both;
}
.success-taskid-label {
  font-size: 12px;
  color: #67c23a;
  font-weight: 500;
  letter-spacing: 1px;
  margin-bottom: 6px;
}
.success-taskid-value {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 22px;
  font-weight: 700;
  color: #303133;
  letter-spacing: 2px;
  cursor: pointer;
  user-select: all;
  transition: all 0.2s;
  display: inline-block;
  padding: 4px 8px;
  border-radius: 4px;
  &:hover {
    background: rgba(103, 194, 58, 0.1);
  }
  &.copied {
    background: #67c23a;
    color: white;
    transform: scale(1.05);
    .success-taskid-copy {
      color: rgba(255, 255, 255, 0.9);
    }
  }
}
.success-taskid-copy {
  display: block;
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', sans-serif;
  font-size: 11px;
  font-weight: normal;
  color: var(--text-secondary);
  margin-top: 4px;
  letter-spacing: 0;
}

/* 下一步 */
.success-next-steps {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-bottom: 16px;
  animation: fadeInUp 0.5s 1s both;
}
.success-step {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  background: var(--bg-base);
  border-radius: 8px;
  text-align: left;
}
.success-step-num {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  background: white;
  border: 2px solid #67c23a;
  color: #67c23a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}
.success-step-text {
  flex: 1;
}
.success-step-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 2px;
}
.success-step-desc {
  font-size: 12px;
  color: var(--text-secondary);
}

/* 倒计时 */
.success-countdown {
  font-size: 12px;
  color: #c0c4cc;
  margin-top: 8px;
  animation: fadeInUp 0.5s 1.1s both;
}

/* ============ 提交失败弹窗 ============ */
.apply-error-dialog {
  .el-message-box__header {
    display: none;
  }
  .el-message-box__content {
    padding: 32px !important;
  }
}
.apply-error-content {
  text-align: center;
}
.error-icon-wrap {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}
.error-icon-circle {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #f56c6c 0%, #e04545 100%);
  border-radius: 50%;
  box-shadow: 0 8px 24px rgba(245, 108, 108, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
}
.error-icon-svg {
  width: 40px;
  height: 40px;
  stroke: white;
  stroke-width: 4;
  stroke-linecap: round;
  fill: none;
}
.error-icon-cross {
  stroke-dasharray: 60;
  stroke-dashoffset: 60;
  animation: error-cross 0.4s cubic-bezier(0.65, 0, 0.45, 1) forwards;
}
@keyframes error-cross {
  to { stroke-dashoffset: 0; }
}
.error-title {
  margin: 0 0 12px;
  font-size: 20px;
  font-weight: 600;
  color: #f56c6c;
}
.error-desc {
  margin: 0;
  font-size: 14px;
  color: var(--text-regular);
  line-height: 1.6;
  word-break: break-all;
}
</style>