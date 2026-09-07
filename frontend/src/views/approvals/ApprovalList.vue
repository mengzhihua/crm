<template>
  <section>
    <div class="page-toolbar">
      <h2>审批中心</h2>
      <el-radio-group v-model="mine" @change="load">
        <el-radio-button :label="true">待我审批</el-radio-button>
        <el-radio-button :label="false">全部申请</el-radio-button>
      </el-radio-group>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="submitter" label="提交人" />
      <el-table-column label="审批角色">
        <template #default="{ row }">{{ text(maps.role, row.approverRole) }}</template>
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
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { approvals } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const mine = ref(true);
const load = async () => {
  rows.value = (await approvals.list({
    page: 1,
    size: 100,
    mine: mine.value,
  })).records;
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
onMounted(load);
</script>
