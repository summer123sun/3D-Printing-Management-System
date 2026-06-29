<script setup lang="ts">
/**
 * 成员管理（v2 深海蓝金版）
 *
 * 功能：
 * - 成员列表（分页 + 搜索）
 * - 改角色（仅社长）
 * - 改技能等级（社长+技术骨干）
 */
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElNotification } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/EmptyState.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { memberList, updateMemberRole, updateMemberSkill, addMember } from '@/api/user'
import { Role, RoleText, SkillLevel, SkillLevelText, type Member } from '@/types/member'
import { useAuthStore } from '@/stores/auth'
import { formatDate } from '@/utils/format'

const authStore = useAuthStore()

const filter = ref({
  keyword: '',
  role: null as number | null,  // 按角色筛选（null = 全部）
  page: 1,
  size: 15,
})

// 角色选项（给筛选下拉框用）
const roleOptions = [
  { value: Role.PRESIDENT, label: RoleText[Role.PRESIDENT] },
  { value: Role.TECH_LEAD, label: RoleText[Role.TECH_LEAD] },
  { value: Role.MEMBER, label: RoleText[Role.MEMBER] },
  { value: Role.NEWBIE, label: RoleText[Role.NEWBIE] },
]

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
      role: filter.value.role ?? undefined,
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

// ============== 新增成员（仅社长） ==============
const addDialog = ref({ visible: false, submitting: false })
const addForm = reactive({
  studentId: '',
  name: '',
  role: Role.MEMBER,
  skillLevel: SkillLevel.NONE,
  password: '',
  phone: '',
  email: '',
  joinDate: new Date().toISOString().slice(0, 10),  // 默认今天
})

const openAddDialog = () => {
  // 重置表单
  addForm.studentId = ''
  addForm.name = ''
  addForm.role = Role.MEMBER
  addForm.skillLevel = SkillLevel.NONE
  addForm.password = ''
  addForm.phone = ''
  addForm.email = ''
  addForm.joinDate = new Date().toISOString().slice(0, 10)
  addDialog.value.visible = true
}

const handleAdd = async () => {
  // 前端基础校验（更严格的留给后端 @Valid）
  if (!/^\d{10}$/.test(addForm.studentId)) {
    ElMessage.warning('学号必须是 10 位数字')
    return
  }
  if (!addForm.name.trim()) {
    ElMessage.warning('姓名不能为空')
    return
  }
  if (addForm.password && addForm.password.length < 6) {
    ElMessage.warning('密码至少 6 位（留空则默认 123456）')
    return
  }

  addDialog.value.submitting = true
  try {
    await addMember({
      studentId: addForm.studentId.trim(),
      name: addForm.name.trim(),
      role: addForm.role,
      skillLevel: addForm.skillLevel,
      password: addForm.password || undefined,
      phone: addForm.phone || undefined,
      email: addForm.email || undefined,
      joinDate: addForm.joinDate || undefined,
    })
    ElNotification.success({
      title: '成员已新增',
      message: `${addForm.name.trim()}（${addForm.studentId.trim()}）已加入，默认密码 123456`,
      duration: 3000,
    })
    addDialog.value.visible = false
    fetchData()
  } catch (e: any) {
    ElMessage.error(e?.message || '新增失败')
  } finally {
    addDialog.value.submitting = false
  }
}
</script>

<template>
  <div class="member-page">
    <PageHeader title="成员管理">
      <el-button v-if="isPresident" type="primary" @click="openAddDialog">
        <el-icon><Plus /></el-icon> 新增成员
      </el-button>
    </PageHeader>

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
        <el-select
          v-model="filter.role"
          placeholder="全部角色"
          clearable
          style="width: 160px"
          @change="onSearch"
        >
          <el-option label="全部角色" :value="undefined as unknown as number" />
          <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
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

    <!-- 新增成员弹窗（仅社长） -->
    <AppDialog
      v-model="addDialog.visible"
      title="新增成员"
      icon="Plus"
      type="primary"
      width="560px"
      confirm-text="新增"
      :loading="addDialog.submitting"
      @confirm="handleAdd"
    >
      <el-form :model="addForm" label-width="100px">
        <el-form-item label="学号" required>
          <el-input v-model="addForm.studentId" placeholder="10 位数字" maxlength="10" />
        </el-form-item>
        <el-form-item label="姓名" required>
          <el-input v-model="addForm.name" placeholder="姓名" maxlength="20" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-radio-group v-model="addForm.role">
            <el-radio :value="Role.TECH_LEAD">技术骨干</el-radio>
            <el-radio :value="Role.MEMBER">普通社员</el-radio>
            <el-radio :value="Role.NEWBIE">新成员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="技能等级">
          <el-select v-model="addForm.skillLevel" style="width: 100%">
            <el-option v-for="(label, value) in SkillLevelText" :key="value" :label="label" :value="Number(value)" />
          </el-select>
        </el-form-item>
        <el-form-item label="初始密码">
          <el-input v-model="addForm.password" placeholder="留空则默认 123456" type="password" show-password maxlength="30" />
        </el-form-item>
        <el-form-item label="入社日期">
          <el-input v-model="addForm.joinDate" placeholder="YYYY-MM-DD，留空则今天" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="addForm.phone" placeholder="可选" maxlength="20" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="addForm.email" placeholder="可选" maxlength="100" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon style="margin-top: 4px">
          权限：仅社长可新增成员。新账号默认初始密码 123456（可在「个人中心→修改密码」首次登录后修改）
        </el-alert>
      </el-form>
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
