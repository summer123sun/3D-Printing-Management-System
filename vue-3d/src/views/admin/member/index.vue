<script setup lang="ts">
/**
 * 成员管理（v2 深海蓝金版）
 *
 * 功能：
 * - 成员列表（分页 + 搜索）
 * - 改角色（仅社长）
 * - 改技能等级（社长+技术骨干）
 */
import { computed, onMounted, ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { memberList, updateMemberRole, updateMemberSkill } from '@/api/user'
import { Role, RoleText, SkillLevel, SkillLevelText, type Member } from '@/types/member'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'

const authStore = useAuthStore()

const filter = ref({
  keyword: '',
  page: 1,
  size: 15,
})

const list = ref<Member[]>([])
const total = ref(0)
const loading = ref(false)

const isPresident = computed(() => authStore.user?.role === Role.PRESIDENT)
const isStaff = computed(() => {
  const r = authStore.user?.role
  return r === Role.PRESIDENT || r === Role.TECH_LEAD
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await memberList({
      page: filter.value.page,
      size: filter.value.size,
      keyword: filter.value.keyword.trim() || undefined,
    })
    list.value = res.list || []
    total.value = res.total
  } catch (e: any) {
    ElMessage.error(e?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const onSearch = () => {
  filter.value.page = 1
  fetchData()
}

const onPageChange = (page: number) => {
  filter.value.page = page
  fetchData()
}

// ============== 改角色 ==============
const roleDialog = ref({ visible: false, member: null as Member | null, newRole: Role.MEMBER, submitting: false })

const openRoleDialog = (m: any) => {
  roleDialog.value.member = m
  roleDialog.value.newRole = m.role
  roleDialog.value.visible = true
}

const handleUpdateRole = async () => {
  if (!roleDialog.value.member) return
  const m = roleDialog.value.member
  if (roleDialog.value.newRole === m.role) {
    ElMessage.info('角色未变化')
    roleDialog.value.visible = false
    return
  }
  roleDialog.value.submitting = true
  try {
    await updateMemberRole(m.studentId, roleDialog.value.newRole)
    ElNotification.success({
      title: '角色已更新',
      message: `${m.name} 现在是「${RoleText[roleDialog.value.newRole]}」`,
      duration: 2500,
    })
    roleDialog.value.visible = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '更新失败')
  } finally {
    roleDialog.value.submitting = false
  }
}

// ============== 改技能 ==============
const skillDialog = ref({ visible: false, member: null as Member | null, newSkill: SkillLevel.NONE, submitting: false })

const openSkillDialog = (m: any) => {
  skillDialog.value.member = m
  skillDialog.value.newSkill = m.skillLevel
  skillDialog.value.visible = true
}

const handleUpdateSkill = async () => {
  if (!skillDialog.value.member) return
  const m = skillDialog.value.member
  if (skillDialog.value.newSkill === m.skillLevel) {
    ElMessage.info('技能等级未变化')
    skillDialog.value.visible = false
    return
  }
  skillDialog.value.submitting = true
  try {
    await updateMemberSkill(m.studentId, skillDialog.value.newSkill)
    ElNotification.success({
      title: '技能等级已更新',
      message: `${m.name} → ${SkillLevelText[skillDialog.value.newSkill]}`,
      duration: 2500,
    })
    skillDialog.value.visible = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '更新失败')
  } finally {
    skillDialog.value.submitting = false
  }
}

const roleTagType = (r: number) => {
  if (r === Role.PRESIDENT) return 'danger'
  if (r === Role.TECH_LEAD) return 'warning'
  return 'info'
}
const skillTagType = (s: number) => {
  if (s >= SkillLevel.BLENDER) return 'success'
  if (s >= SkillLevel.FUSION360) return 'primary'
  return 'info'
}
</script>

<template>
  <div class="member-page">
    <PageHeader title="成员管理" />

    <el-card class="filter-card">
      <div class="filter-row">
        <el-input
          v-model="filter.keyword"
          placeholder="按学号 / 姓名 / 邮箱搜索"
          clearable
          style="width: 320px"
          @keyup.enter="onSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="onSearch">搜索</el-button>
        <el-button @click="fetchData">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </el-card>

    <el-card v-loading="loading" class="content-card">
      <el-table :data="list" stripe>
        <el-table-column prop="studentId" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)" size="small" effect="dark">
              {{ RoleText[row.role as keyof typeof RoleText] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="技能等级" width="120">
          <template #default="{ row }">
            <!-- 关键：effect="dark" 让浅色背景上也能清晰看到白字（light 模式对比度差看不清） -->
            <el-tag :type="skillTagType(row.skillLevel)" size="small" effect="dark">
              {{ SkillLevelText[row.skillLevel as keyof typeof SkillLevelText] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="joinDate" label="入社时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.joinDate, 'YYYY-MM-DD') }}
          </template>
        </el-table-column>
        <el-table-column prop="totalPrints" label="打印次数" width="100" sortable />
        <el-table-column label="联系方式" min-width="200">
          <template #default="{ row }">
            <div v-if="row.phone" class="contact-line">
              <span class="contact-label">📱</span> {{ row.phone }}
            </div>
            <div v-if="row.email" class="contact-line">
              <span class="contact-label">📧</span> {{ row.email }}
            </div>
            <span v-if="!row.phone && !row.email" class="text-muted">未填写</span>
          </template>
        </el-table-column>
        <el-table-column v-if="isStaff" label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="isPresident && row.role !== Role.PRESIDENT"
              size="small"
              type="primary"
              @click="openRoleDialog(row)"
            >
              改角色
            </el-button>
            <el-button size="small" type="success" @click="openSkillDialog(row)">
              改技能
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="list.length === 0 && !loading" class="empty">
        <StatusTag description="没有匹配的成员" hint="试试清空搜索条件" />
      </div>

      <div v-if="total > filter.size" class="pagination-wrap">
        <el-pagination
          :current-page="filter.page"
          :page-size="filter.size"
          :total="total"
          layout="prev, pager, next, total"
          @current-change="onPageChange"
        />
      </div>
    </el-card>

    <!-- 改角色弹窗 -->
    <AppDialog
      v-model="roleDialog.visible"
      title="修改成员角色"
      icon="UserFilled"
      type="primary"
      width="440px"
      confirm-text="确认修改"
      :loading="roleDialog.submitting"
      @confirm="handleUpdateRole"
    >
      <div v-if="roleDialog.member" class="dialog-form">
        <p class="dialog-tip">
          <el-icon><InfoFilled /></el-icon>
          修改 <b>{{ roleDialog.member.name }}</b>（{{ roleDialog.member.studentId }}）的角色
        </p>
        <el-form label-width="80px">
          <el-form-item label="当前角色">
            <el-tag :type="roleTagType(roleDialog.member.role)" effect="dark">
              {{ RoleText[roleDialog.member.role as keyof typeof RoleText] }}
            </el-tag>
          </el-form-item>
          <el-form-item label="新角色">
            <el-radio-group v-model="roleDialog.newRole">
              <el-radio :value="Role.TECH_LEAD">技术骨干</el-radio>
              <el-radio :value="Role.MEMBER">普通社员</el-radio>
              <el-radio :value="Role.NEWBIE">新成员</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>

    <!-- 改技能弹窗 -->
    <AppDialog
      v-model="skillDialog.visible"
      title="修改成员技能等级"
      icon="Medal"
      type="success"
      width="440px"
      confirm-text="确认修改"
      :loading="skillDialog.submitting"
      @confirm="handleUpdateSkill"
    >
      <div v-if="skillDialog.member" class="dialog-form">
        <p class="dialog-tip">
          <el-icon><InfoFilled /></el-icon>
          修改 <b>{{ skillDialog.member.name }}</b>（{{ skillDialog.member.studentId }}）的技能等级
        </p>
        <el-form label-width="80px">
          <el-form-item label="当前">
            <el-tag :type="skillTagType(skillDialog.member.skillLevel)" effect="dark">
              {{ SkillLevelText[skillDialog.member.skillLevel as keyof typeof SkillLevelText] }}
            </el-tag>
          </el-form-item>
          <el-form-item label="新等级">
            <el-radio-group v-model="skillDialog.newSkill">
              <el-radio :value="SkillLevel.NONE">未入门</el-radio>
              <el-radio :value="SkillLevel.TINKERCAD">Tinkercad</el-radio>
              <el-radio :value="SkillLevel.FUSION360">Fusion 360</el-radio>
              <el-radio :value="SkillLevel.BLENDER">Blender</el-radio>
              <el-radio :value="SkillLevel.TUNING">调机熟练</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.member-page { padding: 0; }
.filter-card { margin-bottom: $spacing-medium; }
.filter-row { display: flex; gap: $spacing-small; align-items: center; }
.content-card { min-height: 500px; }
.contact-line {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-regular);
  .contact-label { font-size: 14px; }
}
.text-muted { color: var(--text-placeholder); font-size: 13px; }
.empty { padding: 40px 0; }
.pagination-wrap { display: flex; justify-content: center; margin-top: $spacing-large; }

.dialog-form {
  padding: 4px 0;
}
.dialog-tip {
  display: flex;
  align-items: center;
  gap: 6px;
  margin: 0 0 16px;
  padding: 10px 14px;
  background: color-mix(in srgb, $accent-color 8%, transparent);
  border-radius: 6px;
  font-size: 13px;
  color: var(--text-regular);
  .el-icon { color: $accent-color; }
  b { color: $primary-color; font-weight: 600; }
}
</style>
