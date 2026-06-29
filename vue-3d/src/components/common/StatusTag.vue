<script setup lang="ts">
/**
 * 通用状态徽章（自动按 status 渲染 el-tag）
 *
 * 用法：
 *   <StatusTag :status="0" />              // 自动映射任务状态
 *   <StatusTag status-type="task" :status="task.status" />
 *   <StatusTag status-type="project" :status="project.status" />
 */
import { computed } from 'vue'
import { TaskStatus, TaskStatusText, TaskStatusTagType } from '@/types/task'

type StatusType = 'task' | 'project' | 'generic'

interface Props {
  status: number
  statusType?: StatusType
  text?: string
  type?: 'primary' | 'success' | 'warning' | 'info' | 'danger' | ''
}

const props = withDefaults(defineProps<Props>(), {
  statusType: 'generic',
})

const tagType = computed(() => {
  if (props.statusType === 'task') {
    return TaskStatusTagType[props.status as TaskStatus] || 'info'
  }
  return props.type || 'info'
})

const tagText = computed(() => {
  if (props.statusType === 'task') {
    return TaskStatusText[props.status as TaskStatus] || '-'
  }
  return props.text || '-'
})
</script>

<template>
  <!-- 关键：effect="dark" 让浅色背景上也能清晰看到白字（light 模式对比度差看不清） -->
  <el-tag :type="tagType" size="small" effect="dark" round>
    {{ tagText }}
  </el-tag>
</template>