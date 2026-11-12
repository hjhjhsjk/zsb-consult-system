<template>
  <div class="page-header">
    <div class="left">
      <h2 class="title">{{ title }}</h2>
      <span v-if="subtitle" class="subtitle">{{ subtitle }}</span>
    </div>
    <div class="right">
      <slot />
    </div>
  </div>
</template>

<script setup>
/**
 * 页面统一头：标题 + 副标题 + 右侧操作区
 * 四个管理页原来各自复制了一份 .page-header 样式，这里收成一处。
 */
defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' }
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 12px;
  flex-wrap: wrap;
}
.left {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.title {
  font-size: 20px;
  margin: 0;
  color: #303133;
}
.subtitle {
  font-size: 13px;
  color: #909399;
}
</style>
