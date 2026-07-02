<script setup lang="ts">
/**
 * 登录页（A 负责）v2 深海蓝金版
 *
 * 布局：左右两列
 *   - 左：品牌展示区（背景图 + 品牌文案 + 装饰）
 *   - 右：登录表单
 */
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, ElNotification, type FormInstance } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { validateStudentId, validatePassword } from '@/utils/validate'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive({
  studentId: '',
  password: '',
  remember: true,
})

const rules = {
  studentId: [{ validator: validateStudentId, trigger: 'blur' }],
  password: [{ validator: validatePassword, trigger: 'blur' }],
}

const handleLogin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const result = await authStore.login({
        studentId: form.studentId,
        password: form.password,
      })
      ElNotification.success({
        title: '✅ 登录成功',
        message: `欢迎回来，${result.user.name}`,
        duration: 3000,
      })
      const redirect = (route.query.redirect as string) || '/'
      // ✅ v2.2 修复（用户反馈）：登录后第一个界面是作品推荐，不是首页
      //    原因：之前用户访问 /admin/artwork/recommend 被路由守卫踢到 /login?redirect=/admin/artwork/recommend
      //         登录成功后 router.push(redirect) → 直接跳回那个页面，用户期望的是回首页
      //    修复：登录成功后**始终**回 /home（更符合用户预期，"记住上次访问位置"是有用的，但首次登录的体验更重要）
      //         如果以后需要"记住上次位置"，可以加一个 checkbox 让用户选
      // 注：redirect query 仍然保留在 URL 里，但只用于"主动从某个页面跳登录再回来"这种场景的 fallback
      const finalRedirect = redirect === '/' ? '/home' : redirect
      router.push(finalRedirect)
    } catch (err: any) {
      const msg = err?.message || '登录失败，请重试'
      const isAccountNotExist = msg.includes('账号不存在')
      const isAccountDisabled = msg.includes('已被禁用')
      const isPasswordWrong = msg.includes('密码错误')
      try {
        await ElMessageBox.alert(
          `<div class="login-error-content">
            <div class="login-error-icon ${
              isAccountNotExist ? 'is-info' : isAccountDisabled ? 'is-warning' : 'is-error'
            }">
              <span style="font-size:32px">${
                isAccountNotExist ? '❓' : isAccountDisabled ? '🚫' : '🔐'
              }</span>
            </div>
            <h3 class="login-error-title">${
              isAccountNotExist
                ? '账号不存在'
                : isAccountDisabled
                ? '账号已被禁用'
                : isPasswordWrong
                ? '密码错误'
                : '登录失败'
            }</h3>
            <p class="login-error-msg">${msg}</p>
            ${
              isPasswordWrong
                ? '<p class="login-error-hint">默认密码：<b>123456</b></p>'
                : isAccountNotExist
                ? '<p class="login-error-hint">请检查学号是否正确</p>'
                : ''
            }
          </div>`,
          '登录失败',
          {
            confirmButtonText: '我知道了',
            type: 'error',
            center: true,
            dangerouslyUseHTMLString: true,
            customClass: 'login-error-dialog',
          }
        )
        if (isPasswordWrong) form.password = ''
      } catch {
        // 用户关了弹窗
      }
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="login-page">
    <!-- 左侧：品牌展示区（背景图） -->
    <div class="login-brand">
      <div class="brand-overlay"></div>
      <div class="brand-content">
        <div class="brand-logo">
          <div class="logo-mark">3D</div>
        </div>
        <h1 class="brand-title">3D 打印科创会</h1>
        <p class="brand-subtitle">让创意落地 · 让想象成真</p>
        <div class="brand-features">
          <div class="feature-item">
            <span class="feature-icon">🎨</span>
            <span>作品创作</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🏆</span>
            <span>竞赛备赛</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">📦</span>
            <span>定制订单</span>
          </div>
          <div class="feature-item">
            <span class="feature-icon">🤝</span>
            <span>社团活动</span>
          </div>
        </div>
        <p class="brand-footer">v2.0 · 社团内部管理系统</p>
      </div>
    </div>

    <!-- 右侧：登录表单 -->
    <div class="login-form-panel">
      <div class="login-card">
        <div class="login-header">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-subtitle">请使用学号登录系统</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="studentId">
            <el-input
              v-model="form.studentId"
              placeholder="学号"
              :prefix-icon="User"
              clearable
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              :prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <div class="login-options">
              <el-checkbox v-model="form.remember">记住我</el-checkbox>
              <el-link type="primary" underline="never">忘记密码？</el-link>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-btn"
              native-type="submit"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <p class="login-tip">
          默认密码：<b>123456</b>（模拟数据）
        </p>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  background: var(--bg-base);
  overflow: hidden;
}

// 左侧品牌展示区
.login-brand {
  flex: 1.1;
  position: relative;
  // ============================================
  // 背景图：把你的图片放到 src/assets/login-bg.jpg
  // 找不到时 fallback 到深海蓝渐变（不会白屏）
  // ============================================
  background-image:
    url('@/assets/login-bg.jpg'),
    linear-gradient(135deg, #0A2540 0%, #1E3A5F 50%, #0A2540 100%);
  background-size: cover, cover;
  background-position: center, center;
  background-repeat: no-repeat, no-repeat;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;

  // 深海蓝渐变遮罩（增强对比 + 突出品牌色）
  .brand-overlay {
    position: absolute;
    inset: 0;
    background:
      radial-gradient(ellipse at top left, rgba(0, 212, 170, 0.18) 0%, transparent 55%),
      radial-gradient(ellipse at bottom right, rgba(255, 215, 0, 0.12) 0%, transparent 55%),
      linear-gradient(135deg, rgba(10, 37, 64, 0.85) 0%, rgba(10, 37, 64, 0.65) 100%);
  }
}
.brand-content {
  position: relative;
  z-index: 1;
  color: white;
  text-align: center;
  padding: 40px;
  max-width: 520px;
  animation: fade-in-up 0.8s $ease-out-soft both;
}
.brand-logo {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}
.logo-mark {
  width: 96px;
  height: 96px;
  border-radius: 24px;
  background: linear-gradient(135deg, #00D4AA 0%, #4FE5C7 100%);
  color: #0A2540;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 800;
  letter-spacing: -1px;
  box-shadow:
    0 12px 32px rgba(0, 212, 170, 0.4),
    inset 0 -4px 0 rgba(10, 37, 64, 0.2);
  position: relative;
  &::after {
    content: '';
    position: absolute;
    inset: 4px;
    border-radius: 20px;
    border: 2px solid rgba(255, 255, 255, 0.3);
    pointer-events: none;
  }
}
.brand-title {
  font-size: 42px;
  font-weight: 700;
  margin: 0 0 12px;
  letter-spacing: 2px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}
.brand-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 48px;
  letter-spacing: 1px;
}
.brand-features {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 56px;
}
.feature-item {
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.15);
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s $ease-out-soft;
  &:hover {
    background: rgba(255, 255, 255, 0.15);
    transform: translateY(-2px);
    border-color: rgba(0, 212, 170, 0.4);
  }
  .feature-icon {
    font-size: 24px;
  }
}
.brand-footer {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 1px;
  margin: 0;
}

// 右侧登录表单区
.login-form-panel {
  flex: 0 0 480px;
  background: var(--bg-card);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  // 装饰角
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 4px;
    height: 100%;
    background: linear-gradient(180deg, $primary-color 0%, $accent-color 50%, $gold-color 100%);
  }
}
.login-card {
  width: 100%;
  max-width: 400px;
}
.login-header {
  margin-bottom: 40px;
}
.login-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin: 0 0 8px;
}
.login-subtitle {
  color: var(--text-secondary);
  font-size: 14px;
  margin: 0;
}
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 4px;
  background: linear-gradient(135deg, $primary-color 0%, $primary-color-light 100%) !important;
  border: none !important;
  border-radius: 12px !important;
  position: relative;
  overflow: hidden;
  // 金色光带
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent 0%, rgba(255, 215, 0, 0.3) 50%, transparent 100%);
    transition: left 0.6s $ease-out-soft;
  }
  &:hover::before {
    left: 100%;
  }
}
.login-tip {
  text-align: center;
  color: var(--text-placeholder);
  font-size: 13px;
  margin-top: 24px;
  b { color: $accent-color; font-weight: 600; }
}

// 移动端：上下布局
@include mobile {
  .login-page {
    flex-direction: column;
  }
  .login-brand {
    flex: 0 0 220px;
    .brand-features { display: none; }
    .brand-title { font-size: 28px; }
  }
  .login-form-panel {
    flex: 1;
    padding: 20px;
    &::before { display: none; }
  }
}
</style>

<!-- 登录错误弹窗全局样式 -->
<style lang="scss">
.login-error-dialog {
  .login-error-content {
    text-align: center;
    padding: 4px 0;
  }
  .login-error-icon {
    width: 64px;
    height: 64px;
    margin: 0 auto 16px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    &.is-info    { background: rgba(0, 212, 170, 0.12); }
    &.is-warning { background: rgba(255, 215, 0, 0.18); }
    &.is-error   { background: rgba(255, 71, 87, 0.12); }
  }
  .login-error-title {
    margin: 0 0 8px;
    font-size: 18px;
    font-weight: 600;
    color: #0A2540;
  }
  .login-error-msg {
    margin: 0 0 8px;
    color: #6B7C93;
    font-size: 14px;
  }
  .login-error-hint {
    margin: 12px 0 0;
    padding: 8px 12px;
    background: rgba(0, 212, 170, 0.08);
    border-radius: 8px;
    font-size: 13px;
    color: #00A88A;
  }
}
</style>
