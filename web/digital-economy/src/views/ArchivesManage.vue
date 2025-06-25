<script setup>
import { reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import synergyData from '../assets/SynergyIndex.json';
import serviceData from '../assets/ServiceIndex.json';
import resourceData from '../assets/ResourceIndex.json';
import Table from '../components/Table.vue';
import DataInput from '../components/DataInput.vue';

const isDataInputVisible = ref(false);
const selectedRow = ref(null);
const isTableVisible = ref(false);
const value1 = ref('');
const filteredArchives = ref([]);

// const handleData = (data) => {
//   return Object.entries(data.index).map(([key, value]) => ({ key, value: value[value.length - 1] }));
// };

// console.log(handleData(synergyData))

const archives = reactive([
  { id: 1, date: '2023-10-01', title: '档案1' },
  { id: 2, date: '2024-3-02', title: '档案2' },
  { id: 3, date: '2024-11-12', title: '档案3' },
  { id: 3, date: '2024-12-13', title: '档案4' },
  { id: 3, date: '2024-1-03', title: '档案5' },
  { id: 3, date: '2024-3-11', title: '档案6' },
  { id: 3, date: '2024-5-22', title: '档案7' },
  { id: 3, date: '2024-7-12', title: '档案9' },
  { id: 3, date: '2025-2-03', title: '档案11' },
  { id: 3, date: '2025-3-2', title: '档案13' },
]);

// 查看详情
const handleDetail = (row) => {
  console.log('查看详情:', row);
  selectedRow.value = row;
  isTableVisible.value = true;
};

// 删除档案
const handleDelete = (row) => {
  ElMessage.warning(`删除档案: ${row.title}`);
  // 在实际项目中，这里应该调用后端接口删除数据
  const index = archives.indexOf(row);
  if (index > -1) {
    archives.splice(index, 1);
  }
};

const handleAdd = () => {
  isDataInputVisible.value = true;
};

// 新增：处理取消事件，返回到 Table
const handleCancel = () => {
  isDataInputVisible.value = false;
};

// 新增：根据日期范围筛选档案
const filterArchivesByDate = () => {
  if (!value1.value || value1.value.length !== 2) {
    ElMessage.warning('请选择完整的日期范围');
    return;
  }

  const [startDate, endDate] = value1.value;
  
  if (!startDate || !endDate) {
    ElMessage.warning('请选择完整的日期范围');
    return;
  }

  const start = new Date(startDate);
  const end = new Date(endDate);

  // 如果开始日期大于结束日期，交换它们
  if (start > end) {
    [start, end] = [end, start];
  }

  // 筛选日期范围内的档案
  filteredArchives.value = archives.filter(archive => {
    const archiveDate = new Date(archive.date);
    return archiveDate >= start && archiveDate <= end;
  });

  // 显示筛选结果
  console.log('筛选结果:', filteredArchives.value);
};

// 新增：重置筛选条件并显示所有记录
const resetFilter = () => {
  value1.value = ''; // 清空日期选择器的值
  filteredArchives.value = []; // 清空筛选结果
};

// 新增：在 archives 数组末尾添加一条新的记录
const addArchiveRecord = (newRecord) => {
  archives.push(newRecord);
  ElMessage.success('新增记录成功');
};

</script>

<template>
  <div class="container" id="left">
    <el-container>
      <el-header class="button-field">
        <el-row :gutter="60">
          <el-col span="1">
            <el-button type="primary" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col span="10" offset="2">
            <el-date-picker v-model="value1" type="daterange" range-separator="到" start-placeholder="开始日期"
              end-placeholder="结束日期" :size="default" />
          </el-col>
          <el-col span="4" offset="2">
            <el-button type="warning" @click="resetFilter">重置</el-button> <!-- 修改：点击重置按钮时调用 resetFilter 方法 -->
            <el-button type="primary" @click="filterArchivesByDate">查询</el-button>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 0;">
        <el-table :data="filteredArchives.length > 0 ? filteredArchives : archives" class="table">
          <el-table-column type="index" label="序号" width="100" />
          <el-table-column prop="date" label="创建日期" width="280" />
          <el-table-column prop="title" label="档案名" />
          <el-table-column label="操作" width="150">
            <template #default="scope">
              <el-button size="small" @click="handleDetail(scope.row)">详情</el-button>
              <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-main>
    </el-container>
  </div>
  <div class="container" id="right">
    <component
      :is="isDataInputVisible ? DataInput : (isTableVisible ? Table : null)"
      v-if="!isDataInputVisible || (synergyData && serviceData && resourceData)"
      :synergy-data="synergyData"
      :service-data="serviceData"
      :resource-data="resourceData"
      :selected-row="selectedRow"
      @cancel="handleCancel"
      @submit-success="addArchiveRecord"
    />
  </div>
  <div class="subtitle">
    档案管理
  </div>
</template>

<style scoped>
:deep(.table) {
  width: 100%;
  /* 背景色设置为透明 */
  --el-table-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-row-hover-bg-color: #4682b4;
}

#left {
  position: absolute;
  top: 13vh;
  left: 2vw;
  height: 75.5vh;
  width: 35vw;
  /* 添加模糊背景 */
  backdrop-filter: blur(10px);
  background-color: rgba(0, 0, 0, 0.2);
  align-items: flex-start;
  padding: 0;
}

#right {
  position: absolute;
  top: 13vh;
  left: 38.7vw;
  height: 74vh;
  width: 59vw;
  /* 添加模糊背景 */
  backdrop-filter: blur(10px);
  background-color: rgba(0, 0, 0, 0.2);
  padding-right: 20px;
}

.button-field {
  width: 100%;
  display: flex;
  align-items: center;
  padding: 5px;
}

.el-row {
  left: 10px;
}
</style>