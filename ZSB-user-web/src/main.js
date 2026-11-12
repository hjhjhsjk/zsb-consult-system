import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { createPinia } from 'pinia'
import { ElMessage } from 'element-plus'

const app = createApp(App)

// 组件内部漏 try/catch 的异常，至少别让用户面对白屏
app.config.errorHandler = (err, instance, info) => {
  console.error('[全局异常]', info, err)
  ElMessage.error('页面出了点问题，请刷新重试')
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
