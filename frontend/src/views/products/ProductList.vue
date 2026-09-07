<template>
  <section>
    <div class="page-toolbar">
      <h2>产品目录</h2>
      <el-button v-if="canEdit" type="primary" @click="openCreate">
        新增产品
      </el-button>
    </div>
    <el-form inline :model="filters">
      <el-form-item label="关键词">
        <el-input v-model="filters.keyword" clearable @keyup.enter="load" />
      </el-form-item>
      <el-button @click="load">查询</el-button>
    </el-form>
    <el-table :data="rows" stripe>
      <el-table-column prop="code" label="编码" />
      <el-table-column prop="name" label="名称" />
      <el-table-column prop="category" label="分类" />
      <el-table-column prop="unit" label="单位" />
      <el-table-column prop="listPrice" label="标准价" />
      <el-table-column label="状态">
        <template #default="{ row }">
          <el-tag :type="row.active ? 'success' : 'info'">
            {{ row.active ? "启用" : "停用" }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <template v-if="canEdit">
            <el-button link type="primary" @click="edit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
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
    <el-dialog v-model="dialogVisible" title="产品" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="form.unit" />
        </el-form-item>
        <el-form-item label="标准价">
          <el-input-number v-model="form.listPrice" :min="0" />
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
import { ElMessage, ElMessageBox } from "element-plus";
import { products } from "../../api";
import { currentUser } from "../../utils/permission";

const rows = ref([]);
const page = ref(1);
const size = ref(10);
const total = ref(0);
const filters = reactive({ keyword: "" });
const dialogVisible = ref(false);
const formRef = ref();
const form = reactive({ id: null, code: "", name: "", active: true });
const rules = {
  code: [{ required: true, message: "请输入产品编码", trigger: "blur" }],
  name: [{ required: true, message: "请输入产品名称", trigger: "blur" }],
};
const canEdit = computed(() =>
  ["ADMIN", "SALES_MANAGER"].includes(currentUser()?.role)
);

const load = async () => {
  const result = await products.list({
    page: page.value,
    size: size.value,
    ...filters,
  });
  rows.value = result.records;
  total.value = result.total;
};
const openCreate = () => {
  Object.assign(form, { id: null, code: "", name: "", active: true });
  dialogVisible.value = true;
};
const edit = (row) => {
  Object.assign(form, row);
  dialogVisible.value = true;
};
const save = async () => {
  await formRef.value.validate();
  if (form.id) {
    await products.update(form.id, form);
  } else {
    await products.add(form);
  }
  ElMessage.success("保存成功");
  dialogVisible.value = false;
  load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该产品吗？", "提示");
  await products.remove(row.id);
  ElMessage.success("删除成功");
  load();
};
onMounted(load);
</script>
