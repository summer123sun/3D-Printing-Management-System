/** 作品类型 */
export interface Artwork {
  artworkId: number
  taskId: string
  authorId: string
  authorName?: string
  artworkName: string
  previewImage?: string
  finishPhotos?: string
  experience?: string
  isRecommended: number
  viewCount: number
  createTime?: string
  updateTime?: string
}

/** 作品查询 */
export interface ArtworkQuery {
  page?: number
  size?: number
  authorId?: string
  isRecommended?: number
  keyword?: string
  sortBy?: 'latest' | 'hottest' | 'recommended'
}

/** 作品更新 DTO */
export interface ArtworkUpdateDTO {
  artworkName?: string
  finishPhotos?: string
  experience?: string
}
