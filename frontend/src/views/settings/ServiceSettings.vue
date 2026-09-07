<template>
  <section>
    <h2 class="page-title">服务设置</h2>
    <el-tabs>
      <el-tab-pane label="SLA策略">
        <el-button type="primary" @click="addSla">新增策略</el-button>
        <el-table :data="slaRows" class="top-gap">
          <el-table-column prop="priority" label="优先级" />
          <el-table-column prop="responseHours" label="首响小时" />
          <el-table-column prop="resolveHours" label="解决小时" />
          <el-table-column prop="active" label="启用" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeSla(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
      <el-tab-pane label="分派规则">
        <el-button type="primary" @click="addAssignment">新增规则</el-button>
        <el-table :data="assignmentRows" class="top-gap">
          <el-table-column prop="name" label="名称" />
          <el-table-column prop="priority" label="优先级" />
          <el-table-column prop="assignTo" label="分派给" />
          <el-table-column prop="active" label="启用" />
          <el-table-column label="操作">
            <template #default="{ row }">
              <el-button link type="danger" @click="removeAssignment(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </section>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { serviceSettings } from "../../api";

const slaRows = ref([]);
const assignmentRows = ref([]);
const load = async () => {
  [slaRows.value, assignmentRows.value] = await Promise.all([
    serviceSettings.slaPolicies(),
    serviceSettings.assignmentRules(),
  ]);
};
const addSla = async () => {
  await serviceSettings.addSla({
    priority: "MEDIUM",
    responseHours: 4,
    resolveHours: 24,
    active: true,
  });
  ElMessage.success("策略已保存");
  load();
};
const addAssignment = async () => {
  const { value } = await ElMessageBox.prompt("请输入分派用户名", "新增分派规则");
  await serviceSettings.addAssignment({
    name: "自定义分派规则",
    priority: assignmentRows.value.length + 1,
    assignTo: value,
    active: true,
  });
  ElMessage.success("规则已保存");
  load();
};
const removeSla = async (row) => {
  await serviceSettings.removeSla(row.id);
  load();
};
const removeAssignment = async (row) => {
  await serviceSettings.removeAssignment(row.id);
  load();
};
onMounted(load);
</script>
