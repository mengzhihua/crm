import { createRouter, createWebHistory } from "vue-router";
import Dashboard from "./views/Dashboard.vue";

const routes = [
  { path: "/", redirect: "/dashboard" },
  { path: "/dashboard", component: Dashboard },
  { path: "/leads", component: () => import("./views/leads/LeadList.vue") },
  {
    path: "/accounts",
    component: () => import("./views/accounts/AccountList.vue"),
  },
  {
    path: "/accounts/:id",
    component: () => import("./views/accounts/AccountDetail.vue"),
  },
  {
    path: "/contacts",
    component: () => import("./views/contacts/ContactList.vue"),
  },
  {
    path: "/opportunities",
    component: () => import("./views/opportunities/OpportunityList.vue"),
  },
  {
    path: "/activities",
    component: () => import("./views/activities/ActivityList.vue"),
  },
  { path: "/cases", component: () => import("./views/cases/CaseList.vue") },
  {
    path: "/cases/:id",
    component: () => import("./views/cases/CaseDetail.vue"),
  },
  {
    path: "/knowledge",
    component: () => import("./views/knowledge/KnowledgeList.vue"),
  },
  {
    path: "/knowledge/:id",
    component: () => import("./views/knowledge/KnowledgeDetail.vue"),
  },
];

export default createRouter({
  history: createWebHistory(),
  routes,
});
