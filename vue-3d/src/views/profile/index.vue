<script setup lang="ts">
/**
 * 个人中心（A 负责）
 */
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
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

const handleChangePassword = () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的新密码不一致')
    return
  }
  authStore.changePassword({
    oldPassword: passwordForm.value.oldPassword,
    newPassword: passwordForm.value.newPassword,
  }).then(() => {
    ElMessage.success('密码修改成功')
    changePasswordDialogVisible.value = false
    passwordForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  }).catch(() => {})
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
              <el-tag>{{ authStore.user ? RoleText[authStore.user.role] : '-' }}</el-tag>
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
    <el-dialog v-model="changePasswordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="changePasswordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style lang="scss" scoped>
.profile-page {
  padding: 0;
}
.skill-tip {
  margin: $spacing-small 0 0;
  font-size: $font-size-small;
  color: $text-secondary;
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
  color: $text-secondary;
  margin: $spacing-mini 0 0;
}
</style>