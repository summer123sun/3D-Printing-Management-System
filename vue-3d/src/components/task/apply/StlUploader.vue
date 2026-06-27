<script setup lang="ts">
/**
 * STL 文件上传（**B**）
 *
 * 上传成功后返回服务器 URL（用于 task.stlFilePath）
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { validateFileSize, validateStlType } from '@/utils/validate'
import { uploadFile } from '@/api/file'

const props = withDefaults(defineProps<{
  modelValue?: string
  disabled?: boolean
}>(), {
  modelValue: '',
  disabled: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', val: string): void
}>()

const uploadRef = ref()
const uploading = ref(false)
const fileList = ref<Array<{ name: string; url: string }>>([])

if (props.modelValue) {
  fileList.value = [{ name: props.modelValue.split('/').pop() || 'stl', url: props.modelValue }]
}

const beforeUpload = (file: File) => {
  if (!validateStlType(file)) {
    ElMessage.error('只支持 .stl 文件')
    return false
  }
  if (!validateFileSize(50)(file)) {
    ElMessage.error('STL 文件不能超过 50MB')
    return false
  }
  return true
}

/** 真实上传到 /api/file/upload */
const customUpload = async (options: { file: File }) => {
  const file = options.file
  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const result = await uploadFile(formData)
    const url = result?.url ?? ''
    fileList.value = [{ name: file.name, url }]
    emit('update:modelValue', url)
    if (url) {
      ElMessage.success('STL 文件上传成功')
    } else {
      ElMessage.error('上传成功但未返回文件 URL，请联系管理员检查后端 /api/file/upload 返回格式')
    }
  } catch (e) {
    console.error('[StlUploader] 上传失败:', e)
  } finally {
    uploading.value = false
  }
}

const handleRemove = () => {
  fileList.value = []
  emit('update:modelValue', '')
}
</script>

<template>
  <el-upload
    ref="uploadRef"
    :file-list="fileList"
    :before-upload="beforeUpload"
    :http-request="customUpload"
    :on-remove="handleRemove"
    :show-file-list="true"
    :auto-upload="true"
    :limit="1"
    accept=".stl"
    drag
    :disabled="disabled"
  >
    <el-icon class="el-icon--upload"><upload-filled /></el-icon>
    <div class="el-upload__text">拖拽 STL 文件到此，或<em>点击上传</em></div>
    <template #tip>
      <div class="el-upload__tip">
        仅支持 .stl 格式，文件大小不超过 50MB
      </div>
    </template>
  </el-upload>
</template>

<style lang="scss" scoped>
.el-upload-dragger {
  padding: 20px;
}
</style>
