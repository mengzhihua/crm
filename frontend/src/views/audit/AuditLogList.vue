<template>
  <section>
    <div class="page-toolbar">
      <h2>操作审计</h2>
      <el-input v-model="filters.username" placeholder="用户名" clearable />
      <el-input v-model="filters.module" placeholder="模块" clearable />
      <el-date-picker
        v-model="filters.range"
        type="datetimerange"
        start-placeholder="开始时间"
        end-placeholder="结束时间"
        value-format="YYYY-MM-DDTHH:mm:ss"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="download">导出 CSV</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="username" label="用户" />
      <el-table-column prop="role" label="角色" />
      <el-table-column prop="action" label="操作" />
      <el-table-column prop="module" label="模块" />
      <el-table-column prop="path" label="路径" />
      <el-table-column prop="status" label="状态" width="80" />
      <el-table-column prop="durationMs" label="耗时(ms)" width="100" />
      <el-table-column prop="ip" label="IP" />
    </el-table>
    <el-pagination
      v-model:current-page="page"
      :page-size="size"
      :total="total"
      layout="total, prev, pager, next"
      @current-change="load"
    />
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { auditLogs } from "../../api";

const rows = ref([]);
const page = ref(1);
const size = 20;
const total = ref(0);
const filters = reactive({ username: "", module: "", range: [] });
const load = async () => {
  const result = await auditLogs.list({
    page: page.value,
    size,
    username: filters.username || undefined,
    module: filters.module || undefined,
    from: filters.range?.[0] || undefined,
    to: filters.range?.[1] || undefined,
  });
  rows.value = result.records;
  total.value = result.total;
};
const download = async () => {
  const blob = await auditLogs.export({
    username: filters.username || undefined,
    module: filters.module || undefined,
    from: filters.range?.[0] || undefined,
    to: filters.range?.[1] || undefined,
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "audit-logs.csv";
  link.click();
  URL.revokeObjectURL(url);
};
onMounted(load);
</script>
