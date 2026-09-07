<template>
  <div>
    <div class="page-title">服务工单</div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="主题或工单号" clearable />
      <el-select v-model="status" placeholder="状态" clearable
        ><el-option
          v-for="(label, value) in maps.caseStatus"
          :key="value"
          :label="label"
          :value="value"
      /></el-select>
      <el-select v-model="priority" placeholder="优先级" clearable
        ><el-option
          v-for="(label, value) in maps.priority"
          :key="value"
          :label="label"
          :value="value"
      /></el-select>
      <el-checkbox v-model="overdue">仅超期</el-checkbox>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="exportFile">导出</el-button>
      <el-button type="success" @click="open()">新增工单</el-button>
    </div>
    <el-card>
      <el-table :data="rows" :row-class-name="rowClass">
        <el-table-column prop="caseNo" label="工单号" />
        <el-table-column prop="subject" label="主题" />
        <el-table-column prop="status" label="状态"
          ><template #default="{ row }"
            ><el-tag :type="tagType(row.status)">{{
              text(maps.caseStatus, row.status)
            }}</el-tag></template
          ></el-table-column
        >
        <el-table-column prop="priority" label="优先级"
          ><template #default="{ row }"
            ><el-tag :type="tagType(row.priority)">{{
              text(maps.priority, row.priority)
            }}</el-tag></template
          ></el-table-column
        >
        <el-table-column label="SLA剩余"
          ><template #default="{ row }">{{
            remaining(row.slaDueAt)
          }}</template></el-table-column
        >
        <el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button
              link
              type="primary"
              @click="$router.push(`/cases/${row.id}`)"
              >详情</el-button
            ><el-button link @click="open(row)">编辑</el-button
            ><el-button link type="danger" @click="remove(row)"
              >删除</el-button
            ></template
          ></el-table-column
        >
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        layout="total, prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </el-card>
    <el-dialog v-model="visible" title="工单" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="主题" prop="subject"
          ><el-input v-model="form.subject"
        /></el-form-item>
        <el-form-item label="类型"
          ><el-select v-model="form.type"
            ><el-option label="问题" value="PROBLEM" /><el-option
              label="咨询"
              value="QUESTION" /><el-option
              label="投诉"
              value="COMPLAINT" /></el-select
        ></el-form-item>
        <el-form-item label="优先级"
          ><el-select v-model="form.priority"
            ><el-option
              v-for="(label, value) in maps.priority"
              :key="value"
              :label="label"
              :value="value" /></el-select
        ></el-form-item>
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
import { cases, exportCsv } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");
const status = ref("");
const priority = ref("");
const overdue = ref(false);
const visible = ref(false);
const editingId = ref();
const formRef = ref();
const form = reactive({});
const rules = {
  subject: [{ required: true, message: "请输入主题", trigger: "blur" }],
};

const load = async () => {
  const data = await cases.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    status: status.value,
    priority: priority.value,
    overdue: overdue.value || undefined,
  });
  rows.value = data.records;
  total.value = data.total;
};

const exportFile = async () => {
  const blob = await exportCsv("/cases", {
    keyword: keyword.value,
    status: status.value,
    priority: priority.value,
    overdue: overdue.value || undefined,
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = "cases.csv";
  link.click();
  URL.revokeObjectURL(url);
};
const remaining = (value) => {
  if (!value) return "-";
  const hours = (new Date(value) - Date.now()) / 3600000;
  return hours < 0
    ? `已超期 ${Math.abs(hours).toFixed(1)} 小时`
    : `${hours.toFixed(1)} 小时`;
};
const rowClass = ({ row }) =>
  remaining(row.slaDueAt).startsWith("已超期") &&
  !["RESOLVED", "CLOSED"].includes(row.status)
    ? "overdue-row"
    : "";
const open = (row) => {
  Object.assign(form, row || { priority: "MEDIUM", type: "PROBLEM" });
  editingId.value = row?.id;
  visible.value = true;
};
const save = async () => {
  await formRef.value.validate();
  if (editingId.value) await cases.update(editingId.value, form);
  else await cases.add(form);
  ElMessage.success("保存成功");
  visible.value = false;
  await load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该工单吗？", "提示");
  await cases.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

onMounted(load);
</script>
