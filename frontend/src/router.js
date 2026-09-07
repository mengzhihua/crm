import { createRouter, createWebHistory } from "vue-router";
import Dashboard from "./views/Dashboard.vue";

const routes = [
  { path: "/login", component: () => import("./views/Login.vue") },
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
  { path: "/products", component: () => import("./views/products/ProductList.vue") },
  {
    path: "/pricebooks",
    component: () => import("./views/pricebooks/PriceBookList.vue"),
  },
  {
    path: "/pricebooks/:id",
    component: () => import("./views/pricebooks/PriceBookDetail.vue"),
  },
  { path: "/quotes", component: () => import("./views/quotes/QuoteList.vue") },
  {
    path: "/quotes/:id",
    component: () => import("./views/quotes/QuoteDetail.vue"),
  },
  {
    path: "/approvals",
    component: () => import("./views/approvals/ApprovalList.vue"),
  },
  { path: "/users", component: () => import("./views/users/UserList.vue") },
  {
    path: "/opportunities/:id",
    component: () => import("./views/opportunities/OpportunityDetail.vue"),
  },
  { path: "/campaigns", component: () => import("./views/campaigns/CampaignList.vue") },
  {
    path: "/campaigns/:id",
    component: () => import("./views/campaigns/CampaignDetail.vue"),
  },
  { path: "/contracts", component: () => import("./views/contracts/ContractList.vue") },
  {
    path: "/contracts/:id",
    component: () => import("./views/contracts/ContractDetail.vue"),
  },
  { path: "/forecast", component: () => import("./views/forecast/Forecast.vue") },
  {
    path: "/sales-targets",
    component: () => import("./views/forecast/SalesTargetList.vue"),
  },
  { path: "/settings", component: () => import("./views/settings/ServiceSettings.vue") },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach((to) => {
  if (to.path === "/login") {
    return true;
  }
  if (!localStorage.getItem("crm_token")) {
    return "/login";
  }
  return true;
});

export default router;
