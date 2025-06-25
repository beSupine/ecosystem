<template>
  <div class="form">
    <h3>{{ isEdit ? '编辑档案' : '新增档案' }}</h3>
    <el-form @submit.prevent="onSubmit" label-width="150px" label-position="top" size="default">
      <h4>服务子系统评价指标：</h4>
      <el-row :gutter="20">
        <el-col v-for="indi in serviceIndi" :key="indi.key" :span="6">
          <el-form-item :label="indi.name">
            <el-input-number v-model="indi.value" :precision="4" controls-position="right" style="width: 100%;" placeholder="请输入数值" />
          </el-form-item>
        </el-col>
      </el-row>
      <h4>协同子系统评价指标：</h4>
      <el-row :gutter="20">
        <el-col v-for="indi in synergyIndi" :key="indi.key" :span="6">
          <el-form-item :label="indi.name">
            <el-input-number v-model="indi.value" :precision="4" controls-position="right" style="width: 100%;" placeholder="请输入数值" />
          </el-form-item>
        </el-col>
      </el-row>
      <h4>资源子系统评价指标：</h4>
      <el-row :gutter="20">
        <el-col v-for="indi in resourceIndi" :key="indi.key" :span="6">
          <el-form-item :label="indi.name">
            <el-input-number v-model="indi.value" :precision="4" controls-position="right" style="width: 100%;" placeholder="请输入数值" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="24" style="text-align: right; margin-top: 20px;">
          <el-button @click="$emit('cancel')">取消</el-button>
          <el-button type="primary" @click="onSubmit" :loading="isLoading">
            {{ isEdit ? '保存修改' : '提交新增' }}
          </el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted, defineEmits, defineProps, watch } from 'vue';
import { ElMessage } from 'element-plus';
import apiClient from '../api';
import serviceData from '../assets/service.json';
import synergyData from '../assets/synergy.json';
import resourceData from '../assets/resource.json';

// --- 定义组件的Props和Emits ---
const props = defineProps({
  isEdit: { type: Boolean, default: false },
  initialData: { type: Object, default: null }
});
const emit = defineEmits(['submit-success', 'cancel']);

// --- 响应式数据 ---
const isLoading = ref(false);
const serviceIndi = ref([]);
const synergyIndi = ref([]);
const resourceIndi = ref([]);

// --- 指标名称到后端字段名的映射 ---
const nameToKeyMap = {
  '产出数量': {s:'XV11', r:'ZV11', f:'FV11'}, '产出效率': {s:'XV12'}, '产出质量': {s:'XV13'},
  '产出效益': {s:'XV14'}, '数据覆盖率': {s:'XV21', r:'ZV21', f:'FV21'}, '流动效率': {s:'XV22', r:'ZV22', f:'FV22'},
  '企业分布密度': {s:'XO11'}, '协同主体多样性': {s:'XO21'}, '协同主体协作程度': {s:'XO31', r:'ZO31'},
  '协同主体竞争程度': {s:'XO32'}, '核心企业留存率': {s:'XR11'}, '合作关系稳定性指数': {s:'XR12'},
  '价值链恢复效率': {s:'XR13'}, '协同数据完整性': {s:'XS11'}, '创新产量': {r:'ZV12', f:'FV12'},
  '流程整合能力': {r:'ZV31', f:'FV31'}, '更新淘汰能力': {r:'ZV32', f:'FV32'}, '构件层级深度': {r:'ZO11', f:'FO11'},
  '数字资源多样性': {r:'ZO21'}, '数字资源竞争程度': {r:'ZO32'}, '平均接口响应时间': {r:'ZS11'},
  '风险响应时间': {f:'FR11'}, '风险响应成功率': {f:'FR12'}, '威胁侦测能力': {f:'FR13'},
  '请求响应能力': {f:'FS11'}, '服务请求活跃度': {f:'FS12'}
};

// --- 工具函数 ---
// 将JSON结构扁平化为 { name, key, value } 的数组
function flattenChildren(children, systemPrefix) {
  return children.reduce((acc, child) => {
    if (child.children) {
      return acc.concat(flattenChildren(child.children, systemPrefix));
    } else {
      const key = (nameToKeyMap[child.name] && nameToKeyMap[child.name][systemPrefix]) || '';
      if(key) acc.push({ name: child.name, key: key.toLowerCase(), value: null });
      return acc;
    }
  }, []);
}

// 填充表单数据（用于编辑模式）
function populateForm(data) {
  if (!data) return;
  [synergyIndi, resourceIndi, serviceIndi].forEach(list => {
    list.value.forEach(item => {
      item.value = data[item.key] !== undefined ? data[item.key] : null;
    });
  });
}

// --- 挂载与监听 ---
onMounted(() => {
  synergyIndi.value = flattenChildren(synergyData.children, 's');
  resourceIndi.value = flattenChildren(resourceData.children, 'r');
  serviceIndi.value = flattenChildren(serviceData.children, 'f');

  if (props.isEdit && props.initialData) {
    populateForm(props.initialData);
  }
});

// 监听 initialData 的变化，以便在父组件切换记录时更新表单
watch(() => props.initialData, (newData) => {
  if (props.isEdit && newData) {
    populateForm(newData);
  }
});

// --- 事件处理 ---
const onSubmit = async () => {
  isLoading.value = true;

  // 构造后端需要的 payload
  const payload = {};
  [...synergyIndi.value, ...resourceIndi.value, ...serviceIndi.value].forEach(item => {
    // 确保值是数字或null
    const value = parseFloat(item.value);
    payload[item.key] = isNaN(value) ? null : value;
  });

  try {
    if (props.isEdit) {
      // 编辑模式，发送PUT请求
      await apiClient.put(`/${props.initialData.id}`, payload);
      ElMessage.success('档案修改成功');
    } else {
      // 新增模式，发送POST请求
      await apiClient.post('/', payload);
      ElMessage.success('档案新增成功');
    }
    emit('submit-success'); // 通知父组件操作成功
  } catch (error) {
    ElMessage.error(props.isEdit ? '修改失败' : '新增失败');
    console.error('Submit error:', error);
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.form {
  height: 100%;
  width: 100%;
  text-align: left;
  overflow-y: auto; /* 当内容过多时允许滚动 */
  padding-right: 15px; /* 防止滚动条遮挡内容 */
}
h4 {
  margin-top: 20px;
  margin-bottom: 10px;
  color: #a9c5de;
}
:deep(.el-form-item__label) {
  color: #e0e0e0; /* 修改标签颜色以提高可读性 */
  line-height: 1.2;
}
</style>