<template>
  <section>
    <div class="page-toolbar">
      <h2>价格手册</h2>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        新增手册
      </el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="name" label="名称" />
      <el-table-column label="类型">
        <template #default="{ row }">
          <el-tag :type="row.standard ? 'success' : ''">
            {{ row.standard ? "标准" : "自定义" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }">
          {{ row.active ? "启用" : "停用" }}
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="detail(row)">条目维护</el-button>
          <el-button v-if="canEdit" link type="danger" @click="remove(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="价格手册" width="480px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="标准手册">
          <el-switch v-model="form.standard" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="form.active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { priceBooks } from "../../api";
import { currentUser } from "../../utils/permission";

const router = useRouter();
const rows = ref([]);
const dialogVisible = ref(false);
const formRef = ref();
const form = reactive({ id: null, name: "", standard: false, active: true });
const rules = {
  name: [{ required: true, message: "请输入手册名称", trigger: "blur" }],
};
const canEdit = computed(() =>
  ["ADMIN", "SALES_MANAGER"].includes(currentUser()?.role)
);
const load = async () => {
  rows.value = (await priceBooks.list({ page: 1, size: 100 })).records;
};
const openCreate = () => {
  Object.assign(form, { id: null, name: "", standard: false, active: true });
  dialogVisible.value = true;
};
const save = async () => {
  await formRef.value.validate();
  if (form.id) {
    await priceBooks.update(form.id, form);
  } else {
    await priceBooks.add(form);
  }
  ElMessage.success("保存成功");
  dialogVisible.value = false;
  load();
};
const detail = (row) => router.push(`/pricebooks/${row.id}`);
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该价格手册吗？", "提示");
  await priceBooks.remove(row.id);
  ElMessage.success("删除成功");
  load();
};
onMounted(load);
</script>
