<template>
<el-menu class="nav" mode="horizontal" :ellipsis="false" :router="true">
        <el-sub-menu>
            <template #title>子系统选择</template>
            <el-menu-item index="/synergy">
                协同子系统
            </el-menu-item>
            <el-menu-item index="/service">
                服务子系统
            </el-menu-item>
            <el-menu-item index="/resource">
                资源子系统
            </el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/manage">档案管理</el-menu-item>
        <!-- <el-menu-item>指标管理</el-menu-item> -->
    </el-menu>
</template>

<style scoped>
/* 导航栏 */
.nav {
    position: absolute;
    top: 10px;
    right: 0;
}
</style>