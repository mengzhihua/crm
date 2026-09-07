<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2>智华 CRM</h2>
      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
            @keyup.enter="login"
          />
        </el-form-item>
        <el-button type="primary" class="full-width" @click="login">
          登录
        </el-button>
      </el-form>
      <p class="login-hint">演示账号：admin / admin123</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { auth } from "../api";

const router = useRouter();
const formRef = ref();
const form = reactive({ username: "", password: "" });
const rules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
};

const login = async () => {
  await formRef.value.validate();
  const result = await auth.login(form);
  localStorage.setItem("crm_token", result.token);
  localStorage.setItem("crm_user", JSON.stringify(result.user));
  ElMessage.success("登录成功");
  router.push("/dashboard");
};
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f2f5f9;
}

.login-card {
  width: 380px;
}

.login-card h2 {
  text-align: center;
}

.full-width {
  width: 100%;
}

.login-hint {
  color: #909399;
  text-align: center;
  font-size: 12px;
}
</style>
