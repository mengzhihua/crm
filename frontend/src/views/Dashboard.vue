<template>
  <div>
    <div class="page-toolbar">
      <div class="page-title">销售与服务总览</div>
      <el-button @click="customize">自定义仪表盘</el-button>
    </div>
    <el-row :gutter="16" v-if="layoutWidgets.length">
      <el-col
        v-for="metric in metrics"
        :key="metric.label"
        :span="metricSpan(metric.label)"
      >
        <el-card class="metric">
          <div>{{ metric.label }}</div>
          <div class="num">{{ metric.value }}</div>
        </el-card>
      </el-col>
    </el-row>
    <el-card class="service-summary">
      <template #header>服务指标</template>
      <span>平均满意度：{{ serviceSummary.averageSatisfactionScore || 0 }}</span>
      <span>SLA达成率：{{ serviceSummary.slaAchievementRate || 0 }}%</span>
    </el-card>
    <div class="charts">
      <el-card>
        <template #header>商机管道</template>
        <div ref="pipelineEl" class="chart" />
      </el-card>
      <el-card>
        <template #header>线索来源</template>
        <div ref="sourceEl" class="chart" />
      </el-card>
      <el-card>
        <template #header>工单状态</template>
        <div ref="caseEl" class="chart" />
      </el-card>
      <el-card>
        <template #header>工单优先级</template>
        <div ref="priorityEl" class="chart" />
      </el-card>
    </div>
    <el-card class="recent-card">
      <template #header>最近活动</template>
      <el-timeline>
        <el-timeline-item v-for="item in recent" :key="item.id">
          {{ item.subject }}（{{ item.status }}）
        </el-timeline-item>
      </el-timeline>
    </el-card>
    <el-drawer v-model="drawerVisible" title="自定义仪表盘" size="420px">
      <el-checkbox-group v-model="selectedKeys">
        <div v-for="widget in catalog" :key="widget.key" class="widget-option">
          <el-checkbox :label="widget.key">{{ widget.title }}</el-checkbox>
          <el-select v-model="widgetSpans[widget.key]" size="small">
            <el-option v-for="span in [6, 8, 12, 24]" :key="span" :label="`${span}/24`" :value="span" />
          </el-select>
        </div>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="reset">恢复默认</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref } from "vue";
import * as echarts from "echarts";
import { dashboard, serviceDashboard } from "../api";
import { maps, text } from "../utils/enums";

const metrics = ref([]);
const recent = ref([]);
const serviceSummary = ref({});
const pipelineEl = ref();
const sourceEl = ref();
const caseEl = ref();
const priorityEl = ref();
const charts = [];
const catalog = ref([]);
const layoutWidgets = ref([]);
const drawerVisible = ref(false);
const selectedKeys = ref([]);
const widgetSpans = ref({});

const chart = (element, option) => {
  const instance = echarts.init(element);
  instance.setOption(option);
  charts.push(instance);
};

const render = (data) => {
  chart(pipelineEl.value, {
    tooltip: {},
    xAxis: {
      type: "category",
      data: (data.pipeline || []).map((item) => text(maps.stage, item.stage)),
    },
    yAxis: { type: "value" },
    series: [
      { type: "bar", data: (data.pipeline || []).map((item) => item.count) },
    ],
  });
  chart(sourceEl.value, {
    tooltip: {},
    series: [
      {
        type: "pie",
        radius: "65%",
        data: Object.entries(data.leadBySource || {}).map(([name, value]) => ({
          name: text(maps.leadSource, name),
          value,
        })),
      },
    ],
  });
  chart(caseEl.value, {
    xAxis: {
      type: "category",
      data: Object.keys(data.caseByStatus || {}).map((item) =>
        text(maps.caseStatus, item),
      ),
    },
    yAxis: { type: "value" },
    series: [{ type: "bar", data: Object.values(data.caseByStatus || {}) }],
  });
  chart(priorityEl.value, {
    xAxis: {
      type: "category",
      data: Object.keys(data.caseByPriority || {}).map((item) =>
        text(maps.priority, item),
      ),
    },
    yAxis: { type: "value" },
    series: [{ type: "bar", data: Object.values(data.caseByPriority || {}) }],
  });
};

const resize = () => charts.forEach((instance) => instance.resize());
const metricSpan = (label) => {
  const keys = {
    新增线索: "newLeadCount",
    进行中商机: "openOpportunityCount",
    本月赢单金额: "wonAmountThisMonth",
    超期工单数: "overdueCaseCount",
  };
  const widget = layoutWidgets.value.find((item) => item.key === keys[label]);
  return widget?.span || 6;
};
const customize = async () => {
  catalog.value = await dashboard.widgets();
  const layout = await dashboard.layout();
  const widgets = JSON.parse(layout.widgetsJson || "[]");
  selectedKeys.value = widgets.map((item) => item.key);
  widgetSpans.value = Object.fromEntries(
    widgets.map((item) => [item.key, item.span || 12]),
  );
  drawerVisible.value = true;
};
const save = async () => {
  layoutWidgets.value = selectedKeys.value.map((key) => ({
    key,
    span: widgetSpans.value[key] || 12,
  }));
  await dashboard.saveLayout({
    widgetsJson: JSON.stringify(layoutWidgets.value),
  });
  drawerVisible.value = false;
};
const reset = async () => {
  await dashboard.resetLayout();
  drawerVisible.value = false;
  await customize();
};

onMounted(async () => {
  const data = await dashboard.summary();
  const layout = await dashboard.layout();
  layoutWidgets.value = JSON.parse(layout.widgetsJson || "[]");
  serviceSummary.value = await serviceDashboard();
  metrics.value = [
    { label: "新增线索", value: data.newLeadCount || 0 },
    { label: "进行中商机", value: data.openOpportunityCount || 0 },
    { label: "本月赢单金额", value: data.wonAmountThisMonth || 0 },
    { label: "超期工单数", value: data.overdueCaseCount || 0 },
  ];
  recent.value = data.recentActivities || [];
  await nextTick();
  render(data);
  window.addEventListener("resize", resize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  charts.forEach((instance) => instance.dispose());
});
</script>
