<template>
  <section>
    <h2>报表中心</h2>
    <el-tabs v-model="active" @tab-change="load">
      <el-tab-pane label="销售漏斗" name="funnel">
        <el-button @click="download('sales-funnel')">导出 CSV</el-button>
        <div class="chart-grid">
          <div class="chart-box" ref="funnelChartRef"></div>
          <div class="chart-box" ref="leadChartRef"></div>
        </div>
        <el-table :data="funnelRows" stripe>
          <el-table-column prop="status" label="线索状态" />
          <el-table-column prop="count" label="数量" />
        </el-table>
        <el-table :data="funnelOpportunityRows" stripe class="table-gap">
          <el-table-column prop="stage" label="商机阶段" />
          <el-table-column prop="count" label="数量" />
          <el-table-column prop="amount" label="金额" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="销售业绩" name="performance">
        <div class="toolbar">
          <el-input-number v-model="year" :min="2020" :max="2100" />
          <el-input-number v-model="month" :min="1" :max="12" />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="download('sales-performance')">导出 CSV</el-button>
        </div>
        <div class="chart-box" ref="performanceChartRef"></div>
        <el-table :data="performanceRows" stripe>
          <el-table-column prop="owner" label="负责人" />
          <el-table-column prop="wonCount" label="赢单数" />
          <el-table-column prop="wonAmount" label="赢单金额" />
          <el-table-column prop="targetAmount" label="目标金额" />
          <el-table-column prop="attainmentRate" label="达成率(%)" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="工单分析" name="cases">
        <el-button @click="download('case-analysis')">导出 CSV</el-button>
        <div class="metric-grid">
          <el-card>
            <div>平均解决小时</div>
            <strong>{{ caseReport.avgResolveHours || 0 }}</strong>
          </el-card>
          <el-card>
            <div>SLA 达成率</div>
            <strong>{{ caseReport.slaMetRate || 0 }}%</strong>
          </el-card>
          <el-card>
            <div>平均满意度</div>
            <strong>{{ caseReport.avgSatisfaction || 0 }}</strong>
          </el-card>
          <el-card>
            <div>升级工单数</div>
            <strong>{{ caseReport.escalatedCount || 0 }}</strong>
          </el-card>
        </div>
        <div class="chart-grid">
          <div class="chart-box" ref="caseTypeChartRef"></div>
          <div class="chart-box" ref="casePriorityChartRef"></div>
        </div>
        <el-table :data="caseReport.byStatus || []" stripe>
          <el-table-column prop="status" label="状态" />
          <el-table-column prop="count" label="数量" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="活动 ROI" name="campaigns">
        <el-button @click="download('campaign-roi')">导出 CSV</el-button>
        <div class="chart-box" ref="campaignChartRef"></div>
        <el-table :data="campaignRows" stripe>
          <el-table-column prop="name" label="活动" />
          <el-table-column prop="memberCount" label="成员数" />
          <el-table-column prop="opportunityCount" label="商机数" />
          <el-table-column prop="wonAmount" label="赢单金额" />
          <el-table-column prop="actualCost" label="实际成本" />
          <el-table-column prop="roi" label="ROI(%)" />
        </el-table>
      </el-tab-pane>

      <el-tab-pane label="应收账款" name="receivables">
        <el-button @click="download('receivables')">导出 CSV</el-button>
        <div class="metric-grid">
          <el-card>
            <div>合同数</div>
            <strong>{{ receivable.summary?.contractCount || 0 }}</strong>
          </el-card>
          <el-card>
            <div>合同金额</div>
            <strong>{{ receivable.summary?.totalAmount || 0 }}</strong>
          </el-card>
          <el-card>
            <div>已回款</div>
            <strong>{{ receivable.summary?.receivedAmount || 0 }}</strong>
          </el-card>
          <el-card>
            <div>逾期金额</div>
            <strong>{{ receivable.summary?.overdueAmount || 0 }}</strong>
          </el-card>
        </div>
        <div class="chart-box" ref="receivableChartRef"></div>
        <el-table :data="receivable.monthly || []" stripe>
          <el-table-column prop="month" label="月份" />
          <el-table-column prop="planAmount" label="计划金额" />
          <el-table-column prop="paidAmount" label="已回款" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import * as echarts from "echarts";
import { nextTick, onBeforeUnmount, onMounted, reactive, ref } from "vue";
import { reports } from "../../api";
import { maps, text } from "../../utils/enums";

const active = ref("funnel");
const year = ref(new Date().getFullYear());
const month = ref(new Date().getMonth() + 1);
const funnelRows = ref([]);
const funnelOpportunityRows = ref([]);
const performanceRows = ref([]);
const campaignRows = ref([]);
const caseReport = reactive({});
const receivable = reactive({ summary: {}, monthly: [] });
const funnelChartRef = ref(null);
const leadChartRef = ref(null);
const performanceChartRef = ref(null);
const caseTypeChartRef = ref(null);
const casePriorityChartRef = ref(null);
const campaignChartRef = ref(null);
const receivableChartRef = ref(null);
const charts = {};

const numberValue = (value) => Number(value || 0);
const labelFor = (map, value) => text(map, value);
const getChart = (name, element) => {
  if (!element) {
    return null;
  }
  if (!charts[name]) {
    charts[name] = echarts.init(element);
  }
  return charts[name];
};
const renderFunnel = () => {
  const chart = getChart("funnel", funnelChartRef.value);
  if (!chart) {
    return;
  }
  chart.setOption({
    tooltip: { trigger: "item" },
    title: { text: "商机阶段漏斗", left: "center" },
    series: [{
      type: "funnel",
      left: "8%",
      width: "84%",
      data: funnelOpportunityRows.value.map((item) => ({
        name: labelFor(maps.stage, item.stage),
        value: item.count,
      })),
    }],
  });
  chart.resize();
};
const renderLeads = () => {
  const chart = getChart("leads", leadChartRef.value);
  if (!chart) {
    return;
  }
  chart.setOption({
    tooltip: { trigger: "axis" },
    title: { text: "线索状态", left: "center" },
    xAxis: {
      type: "category",
      data: funnelRows.value.map((item) => labelFor(maps.leadStatus, item.status)),
    },
    yAxis: { type: "value" },
    series: [{
      type: "bar",
      data: funnelRows.value.map((item) => item.count),
    }],
  });
  chart.resize();
};
const renderPerformance = () => {
  const chart = getChart("performance", performanceChartRef.value);
  if (!chart) {
    return;
  }
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["目标", "实际"] },
    xAxis: { type: "category", data: performanceRows.value.map((item) => item.owner) },
    yAxis: { type: "value" },
    series: [
      {
        name: "目标",
        type: "bar",
        data: performanceRows.value.map((item) => numberValue(item.targetAmount)),
      },
      {
        name: "实际",
        type: "bar",
        data: performanceRows.value.map((item) => numberValue(item.wonAmount)),
      },
    ],
  });
  chart.resize();
};
const renderCases = () => {
  const typeChart = getChart("caseType", caseTypeChartRef.value);
  const priorityChart = getChart("casePriority", casePriorityChartRef.value);
  if (typeChart) {
    typeChart.setOption({
      tooltip: { trigger: "item" },
      title: { text: "工单类型", left: "center" },
      series: [{
        type: "pie",
        radius: "60%",
        data: (caseReport.byType || []).map((item) => ({
          name: item.type || item.status,
          value: item.count,
        })),
      }],
    });
    typeChart.resize();
  }
  if (priorityChart) {
    priorityChart.setOption({
      tooltip: { trigger: "axis" },
      title: { text: "工单优先级", left: "center" },
      xAxis: {
        type: "category",
        data: (caseReport.byPriority || []).map((item) =>
          labelFor(maps.priority, item.type || item.status),
        ),
      },
      yAxis: { type: "value" },
      series: [{
        type: "bar",
        data: (caseReport.byPriority || []).map((item) => item.count),
      }],
    });
    priorityChart.resize();
  }
};
const renderCampaigns = () => {
  const chart = getChart("campaigns", campaignChartRef.value);
  if (!chart) {
    return;
  }
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["赢单金额", "实际成本"] },
    xAxis: { type: "category", data: campaignRows.value.map((item) => item.name) },
    yAxis: { type: "value" },
    series: [
      {
        name: "赢单金额",
        type: "bar",
        data: campaignRows.value.map((item) => numberValue(item.wonAmount)),
      },
      {
        name: "实际成本",
        type: "bar",
        data: campaignRows.value.map((item) => numberValue(item.actualCost)),
      },
    ],
  });
  chart.resize();
};
const renderReceivables = () => {
  const chart = getChart("receivables", receivableChartRef.value);
  if (!chart) {
    return;
  }
  chart.setOption({
    tooltip: { trigger: "axis" },
    legend: { data: ["计划金额", "已回款"] },
    xAxis: {
      type: "category",
      data: (receivable.monthly || []).map((item) => item.month),
    },
    yAxis: { type: "value" },
    series: [
      {
        name: "计划金额",
        type: "line",
        data: (receivable.monthly || []).map((item) => numberValue(item.planAmount)),
      },
      {
        name: "已回款",
        type: "bar",
        data: (receivable.monthly || []).map((item) => numberValue(item.paidAmount)),
      },
    ],
  });
  chart.resize();
};
const renderActive = async () => {
  await nextTick();
  if (active.value === "funnel") {
    renderFunnel();
    renderLeads();
  } else if (active.value === "performance") {
    renderPerformance();
  } else if (active.value === "cases") {
    renderCases();
  } else if (active.value === "campaigns") {
    renderCampaigns();
  } else {
    renderReceivables();
  }
};
const load = async () => {
  if (active.value === "funnel") {
    const data = await reports.salesFunnel();
    funnelRows.value = data.leads || [];
    funnelOpportunityRows.value = data.opportunities || [];
  } else if (active.value === "performance") {
    performanceRows.value = await reports.salesPerformance({
      year: year.value,
      month: month.value,
    });
  } else if (active.value === "cases") {
    Object.assign(caseReport, await reports.caseAnalysis());
  } else if (active.value === "campaigns") {
    campaignRows.value = await reports.campaignRoi();
  } else {
    Object.assign(receivable, await reports.receivables());
  }
  renderActive();
};
const download = async (name) => {
  const blob = await reports.export(name, { year: year.value, month: month.value });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = `${name}.csv`;
  link.click();
  URL.revokeObjectURL(url);
};
const resize = () => Object.values(charts).forEach((chart) => chart.resize());

onMounted(() => {
  window.addEventListener("resize", resize);
  load();
});
onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  Object.values(charts).forEach((chart) => chart.dispose());
});
</script>

<style scoped>
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.chart-box {
  width: 100%;
  height: 360px;
  margin: 16px 0;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin: 16px 0;
}

.metric-grid strong {
  display: block;
  margin-top: 8px;
  font-size: 22px;
}

.table-gap {
  margin-top: 18px;
}

@media (max-width: 900px) {
  .chart-grid,
  .metric-grid {
    grid-template-columns: 1fr;
  }
}
</style>
