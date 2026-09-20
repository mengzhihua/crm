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
        <el-button type="primary" @click="openRule()">新增规则</el-button>
        <el-table :data="rules" stripe>
          <el-table-column prop="name" label="名称" />
          <el-table-column label="审批角色">
            <template #default="{ row }">
              <el-tag
                v-for="role in roleList(row.approverRoles)"
                :key="role"
                class="role-tag"
              >
                {{ text(maps.role, role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="targetType" label="目标类型" />
          <el-table-column prop="minAmount" label="最低金额" />
          <el-table-column prop="minDiscountRate" label="最低折扣率" />
          <el-table-column prop="priority" label="优先级" />
          <el-table-column label="启用">
            <template #default="{ row }">
              <el-tag :type="row.active ? 'success' : 'info'">
                {{ row.active ? "是" : "否" }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button link type="primary" @click="openRule(row)">编辑</el-button>
              <el-button link type="danger" @click="removeRule(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
    <el-dialog
      v-model="ruleDialogVisible"
      :title="editingRuleId ? '编辑审批规则' : '新增审批规则'"
      width="560px"
    >
      <el-form :model="ruleForm" label-width="110px">
        <el-form-item label="名称" required>
          <el-input v-model="ruleForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="目标类型">
          <el-select v-model="ruleForm.targetType">
            <el-option label="报价单" value="QUOTE" />
          </el-select>
        </el-form-item>
        <el-form-item label="最低金额">
          <el-input-number
            v-model="ruleForm.minAmount"
            :min="0"
            :precision="2"
            :controls="false"
            clearable
          />
        </el-form-item>
        <el-form-item label="最低折扣率">
          <el-input-number
            v-model="ruleForm.minDiscountRate"
            :min="0"
            :max="100"
            :precision="2"
            :controls="false"
            clearable
          />
        </el-form-item>
        <el-form-item label="审批角色" required>
          <el-select
            v-model="ruleForm.approverRoles"
            multiple
            collapse-tags
            placeholder="请选择审批角色"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.value"
              :label="role.label"
              :value="role.value"
            />
          </el-select>
          <div class="form-hint">按选择顺序逐级审批</div>
          <div class="selected-roles">
            <el-tag
              v-for="(role, index) in ruleForm.approverRoles"
              :key="role"
              closable
              @close="removeRole(index)"
            >
              {{ index + 1 }}. {{ text(maps.role, role) }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="优先级" required>
          <el-input-number v-model="ruleForm.priority" :min="1" :max="9999" />
        </el-form-item>
        <el-form-item label="启用">
          <el-switch v-model="ruleForm.active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRule">保存</el-button>
      </template>
    </el-dialog>
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
const ruleDialogVisible = ref(false);
const editingRuleId = ref(null);
const roleOptions = [
  { value: "ADMIN", label: text(maps.role, "ADMIN") },
  { value: "SALES_MANAGER", label: text(maps.role, "SALES_MANAGER") },
  { value: "SALES_REP", label: text(maps.role, "SALES_REP") },
  { value: "SERVICE_AGENT", label: text(maps.role, "SERVICE_AGENT") },
];
const emptyRule = () => ({
  name: "",
  targetType: "QUOTE",
  minAmount: null,
  minDiscountRate: null,
  approverRoles: [],
  priority: 10,
  active: true,
});
const ruleForm = ref(emptyRule());
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
const roleList = (roles) => (roles || "").split(",").filter(Boolean);
const openRule = (row) => {
  editingRuleId.value = row?.id || null;
  ruleForm.value = row
    ? {
        name: row.name || "",
        targetType: row.targetType || "QUOTE",
        minAmount: row.minAmount ?? null,
        minDiscountRate: row.minDiscountRate ?? null,
        approverRoles: roleList(row.approverRoles),
        priority: row.priority || 10,
        active: row.active !== false,
      }
    : emptyRule();
  ruleDialogVisible.value = true;
};
const removeRole = (index) => {
  ruleForm.value.approverRoles.splice(index, 1);
};
const submitRule = async () => {
  if (!ruleForm.value.name || !ruleForm.value.approverRoles.length) {
    ElMessage.warning("请填写规则名称并选择审批角色");
    return;
  }
  const payload = {
    ...ruleForm.value,
    approverRoles: ruleForm.value.approverRoles.join(","),
  };
  if (editingRuleId.value) {
    await approvalRules.update(editingRuleId.value, payload);
  } else {
    await approvalRules.add(payload);
  }
  ruleDialogVisible.value = false;
  ElMessage.success("规则保存成功");
  loadRules();
};
const removeRule = async (row) => {
  await ElMessageBox.confirm(
    `确认删除审批规则“${row.name}”吗？`,
    "删除确认",
    { type: "warning" },
  );
  await approvalRules.remove(row.id);
  ElMessage.success("规则已删除");
  loadRules();
};
onMounted(() => {
  load();
  if (isAdmin.value) {
    loadRules();
  }
});
</script>

<style scoped>
.role-tag {
  margin-right: 4px;
}

.form-hint {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.selected-roles {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 8px;
}
</style>
