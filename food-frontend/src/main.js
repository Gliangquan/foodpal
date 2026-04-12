// 修复 Node.js 全局变量在浏览器中的兼容性问题
if (typeof global === 'undefined') {
  window.global = window;
}
if (typeof process === 'undefined') {
  window.process = { env: {} };
}
if (typeof Buffer === 'undefined') {
  window.Buffer = {};
}

import { createApp } from 'vue';
import { createPinia } from 'pinia';
import Antd from 'ant-design-vue';
import App from './App.vue';
import 'ant-design-vue/dist/reset.css';
import router from './router'; // 确保引入 router


const app = createApp(App);
const pinia = createPinia();

app.use(router); // 挂载 router
app.use(pinia); // 挂载 Pinia

app.use(Antd).mount('#app');