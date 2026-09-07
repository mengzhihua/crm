<template>
  <div>
    <div class="page-title">联系人管理</div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="姓名或邮箱" clearable />
      <el-select v-model="accountId" placeholder="选择客户" clearable>
        <el-option
          v-for="account in accountsRows"
          :key="account.id"
          :label="account.name"
          :value="account.id"
        />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="exportFile">导出</el-button>
      <el-button type="success" @click="open()">新增联系人</el-button>
    </div>
    <el-card>
      <el-table :data="rows"
        ><el-table-column prop="name" label="姓名" /><el-table-column
          prop="title"
          label="职位"
        /><el-table-column prop="phone" label="电话" /><el-table-column
          prop="email"
          label="邮箱"
        /><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button link @click="open(row)">编辑</el-button
            ><el-button link type="danger" @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        ></el-table
      >
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </el-card>
    <el-dialog v-model="visible" title="联系人" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="客户" prop="accountId"
          ><el-select v-model="form.accountId" filterable
            ><el-option
              v-for="account in accountsRows"
              :key="account.id"
              :label="account.name"
              :value="account.id" /></el-select
        ></el-form-item>
        <el-form-item label="姓名" prop="name"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="职位"
          ><el-input v-model="form.title"
        /></el-form-item>
        <el-form-item label="电话"
          ><el-input v-model="form.phone"
        /></el-form-item>
        <el-form-item label="邮箱"
          ><el-input v-model="form.email"
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
import { accounts, contacts, exportCsv } from "../../api";

const rows = ref([]);
const accountsRows = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");
const accountId = ref();
const visible = ref(false);
const editingId = ref();
const formRef = ref();
const form = reactive({});
const rules = {
  accountId: [{ required: true, message: "请选择客户", trigger: "change" }],
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
};

const load = async () => {
  const data = await contacts.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    accountId: accountId.value,
  });
  rows.value = data.records;
  total.value = data.total;
};

const exportFile = async () => {
  const blob = await exportCsv("/contacts", {
    keyword: keyword.value,
    accountId: accountId.value,
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "contacts.csv";
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
  if (editingId.value) await contacts.update(editingId.value, form);
  else await contacts.add(form);
  ElMessage.success("保存成功");
  visible.value = false;
  await load();
};

const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该联系人吗？", "提示");
  await contacts.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

onMounted(async () => {
  const data = await accounts.list({ page: 1, size: 100 });
  accountsRows.value = data.records;
  await load();
});
</script>
