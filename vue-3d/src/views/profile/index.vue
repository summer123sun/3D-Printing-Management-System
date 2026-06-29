<script setup lang="ts">
/**
 * 个人中心（A 负责）
 */
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useAuthStore } from '@/stores/auth'
import { RoleText, SkillLevelText, type Member } from '@/types/member'
import { formatDate } from '@/utils/format'
import { getUserStats } from '@/api/user'

const authStore = useAuthStore()
const stats = ref({ totalPrints: 0, totalProjects: 0, totalArtworks: 0 })

onMounted(async () => {
  if (!authStore.user) {
    try {
      await authStore.fetchUserInfo()
    } catch {}
  }
  // 拉取个人统计数据
  if (authStore.user?.studentId) {
    try {
      const result = await getUserStats(authStore.user.studentId)
      stats.value = result as unknown as typeof stats.value
    } catch {
      // 统计接口失败不提示
    }
  }
})

const changePasswordDialogVisible = ref(false)
const passwordForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const submittingPassword = ref(false)

/** 密码强度：6-20 位 + 至少字母+数字 */
const passwordStrength = computed(() => {
  const pwd = passwordForm.value.newPassword
  if (!pwd) return { level: 0, label: '', color: '' }
  const hasLetter = /[a-zA-Z]/.test(pwd)
  const hasNumber = /\d/.test(pwd)
  const len = pwd.length
  let level = 0
  if (len >= 6) level++
  if (len >= 10) level++
  if (hasLetter && hasNumber) level++
  const labels = ['', '弱', '中', '强']
  const colors = ['', '#f56c6c', '#e6a23c', '#67c23a']
  return { level, label: labels[level], color: colors[level] }
})

const handleChangePassword = async () => {
  const { oldPassword, newPassword, confirmPassword } = passwordForm.value

  // 1. 基础非空
  if (!oldPassword || !newPassword || !confirmPassword) {
    ElNotification.warning('请填写完整的密码信息')
    return
  }
  // 2. 新旧密码相同
  if (oldPassword === newPassword) {
    try {
      await ElMessageBox.alert(
        `<div style="text-align:center;padding:8px 0">
          <p style="font-size:14px;margin:0 0 8px">⚠️ 新密码不能与旧密码相同</p>
          <p style="color:var(--text-secondary);font-size:13px;margin:0">请换一个不一样的密码</p>
        </div>`,
        '提示',
        { confirmButtonText: '我知道了', type: 'warning', center: true, dangerouslyUseHTMLString: true }
      )
    } catch {}
    return
  }
  // 3. 两次新密码不一致
  if (newPassword !== confirmPassword) {
    ElNotification.error('两次输入的新密码不一致')
    return
  }
  // 4. 密码强度
  if (newPassword.length < 6 || newPassword.length > 20) {
    ElNotification.warning('新密码长度需在 6-20 位之间')
    return
  }

  submittingPassword.value = true
  try {
    await authStore.changePassword({ oldPassword, newPassword })
    // ✅ 醒目的成功弹窗（不要用 3 秒消失的 ElMessage）
    ElNotification.success({
      title: '✅ 密码修改成功',
      message: '请记住新密码，下次登录时使用新密码',
      duration: 4000,
    })
    try {
      await ElMessageBox.alert(
        `<div class="change-pwd-success">
          <div class="success-icon-wrap">
            <div class="success-icon-circle">
              <svg viewBox="0 0 52 52" class="success-icon-svg">
                <circle class="success-icon-circle-bg" cx="26" cy="26" r="25" fill="none"/>
                <path class="success-icon-check" fill="none" d="M14.1 27.2l7.1 7.2 16.7-16.8"/>
              </svg>
            </div>
          </div>
          <h2 class="success-title">密码修改成功！</h2>
          <p class="success-subtitle">下次登录请使用新密码</p>
          <p style="color:var(--text-secondary);font-size:13px;margin:0">
            💡 建议：在密码管理器中保存新密码
          </p>
        </div>`,
        '',
        {
          confirmButtonText: '好的',
          type: 'success',
          center: true,
          dangerouslyUseHTMLString: true,
          showClose: false,
        }
      )
    } catch {}
    changePasswordDialogVisible.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  } catch (e: any) {
    // 错误已在 axios 拦截器提示过；这里只 console
    console.error('[修改密码] 失败：', e)
  } finally {
    submittingPassword.value = false
  }
}

const skillPercent = computed(() => {
  const level = authStore.user?.skillLevel ?? 0
  return (level / 4) * 100
})
</script>

<template>
  <div class="profile-page">
    <PageHeader title="个人中心" />

    <el-row :gutter="16">
      <!-- 左：基本信息 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header>
            <span>基本信息</span>
            <el-button text type="primary" style="float: right">编辑</el-button>
          </template>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="学号">
              {{ authStore.user?.studentId }}
            </el-descriptions-item>
            <el-descriptions-item label="姓名">
              {{ authStore.user?.name }}
            </el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag effect="dark">{{ authStore.user ? RoleText[authStore.user.role] : '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="技能等级">
              {{ authStore.user ? SkillLevelText[authStore.user.skillLevel] : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="入社日期">
              {{ authStore.user?.joinDate ? formatDate(authStore.user.joinDate, 'YYYY-MM-DD') : '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="手机号">
              {{ authStore.user?.phone || '-' }}
            </el-descriptions-item>
            <el-descriptions-item label="邮箱">
              {{ authStore.user?.email || '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>

      <!-- 右：技能 + 统计 -->
      <el-col :xs="24" :md="12">
        <el-card>
          <template #header><span>技能进度</span></template>
          <el-progress :percentage="skillPercent" :stroke-width="20" />
          <p class="skill-tip">
            当前等级：{{ authStore.user ? SkillLevelText[authStore.user.skillLevel] : '-' }}
          </p>
        </el-card>

        <el-card style="margin-top: 16px">
          <template #header><span>累计统计</span></template>
          <el-row :gutter="16">
            <el-col :span="8">
              <div class="stat-card">
                <p class="stat-num">{{ stats.totalPrints }}</p>
                <p class="stat-label">打印次数</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <p class="stat-num">{{ stats.totalProjects }}</p>
                <p class="stat-label">参与项目</p>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="stat-card">
                <p class="stat-num">{{ stats.totalArtworks }}</p>
                <p class="stat-label">作品数</p>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <el-card style="margin-top: 16px">
          <template #header><span>账号安全</span></template>
          <el-button type="primary" @click="changePasswordDialogVisible = true">
            修改密码
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <!-- 修改密码弹窗 -->
    <AppDialog v-model="changePasswordDialogVisible" title="修改密码" icon="Edit" type="primary" width="480px"
               confirm-text="确认修改" :loading="submittingPassword" @confirm="handleChangePassword">
      <el-form :model="passwordForm" label-width="90px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="6-20 位，建议字母+数字" />
          <div v-if="passwordForm.newPassword" class="pwd-strength">
            <span class="pwd-strength-label">强度：</span>
            <div class="pwd-strength-bar">
              <div
                v-for="i in 3"
                :key="i"
                class="pwd-strength-cell"
                :style="{
                  background: i <= passwordStrength.level ? passwordStrength.color : '#e4e7ed',
                }"
              />
            </div>
            <span class="pwd-strength-text" :style="{ color: passwordStrength.color }">
              {{ passwordStrength.label }}
            </span>
          </div>
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="再输入一次新密码"
          />
          <div
            v-if="passwordForm.confirmPassword && passwordForm.confirmPassword !== passwordForm.newPassword"
            class="pwd-mismatch"
          >
            ⚠️ 两次密码不一致
          </div>
        </el-form-item>
      </el-form>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.profile-page {
  padding: 0;
}
.skill-tip {
  margin: $spacing-small 0 0;
  font-size: $font-size-small;
  color: var(--text-secondary);
  text-align: center;
}
.stat-card {
  text-align: center;
  padding: $spacing-medium 0;
}
.stat-num {
  font-size: 28px;
  font-weight: 600;
  color: $brand-color;
  margin: 0;
}
.stat-label {
  font-size: $font-size-small;
  color: var(--text-secondary);
  margin-top: $spacing-mini 0 0;
}
.pwd-strength {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 6px;
  font-size: 12px;
}
.pwd-strength-label {
  color: var(--text-secondary);
}
.pwd-strength-bar {
  display: flex;
  gap: 3px;
  flex: 1;
  max-width: 120px;
}
.pwd-strength-cell {
  flex: 1;
  height: 4px;
  border-radius: 2px;
  transition: background 0.2s;
}
.pwd-strength-text {
  font-weight: 500;
  min-width: 20px;
  text-align: right;
}
.pwd-mismatch {
  margin-top: 4px;
  font-size: 12px;
  color: #f56c6c;
}
</style>

<!-- 弹窗全局样式 -->
<style lang="scss">
.change-pwd-success {
  text-align: center;
  padding: 4px 0;
  .success-icon-wrap {
    display: flex; justify-content: center; margin-bottom: 20px;
  }
  .success-icon-circle {
    width: 72px; height: 72px;
    background: linear-gradient(135deg, #67c23a 0%, #5daf34 100%);
    border-radius: 50%;
    box-shadow: 0 8px 24px rgba(103, 194, 58, 0.3);
    display: flex; align-items: center; justify-content: center;
  }
  .success-icon-svg {
    width: 40px; height: 40px;
    stroke: white; stroke-width: 4;
    stroke-linecap: round; stroke-linejoin: round;
    fill: none;
  }
  .success-icon-circle-bg {
    stroke: rgba(255, 255, 255, 0.4);
    stroke-width: 2;
    stroke-dasharray: 166;
    stroke-dashoffset: 166;
    animation: pwd-success-circle 0.6s cubic-bezier(0.65, 0, 0.45, 1) forwards;
  }
  .success-icon-check {
    stroke-dasharray: 48;
    stroke-dashoffset: 48;
    animation: pwd-success-check 0.4s 0.5s cubic-bezier(0.65, 0, 0.45, 1) forwards;
  }
  @keyframes pwd-success-circle { to { stroke-dashoffset: 0; } }
  @keyframes pwd-success-check { to { stroke-dashoffset: 0; } }
  .success-title {
    margin: 0 0 8px;
    font-size: 22px;
    font-weight: 600;
    color: #303133;
  }
  .success-subtitle {
    margin: 0 0 16px;
    font-size: 14px;
    color: var(--text-regular);
  }
}
</style>