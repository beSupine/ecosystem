<template>
        <!-- 添加背景框和标题 -->
        <div class="login-container">
            <h2 class="login-title">登录</h2>
            <!-- 使用 ElInput 美化账号密码输入框 -->
            <el-input v-model="username" placeholder="账号" style="margin-bottom: 10px; width: 200px;"></el-input>
            <el-input v-model="password" type="password" placeholder="密码" style="margin-bottom: 10px; width: 200px;"></el-input>
            <!-- 使用 ElButton 美化登录按钮 -->
            <el-button type="primary" @click="handleLogin">登录</el-button>
        </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElInput, ElButton } from 'element-plus' // 引入 Element Plus 组件

// 定义账号和密码的响应式变量
const username = ref('')
const password = ref('')

// 获取路由实例
const router = useRouter()

// 登录逻辑
const handleLogin = () => {
    // 模拟登录逻辑，假设账号为 admin，密码为 123456
    if (username.value === 'admin' && password.value === '123456') {
        // 登录成功后跳转到 /service
        router.push('/service')
    } else {
        alert('账号或密码错误')
    }
}
</script>

<style scoped>

.login-container {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 200px;
    width: 300px;
    background-color: #1c1d1f; /* 修改为半透明灰色背景 */
    padding: 30px; /* 增加内边距 */
    border-radius: 12px; /* 更圆润的圆角 */
    border: 1px solid rgba(255, 255, 255, 0.1);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2); /* 更明显的阴影效果 */
    text-align: center; /* 文本居中 */
    transition: all 0.3s ease; /* 添加过渡效果 */
}

.login-container:hover {
    transform: translateY(-5px); /* 鼠标悬停时轻微上移 */
    box-shadow: 0 6px 25px rgba(0, 0, 0, 0.3); /* 鼠标悬停时增加阴影 */
}

.login-title {
    margin-bottom: 25px; /* 增加标题与内容的间距 */
    font-size: 26px; /* 增大标题字体大小 */
    color: #ffffff; /* 修改标题颜色为白色 */
    text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.1); /* 添加标题阴影 */
}

/* 修改输入框和按钮的文字颜色为白色 */
:deep(.el-input__inner), :deep(.el-button) {
    color: #ffffff; /* 输入框和按钮文字颜色改为白色 */
}
</style>