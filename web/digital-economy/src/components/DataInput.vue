<template>
  <div class="form">
    <el-form @submit.prevent="onSubmit" label-width="50px" label-position="top" size="large">
      <el-row :gutter="100">
        <el-col :span="4">
          <h4>服务子系统评价指标：</h4>
        </el-col>
      </el-row>
      <el-row :gutter="100">
        <el-col v-for="(indi, index) in serviceIndi" :key="index" :span="4">
          <el-form-item :label="indi.name">
            <el-input v-model="indi.value" placeholder="请输入" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="100">
        <el-col :span="4">
          <h4>协同子系统评价指标：</h4>
        </el-col>
      </el-row>
      <el-row :gutter="100">
        <el-col v-for="(indi, index) in synergyIndi" :key="index" :span="4">
          <el-form-item :label="indi.name">
            <el-input v-model="indi.value" placeholder="请输入" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="100">
        <el-col :span="4">
          <h4>资源子系统评价指标：</h4>
        </el-col>
      </el-row>
      <el-row :gutter="100">
        <el-col v-for="(indi, index) in resourceIndi" :key="index" :span="4">
          <el-form-item :label="indi.name">
            <el-input v-model="indi.value" placeholder="请输入" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="100">
        <el-col :span="4" :offset="20">
          <el-button type="danger" @click="$emit('cancel')">取消</el-button>
          <el-button type="primary" native-type="submit">提交</el-button>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script setup>
import { ref, onMounted, defineEmits } from 'vue';
import { ElMessage } from 'element-plus';
import serviceData from '../assets/service.json';
import synergyData from '../assets/synergy.json';
import resourceData from '../assets/resource.json';

// 定义组件的事件
const emit = defineEmits(['submit-success']);

const indi = ref([]);
const serviceIndi = ref([]);
const synergyIndi = ref([]);
const resourceIndi = ref([]);

const onSubmit = () => {
  console.log('submit!');
  indi.value = [serviceIndi.value, synergyIndi.value, resourceIndi.value];
  console.log(indi.value);
  // 新增：触发 submit-success 事件并传递新记录的数据
  const newRecord = {
    id: Date.now(), // 使用时间戳作为唯一 ID
    date: new Date().toISOString().split('T')[0], // 当前日期
    title: `档案${indi.value.length + 1}` // 档案标题
  };
  emit('submit-success', newRecord);
};

// 获取指标，组合为数组
function flattenChildren(children) {
  return children.reduce((acc, child) => {
    if (child.children) {
      return acc.concat(flattenChildren(child.children));
    } else {
      acc.push({ name: child.name, value: '' });
      return acc;
    }
  }, []);
}

onMounted(() => {
  serviceIndi.value = flattenChildren(serviceData.children);
  synergyIndi.value = flattenChildren(synergyData.children);
  resourceIndi.value = flattenChildren(resourceData.children);
});
</script>

<style scoped>
.form {
    height: 100%;
    width: 100%;
    text-align: left;
    padding: 20px;
    /* border: 1px solid #ccc;
    padding: 20px;
    border: 2px solid #456d7a;
    border-radius: 20px;
    box-shadow: #456d7a 0 0 5px; */
}

.subtitle{
    position: absolute;
    bottom: 5%;
    left: 50%;
    transform: translate(-50%, 0);
    font-size: 30px;
    font-weight: 500;

}
</style>