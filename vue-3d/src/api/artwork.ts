/**
 * 作品库 API（M4）
 */
import { get, post, put, del } from '@/utils/request'
import type { Artwork, ArtworkQuery, ArtworkUpdateDTO } from '@/types/artwork'
import type { PageResult } from '@/types/api'

/** 作品创建 DTO（前端 + 后端对齐） */
export interface ArtworkCreateDTO {
  taskId: string
  artworkName?: string
  previewImage?: string
  finishPhotos?: string
  experience?: string
}

/** 作品列表 */
export const listArtworks = (query: ArtworkQuery) =>
  get<PageResult<Artwork>>('/artwork', query as any)

/** 推荐作品 */
export const recommendedArtworks = (limit = 12) =>
  get<PageResult<Artwork>>('/artwork/recommended', { limit })

/** 我的作品 */
export const myArtworks = (query: ArtworkQuery) =>
  get<PageResult<Artwork>>('/artwork/my', query as any)

/** 作品详情 */
export const artworkDetail = (id: number) =>
  get<Artwork>(`/artwork/${id}`)

/** 登记作品（从已完结任务） */
export const createArtwork = (dto: ArtworkCreateDTO) =>
  post<number>('/artwork', dto)

/** 更新作品 */
export const updateArtwork = (id: number, dto: ArtworkUpdateDTO & { previewImage?: string }) =>
  put<void>(`/artwork/${id}`, dto)

/** 设置推荐 */
export const setArtworkRecommend = (id: number, recommended: number) =>
  put<void>(`/artwork/${id}/recommend`, undefined, { params: { recommended } } as any)

/** 删除作品 */
export const deleteArtwork = (id: number) =>
  del<void>(`/artwork/${id}`)
