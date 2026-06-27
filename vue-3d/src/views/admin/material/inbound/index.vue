<script setup lang="ts">
/**
 * 耗材入库（M5）
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElNotification } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import { useMaterialStore } from '@/stores/material'
import { MaterialTypes, MaterialTypeText } from '@/types/material'

const router = useRouter()
const store = useMaterialStore()

const form = ref({
  materialType: 'PLA',
  color: '',
  weightChange: 1000,
  remark: '',
})

const submitting = ref(false)

const handleSubmit = async () => {
  if (!form.value.materialType) {
    ElNotification.warning('请选择材料类型')
    return
  }
  if (!form.value.color) {
    ElNotification.warning('请填写颜色')
    return
  }
  if (!form.value.weightChange || form.value.weightChange <= 0) {
    ElNotification.warning('入库重量必须为正数')
    return
  }
  submitting.value = true
  try {
    await store.inbound(form.value)
    ElNotification.success(`入库成功！${form.value.materialType} ${form.value.color} +${form.value.weightChange}g`)
    router.push('/admin/material')
  } catch (e: any) {
    ElNotification.error(e.message || '入库失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="admin-material-inbound-page">
    <PageHeader title="耗材入库" :show-back="true" @back="router.back()" />

    <el-card>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px">
        <p>入库后系统会自动累加该材料+颜色组合的当前库存</p>
        <p>所有入库记录会保存在"耗材流水"中可追溯</p>
      </el-alert>

      <el-form :model="form" label-width="120px" style="max-width: 600px">
        <el-form-item label="材料类型" required>
          <el-select v-model="form.materialType" style="width: 100%">
            <el-option v-for="t in MaterialTypes" :key="t" :label="MaterialTypeText[t]" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色" required>
          <el-input v-model="form.color" placeholder="如 黑色、白色、红色" maxlength="20" />
        </el-form-item>
        <el-form-item label="入库重量" required>
          <el-input-number
            v-model="form.weightChange"
            :min="1"
            :max="100000"
            :step="100"
            :precision="2"
            style="width: 200px"
          />
          <span style="margin-left: 12px; color: #909399">克</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="如：新购入库、淘宝XX店铺"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">确认入库</el-button>
          <el-button @click="router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.admin-material-inbound-page { padding: 0; }
</style>
