<script setup lang="ts">
/**
 * STL 文件上传（**B**）
 *
 * 上传成功后返回服务器 URL（用于 task.stlFilePath）
 *
 * 简化方案：调通用上传接口 /api/file/upload（待 D 实现）
 * 这里 mock：直接用文件名作为路径，不真上传
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { validateFileSize, validateStlType } from '@/utils/validate'

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

/**
 * 自定义上传：调 /api/file/upload
 *
 * TODO: 后端 D 同学实现 /api/file/upload 后，改成真实调用：
 *   const form = new FormData()
 *   form.append('file', file)
 *   form.append('type', 'stl')
 *   const res = await uploadFile(form)
 *   emit('update:modelValue', res.data.url)
 */
const customUpload = (options: { file: File }): Promise<void> => {
  const file = options.file
  // mock：直接用文件名作为路径（生产环境会改成真实上传）
  const fakeUrl = `/uploads/stl/${file.name}`
  fileList.value = [{ name: file.name, url: fakeUrl }]
  emit('update:modelValue', fakeUrl)
  ElMessage.success('STL 文件已上传（mock）')
  return Promise.resolve()
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