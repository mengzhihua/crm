<template>
  <el-container class="layout">
    <el-aside width="220px">
      <div class="brand">智华 CRM</div>
      <el-menu
        router
        :default-active="$route.path"
        background-color="#182230"
        text-color="#d7e3f1"
        active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/leads">线索</el-menu-item>
        <el-menu-item index="/accounts">客户</el-menu-item>
        <el-menu-item index="/contacts">联系人</el-menu-item>
        <el-menu-item index="/opportunities">商机</el-menu-item>
        <el-menu-item index="/activities">活动</el-menu-item>
        <el-menu-item index="/cases">服务工单</el-menu-item>
        <el-menu-item index="/knowledge">知识库</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header>
        <span>{{ titles[$route.path] || "CRM 管理平台" }}</span>
        <span class="user">管理员</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { reactive } from "vue";

const titles = reactive({
  "/dashboard": "销售与服务总览",
  "/leads": "线索管理",
  "/accounts": "客户管理",
  "/contacts": "联系人管理",
  "/opportunities": "商机管理",
  "/activities": "活动管理",
  "/cases": "服务工单",
  "/knowledge": "知识库",
});
</script>
