<template>
  <div class="chart-container">
    <h3>{{ title }}</h3>
    <div ref="chartRef" style="width: 100%; height: 400px;"></div>
  </div>
</template>

<script>
import { ref, onMounted } from "vue"
import * as echarts from "echarts"

export default {
  name: "StyleRadarChart",
  props: {
    title: {
      type: String,
      default: "风格雷达图"
    },
    data: {
      type: Array,
      default: () => []
    }
  },
  setup(props) {
    const chartRef = ref(null)
    let chartInstance = null

    onMounted(() => {
      chartInstance = echarts.init(chartRef.value)
      const option = {
        title: {
          text: props.title
        },
        tooltip: {},
        legend: {
          data: ["当前风格", "目标风格"]
        },
        radar: {
          indicator: [
            { name: "节奏", max: 100 },
            { name: "情感", max: 100 },
            { name: "描述", max: 100 },
            { name: "对话", max: 100 },
            { name: "情节", max: 100 },
            { name: "氛围", max: 100 }
          ]
        },
        series: [{
          name: "风格对比",
          type: "radar",
          data: [
            {
              value: [props.data[0] || 60, props.data[1] || 70, props.data[2] || 80, props.data[3] || 50, props.data[4] || 90, props.data[5] || 75],
              name: "当前风格"
            },
            {
              value: [props.data[6] || 50, props.data[7] || 60, props.data[8] || 70, props.data[9] || 60, props.data[10] || 80, props.data[11] || 65],
              name: "目标风格"
            }
          ]
        }]
      }
      chartInstance.setOption(option)
    })

    return {
      chartRef
    }
  }
}
</script>

<style scoped>
.chart-container {
  width: 100%;
}
</style>