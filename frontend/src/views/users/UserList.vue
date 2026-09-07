<template>
  <section>
    <div class="page-toolbar">
      <h2>用户管理</h2>
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </div>
    <el-table :data="rows" stripe>
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="displayName" label="姓名" />
      <el-table-column label="角色">
        <template #default="{ row }">
          <el-tag :type="tagType(row.role)">{{ text(maps.role, row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态">
        <template #default="{ row }">{{ row.enabled ? "启用" : "停用" }}</template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button link type="primary" @click="resetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="新增用户">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.displayName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option
              v-for="(label, value) in maps.role"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { users } from "../../api";
import { maps, tagType, text } from "../../utils/enums";

const rows = ref([]);
const dialogVisible = ref(false);
const formRef = ref();
const form = reactive({
  username: "",
  password: "",
  displayName: "",
  role: "SALES_REP",
  enabled: true,
});
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  role: [{ required: true, message: "请选择角色", trigger: "change" }],
};
const load = async () => {
  rows.value = (await users.list({ page: 1, size: 100 })).records;
};
const openCreate = () => {
  Object.assign(form, {
    username: "",
    password: "",
    displayName: "",
    role: "SALES_REP",
    enabled: true,
  });
  dialogVisible.value = true;
};
const save = async () => {
  await formRef.value.validate();
  await users.add(form);
  ElMessage.success("用户创建成功");
  dialogVisible.value = false;
  load();
};
const resetPassword = async (row) => {
  const result = await ElMessageBox.prompt("请输入新密码", "重置密码", {
    inputPattern: /.+/,
    inputErrorMessage: "密码不能为空",
  });
  await users.password(row.id, { password: result.value });
  ElMessage.success("密码重置成功");
};
onMounted(load);
</script>
