/**
 * 成员端 / 后台端 视觉风格判定
 *
 * 用法：
 *   const { isMember, isNewbie, isAdmin } = useMemberStyle()
 *   if (isMember.value) { ... } // role >= 3 (社员/新成员) 走友好插图风
 *
 * 设计原则：
 * - 角色 1 (社长) / 2 (技术骨干) -> 后台管理风 (admin)
 * - 角色 3 (社员) / 4 (新成员)  -> 友好插图风 (member)
 */
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { Role } from '@/types/member'

export function useMemberStyle() {
  const authStore = useAuthStore()

  const currentRole = computed(() => authStore.user?.role ?? Role.NEWBIE)

  const isAdmin = computed(
    () => currentRole.value === Role.PRESIDENT || currentRole.value === Role.TECH_LEAD
  )
  const isMember = computed(
    () => currentRole.value === Role.MEMBER || currentRole.value === Role.NEWBIE
  )
  const isNewbie = computed(() => currentRole.value === Role.NEWBIE)
  const isPresident = computed(() => currentRole.value === Role.PRESIDENT)

  return {
    currentRole,
    isAdmin,
    isMember,
    isNewbie,
    isPresident,
  }
}
