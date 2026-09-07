<template>
  <section>
    <div class="page-toolbar">
      <h2>报价单</h2>
    </div>
    <el-form inline :model="filters">
      <el-form-item label="状态">
        <el-select v-model="filters.status" clearable @change="load">
          <el-option
            v-for="(label, value) in maps.quoteStatus"
            :key="value"
            :label="label"
            :value="value"
          />
        </el-select>
      </el-form-item>
      <el-button @click="load">查询</el-button>
    </el-form>
    <el-table :data="rows" stripe>
      <el-table-column label="报价单号">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/quotes/${row.id}`)">
            {{ row.quoteNo }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="totalAmount" label="总额" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="tagType(row.status)">
            {{ text(maps.quoteStatus, row.status) }}
          </el-tag>
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
import { onMounted, reactive, ref } from "vue";
import { quotes } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const filters = reactive({ status: "" });
const load = async () => {
  const result = await quotes.list({
    page: page.value,
    size: size.value,
    ...filters,
  });
  rows.value = result.records;
  total.value = result.total;
};
onMounted(load);
</script>
