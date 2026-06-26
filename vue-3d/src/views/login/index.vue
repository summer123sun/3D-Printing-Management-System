<script setup lang="ts">
/**
 * 登录页（A 负责）
 */
import { reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance } from 'element-plus'
import { User } from '@element-plus/icons-vue'
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
      ElMessage.success(`欢迎回来，${result.user.name}`)
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    } catch (err) {
      // 错误已在 axios 拦截器里统一提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <el-avatar :size="64" class="brand-avatar">
          <el-icon :size="32"><User /></el-icon>
        </el-avatar>
        <h1 class="login-title">3D 打印科创会</h1>
        <p class="login-subtitle">社团内部管理系统</p>
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
            style="width: 100%"
            native-type="submit"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-tip">
        默认密码：123456（模拟数据）
      </p>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, $brand-color 0%, #42a5f5 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-medium;
}
.login-card {
  width: 100%;
  max-width: 420px;
  background: $bg-card;
  border-radius: $border-radius-large;
  padding: $spacing-xl $spacing-large;
  box-shadow: $shadow-light;
}
.login-header {
  text-align: center;
  margin-bottom: $spacing-large;
}
.brand-avatar {
  background: $brand-color-light;
  color: $brand-color;
  margin-bottom: $spacing-small;
}
.login-title {
  font-size: 24px;
  font-weight: 600;
  margin: $spacing-small 0 4px;
}
.login-subtitle {
  color: $text-secondary;
  font-size: $font-size-small;
}
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
.login-tip {
  text-align: center;
  color: $text-placeholder;
  font-size: $font-size-small;
  margin-top: $spacing-medium;
}
</style>