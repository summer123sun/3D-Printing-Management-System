<script setup lang="ts">
/**
 * 任务状态流转时间线（**B**）
 *
 * 展示任务从申请到归档的全流程节点
 */
import { computed } from 'vue'
import type { PrintTask } from '@/types/task'
import { TaskStatus, TaskStatusText } from '@/types/task'

interface Props {
  task: PrintTask
}
const props = defineProps<Props>()

interface Node {
  status: TaskStatus
  label: string
  time?: string
  done: boolean
  current: boolean
}

const nodes = computed<Node[]>(() => {
  const t = props.task
  const current = t.status

  return [
    { status: TaskStatus.PENDING, label: '提交申请', time: t.applyTime, done: current >= TaskStatus.PENDING, current: current === TaskStatus.PENDING },
    {
      status: TaskStatus.APPROVED, label: TaskStatusText[TaskStatus.APPROVED], time: t.approveTime,
      done: current >= TaskStatus.APPROVED && current !== TaskStatus.REJECTED && current !== TaskStatus.CANCELLED,
      current: current === TaskStatus.APPROVED,
    },
    {
      status: TaskStatus.QUEUED, label: TaskStatusText[TaskStatus.QUEUED],
      done: current >= TaskStatus.QUEUED && current !== TaskStatus.CANCELLED,
      current: current === TaskStatus.QUEUED,
    },
    {
      status: TaskStatus.PRINTING, label: TaskStatusText[TaskStatus.PRINTING],
      done: current >= TaskStatus.PRINTING && current !== TaskStatus.CANCELLED,
      current: current === TaskStatus.PRINTING,
    },
    {
      status: TaskStatus.DONE, label: TaskStatusText[TaskStatus.DONE], time: t.finishTime,
      done: current >= TaskStatus.DONE && current !== TaskStatus.CANCELLED,
      current: current === TaskStatus.DONE,
    },
    {
      status: TaskStatus.PRINTING, label: '已签收', time: t.pickupTime,
      done: !!t.pickupTime,
      current: false,
    },
  ]
})

const rejected = computed(() => props.task.status === TaskStatus.REJECTED)
const cancelled = computed(() => props.task.status === TaskStatus.CANCELLED)

const formatTime = (s?: string) => {
  if (!s) return ''
  return s.replace('T', ' ').substring(0, 16)
}
</script>

<template>
  <div class="task-timeline">
    <el-alert v-if="rejected" type="error" :closable="false" show-icon>
      <template #title>审批未通过</template>
      <div v-if="task.approveComment">理由：{{ task.approveComment }}</div>
    </el-alert>
    <el-alert v-else-if="cancelled" type="info" :closable="false" show-icon>
      <template #title>任务已取消</template>
    </el-alert>

    <el-timeline>
      <el-timeline-item
        v-for="(n, i) in nodes"
        :key="i"
        :timestamp="formatTime(n.time)"
        :type="n.current ? 'primary' : (n.done ? 'success' : 'info')"
        :hollow="!n.done"
        size="normal"
      >
        {{ n.label }}
        <span v-if="n.current" class="current-tag">当前</span>
      </el-timeline-item>
    </el-timeline>
  </div>
</template>

<style lang="scss" scoped>
.task-timeline {
  padding: 16px 0;
}
.current-tag {
  margin-left: 8px;
  padding: 1px 6px;
  background: $primary-color;
  color: #fff;
  border-radius: $border-radius-small;
  font-size: $font-size-small;
}
</style>