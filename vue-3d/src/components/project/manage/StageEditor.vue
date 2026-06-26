<script setup lang="ts">
/**
 * 项目阶段动态编辑（**B**）
 *
 * 用于创建/编辑项目时增删改阶段
 */
import { reactive, watch } from 'vue'
import { Plus, Delete } from '@element-plus/icons-vue'
import type { StageDTO } from '@/types/project'

interface Props {
  modelValue: StageDTO[]
}
const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'update:modelValue', val: StageDTO[]): void
}>()

const stages = reactive<StageDTO[]>([...props.modelValue])

watch(stages, (val) => {
  emit('update:modelValue', [...val])
}, { deep: true })

watch(() => props.modelValue, (val) => {
  stages.splice(0, stages.length, ...val)
})

const addStage = () => {
  stages.push({
    stageName: '',
    stageOrder: stages.length + 1,
    description: '',
    responsibleId: '',
  })
}

const removeStage = (idx: number) => {
  stages.splice(idx, 1)
  // 重新计算 order
  stages.forEach((s, i) => (s.stageOrder = i + 1))
}

const moveUp = (idx: number) => {
  if (idx === 0) return
  const tmp = stages[idx - 1]!
  stages[idx - 1] = stages[idx]!
  stages[idx] = tmp
  stages.forEach((s, i) => (s.stageOrder = i + 1))
}

const moveDown = (idx: number) => {
  if (idx === stages.length - 1) return
  const tmp = stages[idx]!
  stages[idx] = stages[idx + 1]!
  stages[idx + 1] = tmp
  stages.forEach((s, i) => (s.stageOrder = i + 1))
}
</script>

<template>
  <div class="stage-editor">
    <div v-if="stages.length === 0" class="empty-tip">
      <el-empty description="还没有阶段，点下方按钮添加" :image-size="60" />
    </div>

    <div v-for="(stage, idx) in stages" :key="idx" class="stage-row">
      <div class="stage-meta">
        <span class="stage-order">#{{ stage.stageOrder }}</span>
        <div class="stage-actions">
          <el-button text size="small" :disabled="idx === 0" @click="moveUp(idx)">↑</el-button>
          <el-button text size="small" :disabled="idx === stages.length - 1" @click="moveDown(idx)">↓</el-button>
          <el-button text size="small" type="danger" @click="removeStage(idx)">
            <el-icon><Delete /></el-icon>
          </el-button>
        </div>
      </div>
      <el-row :gutter="8">
        <el-col :span="8">
          <el-input v-model="stage.stageName" placeholder="阶段名称（如：需求分析）" maxlength="50" show-word-limit />
        </el-col>
        <el-col :span="6">
          <el-input v-model="stage.responsibleId" placeholder="负责人学号（可选）" />
        </el-col>
        <el-col :span="10">
          <el-input v-model="stage.description" placeholder="阶段描述（可选）" />
        </el-col>
      </el-row>
    </div>

    <el-button type="primary" plain :icon="Plus" @click="addStage" style="width: 100%; margin-top: 12px">
      添加阶段
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
.stage-editor {
  .empty-tip {
    margin: $spacing-medium 0;
  }
  .stage-row {
    padding: $spacing-medium;
    margin-bottom: $spacing-small;
    background: $bg-base;
    border-radius: $border-radius-base;
    border-left: 3px solid $brand-color;
  }
  .stage-meta {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-small;
  }
  .stage-order {
    font-weight: 600;
    color: $brand-color;
  }
  .stage-actions {
    .el-button {
      padding: 2px 6px;
    }
  }
}
</style>