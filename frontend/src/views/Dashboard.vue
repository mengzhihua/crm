<template>
  <div>
    <div class="page-toolbar">
      <div class="page-title">销售与服务总览</div>
      <el-button @click="customize">自定义仪表盘</el-button>
    </div>
    <el-row :gutter="16">
      <el-col
        v-for="widget in layoutWidgets"
        :key="widget.key"
        :span="widget.span || 12"
      >
        <el-card class="dashboard-widget">
          <template #header>{{ widgetTitle(widget.key) }}</template>
          <div v-if="widgetType(widget.key) === 'metric'" class="metric">
            <div class="num">{{ metricValue(widget.key) }}</div>
            <div v-if="widget.key === 'serviceSummary'">
              SLA 达成率：{{ serviceData.slaAchievementRate || 0 }}%
            </div>
          </div>
          <div
            v-else-if="widgetType(widget.key) === 'chart'"
            :ref="(element) => setWidgetRef(widget.key, element)"
            class="chart"
          />
          <el-timeline v-else-if="widget.key === 'recentActivities'">
            <el-timeline-item v-for="item in widgetData(widget.key)" :key="item.id">
              {{ item.subject }}（{{ item.status }}）
            </el-timeline-item>
          </el-timeline>
          <el-table
            v-else
            :data="widgetData(widget.key)"
            size="small"
            empty-text="暂无数据"
          >
            <el-table-column prop="title" label="标题" />
            <el-table-column prop="subject" label="主题" />
            <el-table-column prop="status" label="状态" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    <el-drawer v-model="drawerVisible" title="自定义仪表盘" size="440px">
      <el-checkbox-group v-model="selectedKeys">
        <div v-for="widget in catalog" :key="widget.key" class="widget-option">
          <el-checkbox :label="widget.key">{{ widget.title }}</el-checkbox>
          <div class="widget-actions">
            <el-button size="small" @click="move(widget.key, -1)">上移</el-button>
            <el-button size="small" @click="move(widget.key, 1)">下移</el-button>
            <el-select v-model="widgetSpans[widget.key]" size="small">
              <el-option
                v-for="span in [6, 8, 12, 24]"
                :key="span"
                :label="`${span}/24`"
                :value="span"
              />
            </el-select>
          </div>
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

const catalog = ref([]);
const layoutWidgets = ref([]);
const selectedKeys = ref([]);
const widgetSpans = ref({});
const dataMap = ref({});
const serviceData = ref({});
const drawerVisible = ref(false);
const widgetRefs = new Map();
const chartInstances = new Map();

const setWidgetRef = (key, element) => {
  if (element) {
    widgetRefs.set(key, element);
  } else {
    widgetRefs.delete(key);
  }
};

const catalogItem = (key) => catalog.value.find((item) => item.key === key) || {};
const widgetTitle = (key) => catalogItem(key).title || key;
const widgetType = (key) => catalogItem(key).type || "list";
const widgetData = (key) => {
  const value = dataMap.value[key];
  if (Array.isArray(value)) {
    return value;
  }
  if (value && Array.isArray(value.records)) {
    return value.records;
  }
  return [];
};
const metricValue = (key) => {
  if (key === "serviceSummary") {
    return serviceData.value.averageSatisfactionScore || 0;
  }
  return dataMap.value[key] ?? 0;
};

const chartOption = (key, data) => {
  if (key === "pipeline") {
    return {
      tooltip: {},
      xAxis: { type: "category", data: (data || []).map((item) =>
        text(maps.stage, item.stage),
      ) },
      yAxis: { type: "value" },
      series: [{ type: "bar", data: (data || []).map((item) => item.count) }],
    };
  }
  if (key === "leadBySource") {
    return {
      tooltip: {},
      series: [{
        type: "pie",
        radius: "65%",
        data: Object.entries(data || {}).map(([name, value]) => ({
          name: text(maps.leadSource, name),
          value,
        })),
      }],
    };
  }
  const values = Object.entries(data || {});
  const labels = key === "caseByStatus"
    ? values.map(([name]) => text(maps.caseStatus, name))
    : values.map(([name]) => text(maps.priority, name));
  return {
    xAxis: { type: "category", data: labels },
    yAxis: { type: "value" },
    series: [{ type: "bar", data: values.map(([, value]) => value) }],
  };
};

const renderCharts = async () => {
  await nextTick();
  const activeKeys = new Set(
    layoutWidgets.value
      .filter((widget) => widgetType(widget.key) === "chart")
      .map((widget) => widget.key),
  );
  chartInstances.forEach((instance, key) => {
    const element = widgetRefs.get(key);
    if (!activeKeys.has(key) || (element && instance.getDom() !== element)) {
      instance.dispose();
      chartInstances.delete(key);
    }
  });
  layoutWidgets.value
    .filter((widget) => widgetType(widget.key) === "chart")
    .forEach((widget) => {
      const element = widgetRefs.get(widget.key);
      if (!element || !dataMap.value[widget.key]) {
        return;
      }
      let instance = chartInstances.get(widget.key);
      if (instance && instance.getDom() !== element) {
        instance.dispose();
        chartInstances.delete(widget.key);
        instance = null;
      }
      if (!instance) {
        instance = echarts.init(element);
        chartInstances.set(widget.key, instance);
      }
      instance.setOption(chartOption(widget.key, dataMap.value[widget.key]));
      instance.resize();
    });
};

const resize = () => chartInstances.forEach((instance) => instance.resize());

const loadLayout = async () => {
  const layout = await dashboard.layout();
  layoutWidgets.value = JSON.parse(layout.widgetsJson || "[]");
  selectedKeys.value = layoutWidgets.value.map((widget) => widget.key);
  widgetSpans.value = Object.fromEntries(
    layoutWidgets.value.map((widget) => [widget.key, widget.span || 12]),
  );
  dataMap.value = {};
  await Promise.all(layoutWidgets.value.map(async (widget) => {
    if (widget.key === "serviceSummary") {
      serviceData.value = await serviceDashboard();
    } else {
      dataMap.value[widget.key] = await dashboard.widget(widget.key);
    }
  }));
  await renderCharts();
};

const customize = async () => {
  catalog.value = await dashboard.widgets();
  await loadLayout();
  drawerVisible.value = true;
};

const move = (key, direction) => {
  const index = selectedKeys.value.indexOf(key);
  const target = index + direction;
  if (index < 0 || target < 0 || target >= selectedKeys.value.length) {
    return;
  }
  const keys = [...selectedKeys.value];
  [keys[index], keys[target]] = [keys[target], keys[index]];
  selectedKeys.value = keys;
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
  await loadLayout();
};

const reset = async () => {
  await dashboard.resetLayout();
  drawerVisible.value = false;
  await loadLayout();
};

onMounted(async () => {
  catalog.value = await dashboard.widgets();
  await loadLayout();
  window.addEventListener("resize", resize);
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", resize);
  chartInstances.forEach((instance) => instance.dispose());
});
</script>

<style scoped>
.page-toolbar {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 16px;
}

.dashboard-widget {
  margin-bottom: 16px;
}

.metric {
  min-height: 80px;
  text-align: center;
}

.metric .num {
  font-size: 28px;
  font-weight: 600;
}

.chart {
  height: 280px;
  width: 100%;
}

.widget-option {
  align-items: center;
  display: flex;
  justify-content: space-between;
  margin-bottom: 12px;
}

.widget-actions {
  align-items: center;
  display: flex;
  gap: 4px;
}
</style>
