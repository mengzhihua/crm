<template>
  <section>
    <div class="page-toolbar">
      <h2 class="page-title">{{ campaign.name }}</h2>
      <el-button @click="$router.back()">返回</el-button>
    </div>
    <el-descriptions border :column="4">
      <el-descriptions-item label="类型">{{ text(maps.campaignType, campaign.type) }}</el-descriptions-item>
      <el-descriptions-item label="状态">{{ text(maps.campaignStatus, campaign.status) }}</el-descriptions-item>
      <el-descriptions-item label="预算">{{ campaign.budgetCost || 0 }}</el-descriptions-item>
      <el-descriptions-item label="实际成本">{{ campaign.actualCost || 0 }}</el-descriptions-item>
    </el-descriptions>
    <el-row :gutter="12" class="top-gap">
      <el-col v-for="item in statCards" :key="item.label" :span="4">
        <el-card><div>{{ item.label }}</div><strong>{{ item.value }}</strong></el-card>
      </el-col>
    </el-row>
    <el-card class="top-gap">
      <template #header>
        <span>活动成员</span>
        <el-button v-if="canEdit" style="float: right" type="primary" @click="addMembers">
          批量添加
        </el-button>
      </template>
      <el-table :data="members">
        <el-table-column prop="memberType" label="成员类型" />
        <el-table-column prop="memberId" label="成员ID" />
        <el-table-column prop="status" label="状态" />
        <el-table-column v-if="canEdit" label="操作">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeMember(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-dialog v-model="dialogVisible" title="批量添加成员">
      <el-form label-width="100px">
        <el-form-item label="线索ID">
          <el-input v-model="leadIds" placeholder="多个ID用逗号分隔" />
        </el-form-item>
        <el-form-item label="联系人ID">
          <el-input v-model="contactIds" placeholder="多个ID用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer><el-button type="primary" @click="saveMembers">添加</el-button></template>
    </el-dialog>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { campaigns } from "../../api";
import { maps, text } from "../../utils/enums";
import { currentUser } from "../../utils/permission";
import { useRoute } from "vue-router";

const route = useRoute();
const campaign = reactive({});
const members = ref([]);
const stats = reactive({});
const dialogVisible = ref(false);
const leadIds = ref("");
const contactIds = ref("");
const canEdit = computed(() => ["ADMIN", "SALES_MANAGER"].includes(currentUser()?.role));
const statCards = computed(() => [
  { label: "成员数", value: stats.memberCount || 0 },
  { label: "已响应", value: stats.respondedCount || 0 },
  { label: "线索数", value: stats.leadCount || 0 },
  { label: "商机数", value: stats.opportunityCount || 0 },
  { label: "赢单金额", value: stats.wonAmount || 0 },
  { label: "ROI", value: stats.roi || 0 },
]);
const load = async () => {
  Object.assign(campaign, await campaigns.get(route.params.id));
  members.value = await campaigns.members(route.params.id);
  Object.assign(stats, await campaigns.stats(route.params.id));
};
const addMembers = () => {
  leadIds.value = "";
  contactIds.value = "";
  dialogVisible.value = true;
};
const saveMembers = async () => {
  await campaigns.addMembers(route.params.id, {
    leadIds: leadIds.value.split(",").filter(Boolean).map(Number),
    contactIds: contactIds.value.split(",").filter(Boolean).map(Number),
  });
  dialogVisible.value = false;
  ElMessage.success("成员添加成功");
  load();
};
const removeMember = async (row) => {
  await campaigns.removeMember(route.params.id, row.id);
  ElMessage.success("成员已移除");
  load();
};
onMounted(load);
</script>
