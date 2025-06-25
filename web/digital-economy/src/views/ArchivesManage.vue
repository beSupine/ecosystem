<script setup>
import { reactive, ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import apiClient from '../api';
import Table from '../components/Table.vue';
import DataInput from '../components/DataInput.vue';
import dayjs from 'dayjs';

// --- 组件状态 ---
const isDataInputVisible = ref(false); // 控制新增/编辑表单的显示
const isTableVisible = ref(false);     // 控制详情表格的显示
const dateRange = ref('');             // 日期选择器的值
const archives = ref([]);              // 表格显示的档案列表
const selectedRecord = ref(null);      // 当前选中的记录（用于详情/编辑）
const isEditMode = ref(false);         // 标记当前是新增还是编辑模式

// --- 数据获取与操作 ---
// 获取档案列表
const fetchArchives = async (startDate, endDate) => {
  try {
    // 如果没有提供日期，则默认查询一个非常大的范围来获取所有数据
    const params = {
      startDate: startDate ? dayjs(startDate).startOf('day').toISOString() : '1970-01-01T00:00:00',
      endDate: endDate ? dayjs(endDate).endOf('day').toISOString() : '2999-12-31T23:59:59',
    };
    const response = await apiClient.get('/by-date-range', { params });
    // 后端返回的数据是按时间倒序的，这里保持
    archives.value = response.data.records.map(r => ({ ...r, date: dayjs(r.entryTime).format('YYYY-MM-DD HH:mm:ss') }));
  } catch (error) {
    ElMessage.error('获取档案列表失败');
    console.error(error);
  }
};

// 组件挂载时获取初始数据
onMounted(() => {
  fetchArchives();
});

// 查看详情
const handleDetail = async (row) => {
  try {
    const response = await apiClient.get(`/${row.id}`);
    selectedRecord.value = response.data;
    isTableVisible.value = true;
    isDataInputVisible.value = false; // 确保与新增/编辑表单互斥
  } catch (error) {
    ElMessage.error('获取档案详情失败');
    console.error(error);
  }
};

// 删除档案
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除档案 (ID: ${row.id}) 吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    try {
      await apiClient.delete(`/${row.id}`);
      ElMessage.success('删除成功');
      fetchArchives(); // 重新加载列表
    } catch (error) {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }).catch(() => {
    // 用户取消操作
  });
};

// 点击新增按钮
const handleAdd = () => {
  isEditMode.value = false;
  selectedRecord.value = null; // 清空旧数据
  isDataInputVisible.value = true;
  isTableVisible.value = false;
};

// 点击“修改”按钮（来自Table组件的事件）
const handleEdit = async (id) => {
  try {
    const response = await apiClient.get(`/${id}`);
    selectedRecord.value = response.data;
    isEditMode.value = true;
    isDataInputVisible.value = true;
    isTableVisible.value = false;
  } catch (error) {
    ElMessage.error('加载待编辑数据失败');
  }
};

// 处理取消事件，返回到 Table
const handleCancel = () => {
  isDataInputVisible.value = false;
};

// 处理表单提交成功事件（来自DataInput组件）
const handleSubmitSuccess = () => {
  isDataInputVisible.value = false;
  fetchArchives(); // 刷新列表
};


// 根据日期范围筛选档案
const filterArchivesByDate = () => {
  if (!dateRange.value || dateRange.value.length !== 2) {
    ElMessage.warning('请选择完整的日期范围');
    return;
  }
  const [startDate, endDate] = dateRange.value;
  fetchArchives(startDate, endDate);
};

// 重置筛选条件并显示所有记录
const resetFilter = () => {
  dateRange.value = ''; // 清空日期选择器的值
  fetchArchives(); // 重新获取所有数据
};

</script>

<template>
  <div class="container" id="left">
    <el-container>
      <el-header class="button-field">
        <el-row :gutter="20" align="middle" style="width: 100%;">
          <el-col :span="4">
            <el-button type="primary" @click="handleAdd">新增</el-button>
          </el-col>
          <el-col :span="14">
            <el-date-picker v-model="dateRange" type="daterange" range-separator="到" start-placeholder="开始日期"
                            end-placeholder="结束日期" style="width: 100%;"/>
          </el-col>
          <el-col :span="6">
            <el-button @click="resetFilter">重置</el-button>
            <el-button type="primary" @click="filterArchivesByDate">查询</el-button>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 0;">
        <el-table :data="archives" class="table">
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="date" label="创建日期" />
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
    <DataInput
        v-if="isDataInputVisible"
        :is-edit="isEditMode"
        :initial-data="selectedRecord"
        @cancel="handleCancel"
        @submit-success="handleSubmitSuccess"
    />
    <Table
        v-else-if="isTableVisible && selectedRecord"
        :record-data="selectedRecord"
        @edit="handleEdit"
    />
    <div v-else class="placeholder">
      请在左侧选择一个档案查看详情，或点击“新增”按钮。
    </div>
  </div>
  <div class="subtitle">
    档案管理
  </div>
</template>

<style scoped>
:deep(.table) {
  width: 100%;
  --el-table-bg-color: transparent;
  --el-table-header-bg-color: transparent;
  --el-table-tr-bg-color: transparent;
  --el-table-row-hover-bg-color: #4682b4;
}
#left {
  position: absolute; top: 13vh; left: 2vw; height: 75.5vh; width: 35vw;
  backdrop-filter: blur(10px); background-color: rgba(0, 0, 0, 0.2);
  align-items: flex-start; padding: 0;
}
#right {
  position: absolute; top: 13vh; left: 38.7vw; height: 74vh; width: 59vw;
  backdrop-filter: blur(10px); background-color: rgba(0, 0, 0, 0.2);
  padding: 20px;
}
.button-field {
  width: 100%; display: flex; align-items: center; padding: 15px 10px; height: auto;
}
.placeholder {
  color: #ccc; font-size: 16px; text-align: center;
  display: flex; justify-content: center; align-items: center; height: 100%;
}
</style>