<template>
  <div>
    <div class="page-title">客户管理</div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="客户名称" clearable />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="exportFile">导出</el-button>
      <el-button type="success" @click="open()">新增客户</el-button>
    </div>
    <el-card>
      <el-table :data="rows" stripe>
        <el-table-column prop="name" label="客户名称" />
        <el-table-column prop="industry" label="行业" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="level" label="等级" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column label="操作" width="220">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              @click="$router.push(`/accounts/${row.id}`)"
              >360详情</el-button
            >
            <el-button link @click="open(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
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
    </el-card>
    <el-dialog
      v-model="visible"
      :title="editingId ? '编辑客户' : '新增客户'"
      width="560px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="行业"
          ><el-input v-model="form.industry"
        /></el-form-item>
        <el-form-item label="电话"
          ><el-input v-model="form.phone"
        /></el-form-item>
        <el-form-item label="网站"
          ><el-input v-model="form.website"
        /></el-form-item>
        <el-form-item label="描述"
          ><el-input v-model="form.description" type="textarea"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button type="primary" @click="save">保存</el-button></template
      >
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { accounts, exportCsv } from "../../api";

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");
const visible = ref(false);
const editingId = ref();
const formRef = ref();
const form = reactive({});
const rules = {
  name: [{ required: true, message: "请输入客户名称", trigger: "blur" }],
};

const load = async () => {
  const data = await accounts.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
  });
  rows.value = data.records;
  total.value = data.total;
};

const exportFile = async () => {
  const blob = await exportCsv("/accounts", { keyword: keyword.value });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "accounts.csv";
  link.click();
  URL.revokeObjectURL(url);
};

const open = (row) => {
  Object.assign(form, row || {});
  editingId.value = row?.id;
  visible.value = true;
};

const save = async () => {
  await formRef.value.validate();
  if (editingId.value) await accounts.update(editingId.value, form);
  else await accounts.add(form);
  ElMessage.success("保存成功");
  visible.value = false;
  await load();
};

const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该客户吗？", "提示");
  await accounts.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

onMounted(load);
</script>
