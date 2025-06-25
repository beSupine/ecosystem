<template>
  <div id="body">
    <div class="container" id="c1">
      <div id="scoreChart"></div>
      <div id="pieChart"></div>
    </div>
    <div class="container" id="c2">
      <div id="radarChart"></div>
      <div id="horizontalBarChart"></div>
    </div>
    <div class="container" id="c3">
      <span>历年趋势</span>
      <div id="smallBarChart1"></div>
      <div id="smallBarChart2"></div>
      <div id="smallBarChart3"></div>
      <div id="smallBarChart4"></div>
    </div>
    <div class="container" id="c4">
      <div id="treeChart"></div>
      <div v-if="isPopupVisible" class="popup">
        <div ref="barChartRef" style="width: 500px; height: 300px;"></div>
        <button @click="closePopup">关闭</button>
      </div>
    </div>
    <div class="subtitle">
      协同子系统
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick, watch } from 'vue';
import * as echarts from 'echarts';
import apiClient from '../api'; // 导入API客户端
import treeData from '../assets/synergy.json'; // 树状图结构

// --- ECharts 图表实例 ---
let scoreChart, pieChart, radarChart, horizontalBarChart, smallBarChart1, smallBarChart2, smallBarChart3, smallBarChart4, treeChart;

// --- 数据和工具函数定义 ---
// 将后端返回的评估等级（如“良好”）映射为图表需要的分数
const levelToScore = (level) => {
  const mapping = { '差': 20, '较差': 40, '中等': 60, '良好': 80, '优秀': 95 };
  return mapping[level] || 0;
};

// 后端返回的指标key（如X_V11）到前端树状图显示名称（如产出数量）的映射
const keyToName = {
  "X_V11": "产出数量", "X_V12": "产出效率", "X_V13": "产出质量", "X_V14": "产出效益",
  "X_V21": "数据覆盖率", "X_V22": "流动效率", "X_O11": "企业分布密度", "X_O21": "协同主体多样性",
  "X_O31": "协同主体协作程度", "X_O32": "协同主体竞争程度", "X_R11": "核心企业留存率",
  "X_R12": "合作关系稳定性指数", "X_R13": "价值链恢复效率", "X_S11": "协同数据完整性"
};
const nameToKey = Object.fromEntries(Object.entries(keyToName).map(a => a.reverse()));

// --- 响应式数据定义 ---
const score = ref(0);
const pieData = ref([]);
const horizontalBarData = ref([]);
const radarData = ref([]); // 单独为雷达图创建数据
const vorsTrendData = ref({ V: [], O: [], R: [], S: [] }); // 存储VORS历年趋势
const indicatorTrendsData = ref([]); // 所有指标的历年趋势原始数据
const years = ref([]); // 年份标签
const popupTitle = ref('');

// --- ECharts 颜色和等级工具函数 ---
function getColor(value) {
  if (value >= 90) return '#008000'; // 绿色
  if (value >= 75) return '#0000FF'; // 蓝色
  if (value >= 60) return '#FFFF00'; // 黄色
  return '#FF0000'; // 红色
}

function getLevel(value) {
  if (value >= 90) return 'A';
  if (value >= 75) return 'B';
  if (value >= 60) return 'C';
  return 'D';
}

// --- 数据获取与处理 ---
async function fetchData() {
  try {
    // 使用 Promise.all 并行获取所有需要的数据
    const [
      overallRes,
      radarRes,
      bottomIndicatorsRes,
      radarTrendRes,
      indicatorTrendsRes
    ] = await Promise.all([
      apiClient.get('/overall'),
      apiClient.get('/radar'),
      apiClient.get('/bottom-indicators'),
      apiClient.get('/radar-trend'),
      apiClient.get('/indicator-trends')
    ]);

    // 1. 处理总体评价数据 -> 仪表盘
    if (overallRes.data && overallRes.data.collaborativeSystemEvaluation) {
      score.value = levelToScore(overallRes.data.collaborativeSystemEvaluation);
    }

    // 2. 处理雷达图数据 -> 饼图和雷达图
    if (radarRes.data && radarRes.data.collaborativeSystem) {
      const collaborativeSystem = radarRes.data.collaborativeSystem;
      // ECharts 雷达图需要严格的顺序
      const orderedKeys = ['活力', '组织力', '稳定性', '服务能力'];
      radarData.value = orderedKeys.map(key => levelToScore(collaborativeSystem[key]));

      pieData.value = [
        { value: levelToScore(collaborativeSystem['活力']), name: 'V' },
        { value: levelToScore(collaborativeSystem['组织力']), name: 'O' },
        { value: levelToScore(collaborativeSystem['稳定性']), name: 'R' },
        { value: levelToScore(collaborativeSystem['服务能力']), name: 'S' }
      ];
    }

    // 3. 处理倒数指标 -> 横向柱状图
    if (bottomIndicatorsRes.data && bottomIndicatorsRes.data.collaborative) {
      horizontalBarData.value = bottomIndicatorsRes.data.collaborative.map(item => ({
        name: keyToName[item.indicatorName] || item.indicatorName,
        value: levelToScore(item.indicatorLevel)
      })).reverse(); // reverse使其从上到下由差到好
    }

    // 4. 处理雷达图趋势 -> VORS历年趋势小图
    if (radarTrendRes.data && radarTrendRes.data.length > 0) {
      const trends = radarTrendRes.data.reverse(); // 时间倒序改回正序
      years.value = trends.map(item => new Date(item.entryTime).getFullYear());
      vorsTrendData.value.V = trends.map(item => levelToScore(item.collaborativeSystem['活力']));
      vorsTrendData.value.O = trends.map(item => levelToScore(item.collaborativeSystem['组织力']));
      vorsTrendData.value.R = trends.map(item => levelToScore(item.collaborativeSystem['稳定性']));
      vorsTrendData.value.S = trends.map(item => levelToScore(item.collaborativeSystem['服务能力']));
    }

    // 5. 处理所有指标的历年趋势 -> 用于点击弹窗
    if (indicatorTrendsRes.data && indicatorTrendsRes.data.length > 0) {
      indicatorTrendsData.value = indicatorTrendsRes.data.reverse(); // 时间倒序改回正序
    }

  } catch (error) {
    console.error("数据加载失败:", error);
    // 这里可以添加用户提示，例如使用Element Plus的ElMessage
  }
}

// --- 图表初始化与更新 ---
function initCharts() {
  // 获取DOM元素
  const scoreChartDom = document.getElementById('scoreChart');
  const pieChartDom = document.getElementById('pieChart');
  const radarChartDom = document.getElementById('radarChart');
  const horizontalBarChartDom = document.getElementById('horizontalBarChart');
  const smallBarChart1Dom = document.getElementById('smallBarChart1');
  const smallBarChart2Dom = document.getElementById('smallBarChart2');
  const smallBarChart3Dom = document.getElementById('smallBarChart3');
  const smallBarChart4Dom = document.getElementById('smallBarChart4');
  const treeChartDom = document.getElementById('treeChart');

  // 初始化
  if (scoreChartDom) scoreChart = echarts.init(scoreChartDom);
  if (pieChartDom) pieChart = echarts.init(pieChartDom);
  if (radarChartDom) radarChart = echarts.init(radarChartDom);
  if (horizontalBarChartDom) horizontalBarChart = echarts.init(horizontalBarChartDom);
  if (smallBarChart1Dom) smallBarChart1 = echarts.init(smallBarChart1Dom);
  if (smallBarChart2Dom) smallBarChart2 = echarts.init(smallBarChart2Dom);
  if (smallBarChart3Dom) smallBarChart3 = echarts.init(smallBarChart3Dom);
  if (smallBarChart4Dom) smallBarChart4 = echarts.init(smallBarChart4Dom);
  if (treeChartDom) treeChart = echarts.init(treeChartDom);

  // 设置基础配置（只执行一次）
  setupTreeChart();
}

function updateCharts() {
  // 仪表盘
  const scoreOption = {
    tooltip: { formatter: '得分 : {c}' },
    series: [{
      type: 'gauge', radius: '80%', progress: { show: true, roundCap: true, itemStyle: { color: getColor(score.value) }, width: 13 },
      axisLine: { lineStyle: { width: 13 }, roundCap: true },
      detail: { valueAnimation: true, formatter: getLevel(score.value), color: '#fff', offsetCenter: [0, '100%'] },
      pointer: { itemStyle: { color: getColor(score.value) } },
      data: [{ value: score.value, name: '综合评价' }],
      title: { offsetCenter: [0, '70%'], color: '#fff', fontSize: 20 }
    }]
  };
  scoreChart.setOption(scoreOption);
  scoreChart.on('click', () => showPopup('综合评价'));

  // 饼图
  const pieOption = {
    title: { text: '维度贡献率', left: 'center', bottom: '0%', textStyle: { color: '#fff', fontSize: 17 } },
    tooltip: { trigger: 'item' },
    series: [{ name: '数据', type: 'pie', radius: '70%', center: ['50%', '45%'], data: pieData.value, emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } } }]
  };
  pieChart.setOption(pieOption);

  // 雷达图
  const radarOption = {
    title: { text: 'VORS雷达图', left: 'center', top: 0, textStyle: { color: '#fff' } },
    tooltip: { trigger: 'item' },
    radar: {
      indicator: [{ name: 'V', max: 100 }, { name: 'O', max: 100 }, { name: 'R', max: 100 }, { name: 'S', max: 100 }],
      radius: '70%', center: ['50%', '55%'], splitArea: { show: false }, axisNameGap: 3
    },
    series: [{ type: 'radar', symbol: 'none', areaStyle: { color: 'rgba(255, 127, 80,1.0)' }, lineStyle: { color: 'rgba(255, 127, 80,1.0)' }, data: [{ value: radarData.value, name: 'VORS' }] }]
  };
  radarChart.setOption(radarOption);

  // 横向柱状图
  const horizontalBarOption = {
    title: { text: '核心预警指标', left: 'center', top: 0, textStyle: { color: '#fff' } },
    tooltip: {},
    xAxis: { type: 'value', boundaryGap: [0, 0.01], splitLine: { show: false }, axisLine: { show: true } },
    yAxis: { type: 'category', data: horizontalBarData.value.map(item => item.name), axisLabel: { interval: 0 } },
    series: [{ type: 'bar', data: horizontalBarData.value.map(item => item.value), itemStyle: { color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{ offset: 0, color: '#ff4d4d' }, { offset: 1, color: '#ffc371' }]) } }],
    grid: { left: '30%', top: '20%', bottom: '10%', right: '10%' },
  };
  horizontalBarChart.setOption(horizontalBarOption);

  // VORS趋势图
  const smallBarOptionBase = {
    title: { textStyle: { color: 'rgba(223, 228, 234,1.0)' }, left: 20 },
    tooltip: {},
    xAxis: { type: 'category', data: years.value },
    yAxis: { type: 'value', axisLine: { show: true }, axisTick: { show: true }, splitLine: { show: false }, min: 0, max: 100 },
    series: [{ type: 'bar', barWidth: 30, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#66ccff' }, { offset: 1, color: '#99ffcc' }]) } }, { type: 'line' }],
    grid: { left: 40, top: 40, bottom: 40, right: 20 },
  };

  smallBarChart1.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'V' }, series: [{ ...smallBarOptionBase.series[0], data: vorsTrendData.value.V }, { ...smallBarOptionBase.series[1], data: vorsTrendData.value.V }] });
  smallBarChart2.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'O' }, series: [{ ...smallBarOptionBase.series[0], data: vorsTrendData.value.O }, { ...smallBarOptionBase.series[1], data: vorsTrendData.value.O }] });
  smallBarChart3.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'R' }, series: [{ ...smallBarOptionBase.series[0], data: vorsTrendData.value.R }, { ...smallBarOptionBase.series[1], data: vorsTrendData.value.R }] });
  smallBarChart4.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'S' }, series: [{ ...smallBarOptionBase.series[0], data: vorsTrendData.value.S }, { ...smallBarOptionBase.series[1], data: vorsTrendData.value.S }] });
}

function setupTreeChart() {
  const treeOption = {
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: [{
      type: 'tree', data: [treeData], top: '0%', left: '17%', symbolSize: 17, symbol: 'circle',
      label: { position: 'left', fontSize: 15, color: '#fff' },
      leaves: { label: { position: 'right', verticalAlign: 'middle', align: 'left' } },
      emphasis: { focus: 'descendant' }, expandAndCollapse: true, animationDuration: 550,
      animationDurationUpdate: 750, lineStyle: { color: '#6bd1f9' }
    }]
  };
  treeChart.setOption(treeOption);
  treeChart.on('click', (params) => {
    if (!params.data.children) {
      showPopup(params.data.name);
    }
  });
}

onMounted(async () => {
  initCharts();
  await fetchData();
  updateCharts();
});

// 使用 watch 监听响应式数据的变化，确保图表在数据更新后重绘
watch([score, pieData, horizontalBarData, radarData, vorsTrendData], () => {
  if(scoreChart) { // 确保图表已初始化
    updateCharts();
  }
}, { deep: true });


// --- 弹窗逻辑 ---
const barChartRef = ref(null);
const isPopupVisible = ref(false);

const showPopup = (name) => {
  popupTitle.value = name;
  isPopupVisible.value = true;
};

const closePopup = () => {
  isPopupVisible.value = false;
};

// 监听弹窗可见性，当可见时初始化内部的图表
watch(isPopupVisible, (visible) => {
  if (visible) {
    nextTick(() => {
      let data, trendYears;

      if (popupTitle.value === '综合评价') {
        // '综合评价' 使用 VORS 趋势数据
        data = vorsTrendData.value.V.map((v, i) => (v + vorsTrendData.value.O[i] + vorsTrendData.value.R[i] + vorsTrendData.value.S[i]) / 4);
        trendYears = years.value;
      } else {
        // 其他指标从 indicatorTrendsData 中查找
        const indicatorKey = nameToKey[popupTitle.value];
        if (!indicatorKey || indicatorTrendsData.value.length === 0) {
          console.warn(`未找到指标'${popupTitle.value}'的趋势数据。`);
          return;
        }

        trendYears = indicatorTrendsData.value.map(item => new Date(item.entryTime).getFullYear());
        data = indicatorTrendsData.value.map(record => {
          const foundIndicator = record.collaborative.find(ind => ind.indicatorName === indicatorKey);
          return foundIndicator ? foundIndicator.value : 0;
        });
      }

      const barChart = echarts.init(barChartRef.value);
      const barOption = {
        title: { text: popupTitle.value + ' - 历年数据', left: 30, top: 10 },
        tooltip: {},
        xAxis: { type: 'category', data: trendYears },
        yAxis: { type: 'value', axisLine: { show: true }, axisTick: { show: true } },
        series: [
          { data: data, type: 'bar', barWidth: 40 },
          { data: data, type: 'line' }
        ],
        grid: { left: 70, top: 70, bottom: 40 }
      };
      barChart.setOption(barOption);
    });
  }
});

</script>

<style scoped>
#c1 {
    position: absolute;
    top: 100px;
    left: 30px;
    height: 22vh;
    width: 700px;
}

#c2 {
    position: absolute;
    top: 460px;
    left: 30px;
    width: 700px;
    height: 250px;
}

#c3 {
    position: absolute;
    bottom: 50px;
    width: 700px;
    left: 30px;
    height: 420px;
    display: grid;
    grid-template-columns: 1fr 1fr;
    grid-template-rows: 1fr 5fr 5fr;
}

#c3 span {
    grid-area: 1/1/2/2;
    width: 100%;
    height: 100%;
    text-align: left;
    padding-left: 20px;
    font-size: larger;
    font-weight: bolder;
}

#c4 {
    position: absolute;
    height: 73vh;
    width: 65vw;
    top: 13vh;
    left: 33vw;
}

#scoreChart {
    height: 100%;
    width: 50%;
}

#pieChart {
    height: 100%;
    width: 50%;
}

#radarChart {
    height: 100%;
    width: 40%;
}

#horizontalBarChart {
    height: 100%;
    width: 60%;
}

#smallBarChart1 {
    height: 100%;
    width: 100%;
    grid-area: 2 / 1 / 3 / 2;
}

#smallBarChart2 {
    height: 100%;
    width: 100%;
    grid-area: 2 / 2 / 3 / 3;
}

#smallBarChart3 {
    height: 100%;
    width: 100%;
    grid-area: 3 / 1 / 4 / 2;
}

#smallBarChart4 {
    height: 100%;
    width: 100%;
    grid-area: 3 / 2 / 4 / 3;
}

#treeChart {
    height: 100%;
    width: 100%;
}

/* 定义弹出层样式 */
.popup {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    background-color: rgba(255, 255, 255, 0.9);
    border: 2px solid #456d7a;
    border-radius: 10px;
    padding: 20px;
    z-index: 1000;
}

/* .subtitle {
    position: absolute;
    bottom: 5%;
    left: 50%;
    transform: translate(-50%, 0);
    font-size: 30px;
    font-weight: 500;
} */
</style>