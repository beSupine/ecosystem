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
    </div>
    <div class="container" id="c4">
      <div id="treeChart"></div>
      <div v-if="isPopupVisible" class="popup">
        <div ref="barChartRef" style="width: 500px; height: 300px;"></div>
        <button @click="closePopup">关闭</button>
      </div>
    </div>
    <div class="subtitle">
      资源子系统
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick, watch } from 'vue';
import * as echarts from 'echarts';
import apiClient from '../api'; // 导入API客户端
import treeData from '../assets/resource.json'; // 树状图结构

// --- ECharts 图表实例 ---
let scoreChart, pieChart, radarChart, horizontalBarChart, smallBarChart1, smallBarChart2, smallBarChart3, treeChart;

// --- 数据和工具函数定义 ---
const levelToScore = (level) => {
  const mapping = { '差': 20, '较差': 40, '中等': 60, '良好': 80, '优秀': 95 };
  return mapping[level] || 0;
};

const keyToName = {
  "Z_V11": "产出数量", "Z_V12": "创新产量", "Z_V21": "数据覆盖率", "Z_V22": "流动效率",
  "Z_V31": "流程整合能力", "Z_V32": "更新淘汰能力", "Z_O11": "构件层级深度",
  "Z_O21": "数字资源多样性", "Z_O31": "算法协作程度", "Z_O32": "数字资源竞争程度", "Z_S11": "平均接口响应时间"
};
const nameToKey = Object.fromEntries(Object.entries(keyToName).map(a => a.reverse()));

// --- 响应式数据定义 ---
const score = ref(0);
const pieData = ref([]);
const horizontalBarData = ref([]);
const radarData = ref([]);
const vosTrendData = ref({ V: [], O: [], S: [] }); // 资源子系统是 V, O, S
const indicatorTrendsData = ref([]);
const years = ref([]);
const popupTitle = ref('');

// --- ECharts 颜色和等级工具函数 ---
function getColor(value) {
  if (value >= 90) return '#008000';
  if (value >= 75) return '#0000FF';
  if (value >= 60) return '#FFFF00';
  return '#FF0000';
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
    const [overallRes, radarRes, bottomIndicatorsRes, radarTrendRes, indicatorTrendsRes] = await Promise.all([
      apiClient.get('/overall'),
      apiClient.get('/radar'),
      apiClient.get('/bottom-indicators'),
      apiClient.get('/radar-trend'),
      apiClient.get('/indicator-trends')
    ]);

    if (overallRes.data && overallRes.data.resourceSystemEvaluation) {
      score.value = levelToScore(overallRes.data.resourceSystemEvaluation);
    }

    if (radarRes.data && radarRes.data.resourceSystem) {
      const resourceSystem = radarRes.data.resourceSystem;
      const orderedKeys = ['活力', '组织力', '服务能力'];
      radarData.value = orderedKeys.map(key => levelToScore(resourceSystem[key]));
      pieData.value = [
        { value: levelToScore(resourceSystem['活力']), name: 'V' },
        { value: levelToScore(resourceSystem['组织力']), name: 'O' },
        { value: levelToScore(resourceSystem['服务能力']), name: 'S' }
      ];
    }

    if (bottomIndicatorsRes.data && bottomIndicatorsRes.data.resource) {
      horizontalBarData.value = bottomIndicatorsRes.data.resource.map(item => ({
        name: keyToName[item.indicatorName] || item.indicatorName,
        value: levelToScore(item.indicatorLevel)
      })).reverse();
    }

    if (radarTrendRes.data && radarTrendRes.data.length > 0) {
      const trends = radarTrendRes.data.reverse();
      years.value = trends.map(item => new Date(item.entryTime).getFullYear());
      vosTrendData.value.V = trends.map(item => levelToScore(item.resourceSystem['活力']));
      vosTrendData.value.O = trends.map(item => levelToScore(item.resourceSystem['组织力']));
      vosTrendData.value.S = trends.map(item => levelToScore(item.resourceSystem['服务能力']));
    }

    if (indicatorTrendsRes.data && indicatorTrendsRes.data.length > 0) {
      indicatorTrendsData.value = indicatorTrendsRes.data.reverse();
    }

  } catch (error) {
    console.error("数据加载失败:", error);
  }
}

// --- 图表初始化与更新 ---
function initCharts() {
  scoreChart = echarts.init(document.getElementById('scoreChart'));
  pieChart = echarts.init(document.getElementById('pieChart'));
  radarChart = echarts.init(document.getElementById('radarChart'));
  horizontalBarChart = echarts.init(document.getElementById('horizontalBarChart'));
  smallBarChart1 = echarts.init(document.getElementById('smallBarChart1'));
  smallBarChart2 = echarts.init(document.getElementById('smallBarChart2'));
  smallBarChart3 = echarts.init(document.getElementById('smallBarChart3'));
  treeChart = echarts.init(document.getElementById('treeChart'));
  setupTreeChart();
}

function updateCharts() {
  scoreChart.setOption({
    tooltip: { formatter: '得分 : {c}' },
    series: [{ type: 'gauge', radius: '80%', progress: { show: true, roundCap: true, itemStyle: { color: getColor(score.value) }, width: 13 }, axisLine: { lineStyle: { width: 13 }, roundCap: true }, detail: { valueAnimation: true, formatter: getLevel(score.value), color: '#fff', offsetCenter: [0, '100%'] }, pointer: { itemStyle: { color: getColor(score.value) } }, data: [{ value: score.value, name: '综合评价' }], title: { offsetCenter: [0, '70%'], color: '#fff', fontSize: 20 } }]
  });
  scoreChart.on('click', () => showPopup('综合评价'));

  pieChart.setOption({
    title: { text: '维度贡献率', left: 'center', bottom: '0%', textStyle: { color: '#fff', fontSize: 17 } },
    tooltip: { trigger: 'item' },
    series: [{ name: '数据', type: 'pie', radius: '70%', center: ['50%', '45%'], data: pieData.value, emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0, 0, 0, 0.5)' } } }]
  });

  radarChart.setOption({
    title: { text: 'VOS雷达图', left: 'center', top: 0, textStyle: { color: '#fff' } },
    tooltip: { trigger: 'item' },
    radar: { indicator: [{ name: 'V', max: 100 }, { name: 'O', max: 100 }, { name: 'S', max: 100 }], radius: '70%', center: ['50%', '55%'], splitArea: { show: false }, axisNameGap: 3 },
    series: [{ type: 'radar', symbol: 'none', areaStyle: { color: 'rgba(255, 127, 80,1.0)' }, lineStyle: { color: 'rgba(255, 127, 80,1.0)' }, data: [{ value: radarData.value, name: 'VOS' }] }]
  });

  horizontalBarChart.setOption({
    title: { text: '核心预警指标', left: 'center', top: 0, textStyle: { color: '#fff' } },
    tooltip: {},
    xAxis: { type: 'value', boundaryGap: [0, 0.01], splitLine: { show: false }, axisLine: { show: true } },
    yAxis: { type: 'category', data: horizontalBarData.value.map(item => item.name), axisLabel: { interval: 0 } },
    series: [{ type: 'bar', data: horizontalBarData.value.map(item => item.value), itemStyle: { color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{ offset: 0, color: '#ff4d4d' }, { offset: 1, color: '#ffc371' }]) } }],
    grid: { left: '30%', top: '20%', bottom: '10%', right: '10%' },
  });

  const smallBarOptionBase = {
    title: { textStyle: { color: 'rgba(223, 228, 234,1.0)' }, left: 20 },
    tooltip: {},
    xAxis: { type: 'category', data: years.value },
    yAxis: { type: 'value', axisLine: { show: true }, axisTick: { show: true }, splitLine: { show: false }, min: 0, max: 100 },
    series: [{ type: 'bar', barWidth: 30, itemStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#66ccff' }, { offset: 1, color: '#99ffcc' }]) } }, { type: 'line' }],
    grid: { left: 40, top: 40, bottom: 40, right: 20 },
  };
  smallBarChart1.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'V' }, series: [{ ...smallBarOptionBase.series[0], data: vosTrendData.value.V }, { ...smallBarOptionBase.series[1], data: vosTrendData.value.V }] });
  smallBarChart2.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'O' }, series: [{ ...smallBarOptionBase.series[0], data: vosTrendData.value.O }, { ...smallBarOptionBase.series[1], data: vosTrendData.value.O }] });
  smallBarChart3.setOption({ ...smallBarOptionBase, title: { ...smallBarOptionBase.title, text: 'S' }, series: [{ ...smallBarOptionBase.series[0], data: vosTrendData.value.S }, { ...smallBarOptionBase.series[1], data: vosTrendData.value.S }] });
}

function setupTreeChart() {
  treeChart.setOption({
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: [{ type: 'tree', data: [treeData], top: '0%', left: '17%', symbolSize: 17, symbol: 'circle', label: { position: 'left', fontSize: 15, color: '#fff' }, leaves: { label: { position: 'right', verticalAlign: 'middle', align: 'left' } }, emphasis: { focus: 'descendant' }, expandAndCollapse: true, animationDuration: 550, animationDurationUpdate: 750, lineStyle: { color: '#6bd1f9' } }]
  });
  treeChart.on('click', (params) => {
    if (!params.data.children) showPopup(params.data.name);
  });
}

onMounted(async () => {
  initCharts();
  await fetchData();
  updateCharts();
});

watch([score, pieData, horizontalBarData, radarData, vosTrendData], () => {
  if(scoreChart) updateCharts();
}, { deep: true });

// --- 弹窗逻辑 ---
const barChartRef = ref(null);
const isPopupVisible = ref(false);

const showPopup = (name) => {
  popupTitle.value = name;
  isPopupVisible.value = true;
};
const closePopup = () => { isPopupVisible.value = false; };

watch(isPopupVisible, (visible) => {
  if (visible) {
    nextTick(() => {
      let data, trendYears;
      if (popupTitle.value === '综合评价') {
        data = vosTrendData.value.V.map((v, i) => (v + vosTrendData.value.O[i] + vosTrendData.value.S[i]) / 3);
        trendYears = years.value;
      } else {
        const indicatorKey = nameToKey[popupTitle.value];
        if (!indicatorKey || indicatorTrendsData.value.length === 0) return;
        trendYears = indicatorTrendsData.value.map(item => new Date(item.entryTime).getFullYear());
        data = indicatorTrendsData.value.map(record => {
          const foundIndicator = record.resource.find(ind => ind.indicatorName === indicatorKey);
          return foundIndicator ? foundIndicator.value : 0;
        });
      }
      const barChart = echarts.init(barChartRef.value);
      barChart.setOption({
        title: { text: popupTitle.value + ' - 历年数据', left: 30, top: 10 }, tooltip: {},
        xAxis: { type: 'category', data: trendYears }, yAxis: { type: 'value', axisLine: { show: true }, axisTick: { show: true } },
        series: [{ data, type: 'bar', barWidth: 40 }, { data, type: 'line' }],
        grid: { left: 70, top: 70, bottom: 40 }
      });
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