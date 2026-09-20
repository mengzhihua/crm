<template>
  <section>
    <h2>报表中心</h2>
    <el-tabs v-model="active" @tab-change="load">
      <el-tab-pane label="销售漏斗" name="funnel">
        <el-button @click="download('sales-funnel')">导出 CSV</el-button>
        <el-table :data="funnelRows" stripe>
          <el-table-column prop="status" label="状态" />
          <el-table-column prop="count" label="数量" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="销售业绩" name="performance">
        <div class="toolbar">
          <el-input-number v-model="year" :min="2020" :max="2100" />
          <el-input-number v-model="month" :min="1" :max="12" />
          <el-button type="primary" @click="load">查询</el-button>
          <el-button @click="download('sales-performance')">导出 CSV</el-button>
        </div>
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
        <el-descriptions :column="3" border>
          <el-descriptions-item label="平均解决小时">
            {{ caseReport.avgResolveHours }}
          </el-descriptions-item>
          <el-descriptions-item label="SLA 达成率">
            {{ caseReport.slaMetRate }}
          </el-descriptions-item>
          <el-descriptions-item label="平均满意度">
            {{ caseReport.avgSatisfaction }}
          </el-descriptions-item>
        </el-descriptions>
        <el-table :data="caseReport.byStatus" stripe>
          <el-table-column prop="status" label="状态" />
          <el-table-column prop="count" label="数量" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="活动 ROI" name="campaigns">
        <el-button @click="download('campaign-roi')">导出 CSV</el-button>
        <el-table :data="campaignRows" stripe>
          <el-table-column prop="name" label="活动" />
          <el-table-column prop="memberCount" label="成员数" />
          <el-table-column prop="opportunityCount" label="商机数" />
          <el-table-column prop="wonAmount" label="赢单金额" />
          <el-table-column prop="roi" label="ROI(%)" />
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="应收账款" name="receivables">
        <el-button @click="download('receivables')">导出 CSV</el-button>
        <el-descriptions :column="3" border>
          <el-descriptions-item label="合同数">
            {{ receivable.summary?.contractCount }}
          </el-descriptions-item>
          <el-descriptions-item label="合同金额">
            {{ receivable.summary?.totalAmount }}
          </el-descriptions-item>
          <el-descriptions-item label="应收金额">
            {{ receivable.summary?.receivableAmount }}
          </el-descriptions-item>
        </el-descriptions>
        <el-table :data="receivable.monthly" stripe>
          <el-table-column prop="month" label="月份" />
          <el-table-column prop="planAmount" label="计划金额" />
          <el-table-column prop="paidAmount" label="已回款" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { reports } from "../../api";

const active = ref("funnel");
const year = ref(new Date().getFullYear());
const month = ref(new Date().getMonth() + 1);
const funnelRows = ref([]);
const performanceRows = ref([]);
const campaignRows = ref([]);
const caseReport = reactive({});
const receivable = reactive({ summary: {}, monthly: [] });
const load = async () => {
  if (active.value === "funnel") {
    funnelRows.value = (await reports.salesFunnel()).leads || [];
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
onMounted(load);
</script>
