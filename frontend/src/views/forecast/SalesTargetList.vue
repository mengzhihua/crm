<template>
  <section>
    <div class="page-toolbar">
      <h2 class="page-title">销售目标</h2>
      <el-button type="primary" @click="open">新增目标</el-button>
    </div>
    <el-table :data="rows">
      <el-table-column prop="owner" label="负责人" />
      <el-table-column prop="year" label="年份" />
      <el-table-column prop="month" label="月份" />
      <el-table-column prop="targetAmount" label="目标金额" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="visible" title="销售目标">
      <el-form :model="form" label-width="90px">
        <el-form-item label="负责人"><el-input v-model="form.owner" /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="form.year" /></el-form-item>
        <el-form-item label="月份"><el-input-number v-model="form.month" :min="1" :max="12" /></el-form-item>
        <el-form-item label="目标金额"><el-input-number v-model="form.targetAmount" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { salesTargets } from "../../api";

const rows = ref([]);
const visible = ref(false);
const form = reactive({});
const load = async () => {
  const data = await salesTargets.list({ page: 1, size: 100 });
  rows.value = data.records;
};
const open = () => {
  Object.assign(form, {
    owner: "sales",
    year: new Date().getFullYear(),
    month: new Date().getMonth() + 1,
    targetAmount: 0,
  });
  visible.value = true;
};
const save = async () => {
  await salesTargets.add(form);
  visible.value = false;
  ElMessage.success("目标已保存");
  load();
};
const remove = async (row) => {
  await salesTargets.remove(row.id);
  ElMessage.success("目标已删除");
  load();
};
onMounted(load);
</script>
