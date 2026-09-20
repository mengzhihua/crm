<template>
  <section>
    <el-tabs v-model="active">
      <el-tab-pane label="审批申请" name="requests">
        <div class="page-toolbar">
          <h2>审批中心</h2>
          <el-radio-group v-model="mine" @change="load">
            <el-radio-button :label="true">待我审批</el-radio-button>
            <el-radio-button :label="false">全部申请</el-radio-button>
          </el-radio-group>
        </div>
        <el-table :data="rows" stripe @expand-change="expand">
          <el-table-column type="expand">
            <template #default="{ row }">
              <el-timeline>
                <el-timeline-item
                  v-for="step in steps[row.id] || []"
                  :key="step.id"
                  :timestamp="step.decidedAt || '待处理'"
                >
                  {{ text(maps.role, step.approverRole) }}：
                  {{ text(maps.approvalStatus, step.status) }}
                  <span v-if="step.approver">（{{ step.approver }}）</span>
                  <span v-if="step.comment"> {{ step.comment }}</span>
                </el-timeline-item>
              </el-timeline>
            </template>
          </el-table-column>
          <el-table-column prop="title" label="标题" />
          <el-table-column prop="submitter" label="提交人" />
          <el-table-column label="审批角色">
            <template #default="{ row }">{{ text(maps.role, row.approverRole) }}</template>
          </el-table-column>
          <el-table-column label="进度" width="90">
            <template #default="{ row }">{{ row.currentStep }}/{{ row.totalSteps }}</template>
          </el-table-column>
          <el-table-column label="状态">
            <template #default="{ row }">
              <el-tag :type="tagType(row.status)">
                {{ text(maps.approvalStatus, row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="190">
            <template #default="{ row }">
              <template v-if="row.status === 'PENDING'">
                <el-button link type="success" @click="decide(row, true)">同意</el-button>
                <el-button link type="danger" @click="decide(row, false)">拒绝</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane v-if="isAdmin" label="审批规则" name="rules">
        <el-button type="primary" @click="addRule">新增规则</el-button>
        <el-table :data="rules" stripe>
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="approverRoles" label="审批角色" />
          <el-table-column prop="minAmount" label="最低金额" />
          <el-table-column prop="minDiscountRate" label="最低折扣率" />
          <el-table-column prop="priority" label="优先级" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeRule(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { approvalRules, approvals } from "../../api";
import { maps, tagType, text } from "../../utils/enums";
import { currentUser } from "../../utils/permission";

const user = currentUser();
const isAdmin = computed(() => user?.role === "ADMIN");
const active = ref("requests");
const rows = ref([]);
const rules = ref([]);
const steps = ref({});
const mine = ref(true);
const load = async () => {
  rows.value = (await approvals.list({
    page: 1,
    size: 100,
    mine: mine.value,
  })).records;
};
const loadRules = async () => {
  rules.value = await approvalRules.list();
};
const expand = async (row) => {
  steps.value[row.id] = await approvals.steps(row.id);
};
const decide = async (row, approved) => {
  const result = await ElMessageBox.prompt(
    approved ? "请输入审批意见（可选）" : "请输入驳回原因",
    approved ? "同意审批" : "拒绝审批",
    { inputPattern: approved ? undefined : /.+/, inputErrorMessage: "请输入驳回原因" },
  );
  const data = { comment: result.value };
  if (approved) {
    await approvals.approve(row.id, data);
  } else {
    await approvals.reject(row.id, data);
  }
  ElMessage.success("审批处理成功");
  load();
};
const addRule = async () => {
  await approvalRules.add({
    name: "新审批规则",
    targetType: "QUOTE",
    approverRoles: "SALES_MANAGER",
    priority: 10,
    active: true,
  });
  loadRules();
};
const removeRule = async (row) => {
  await approvalRules.remove(row.id);
  loadRules();
};
onMounted(() => {
  load();
  if (isAdmin.value) {
    loadRules();
  }
});
</script>
