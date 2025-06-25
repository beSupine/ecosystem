<template>
  <div style="width: 100%; height: 100%;" class="table-container">
    <div class="header-bar">
      <h3>档案详情 (ID: {{ recordData.id }})</h3>
      <el-button type="primary" size="small" @click="$emit('edit', recordData.id)">修改此档案</el-button>
    </div>
    <el-row :gutter="10">
      <el-col :span="8">
        <h4>协同子系统</h4>
        <el-table :data="synergyTableData" class="table" border height="58vh">
          <el-table-column prop="name" label="指标" />
          <el-table-column prop="value" label="值" />
        </el-table>
      </el-col>
      <el-col :span="8">
        <h4>服务子系统</h4>
        <el-table :data="serviceTableData" class="table" border height="58vh">
          <el-table-column prop="name" label="指标" />
          <el-table-column prop="value" label="值" />
        </el-table>
      </el-col>
      <el-col :span="8">
        <h4>资源子系统</h4>
        <el-table :data="resourceTableData" class="table" border height="58vh">
          <el-table-column prop="name" label="指标" />
          <el-table-column prop="value" label="值" />
        </el-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { defineProps, computed, defineEmits } from 'vue';

// --- 定义 Props 和 Emits ---
const props = defineProps({
  recordData: { type: Object, required: true }
});
defineEmits(['edit']); // 定义 'edit' 事件

// --- 指标 Key 到中文名的映射 ---
const keyToNameMap = {
  // 协同
  xv11: "产出数量", xv12: "产出效率", xv13: "产出质量", xv14: "产出效益",
  xv21: "数据覆盖率", xv22: "流动效率", xo11: "企业分布密度", xo21: "协同主体多样性",
  xo31: "协同主体协作程度", xo32: "协同主体竞争程度", xr11: "核心企业留存率",
  xr12: "合作关系稳定性指数", xr13: "价值链恢复效率", xs11: "协同数据完整性",
  // 资源
  zv11: "产出数量", zv12: "创新产量", zv21: "数据覆盖率", zv22: "流动效率",
  zv31: "流程整合能力", zv32: "更新淘汰能力", zo11: "构件层级深度",
  zo21: "数字资源多样性", zo31: "算法协作程度", zo32: "数字资源竞争程度", zs11: "平均接口响应时间",
  // 服务
  fv11: "产出数量", fv12: "创新产量", fv21: "数据覆盖率", fv22: "流动效率",
  fv31: "流程整合能力", fv32: "更新淘汰能力", fo11: "构件层级深度",
  fr11: "风险响应时间", fr12: "风险响应成功率", fr13: "威胁侦测能力",
  fs11: "请求响应能力", fs12: "服务请求活跃度"
};

// --- 数据处理 ---
// 将传入的单个记录对象，转换为三个表格所需的数据格式
const processDataForTable = (prefix) => {
  if (!props.recordData) return [];
  return Object.entries(props.recordData)
      .filter(([key]) => key.startsWith(prefix))
      .map(([key, value]) => ({
        name: keyToNameMap[key] || key, // 优先使用中文名
        value: value
      }));
};

// 使用计算属性，当 recordData 变化时自动更新表格数据
const synergyTableData = computed(() => processDataForTable('x'));
const serviceTableData = computed(() => processDataForTable('f'));
const resourceTableData = computed(() => processDataForTable('z'));

</script>

<style scoped>
.table-container {
  display: flex;
  flex-direction: column;
  height: 100%;
}
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
h3, h4 {
  margin: 0;
  color: #a9c5de;
}
h4 {
  text-align: center;
  margin-bottom: 10px;
}
:deep(.table) {
  width: 100%;
  --el-table-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-row-hover-bg-color: #4682b4;
}
</style>