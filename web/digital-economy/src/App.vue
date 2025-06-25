<script setup>
import Nav from './components/Navigator.vue'
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const isLogin = ref(true)

// 判断当前路由是否为 /login
const checkRoute = () => {
  isLogin.value = route.path !== '/login'
}

// 页面加载时检查路由
checkRoute()

// 监听路由变化
watch(
  () => route.path,
  () => {
    checkRoute()
  }
)
</script>

<template>
    <Nav v-if="isLogin"/>
    <RouterView />
</template>

<style scoped>
/* 导航栏 */
.nav {
    position: absolute;
    top: 10px;
    right: 0;
}
</style>