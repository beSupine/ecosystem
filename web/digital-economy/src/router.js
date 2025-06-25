import { createWebHistory, createRouter } from 'vue-router'

import ResourceMainpage from './components/ResourceMainpage.vue'
import ServiceMainpage from './components/ServiceMainpage.vue'
import SynergyMainpage from './components/SynergyMainpage.vue'
import ArchivesManage from './views/ArchivesManage.vue'
import LoginPage from './views/Login.vue'

const routes = [
    { path: '/', redirect: '/synergy' },
    { path: '/resource', component: ResourceMainpage },
    { path: '/service', component: ServiceMainpage },
    { path: '/synergy', component: SynergyMainpage },
    { path: '/manage', component: ArchivesManage },
    { path: '/login', component: LoginPage },
    
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router