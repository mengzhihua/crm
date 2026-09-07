<template>
  <div>
    <div class="page-title">
      {{ overview.account?.name || "客户详情" }}
      <el-button link @click="$router.back()">返回</el-button>
    </div>
    <el-card v-if="overview.account" class="detail-card">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="行业">{{
          overview.account.industry || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="电话">{{
          overview.account.phone || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="网站">{{
          overview.account.website || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="地址">{{
          overview.account.address || "-"
        }}</el-descriptions-item>
        <el-descriptions-item label="描述">{{
          overview.account.description || "-"
        }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
    <el-card>
      <el-tabs v-model="active">
        <el-tab-pane label="联系人" name="contacts">
          <el-button type="primary" @click="openContact">新增联系人</el-button>
          <el-table :data="overview.contacts || []"
            ><el-table-column prop="name" label="姓名" /><el-table-column
              prop="phone"
              label="电话" /><el-table-column prop="email" label="邮箱"
          /></el-table>
        </el-tab-pane>
        <el-tab-pane label="商机" name="opportunities">
          <el-button type="primary" @click="openOpportunity"
            >新增商机</el-button
          >
          <el-table :data="overview.opportunities || []"
            ><el-table-column prop="name" label="名称" /><el-table-column
              prop="amount"
              label="金额" /><el-table-column prop="stage" label="阶段"
          /></el-table>
        </el-tab-pane>
        <el-tab-pane label="工单" name="cases">
          <el-button type="primary" @click="openCase">新增工单</el-button>
          <el-table :data="overview.cases || []"
            ><el-table-column prop="caseNo" label="工单号" /><el-table-column
              prop="subject"
              label="主题" /><el-table-column prop="status" label="状态"
          /></el-table>
        </el-tab-pane>
        <el-tab-pane label="活动" name="activities">
          <el-button type="primary" @click="openActivity">新增活动</el-button>
          <el-table :data="activitiesRows"
            ><el-table-column prop="subject" label="主题" /><el-table-column
              prop="dueTime"
              label="截止时间" /><el-table-column prop="status" label="状态"
          /></el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    <el-dialog v-model="visible" title="新增记录" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="名称" prop="name" v-if="active === 'opportunities'"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item label="姓名" prop="name" v-if="active === 'contacts'"
          ><el-input v-model="form.name"
        /></el-form-item>
        <el-form-item
          label="主题"
          prop="subject"
          v-if="active === 'cases' || active === 'activities'"
          ><el-input v-model="form.subject"
        /></el-form-item>
        <el-form-item label="金额" v-if="active === 'opportunities'"
          ><el-input-number v-model="form.amount" :min="0"
        /></el-form-item>
        <el-form-item label="电话" v-if="active === 'contacts'"
          ><el-input v-model="form.phone"
        /></el-form-item>
        <el-form-item label="内容" v-if="active === 'activities'"
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
import { useRoute } from "vue-router";
import { ElMessage } from "element-plus";
import {
  accounts,
  activities,
  cases,
  contacts,
  opportunities,
} from "../../api";

const route = useRoute();
const active = ref("contacts");
const overview = reactive({
  contacts: [],
  opportunities: [],
  cases: [],
  account: null,
});
const activitiesRows = ref([]);
const visible = ref(false);
const formRef = ref();
const form = reactive({});
const rules = {
  name: [{ required: true, message: "请输入名称", trigger: "blur" }],
  subject: [{ required: true, message: "请输入主题", trigger: "blur" }],
};

const load = async () => {
  Object.assign(overview, await accounts.overview(route.params.id));
  const data = await activities.list({
    page: 1,
    size: 100,
    relatedType: "ACCOUNT",
    relatedId: route.params.id,
  });
  activitiesRows.value = data.records;
};

const openContact = () => {
  active.value = "contacts";
  Object.assign(form, { accountId: Number(route.params.id) });
  visible.value = true;
};
const openOpportunity = () => {
  active.value = "opportunities";
  Object.assign(form, { accountId: Number(route.params.id) });
  visible.value = true;
};
const openCase = () => {
  active.value = "cases";
  Object.assign(form, { accountId: Number(route.params.id) });
  visible.value = true;
};
const openActivity = () => {
  active.value = "activities";
  Object.assign(form, {
    relatedType: "ACCOUNT",
    relatedId: Number(route.params.id),
  });
  visible.value = true;
};

const save = async () => {
  if (active.value === "contacts") await contacts.add(form);
  if (active.value === "opportunities") await opportunities.add(form);
  if (active.value === "cases") await cases.add(form);
  if (active.value === "activities") await activities.add(form);
  ElMessage.success("新增成功");
  visible.value = false;
  await load();
};

watch(active, load);
onMounted(load);
</script>
