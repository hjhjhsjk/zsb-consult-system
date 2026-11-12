import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { ElMessage } from 'element-plus'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 注意：图标不再全量注册（原来 1000+ 个图标全打进包里）。
// 各页面按需 import，缺哪个补哪个即可。
app.config.errorHandler = (err, instance, info) => {
  console.error('[全局异常]', info, err)
  ElMessage.error('页面出了点问题，请刷新重试')
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
