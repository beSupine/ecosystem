<template>
  <div style="width: 100%; height: 100%;">
    <el-row :gutter="30">
      <el-col :span="8">
        <h3>{{ systemNames.synergy }}</h3>
      </el-col>
      <el-col :span="8">
        <h3>{{ systemNames.service }}</h3>
      </el-col>
      <el-col :span="8">
        <h3>{{ systemNames.resource }}</h3>
      </el-col>
    </el-row>
    <el-row :gutter="30">
      <el-col :span="8">
        <el-table :data="handleData(synergyData)" class="table" border>
          <el-table-column prop="key" label="指标" />
          <el-table-column prop="value" label="值" />
          <el-table-column label="操作" width="75">
            <template #default="scope">
              <el-button size="small" @click="handleDetail(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :span="8">
        <el-table :data="handleData(serviceData)" class="table" border>
          <el-table-column prop="key" label="指标" />
          <el-table-column prop="value" label="值" />
          <el-table-column label="操作" width="75">
            <template #default="scope">
              <el-button size="small" @click="handleDetail(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <el-col :span="8">
        <el-table :data="handleData(resourceData)" class="table" border>
          <el-table-column prop="key" label="指标" />
          <el-table-column prop="value" label="值" />
          <el-table-column label="操作" width="75">
            <template #default="scope">
              <el-button size="small" @click="handleDetail(scope.row)">修改</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <!-- 新增的对话框 -->
    <el-dialog v-model="dialogVisible" title="修改值" label-width="auto" style="max-width: 300px">
      <el-form>
        <el-form-item label="指标">
          <el-input v-model="editingRow.key" disabled />
        </el-form-item>
        <el-form-item label="值">
          <el-input v-model.number="editingRow.value" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import LeftTable from 'element-plus/es/components/table-v2/src/renderers/left-table.mjs';
import { defineProps, ref } from 'vue'

// 定义 props
const props = defineProps({
  synergyData: {
    type: Array,
    required: true
  },
  serviceData: {
    type: Array,
    required: true
  },
  resourceData: {
    type: Array,
    required: true
  },
  systemNames: {
    type: Object,
    default: () => ({
      synergy: '协同子系统',
      service: '服务子系统',
      resource: '资源子系统'
    })
  }
})

// 数据处理函数
const handleData = (data) => {
  if (typeof data === 'object' && !Array.isArray(data)) {
    return Object.entries(data.index || {}).map(([key, value]) => ({
      key,
      value: Array.isArray(value) ? value[value.length - 1] : value
    }));
  }
  console.log(data);
  return Array.isArray(data) ? data.slice(0, 6) : [];
}

// 对话框相关变量
const dialogVisible = ref(false);
const editingRow = ref({});

// 打开对话框并设置当前行数据
const handleDetail = (row) => {
  editingRow.value = { ...row };
  dialogVisible.value = true;
};

// 保存修改
const saveEdit = () => {
  // 这里可以添加额外的验证逻辑
  const index = props.synergyData.findIndex(item => item.key === editingRow.value.key);
  if (index !== -1) {
    props.synergyData[index].value = editingRow.value.value;
  } else {
    // 如果未找到对应项，则可以选择抛出错误或进行其他处理
    console.warn(`Key ${editingRow.value.key} not found in synergyData.`);
  }
  dialogVisible.value = false;
};
</script>

// 暴露给父组件的方法
defineExpose({ handleData })