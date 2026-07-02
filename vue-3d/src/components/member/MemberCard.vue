<script setup lang="ts">
/**
 * MemberCard - 成员端通用卡片（柔阴影 + 大圆角 + hover 上浮）
 *
 * 用法：
 *   <MemberCard hoverable>
 *     <template #header>标题</template>
 *     内容
 *   </MemberCard>
 */
interface Props {
  /** 是否可点击（hover 上浮） */
  hoverable?: boolean
  /** 自定义内边距 */
  padding?: string
  /** 圆角大小，默认 16px */
  radius?: number
}

withDefaults(defineProps<Props>(), {
  hoverable: false,
  padding: '24px',
  radius: 16,
})
</script>

<template>
  <div
    class="member-card"
    :class="{ 'is-hoverable': hoverable }"
    :style="{ padding, borderRadius: radius + 'px' }"
  >
    <div v-if="$slots.header" class="member-card-header">
      <slot name="header" />
    </div>
    <div class="member-card-body">
      <slot />
    </div>
    <div v-if="$slots.footer" class="member-card-footer">
      <slot name="footer" />
    </div>
  </div>
</template>

<style lang="scss" scoped>
.member-card {
  background: #FFFFFF;
  border: 1px solid var(--border-extra-light);
  box-shadow: 0 2px 12px rgba(10, 37, 64, 0.05);
  transition: transform 0.25s cubic-bezier(0.16, 1, 0.3, 1),
              box-shadow 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  display: flex;
  flex-direction: column;
  gap: 12px;

  &.is-hoverable {
    cursor: pointer;

    &:hover {
      transform: translateY(-3px);
      box-shadow: 0 10px 24px rgba(10, 37, 64, 0.12);
    }
  }
}

.member-card-header {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.member-card-body {
  flex: 1;
  color: var(--text-regular);
  font-size: 14px;
  line-height: 1.6;
}

.member-card-footer {
  border-top: 1px solid var(--border-extra-light);
  padding-top: 12px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}
</style>
