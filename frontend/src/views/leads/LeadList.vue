<template>
  <div>
    <div class="page-title">线索管理</div>
    <div class="toolbar">
      <el-input v-model="filters.keyword" placeholder="姓名或公司" clearable />
      <el-select v-model="filters.status" placeholder="状态" clearable>
        <el-option
          v-for="(label, value) in maps.leadStatus"
          :key="value"
          :label="label"
          :value="value"
        />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="open()">新增线索</el-button>
    </div>
    <el-card>
      <el-table :data="rows" stripe>
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="company" label="公司" />
        <el-table-column prop="phone" label="电话" />
        <el-table-column prop="source" label="来源">
          <template #default="{ row }">
            <el-tag>{{ text(maps.leadSource, row.source) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="{ row }">
            <el-tag :type="tagType(row.status)">{{
              text(maps.leadStatus, row.status)
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="230">
          <template #default="{ row }">
            <el-button link type="primary" @click="open(row)">编辑</el-button>
            <el-button link type="success" @click="convert(row)"
              >转化</el-button
            >
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
      :title="form.id ? '编辑线索' : '新增线索'"
      width="560px"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="姓名" prop="name"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="公司"
          ><el-input v-model="form.company"
        /></el-form-item>
        <el-form-item label="电话"
          ><el-input v-model="form.phone"
        /></el-form-item>
        <el-form-item label="邮箱"
          ><el-input v-model="form.email"
        /></el-form-item>
        <el-form-item label="来源"
          ><el-select v-model="form.source" clearable
            ><el-option
              v-for="(label, value) in maps.leadSource"
              :key="value"
              :label="label"
              :value="value" /></el-select
        ></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button type="primary" @click="save">保存</el-button></template
      >
    </el-dialog>
    <el-dialog v-model="convertVisible" title="转化线索" width="480px">
      <el-form :model="convertForm" label-width="100px">
        <el-form-item label="创建商机"
          ><el-switch v-model="convertForm.createOpportunity"
        /></el-form-item>
        <el-form-item label="商机名称"
          ><el-input v-model="convertForm.opportunityName"
        /></el-form-item>
        <el-form-item label="商机金额"
          ><el-input-number v-model="convertForm.amount" :min="0"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="convertVisible = false">取消</el-button
        ><el-button type="primary" @click="submitConvert"
          >确认转化</el-button
        ></template
      >
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { leads } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const visible = ref(false);
const convertVisible = ref(false);
const formRef = ref();
const editingId = ref();
const form = reactive({});
const convertForm = reactive({ createOpportunity: true });
const filters = reactive({ keyword: "", status: "" });
const rules = {
  name: [{ required: true, message: "请输入姓名", trigger: "blur" }],
};

const load = async () => {
  const data = await leads.list({
    page: page.value,
    size: size.value,
    ...filters,
  });
  rows.value = data.records;
  total.value = data.total;
};

const open = (row) => {
  Object.assign(form, row || {});
  editingId.value = row?.id;
  visible.value = true;
};

const save = async () => {
  await formRef.value.validate();
  if (editingId.value) await leads.update(editingId.value, form);
  else await leads.add(form);
  ElMessage.success("保存成功");
  visible.value = false;
  await load();
};

const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该线索吗？", "提示");
  await leads.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

const convert = (row) => {
  editingId.value = row.id;
  convertVisible.value = true;
};

const submitConvert = async () => {
  await leads.convert(editingId.value, convertForm);
  ElMessage.success("转化成功");
  convertVisible.value = false;
  await load();
};

onMounted(load);
</script>
