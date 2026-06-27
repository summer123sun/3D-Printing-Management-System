/**
 * 打印机 + 维护 Store（M5）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as printerApi from '@/api/printer'
import type { Maintenance, Printer } from '@/types/printer'
import type { PageResult } from '@/types/api'

export const usePrinterStore = defineStore('printer', () => {
  const list = ref<PageResult<Printer> | null>(null)
  const availableList = ref<Printer[]>([])
  const current = ref<Printer | null>(null)
  const maintenanceList = ref<PageResult<Maintenance> | null>(null)
  const loading = ref(false)

  const fetchList = async (params: { page?: number; size?: number; status?: number; keyword?: string } = {}) => {
    loading.value = true
    try {
      list.value = await printerApi.listPrinters(params)
    } finally {
      loading.value = false
    }
  }

  const fetchAvailable = async () => {
    availableList.value = await printerApi.availablePrinters()
  }

  const fetchDetail = async (id: string) => {
    current.value = await printerApi.printerDetail(id)
    return current.value
  }

  const create = async (printer: Partial<Printer>) => {
    await printerApi.createPrinter(printer)
  }

  const update = async (id: string, printer: Partial<Printer>) => {
    await printerApi.updatePrinter(id, printer)
  }

  const remove = async (id: string) => {
    await printerApi.deletePrinter(id)
  }

  const setStatus = async (id: string, status: number, remark?: string) => {
    await printerApi.setPrinterStatus(id, status, remark)
  }

  const fetchMaintenance = async (params: { page?: number; size?: number; printerId?: string } = {}) => {
    maintenanceList.value = await printerApi.listMaintenance(params)
  }

  const addMaintenance = async (dto: Partial<Maintenance>) => {
    await printerApi.addMaintenance(dto)
  }

  const removeMaintenance = async (id: number) => {
    await printerApi.deleteMaintenance(id)
  }

  return {
    list,
    availableList,
    current,
    maintenanceList,
    loading,
    fetchList,
    fetchAvailable,
    fetchDetail,
    create,
    update,
    remove,
    setStatus,
    fetchMaintenance,
    addMaintenance,
    removeMaintenance,
  }
})
