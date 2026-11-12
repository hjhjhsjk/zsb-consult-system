<template>
  <div class="school-logo" :style="boxStyle">
    <img
      v-if="showImg"
      :src="fullSrc"
      :alt="name"
      :loading="lazy ? 'lazy' : 'eager'"
      decoding="async"
      @error="failed = true"
    />
    <span v-else class="fallback">{{ initial }}</span>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { fixImg } from '@/utils/image'

/**
 * 院校 Logo 统一组件：加载失败自动退化为「首字色块」。
 * 首页、院校库、搜索、详情页共用，避免每个页面重复写 fixImg + onerror。
 */
const props = defineProps({
  src: { type: String, default: '' },
  name: { type: String, default: '' },
  size: { type: Number, default: 40 },
  radius: { type: Number, default: 8 },
  lazy: { type: Boolean, default: true }
})

const failed = ref(false)
watch(() => props.src, () => { failed.value = false })

const showImg = computed(() => !!props.src && !failed.value)
const fullSrc = computed(() => fixImg(props.src))
const initial = computed(() => (props.name || '?').trim().charAt(0))
const boxStyle = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px',
  borderRadius: props.radius + 'px',
  fontSize: Math.round(props.size * 0.42) + 'px'
}))
</script>

<style scoped>
.school-logo {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  background: #f5f7fa;
}
.school-logo img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
}
.fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #409eff, #2c5aa0);
  color: #fff;
  font-weight: 600;
}
</style>
