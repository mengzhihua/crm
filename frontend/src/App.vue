<template>
  <router-view v-if="$route.path === '/login'" />
  <el-container v-else class="layout">
    <el-aside width="220px">
      <div class="brand">智华 CRM</div>
      <el-menu
        router
        :default-active="$route.path"
        background-color="#182230"
        text-color="#d7e3f1"
        active-text-color="#fff"
      >
        <el-menu-item
          v-for="item in visibleMenus"
          :key="item.path"
          :index="item.path"
        >
          {{ item.label }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span>{{ titles[$route.path] || "CRM 管理平台" }}</span>
        <span class="user">
          {{ user?.displayName || user?.username }}（{{ roleText(user?.role) }}）
          <el-button link type="primary" @click="logout">退出</el-button>
        </span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { maps } from "./utils/enums";
import { canSee, currentUser, menuPermissions } from "./utils/permission";

const titles = reactive({
  "/dashboard": "销售与服务总览",
  "/leads": "线索管理",
  "/accounts": "客户管理",
  "/contacts": "联系人管理",
  "/opportunities": "商机管理",
  "/activities": "活动管理",
  "/cases": "服务工单",
  "/knowledge": "知识库",
  "/products": "产品目录",
  "/pricebooks": "价格手册",
  "/quotes": "报价单",
  "/approvals": "审批中心",
  "/users": "用户管理",
});

const router = useRouter();
const user = ref(currentUser());
const menuLabels = {
  "/dashboard": "仪表盘",
  "/leads": "线索",
  "/accounts": "客户",
  "/contacts": "联系人",
  "/opportunities": "商机",
  "/activities": "活动",
  "/cases": "服务工单",
  "/knowledge": "知识库",
  "/products": "产品",
  "/pricebooks": "价格手册",
  "/quotes": "报价单",
  "/approvals": "审批中心",
  "/users": "用户管理",
};
const visibleMenus = computed(() =>
  Object.keys(menuPermissions)
    .filter((path) => canSee(path, user.value?.role))
    .map((path) => ({ path, label: menuLabels[path] })),
);
const roleText = (role) => maps.role[role] || role || "";
const logout = () => {
  localStorage.removeItem("crm_token");
  localStorage.removeItem("crm_user");
  router.push("/login");
};
</script>
