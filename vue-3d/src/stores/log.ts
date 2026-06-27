/**
 * 系统日志 Store（M6）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as logApi from '@/api/log'
import type { LogQuery, SystemLog } from '@/types/log'
import type { PageResult } from '@/types/api'

export const useLogStore = defineStore('log', () => {
  const list = ref<PageResult<SystemLog> | null>(null)
  const loading = ref(false)

  const fetchList = async (query: LogQuery = {}) => {
    loading.value = true
    try {
      list.value = await logApi.listLogs(query)
    } finally {
      loading.value = false
    }
  }

  const clean = async (keepDays = 90) => {
    return await logApi.cleanLogs(keepDays)
  }

  return { list, loading, fetchList, clean }
})
