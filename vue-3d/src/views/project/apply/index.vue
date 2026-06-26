<script setup lang="ts">
/**
 * 申请加入项目（**B** - 社员端）
 *
 * v2 设计：成员由项目负责人/技术骨干直接添加，没有"申请加入"流程。
 * 本页只做一个提示页，引导用户去详情页联系负责人。
 */
import { useRouter } from 'vue-router'
import PageHeader from '@/components/common/PageHeader.vue'

const router = useRouter()
</script>

<template>
  <div class="project-apply-page">
    <PageHeader title="申请加入项目" :show-back="true" @back="router.back()" />

    <el-card>
      <el-result icon="info" title="项目成员由负责人直接添加">
        <template #sub-title>
          <div style="text-align: left">
            <p>当前版本没有"申请加入"流程，社团成员由项目负责人/技术骨干直接添加。</p>
            <p>如果你想参与某个项目：</p>
            <ol>
              <li>在项目列表找感兴趣的项目</li>
              <li>打开详情页查看负责人是谁</li>
              <li>线下联系负责人，由他/她在项目详情页"添加成员"即可</li>
            </ol>
          </div>
        </template>
        <template #extra>
          <el-button type="primary" @click="router.push('/project/list')">去看看项目</el-button>
        </template>
      </el-result>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.project-apply-page {
  padding: 0;
}
</style>