<script setup lang="ts">
/**
 * 打印参数表单（**B**）
 *
 * 用于提交申请页和编辑任务
 * 使用 defineModel 实现双向绑定，避免循环更新问题
 *
 * 📱 v2.20 手机端适配：
 *   - xs (<768px)：表单 label 移到上方（label-position="top"，label-width="0"），
 *                  所有表单项全宽（el-col :xs="24"），el-radio-group 强制 column 竖排
 *   - sm (≥768px)：双列（:sm="12"），label 回到 right，宽度 80px
 *   - md+ (≥992px)：原样（12/8/6 混合布局）
 *
 * 设计原则（避免造轮子）：
 *   - 复用项目已有的 @mixin sm / mobile（@styles/variables.scss）
 *   - 复用 useMediaQuery composable（@composables/useMediaQuery）
 *   - CSS 颜色全走 var(--xxx) 或 html.dark & {}（不破坏暗色/亮色主题切换）
 */
import type { TaskApplyDTO } from '@/types/task'
import {
  MaterialType, MaterialColor,
  Priority, PriorityText,
} from '@/types/task'
import { useMediaQuery } from '@/composables/useMediaQuery'

const form = defineModel<TaskApplyDTO>({ required: true })

const { isMobile } = useMediaQuery()
</script>

<template>
  <el-form
    :model="form"
    :label-position="isMobile ? 'top' : 'right'"
    :label-width="isMobile ? 'auto' : '100px'"
  >
    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="12">
        <el-form-item label="任务标题" required>
          <el-input v-model="form.title" placeholder="如：刘洋-机械键盘键帽" maxlength="100" show-word-limit />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12">
        <el-form-item label="模型名称" required>
          <el-input v-model="form.modelName" placeholder="如：机械键盘键帽 v2" maxlength="100" />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="8">
        <el-form-item label="材料类型" required>
          <el-select v-model="form.materialType" placeholder="选择材料" style="width: 100%">
            <el-option v-for="m in MaterialType" :key="m" :label="m" :value="m" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12" :md="8">
        <el-form-item label="颜色" required>
          <el-select v-model="form.color" placeholder="选择颜色" style="width: 100%">
            <el-option v-for="c in MaterialColor" :key="c" :label="c" :value="c" />
          </el-select>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="24" :md="8">
        <el-form-item label="优先级" required>
          <!-- 📱 v2.20 手机端：强制 column 竖排（避免 3 个 radio 挤在一行） -->
          <el-radio-group
            v-model="form.priority"
            :class="{ 'is-mobile-stack': isMobile }"
          >
            <el-radio :value="Priority.URGENT">{{ PriorityText[Priority.URGENT] }}</el-radio>
            <el-radio :value="Priority.NORMAL">{{ PriorityText[Priority.NORMAL] }}</el-radio>
            <el-radio :value="Priority.LOW">{{ PriorityText[Priority.LOW] }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="6">
        <el-form-item label="层高 (mm)" required>
          <el-input-number
            v-model="form.layerHeight" :min="0.05" :max="0.4" :step="0.05" :precision="2"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-form-item label="填充率 (%)" required>
          <el-input-number
            v-model="form.infillRate" :min="0" :max="100" :step="5"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-form-item label="需要支撑">
          <el-radio-group
            v-model="form.needSupport"
            :class="{ 'is-mobile-stack': isMobile }"
          >
            <el-radio :value="1">需要</el-radio>
            <el-radio :value="0">不需要</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-form-item label="关联项目">
          <el-input-number
            v-model="form.projectId" :min="1" placeholder="不关联可留空"
            controls-position="right" style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :sm="12" :md="12">
        <el-form-item label="预估重量 (g)">
          <el-input-number
            v-model="form.estWeight" :min="0" :precision="2"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
      <el-col :xs="24" :sm="12" :md="12">
        <el-form-item label="预估耗时 (分钟)">
          <el-input-number
            v-model="form.estTime" :min="1"
            style="width: 100%"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<style lang="scss" scoped>
/* 📱 v2.20 手机端 radio 竖排（避免挤在一行换行错乱） */
:deep(.is-mobile-stack) {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;

  .el-radio {
    margin-right: 0 !important;
  }
}

/* 📱 v2.20 手机端 description 项整行占满 */
@include mobile {
  :deep(.el-form-item__label) {
    width: auto !important;
    text-align: left !important;
    padding-bottom: 4px;
  }
}
</style>
