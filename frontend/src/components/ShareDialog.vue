<template>
  <el-dialog v-model="visible" title="共享记录" width="560px">
    <el-form inline>
      <el-select v-model="user" placeholder="选择用户" filterable>
        <el-option
          v-for="item in users"
          :key="item.username"
          :label="item.displayName || item.username"
          :value="item.username"
        />
      </el-select>
      <el-select v-model="accessLevel">
        <el-option label="只读" value="READ" />
        <el-option label="可编辑" value="EDIT" />
      </el-select>
      <el-button type="primary" @click="add">共享</el-button>
    </el-form>
    <el-table :data="items">
      <el-table-column prop="sharedWith" label="用户" />
      <el-table-column prop="accessLevel" label="权限" />
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="danger" @click="remove(row)">撤销</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup>
import { ref } from "vue";
import { shares, users as userApi } from "../api";

const visible = ref(false);
const objectType = ref("");
const recordId = ref();
const items = ref([]);
const users = ref([]);
const user = ref("");
const accessLevel = ref("READ");
const load = async () => {
  items.value = await shares.list({
    objectType: objectType.value,
    recordId: recordId.value,
  });
};
const open = async (type, id) => {
  objectType.value = type;
  recordId.value = id;
  users.value = (await userApi.list({ page: 1, size: 100 })).records || [];
  user.value = "";
  accessLevel.value = "READ";
  await load();
  visible.value = true;
};
const add = async () => {
  await shares.add({
    objectType: objectType.value,
    recordId: recordId.value,
    sharedWith: user.value,
    accessLevel: accessLevel.value,
  });
  await load();
};
const remove = async (row) => {
  await shares.remove(row.id);
  await load();
};
defineExpose({ open });
</script>
