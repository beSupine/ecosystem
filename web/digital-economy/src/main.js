import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import 'element-plus/dist/index.css';
import 'dayjs/locale/zh-cn';
import locale from 'element-plus/es/locale/lang/zh-cn';

createApp(App)
    .use(ElementPlus,{locale})
    .use(router)
    .mount('#app')
