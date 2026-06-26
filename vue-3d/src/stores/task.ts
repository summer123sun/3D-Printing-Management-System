/**
 * 任务 Store（E）
 *
 * 管理：我的任务列表 / 待审批 / 队列 / 当前任务详情
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as taskApi from '@/api/task'
import type {
  PrintTask,
  TaskQuery,
  TaskApplyDTO,
  ApproveDTO,
  RejectDTO,
  AssignPrinterDTO,
  FinishPrintDTO,
  PickupDTO,
} from '@/types/task'
import type { PageResult } from '@/types/api'

export const useTaskStore = defineStore('task', () => {
  // ============== State ==============
  const myTasks = ref<PageResult<PrintTask> | null>(null)
  const pendingTasks = ref<PageResult<PrintTask> | null>(null)
  const queue = ref<PageResult<PrintTask> | null>(null)
  const currentTask = ref<PrintTask | null>(null)

  const loading = ref(false)
  const submitting = ref(false)

  // ============== Actions ==============

  /** 提交申请 */
  const apply = async (dto: TaskApplyDTO) => {
    submitting.value = true
    try {
      const taskId = await taskApi.applyTask(dto)
      return taskId
    } finally {
      submitting.value = false
    }
  }

  /** 我的任务 */
  const fetchMyTasks = async (query: TaskQuery = {}) => {
    loading.value = true
    try {
      myTasks.value = await taskApi.myTasks(query)
    } finally {
      loading.value = false
    }
  }

  /** 待审批 */
  const fetchPendingTasks = async (query: TaskQuery = {}) => {
    loading.value = true
    try {
      pendingTasks.value = await taskApi.pendingTasks(query)
    } finally {
      loading.value = false
    }
  }

  /** 队列 */
  const fetchQueue = async (query: TaskQuery = {}) => {
    loading.value = true
    try {
      queue.value = await taskApi.taskQueue(query)
    } finally {
      loading.value = false
    }
  }

  /** 详情 */
  const fetchTaskDetail = async (id: string) => {
    loading.value = true
    try {
      currentTask.value = await taskApi.taskDetail(id)
      return currentTask.value
    } finally {
      loading.value = false
    }
  }

  /** 审批 */
  const approve = async (id: string, dto: ApproveDTO = {}) => {
    await taskApi.approveTask(id, dto)
  }

  const reject = async (id: string, dto: RejectDTO) => {
    await taskApi.rejectTask(id, dto)
  }

  /** 分配/开始/完成 */
  const assignPrinter = async (id: string, dto: AssignPrinterDTO) => {
    await taskApi.assignPrinter(id, dto)
  }

  const startPrint = async (id: string) => {
    await taskApi.startPrint(id)
  }

  const finishPrint = async (id: string, dto: FinishPrintDTO) => {
    await taskApi.finishPrint(id, dto)
  }

  /** 取件/取消 */
  const pickup = async (id: string, dto: PickupDTO = {}) => {
    await taskApi.pickup(id, dto)
  }

  const cancel = async (id: string) => {
    await taskApi.cancelTask(id)
  }

  return {
    // state
    myTasks,
    pendingTasks,
    queue,
    currentTask,
    loading,
    submitting,
    // actions
    apply,
    fetchMyTasks,
    fetchPendingTasks,
    fetchQueue,
    fetchTaskDetail,
    approve,
    reject,
    assignPrinter,
    startPrint,
    finishPrint,
    pickup,
    cancel,
  }
})