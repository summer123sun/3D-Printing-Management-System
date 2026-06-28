<script setup lang="ts">
/**
 * 耗材入库（M5）
 * 流程：填表 → 点"确认入库" → ElMessageBox 二次确认 → 调后端 → AppDialog 大弹窗显示结果
 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import AppDialog from '@/components/common/AppDialog.vue'
import { useMaterialStore } from '@/stores/material'
import { MaterialTypes, MaterialTypeText } from '@/types/material'

const router = useRouter()
const store = useMaterialStore()

const form = reactive({
  materialType: 'PLA',
  color: '',
  weightChange: 1000,
  remark: '',
})

const submitting = ref(false)

// ============== 成功/失败反馈弹窗 ==============
const resultDialog = reactive({
  visible: false,
  type: 'success' as 'success' | 'danger',
  title: '',
  summary: '',
  reason: '',
})

const closeResult = () => {
  resultDialog.visible = false
}

const backToList = () => {
  resultDialog.visible = false
  router.push('/admin/material')
}

const handleSubmit = async () => {
  if (!form.materialType) {
    ElMessage.warning('请选择材料类型')
    return
  }
  if (!form.color || !form.color.trim()) {
    ElMessage.warning('请填写颜色')
    return
  }
  if (!form.weightChange || form.weightChange <= 0) {
    ElMessage.warning('入库重量必须为正数')
    return
  }

  // 二次确认：显示要入库的详情，避免误操作
  const summary = `${MaterialTypeText[form.materialType]} ${form.color.trim()} +${form.weightChange}g`
  try {
    await ElMessageBox.confirm(
      `确定要入库 ${summary} 吗？入库后会自动累加到当前库存。`,
      '确认入库',
      {
        type: 'warning',
        confirmButtonText: '确认入库',
        cancelButtonText: '再检查一下',
        customClass: 'msgbox-confirm',
      },
    )
  } catch {
    // 用户点了"再检查一下"，不提交
    return
  }

  submitting.value = true
  try {
    await store.inbound({
      materialType: form.materialType,
      color: form.color.trim(),
      weightChange: form.weightChange,
      remark: form.remark.trim() || undefined,
    })
    // 成功：弹 AppDialog 大弹窗
    resultDialog.type = 'success'
    resultDialog.title = '入库成功'
    resultDialog.summary = summary
    resultDialog.reason = ''
    resultDialog.visible = true
  } catch (e: any) {
    // 失败：弹 AppDialog 大弹窗
    resultDialog.type = 'danger'
    resultDialog.title = '入库失败'
    resultDialog.summary = summary
    resultDialog.reason = e?.message || '请稍后再试'
    resultDialog.visible = true
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
          <span style="margin-left: 12px; color: var(--text-secondary)">克</span>
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

    <!-- 入库结果反馈弹窗（成功 / 失败） -->
    <AppDialog
      v-model="resultDialog.visible"
      :title="resultDialog.title"
      icon="Check"
      :type="resultDialog.type"
      width="460px"
      :confirm-text="resultDialog.type === 'success' ? '返回列表' : '我知道了'"
      :cancel-text="resultDialog.type === 'success' ? '继续入库' : ''"
      :show-footer="true"
      @confirm="backToList"
      @cancel="closeResult"
    >
      <div class="result-content">
        <div class="result-icon-big" :class="`is-${resultDialog.type}`">
          <el-icon :size="56">
            <component :is="resultDialog.type === 'success' ? 'Check' : 'Warning'" />
          </el-icon>
        </div>
        <h3 v-if="resultDialog.type === 'success'" class="result-title">「{{ resultDialog.summary }}」已入库</h3>
        <h3 v-else class="result-title">入库未成功</h3>
        <p v-if="resultDialog.type === 'success'" class="result-tip">
          <el-icon><component :is="'InfoFilled'" /></el-icon>
          库存已自动累加，可到"耗材流水"查看记录
        </p>
        <p v-else class="result-reason">{{ resultDialog.reason }}</p>
        <p v-if="resultDialog.type === 'danger'" class="result-hint">
          可能原因：<br />
          1) 网络异常（后端未启动或超时）<br />
          2) 登录已过期，请重新登录<br />
          3) 后端服务异常，请联系管理员
        </p>
      </div>
    </AppDialog>
  </div>
</template>

<style lang="scss" scoped>
.admin-material-inbound-page { padding: 0; }

.result-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 8px 8px;
  text-align: center;
}
.result-icon-big {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 16px;
  &.is-success {
    background: linear-gradient(135deg, #00D4AA 0%, #00A88A 100%);
    color: #fff;
    box-shadow: 0 8px 24px rgba(0, 212, 170, 0.35);
  }
  &.is-danger {
    background: linear-gradient(135deg, #FF4757 0%, #E0303F 100%);
    color: #fff;
    box-shadow: 0 8px 24px rgba(255, 71, 87, 0.35);
  }
}
.result-title {
  margin: 0 0 12px;
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}
.result-tip {
  margin: 0 0 4px;
  font-size: 14px;
  color: var(--text-regular);
  display: flex;
  align-items: center;
  gap: 6px;
}
.result-reason {
  margin: 0 0 12px;
  font-size: 15px;
  color: $danger-color;
  font-weight: 500;
}
.result-hint {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.7;
  text-align: left;
  background: var(--bg-base);
  padding: 12px 16px;
  border-radius: 8px;
  width: 100%;
  box-sizing: border-box;
}
</style>
