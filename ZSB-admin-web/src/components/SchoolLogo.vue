<template>
  <div class="school-logo" :style="boxStyle">
    <img
      v-if="showImg"
      :src="fullSrc"
      :alt="name"
      :loading="lazy ? 'lazy' : 'eager'"
      @error="failed = true"
    />
    <span v-else class="fallback">{{ initial }}</span>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { fixImg } from '@/utils/image'

/**
 * 院校 Logo：图片加载失败自动退化成「首字色块」。
 * 原先 5 个页面各写一份 fixImg + @error，重复且容易忘。
 */
const props = defineProps({
  src: { type: String, default: '' },
  name: { type: String, default: '' },
  size: { type: Number, default: 40 },
  radius: { type: Number, default: 6 },
  lazy: { type: Boolean, default: true }
})

const failed = ref(false)

// 换了校徽要重新给一次机会
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
