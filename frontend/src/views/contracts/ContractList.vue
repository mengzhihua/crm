<template>
  <section>
    <div class="page-toolbar">
      <h2 class="page-title">合同管理</h2>
      <el-button @click="exportFile">导出</el-button>
    </div>
    <el-form inline @submit.prevent>
      <el-input v-model="keyword" placeholder="合同号或名称" clearable />
      <el-select v-model="status" placeholder="状态" clearable>
        <el-option
          v-for="(label, value) in maps.contractStatus"
          :key="value"
          :label="label"
          :value="value"
        />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
    </el-form>
    <el-table :data="records">
      <el-table-column prop="contractNo" label="合同号" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="amount" label="金额" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">
            {{ text(maps.contractStatus, row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="receivedAmount" label="已回款" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="$router.push(`/contracts/${row.id}`)">
            详情
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      layout="total, prev, pager, next"
      :total="total"
      @current-change="load"
    />
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { contracts, exportCsv } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const records = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const keyword = ref("");
const status = ref("");
const load = async () => {
  const data = await contracts.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    status: status.value || undefined,
  });
  records.value = data.records;
  total.value = data.total;
};
const exportFile = async () => {
  const blob = await exportCsv("/contracts", { keyword: keyword.value, status: status.value });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "contracts.csv";
  link.click();
  URL.revokeObjectURL(url);
};
onMounted(load);
</script>
