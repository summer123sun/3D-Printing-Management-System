/**
 * 作品 Store（M4）
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as artworkApi from '@/api/artwork'
import type { Artwork, ArtworkQuery, ArtworkUpdateDTO } from '@/types/artwork'
import type { PageResult } from '@/types/api'

export const useArtworkStore = defineStore('artwork', () => {
  const list = ref<PageResult<Artwork> | null>(null)
  const myArtworks = ref<PageResult<Artwork> | null>(null)
  const recommended = ref<PageResult<Artwork> | null>(null)
  const current = ref<Artwork | null>(null)
  const loading = ref(false)

  const fetchList = async (query: ArtworkQuery = {}) => {
    loading.value = true
    try {
      list.value = await artworkApi.listArtworks(query)
    } finally {
      loading.value = false
    }
  }

  const fetchRecommended = async (limit = 12) => {
    recommended.value = await artworkApi.recommendedArtworks(limit)
  }

  const fetchMine = async (query: ArtworkQuery = {}) => {
    loading.value = true
    try {
      myArtworks.value = await artworkApi.myArtworks(query)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id: number) => {
    loading.value = true
    try {
      current.value = await artworkApi.artworkDetail(id)
      return current.value
    } finally {
      loading.value = false
    }
  }

  const update = async (id: number, dto: ArtworkUpdateDTO) => {
    await artworkApi.updateArtwork(id, dto)
  }

  const setRecommend = async (id: number, recommended: number) => {
    await artworkApi.setArtworkRecommend(id, recommended)
  }

  const remove = async (id: number) => {
    await artworkApi.deleteArtwork(id)
  }

  return {
    list,
    myArtworks,
    recommended,
    current,
    loading,
    fetchList,
    fetchRecommended,
    fetchMine,
    fetchDetail,
    update,
    setRecommend,
    remove,
  }
})
