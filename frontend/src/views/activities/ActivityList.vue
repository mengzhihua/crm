<template>
  <div>
    <div class="page-title">活动管理</div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="主题" clearable />
      <el-select v-model="relatedType" placeholder="关联对象" clearable
        ><el-option label="客户" value="ACCOUNT" /><el-option
          label="联系人"
          value="CONTACT" /><el-option
          label="商机"
          value="OPPORTUNITY" /><el-option label="工单" value="CASE"
      /></el-select>
      <el-select v-model="status" placeholder="状态" clearable
        ><el-option label="计划中" value="PLANNED" /><el-option
          label="已完成"
          value="DONE"
      /></el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="success" @click="open()">新增活动</el-button>
    </div>
    <el-card>
      <el-table :data="rows"
        ><el-table-column prop="subject" label="主题" /><el-table-column
          prop="relatedType"
          label="关联对象"
        /><el-table-column prop="dueTime" label="截止时间" /><el-table-column
          prop="status"
          label="状态"
          ><template #default="{ row }"
            ><el-tag :type="tagType(row.status)">{{
              text(maps.activityStatus, row.status)
            }}</el-tag></template
          ></el-table-column
        ><el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              v-if="row.status === 'PLANNED'"
              link
              type="success"
              @click="complete(row)"
              >完成</el-button
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
    <el-dialog v-model="visible" title="活动" width="540px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="主题" prop="subject"
          ><el-input v-model="form.subject"
        /></el-form-item>
        <el-form-item label="关联对象"
          ><el-select v-model="form.relatedType"
            ><el-option label="客户" value="ACCOUNT" /><el-option
              label="联系人"
              value="CONTACT" /><el-option
              label="商机"
              value="OPPORTUNITY" /><el-option
              label="工单"
              value="CASE" /></el-select
        ></el-form-item>
        <el-form-item label="关联记录"
          ><el-select v-model="form.relatedId" filterable
            ><el-option
              v-for="item in relatedOptions"
              :key="item.id"
              :label="item.name || item.subject || item.caseNo"
              :value="item.id" /></el-select
        ></el-form-item>
        <el-form-item label="截止时间"
          ><el-date-picker
            v-model="form.dueTime"
            type="datetime"
            value-format="YYYY-MM-DDTHH:mm:ss"
        /></el-form-item>
        <el-form-item label="内容"
          ><el-input v-model="form.content" type="textarea"
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
import { onMounted, reactive, ref, watch } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { accounts, activities, cases, opportunities } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const relatedOptions = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");
const relatedType = ref("");
const status = ref("");
const visible = ref(false);
const editingId = ref();
const formRef = ref();
const form = reactive({});
const rules = {
  subject: [{ required: true, message: "请输入主题", trigger: "blur" }],
};

const load = async () => {
  const data = await activities.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    relatedType: relatedType.value,
    status: status.value,
  });
  rows.value = data.records;
};
const loadRelated = async () => {
  if (form.relatedType === "ACCOUNT")
    relatedOptions.value = (
      await accounts.list({ page: 1, size: 100 })
    ).records;
  if (form.relatedType === "OPPORTUNITY")
    relatedOptions.value = (
      await opportunities.list({ page: 1, size: 100 })
    ).records;
  if (form.relatedType === "CASE")
    relatedOptions.value = (await cases.list({ page: 1, size: 100 })).records;
  if (form.relatedType === "CONTACT") relatedOptions.value = [];
};
const open = async (row) => {
  Object.assign(form, row || { status: "PLANNED" });
  editingId.value = row?.id;
  await loadRelated();
  visible.value = true;
};
const save = async () => {
  await formRef.value.validate();
  if (editingId.value) await activities.update(editingId.value, form);
  else await activities.add(form);
  ElMessage.success("保存成功");
  visible.value = false;
  await load();
};
const complete = async (row) => {
  await activities.complete(row.id);
  ElMessage.success("活动已完成");
  await load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该活动吗？", "提示");
  await activities.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

watch(() => form.relatedType, loadRelated);
onMounted(load);
</script>
