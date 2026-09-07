<template>
  <div>
    <div class="page-title">商机管理</div>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="商机名称" clearable />
      <el-select v-model="stage" placeholder="阶段" clearable>
        <el-option
          v-for="(label, value) in maps.stage"
          :key="value"
          :label="label"
          :value="value"
        />
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="view = view === 'table' ? 'kanban' : 'table'">{{
        view === "table" ? "看板视图" : "表格视图"
      }}</el-button>
      <el-button type="success" @click="open()">新增商机</el-button>
    </div>
    <el-card v-if="view === 'table'">
      <el-table :data="rows">
        <el-table-column prop="name" label="名称" />
        <el-table-column prop="amount" label="金额" />
        <el-table-column prop="stage" label="阶段"
          ><template #default="{ row }"
            ><el-tag :type="tagType(row.stage)">{{
              text(maps.stage, row.stage)
            }}</el-tag></template
          ></el-table-column
        >
        <el-table-column prop="probability" label="概率" />
        <el-table-column label="操作"
          ><template #default="{ row }"
            ><el-button link type="primary" @click="advance(row)"
              >推进阶段</el-button
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
    <el-card v-else>
      <div class="kanban">
        <div v-for="item in stageList" :key="item" class="kanban-col">
          <strong
            >{{ text(maps.stage, item) }}（{{ grouped[item].length }}）</strong
          >
          <div class="muted">
            总金额：{{
              grouped[item].reduce(
                (sum, row) => sum + Number(row.amount || 0),
                0,
              )
            }}
          </div>
          <div v-for="row in grouped[item]" :key="row.id" class="kanban-card">
            <div>{{ row.name }}</div>
            <div>金额：{{ row.amount || 0 }}</div>
            <el-button link type="primary" @click="advance(row)"
              >推进阶段</el-button
            >
          </div>
        </div>
      </div>
    </el-card>
    <el-dialog v-model="visible" title="商机" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="客户"
          ><el-select v-model="form.accountId" filterable clearable
            ><el-option
              v-for="account in accountsRows"
              :key="account.id"
              :label="account.name"
              :value="account.id" /></el-select
        ></el-form-item>
        <el-form-item label="金额"
          ><el-input-number v-model="form.amount" :min="0"
        /></el-form-item>
        <el-form-item label="预计日期"
          ><el-date-picker
            v-model="form.expectedCloseDate"
            type="date"
            value-format="YYYY-MM-DD"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="visible = false">取消</el-button
        ><el-button type="primary" @click="save">保存</el-button></template
      >
    </el-dialog>
    <el-dialog v-model="stageVisible" title="推进商机阶段" width="460px">
      <el-form
        ref="stageRef"
        :model="stageForm"
        :rules="stageRules"
        label-width="90px"
      >
        <el-form-item label="目标阶段" prop="stage"
          ><el-select v-model="stageForm.stage"
            ><el-option
              v-for="(label, value) in maps.stage"
              :key="value"
              :label="label"
              :value="value" /></el-select
        ></el-form-item>
        <el-form-item
          v-if="stageForm.stage === 'CLOSED_LOST'"
          label="丢单原因"
          prop="lostReason"
          ><el-input v-model="stageForm.lostReason" type="textarea"
        /></el-form-item>
      </el-form>
      <template #footer
        ><el-button @click="stageVisible = false">取消</el-button
        ><el-button type="primary" @click="saveStage">保存</el-button></template
      >
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { accounts, opportunities } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const accountsRows = ref([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const keyword = ref("");
const stage = ref("");
const view = ref("table");
const visible = ref(false);
const stageVisible = ref(false);
const editingId = ref();
const stageId = ref();
const formRef = ref();
const stageRef = ref();
const form = reactive({});
const stageForm = reactive({});
const stageList = Object.keys(maps.stage);
const rules = {
  name: [{ required: true, message: "请输入名称", trigger: "blur" }],
};
const stageRules = {
  stage: [{ required: true, message: "请选择阶段", trigger: "change" }],
  lostReason: [{ required: true, message: "请输入丢单原因", trigger: "blur" }],
};
const grouped = computed(() =>
  stageList.reduce((result, value) => {
    result[value] = rows.value.filter((row) => row.stage === value);
    return result;
  }, {}),
);

const load = async () => {
  const data = await opportunities.list({
    page: page.value,
    size: size.value,
    keyword: keyword.value,
    stage: stage.value,
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
  if (editingId.value) await opportunities.update(editingId.value, form);
  else await opportunities.add(form);
  ElMessage.success("保存成功");
  visible.value = false;
  await load();
};
const advance = (row) => {
  stageId.value = row.id;
  Object.assign(stageForm, { stage: row.stage, lostReason: "" });
  stageVisible.value = true;
};
const saveStage = async () => {
  await stageRef.value.validate();
  await opportunities.stage(stageId.value, stageForm);
  ElMessage.success("阶段更新成功");
  stageVisible.value = false;
  await load();
};
const remove = async (row) => {
  await ElMessageBox.confirm("确认删除该商机吗？", "提示");
  await opportunities.remove(row.id);
  ElMessage.success("删除成功");
  await load();
};

onMounted(async () => {
  const data = await accounts.list({ page: 1, size: 100 });
  accountsRows.value = data.records;
  await load();
});
</script>
