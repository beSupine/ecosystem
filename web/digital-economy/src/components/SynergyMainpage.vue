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
import { onMounted, ref, nextTick } from 'vue';
import * as echarts from 'echarts';
//import indexData from '../assets/SynergyIndex.json'
import treeData from '../assets/synergy.json'
import router from '../router';
// --- 数据和工具函数定义 ---

// 将后端返回的评估等级（如“良好”）映射为图表需要的分数
const levelToScore = (level) => {
  const mapping = { '差': 20, '较差': 40, '中等': 60, '良好': 80, '优秀': 95 };
  return mapping[level] || 0;
};

// 后端返回的指标key（如X_V11）到前端树状图显示名称（如产出数量）的映射
// 这个映射关系是根据 `IndicatorEvaluationService.java` 和 `synergy.json` 的结构手动建立的
const keyToName = {
  "X_V11": "产出数量", "X_V12": "产出效率", "X_V13": "产出质量", "X_V14": "产出效益",
  "X_V21": "数据覆盖率", "X_V22": "流动效率", "X_O11": "企业分布密度", "X_O21": "协同主体多样性",
  "X_O31": "协同主体协作程度", "X_O32": "协同主体竞争程度", "X_R11": "核心企业留存率",
  "X_R12": "合作关系稳定性指数", "X_R13": "价值链恢复效率", "X_S11": "协同数据完整性"
};

// 用于存储从API获取的数据的响应式变量
const score = ref(0);
const pieData = ref([]);
const horizontalBarData = ref([]);
const smallBarData1 = ref([]); // 活力趋势
const smallBarData2 = ref([]); // 组织力趋势
const smallBarData3 = ref([]); // 稳定性趋势
const smallBarData4 = ref([]); // 服务能力趋势
const indicatorTrendsData = ref([]); // 所有指标的历年趋势原始数据
const years = ref([]); // 年份标签
//数据处理
const scores = ref(indexData.scores)
const score = ref(scores.value[scores.value.length - 1])
const level = ref('')
// console.log(scores.value)
const pieData = ref([indexData.V[indexData.V.length - 1],
indexData.O[indexData.O.length - 1],
indexData.R[indexData.R.length - 1],
indexData.S[indexData.S.length - 1]
]);

// 新增横向柱状图的数据
const horizontalBarData = ref([
    { name: '协同主体竞争程度', value: 0.21 },
    { name: '企业分布密度', value: 0.13 },
    { name: '流动效率', value: 3.07 },
    { name: '价值链恢复效率', value: 1.19 },
    { name: '产出质量', value: 0.97 }
]);

// VORS趋势数据
const smallBarData1 = ref(indexData.V);
const smallBarData2 = ref(indexData.O);
const smallBarData3 = ref(indexData.R);
const smallBarData4 = ref(indexData.S);

function getColor(value) {
    if (value >= 90) {
        level.value = 'A';
        return '#008000'; // 绿色
    } else if (value >= 75 && value <= 89) {
        level.value = 'B';
        return '#0000FF'; // 蓝色
    } else if (value >= 60 && value <= 74) {
        level.value = 'C';
        return '#FFFF00'; // 黄色
    } else {
        level.value = 'D';
        return '#FF0000'; // 红色
    }
}

function getLevel(value) {
    if (value >= 90) {
        return 'A';
    } else if (value >= 75 && value <= 89) {
        return 'B';
    } else if (value >= 60 && value <= 74) {
        return 'C';
    } else {
        return 'D';
    }
}

onMounted(() => {

    // 总体评价仪表盘
    const scoreChart = echarts.init(document.getElementById('scoreChart'));
    const scoreOption = {
        tooltip: {
            formatter: '得分 : {c}'
        },
        series: [
            {
                type: 'gauge',
                radius: '80%',
                progress: {
                    show: true,
                    roundCap: true, // 添加圆角效果
                    itemStyle: {
                        color: getColor(score.value),
                    },
                    width: 13
                },
                axisLine: {
                    lineStyle: {
                        width: 13,
                    },
                    roundCap: true, // 添加圆角效果
                },
                detail: {
                    valueAnimation: true,
                    formatter: getLevel(score.value),
                    color: '#fff',
                    offsetCenter: [0, '100%'] // 下移数值显示的位置
                },
                pointer: {
                    itemStyle: {
                        color: getColor(score.value) // 使用 getColor 函数设置指针颜色
                    }
                },
                data: [
                    {
                        value: score.value,
                        name: '综合评价'
                    }
                ],
                title: {
                    offsetCenter: [0, '70%'],
                    color: '#fff',
                    fontSize: 20
                }
            }
        ]
    };
    scoreChart.setOption(scoreOption);
    // 点击事件
    scoreChart.on('click', (params) => {
        // console.log(params);
        showPopup(params.data.name);
    });

    // 饼状图
    const pieChart = echarts.init(document.getElementById('pieChart'));
    const pieOption = {
        title: {
            text: '维度贡献率',
            left: 'center',
            bottom: '0%',
            textStyle: {
                color: '#fff',
                fontSize: 17
            }
        },
        tooltip: {
            trigger: 'item'
        },
        series: [
            {
                name: '数据',
                type: 'pie',
                radius: '70%',
                center: ['50%', '45%'],
                data: pieData.value.map((value, index) => ({
                    value,
                    name: ['S', 'V', 'R', 'O'][index]
                })),
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    }
                }
            }
        ],
    };
    pieChart.setOption(pieOption);

    // 雷达图
    const radarChart = echarts.init(document.getElementById('radarChart'));
    const radarOption = {
        title: {
            text: 'VORS雷达图',
            left: 'center',
            top: 0,
            textStyle: {
                color: '#fff'
            }
        },
        tooltip: {
            trigger: 'item'
        },
        radar: {
            indicator: [
                { name: 'S', max: 100 },
                { name: 'V', max: 100 },
                { name: 'R', max: 100 },
                { name: 'O', max: 100 }
            ],
            radius: '70%',
            center: ['50%', '55%'],
            splitArea: {
                show: false
            },
            axisNameGap: 3
        },
        series: [{
            type: 'radar',
            symbol: 'none',
            areaStyle: {
                color: 'rgba(255, 127, 80,1.0)'
            },
            lineStyle: {
                color: 'rgba(255, 127, 80,1.0)'
            },
            data: [
                {
                    value: pieData.value,
                    name: 'VORS'
                }
            ]
        }],
    };
    radarChart.setOption(radarOption);

    // 核心指标预警柱状图
    const horizontalBarChart = echarts.init(document.getElementById('horizontalBarChart'));
    const horizontalBarOption = {
        title: {
            text: '核心指标预警',
            left: 'center',
            top: 0,
            textStyle: {
                color: '#fff'
            }
        },
        tooltip: {},
        xAxis: {
            type: 'value',
            boundaryGap: [0, 0.01],
            splitLine: { show: false },
            axisLine: { show: true },
        },
        yAxis: {
            type: 'category',
            data: horizontalBarData.value.map(item => item.name)
        },
        series: [
            {
                type: 'bar',
                data: horizontalBarData.value.map(item => item.value),
                itemStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#66ccff' }, // 浅蓝色
                        { offset: 1, color: '#99ffcc' }  // 浅绿色
                    ])
                }
            }
        ],
        grid: {
            left: '30%',
            top: '20%',
            bottom: '20%',
        },
    };
    horizontalBarChart.setOption(horizontalBarOption);

    // VORS趋势图
    const smallBarChart1 = echarts.init(document.getElementById('smallBarChart1'));
    const smallBarChart2 = echarts.init(document.getElementById('smallBarChart2'));
    const smallBarChart3 = echarts.init(document.getElementById('smallBarChart3'));
    const smallBarChart4 = echarts.init(document.getElementById('smallBarChart4'));

    const smallBarOption = {
        title: {
            textStyle: {
                color: 'rgba(223, 228, 234,1.0)'
            },
            left: 20,
        },
        tooltip: {},
        xAxis: {
            type: 'category',
            data: ['2020', '2021', '2022', '2023', '2024'],
        },
        yAxis: {
            type: 'value',
            axisLabel: {
                customValues: [0, 20, 40, 60, 80, 100]
            },
            axisLine: { show: true },
            axisTick: { show: true },
            splitLine: { show: false },
            min: 0,
            max: 100,
        },
        series: {
            type: 'bar',
            barWidth: 30,
            itemStyle: {
                color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    { offset: 0, color: '#66ccff' }, // 浅蓝色
                    { offset: 1, color: '#99ffcc' }  // 浅绿色
                ])
            }
        },
        grid: {
            left: 40,
            top: 40,
            bottom: 40,
        },
    };

    smallBarChart1.setOption({
        ...smallBarOption,
        title: { ...smallBarOption.title, text: 'V' },
        series: [{ ...smallBarOption.series, data: smallBarData1.value },
        { type: 'line', data: smallBarData1.value }]
    })
    smallBarChart2.setOption({
        ...smallBarOption,
        title: { ...smallBarOption.title, text: 'O' },
        series: [{ ...smallBarOption.series, data: smallBarData2.value },
        { type: 'line', data: smallBarData2.value }]
    })
    smallBarChart3.setOption({
        ...smallBarOption,
        title: { ...smallBarOption.title, text: 'R' },
        series: [{ ...smallBarOption.series, data: smallBarData3.value },
        { type: 'line', data: smallBarData3.value }]
    })
    smallBarChart4.setOption({
        ...smallBarOption,
        title: { ...smallBarOption.title, text: 'S' },
        series: [{ ...smallBarOption.series, data: smallBarData4.value },
        { type: 'line', data: smallBarData4.value }]
    })

    // 树状图
    // const getTreeColor = (name) => {

    // }
    const treeChart = echarts.init(document.getElementById('treeChart'));
    const treeOption = {
        tooltip: {
            trigger: 'item',
            triggerOn: 'mousemove'
        },
        series: [
            {
                type: 'tree',
                data: [treeData],
                top: '0%',
                left: '17%',
                symbolSize: 17,
                symbol: 'circle',
                itemStyle: {
                    //color: GetTreeColor()
                },
                label: {
                    position: 'left',
                    // verticalAlign: 'middle',
                    // align: 'right',
                    fontSize: 15,
                    color: '#fff'
                },
                leaves: {
                    label: {
                        position: 'right',
                        verticalAlign: 'middle',
                        align: 'left'
                    }
                },
                emphasis: {
                    focus: 'descendant'
                },
                expandAndCollapse: true,
                animationDuration: 550,
                animationDurationUpdate: 750,
                lineStyle: {
                    color: '#6bd1f9' // 添加此行，设置连线颜色为蓝色
                }
            }
        ]
    };
    treeChart.setOption(treeOption);
    // 点击事件
    treeChart.on('click', (params) => {
        if (!params.data.children) {
            showPopup(params.data.name);
        }
    });
});

const barChartRef = ref(null);
const isPopupVisible = ref(false);

const showPopup = (name) => {
    isPopupVisible.value = true;
    nextTick(() => {
        initBarChart(name);
    });
};

const initBarChart = (name) => {
    // 从indexData的indexData中的index对象中获取name对应的数据
    const year = indexData.year;
    let data;
    if(name==='综合评价'){
        data = indexData.scores
    }else{
        data = indexData.index[name]
    }
    // console.log(data);
    const barChart = echarts.init(barChartRef.value);
    const barOption = {
        title: {
            text: name,
            left: 30,
            top: 0,
        },
        tooltip: {},
        xAxis: {
            type: 'category',
            data: year,
        },
        yAxis: {
            type: 'value',
            axisLine: { show: true },
            axisTick: { show: true }
        },
        series: [
            {
                data: data,
                type: 'bar',
                barWidth: 40
            },
            {
                data: data,
                type: 'line'
            }
        ],
        grid: {
            left: 70,
            top: 70,
            bottom: 40
        },
    };
    barChart.setOption(barOption);
};

const closePopup = () => {
    isPopupVisible.value = false;
};



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