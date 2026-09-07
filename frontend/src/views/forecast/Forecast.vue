<template>
  <section>
    <div class="page-toolbar">
      <h2 class="page-title">销售预测</h2>
      <div>
        <el-input-number v-model="year" :min="2020" />
        <el-input-number v-model="month" :min="1" :max="12" />
        <el-button type="primary" @click="load">查询</el-button>
      </div>
    </div>
    <el-table :data="rows">
      <el-table-column prop="owner" label="负责人" />
      <el-table-column prop="targetAmount" label="目标金额" />
      <el-table-column prop="wonAmount" label="赢单金额" />
      <el-table-column prop="pipelineAmount" label="预测管道" />
      <el-table-column prop="weightedAmount" label="加权金额" />
      <el-table-column prop="achievementRate" label="达成率">
        <template #default="{ row }">{{ row.achievementRate }}%</template>
      </el-table-column>
      <el-table-column prop="gap" label="差额" />
    </el-table>
    <el-card class="top-gap">
      <template #header>月度趋势</template>
      <div ref="chartRef" class="chart-box"></div>
    </el-card>
  </section>
</template>

<script setup>
import * as echarts from "echarts";
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import { forecast } from "../../api";

const year = ref(new Date().getFullYear());
const month = ref(new Date().getMonth() + 1);
const rows = ref([]);
const chartRef = ref(null);
let chart;
const load = async () => {
  rows.value = await forecast.list({ year: year.value, month: month.value });
  const trend = await forecast.trend(year.value);
  await nextTick();
  if (!chart) {
    chart = echarts.init(chartRef.value);
  }
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["赢单", "加权", "目标"] },
    xAxis: { type: "category", data: trend.map((item) => `${item.month}月`) },
    yAxis: { type: "value" },
    series: [
      { name: "赢单", type: "line", data: trend.map((item) => item.wonAmount) },
      { name: "加权", type: "line", data: trend.map((item) => item.weightedAmount) },
      { name: "目标", type: "line", data: trend.map((item) => item.targetAmount) },
    ],
  });
};
const resize = () => chart?.resize();
onMounted(() => {
  window.addEventListener("resize", resize);
  load();
});
onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  chart?.dispose();
});
</script>

<style scoped>
.chart-box {
  height: 360px;
}
</style>
