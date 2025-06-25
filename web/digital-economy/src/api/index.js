import axios from 'axios';

// 创建一个axios实例，并配置基础URL
// 这样在调用接口时，我们就不需要重复写 'http://localhost:8080/api/evaluation'
const apiClient = axios.create({
    baseURL: 'http://localhost:8080/api/evaluation', // 后端API的基础路径
    headers: {
        'Content-Type': 'application/json',
    },
});

// 导出配置好的实例
export default apiClient;