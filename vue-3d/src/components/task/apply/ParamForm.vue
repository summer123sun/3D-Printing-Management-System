<script setup lang="ts">
/**
 * 打印参数表单（**B**）
 *
 * 用于提交申请页和编辑任务
 */
import { reactive, watch } from 'vue'
import type { TaskApplyDTO } from '@/types/task'
import {
  MaterialType, MaterialColor,
  Priority, PriorityText,
} from '@/types/task'

interface Props {
  modelValue: TaskApplyDTO
}
const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', val: TaskApplyDTO): void
}>()

const form = reactive<TaskApplyDTO>({ ...props.modelValue })

watch(form, (val) => {
  emit('update:modelValue', { ...val })
}, { deep: true })

watch(() => props.modelValue, (val) => {
  Object.assign(form, val)
})
</script>

<template>
  <el-form :model="form" label-width="100px" label-position="right">
    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item label="任务标题" required>
          <el-input v-model="form.title" placeholder="如：刘洋-机械键盘键帽" maxlength="100" show-word-limit />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="模型名称" required>
          <el-input v-model="form.modelName" placeholder="如：机械键盘键帽 v2" maxlength="100" />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="8">
        <el-form-item label="材料类型" required>
          <el-select v-model="form.materialType" placeholder="选择材料" style="width: 100%">
            <el-option v-for="m in MaterialType" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="颜色">
          <el-select v-model="form.color" placeholder="选择颜色" clearable style="width: 100%">
            <el-option v-for="c in MaterialColor" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :span="8">
        <el-form-item label="优先级" required>
          <el-radio-group v-model="form.priority">
            <el-radio :value="Priority.URGENT">{{ PriorityText[Priority.URGENT] }}</el-radio>
            <el-radio :value="Priority.NORMAL">{{ PriorityText[Priority.NORMAL] }}</el-radio>
            <el-radio :value="Priority.LOW">{{ PriorityText[Priority.LOW] }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="6">
        <el-form-item label="层高 (mm)" required>
          <el-input-number v-model="form.layerHeight" :min="0.05" :max="0.4" :step="0.05" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="填充率 (%)" required>
          <el-input-number v-model="form.infillRate" :min="0" :max="100" :step="5" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="需要支撑">
          <el-radio-group v-model="form.needSupport">
            <el-radio :value="1">需要</el-radio>
            <el-radio :value="0">不需要</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :span="6">
        <el-form-item label="关联项目">
          <el-input-number v-model="form.projectId" :min="1" placeholder="不关联可留空" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :span="12">
        <el-form-item label="预估重量 (g)">
          <el-input-number v-model="form.estWeight" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="12">
        <el-form-item label="预估耗时 (分钟)">
          <el-input-number v-model="form.estTime" :min="1" style="width: 100%" />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>