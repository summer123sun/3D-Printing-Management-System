/**
 * 耗材 Store（M5）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as materialApi from '@/api/material'
import type { MaterialInboundDTO, MaterialLog, MaterialStock } from '@/types/material'
import type { PageResult } from '@/types/api'

export const useMaterialStore = defineStore('material', () => {
  const stocks = ref<MaterialStock[]>([])
  const warnings = ref<MaterialStock[]>([])
  const logList = ref<PageResult<MaterialLog> | null>(null)
  const summary = ref<any>(null)
  const loading = ref(false)

  const fetchStocks = async (materialType?: string) => {
    loading.value = true
    try {
      stocks.value = await materialApi.listStocks(materialType)
    } finally {
      loading.value = false
    }
  }

  const fetchWarnings = async (threshold?: number) => {
    warnings.value = await materialApi.warningStocks(threshold)
  }

  const fetchLogs = async (params: any = {}) => {
    logList.value = await materialApi.listMaterialLogs(params)
  }

  const inbound = async (dto: MaterialInboundDTO) => {
    await materialApi.inboundMaterial(dto)
  }

  const fetchSummary = async () => {
    summary.value = await materialApi.materialSummary()
  }

  return {
    stocks,
    warnings,
    logList,
    summary,
    loading,
    fetchStocks,
    fetchWarnings,
    fetchLogs,
    inbound,
    fetchSummary,
  }
})
