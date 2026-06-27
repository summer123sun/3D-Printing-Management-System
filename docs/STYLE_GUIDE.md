# 视觉设计规范（v2 深海蓝金）

> 3D 打印科创会管理系统 · 视觉指南
> 版本：v2.0 · 更新于 2026-06-27

---

## 一、配色方案：深海蓝金

**设计定位**：SaaS / 金融科技 / 企业官网风格 — 沉稳、权威、有质感。

| 角色 | 色值 | 用途 |
|------|------|------|
| **主色** | `#0A2540` | 背景、标题、品牌主色、按钮 |
| **辅色** | `#00D4AA` | 薄荷青 — 按钮 hover、链接、focus、强调 |
| **点缀** | `#FFD700` | 金色 — 重要数据、星级、推荐、徽章 |
| **背景** | `#F6F9FC` | 浅白 — 页面背景、卡片次背景 |
| **文字** | `#0A2540` | 主文字（与主色同源，深色稳重） |
| **次文字** | `#6B7C93` | 蓝灰 — 副标题、说明 |

### 1.1 变体色

| 名称 | 浅 1 | 浅 2 | 浅 3 | 原色 | 深 1 |
|------|------|------|------|------|------|
| 主色（深海蓝） | `#E8EEF5` | `#1E3A5F` | `#2C4F75` | `#0A2540` | `#061a2e` |
| 辅色（薄荷青） | `#E0FAF4` | `#4FE5C7` | `#33DBB8` | `#00D4AA` | `#00A88A` |
| 点缀（金色） | `#FFF9D6` | `#FFE44D` | `#FFEB7A` | `#FFD700` | `#CCB000` |

### 1.2 功能色

| 状态 | 色值 | 备注 |
|------|------|------|
| 成功 | `#00D4AA` | = 辅色，整体感强 |
| 警告 | `#FFD700` | = 点缀色，金色醒目 |
| 危险 | `#FF4757` | 红，对比强 |
| 信息 | `#6B7C93` | 蓝灰，沉稳 |

### 1.3 中性色

| 名称 | 色值 | 用途 |
|------|------|------|
| 文字主 | `#0A2540` | 标题、强调 |
| 文字常规 | `#2C3E50` | 正文 |
| 文字次 | `#6B7C93` | 副标题、说明 |
| 文字占位 | `#9AA8BC` | placeholder |
| 边框 | `#DCE3EC` | 卡片、输入框 |
| 边框浅 | `#E8EEF5` | 分割线 |
| 背景 | `#F6F9FC` | 页面背景 |
| 卡片 | `#FFFFFF` | 卡片、弹窗 |
| 深色 | `#0A2540` | header、弹窗标题栏 |

### 1.4 渐变

```scss
// 品牌主渐变
$brand-gradient: linear-gradient(135deg, #0A2540 0%, #1E3A5F 100%);

// 辅色渐变
$brand-gradient-accent: linear-gradient(135deg, #00D4AA 0%, #4FE5C7 100%);

// 金色渐变
$brand-gradient-gold: linear-gradient(135deg, #FFD700 0%, #FFA500 100%);

// 三色混合（弹窗顶部装饰条）
background: linear-gradient(90deg, #0A2540 0%, #00D4AA 50%, #FFD700 100%);
```

---

## 二、弹窗规范

### 2.1 统一组件

所有弹窗**统一使用 `AppDialog` 组件**（`src/components/common/AppDialog.vue`），不要再用 Element Plus 的 `el-dialog`。

```vue
<AppDialog
  v-model="visible"
  title="分配打印机"
  icon="Printer"
  type="primary"      <!-- primary | success | warning | danger | info -->
  width="520px"
  confirm-text="确认"
  cancel-text="取消"
  :loading="submitting"
  @confirm="handleConfirm"
  @cancel="handleCancel"
>
  <el-form>...</el-form>
</AppDialog>
```

**特性**：
- 圆角 16px + 大阴影
- 顶部彩色渐变标题栏（按 type 变色）
- 弹跳进入动画（scale + 弹性回弹）
- 关闭按钮 hover 旋转 90° + 放大
- 按钮 hover 上浮 + 主题色阴影
- 遮罩半透明黑 + 模糊

### 2.2 ElMessageBox 兼容

如果代码里**已经有** `ElMessageBox.confirm()` / `ElMessageBox.alert()`，**不需要改代码**。

全局 CSS（`src/styles/index.scss`）会自动拦截并应用深海蓝金风格：
- 圆角 16px + 阴影
- 顶部 6px 三色渐变装饰条
- 状态图标圆形背景（按类型着色）
- 按钮主色 = 深海蓝渐变

> 39 处 ElMessageBox 调用全部自动统一样式，不用逐个改。

### 2.3 弹窗类型色对照

| type | 标题栏 | 主按钮 | 适用场景 |
|------|--------|--------|----------|
| `primary` | 深海蓝渐变 | 深海蓝 | 默认：表单、确认、编辑 |
| `success` | 薄荷青渐变 | 薄荷青 | 成功反馈、完成提示 |
| `warning` | 金色渐变 | 金色 | 警告、注意事项 |
| `danger` | 红色渐变 | 红色 | 删除、不可逆操作 |
| `info` | 蓝灰渐变 | 蓝灰 | 信息提示、说明 |

---

## 三、动效清单

### 3.1 弹窗动画

```scss
// 弹跳进入
animation: dialog-pop-in 0.32s cubic-bezier(0.34, 1.56, 0.64, 1);

// 弹窗退出
animation: dialog-pop-out 0.2s cubic-bezier(0.65, 0, 0.35, 1);
```

### 3.2 页面切换动画（路由级）

每个路由的 `meta.transition` 决定动画：

| transition | 效果 | 适用 |
|------------|------|------|
| `page-fade` | 纯淡入 | 登录、错误页 |
| `page-slide-up` | 淡入+上滑 | 首页、统计看板 |
| `page-zoom` | 淡入+缩放 | 列表页 |
| `page-slide-left` | 从右滑入 | 详情页 |
| `page-flip` | 3D 翻转 | 申请/创建/编辑页 |
| `page-slide-down` | 从上滑入 | 设置/配置 |

### 3.3 按钮 hover

- **上浮**：`translateY(-1px)`
- **阴影**：`box-shadow: 0 4px 10px` 主题色 18% 透明
- **过渡**：`0.2s $ease-out-soft`
- **active**：回落 0 + 阴影减弱

### 3.4 菜单 hover

- **左移**：`padding-left: 22px`（从默认 20px 滑入）
- **背景色**：浅主题色

### 3.5 表格 hover

- **行背景**：`color-mix($accent-color 6%, transparent)`
- **过渡**：`0.2s ease`

### 3.6 输入框 focus

- **光晕**：`box-shadow: 0 0 0 3px` 薄荷青 25% 透明
- **过渡**：`0.25s $ease-out-soft`

### 3.7 顶部进度条

NProgress 颜色已改成薄荷青渐变 + 发光阴影：
```scss
background: linear-gradient(90deg, #7c3aed 0%, #a78bfa 50%, #c4b5fd 100%);
```

### 3.8 关键帧一览

`src/styles/variables.scss` 中定义：
- `fade-in` / `fade-in-up` / `fade-in-down` / `fade-in-scale`
- `slide-in-left` / `slide-in-right`
- `flip-in` / `pop-in`
- `fade-out-scale` / `success-pop`
- `gradient-flow` / `progress-stripe`
- `spin`

### 3.9 缓动函数

```scss
$ease-out-soft: cubic-bezier(0.16, 1, 0.3, 1);   // 缓出
$ease-out-back: cubic-bezier(0.34, 1.56, 0.64, 1); // 弹性回弹
$ease-in-out:   cubic-bezier(0.65, 0, 0.35, 1);  // 缓入缓出
```

---

## 四、登录页布局

**v2 布局**：左右两列

```
┌─────────────────────┬──────────────┐
│   左侧：品牌展示     │  右侧：表单  │
│   ┌─────────────┐  │              │
│   │  背景图     │  │  欢迎登录    │
│   │  + 渐变     │  │  学号 [...]  │
│   │  + Logo     │  │  密码 [...]  │
│   │  + 4 特性   │  │  [ 登录 ]   │
│   │  + 装饰     │  │  默认密码    │
│   └─────────────┘  │              │
└─────────────────────┴──────────────┘
```

**背景图**：
- 路径：`src/assets/login-bg.jpg`
- 找不到时自动 fallback 到深海蓝渐变（不会白屏）
- 推荐尺寸：1920×1080 或 2560×1440
- 主题建议：3D 打印相关、暗色调、留出右侧文字空间

**移动端**：上下布局（品牌区压缩到 220px，4 特性隐藏）

---

## 五、Element Plus 主题覆盖

`src/styles/element-theme.ts` 在 main.ts 启动时动态注入 CSS 变量：

```ts
--el-color-primary: #0A2540;  // 深海蓝
--el-color-success: #00D4AA;  // 薄荷青
--el-color-warning: #FFD700;  // 金色
--el-color-danger:  #FF4757;  // 红
--el-color-info:    #6B7C93;  // 蓝灰
```

每个颜色自动生成 9 档 light + dark 变体（程序计算 hex 混合）。

---

## 六、滚动条 + 选中配色

```scss
// 滚动条
::-webkit-scrollbar-thumb {
  background: color-mix($primary-color 25%, #d1d5db);
  &:hover { background: color-mix($primary-color 45%, #d1d5db); }
}

// 文字选中
::selection {
  background: color-mix($accent-color 30%, transparent);
  color: $text-primary;
}
```

---

## 七、文件清单

| 路径 | 作用 |
|------|------|
| `src/styles/variables.scss` | SCSS 变量 + mixin + 关键帧 |
| `src/styles/index.scss` | 全局样式入口（按钮/弹窗/表格/菜单/进度条/路由动画） |
| `src/styles/element-theme.ts` | Element Plus CSS 变量覆盖 |
| `src/components/common/AppDialog.vue` | 统一弹窗组件 |
| `src/views/login/index.vue` | 登录页（左右两列 + 背景图） |
| `src/components/layout/AppMain.vue` | 路由切换动画容器 |
| `src/router/routes.ts` | 每个路由的 `meta.transition` |
| `src/router/guard.ts` | NProgress 紫色定制 |

---

## 八、约定

1. **新弹窗**统一用 `AppDialog`，不要再用 `el-dialog` 或 `ElMessageBox.confirm`
2. **配色**用 SCSS 变量（`$primary-color` 等），不要硬编码 hex
3. **动效**用 `variables.scss` 里的 mixin 和 keyframes，统一节奏
4. **图标的图标**在 `AppDialog` 的 `icon` 属性里指定组件名（`Printer` / `Check` 等），会自动渲染
5. **危险操作**（删除/取消）弹窗用 `type="danger"`，提醒用户注意

---

**v2 设计已锁定，不要随意改主题色。改完记得同步更新本文档。**
