import api from "./request";

const crud = (path) => ({
  list: (params = {}) => api.get(path, { params }),
  get: (id) => api.get(`${path}/${id}`),
  add: (data) => api.post(path, data),
  update: (id, data) => api.put(`${path}/${id}`, data),
  remove: (id) => api.delete(`${path}/${id}`),
});

export const leads = crud("/leads");
leads.convert = (id, data) => api.post(`/leads/${id}/convert`, data);

export const accounts = crud("/accounts");
accounts.overview = (id) => api.get(`/accounts/${id}/overview`);

export const contacts = crud("/contacts");

export const opportunities = crud("/opportunities");
opportunities.pipeline = () => api.get("/opportunities/pipeline");
opportunities.stage = (id, data) => api.put(`/opportunities/${id}/stage`, data);

export const activities = crud("/activities");
activities.complete = (id) => api.put(`/activities/${id}/complete`);

export const cases = crud("/cases");
cases.status = (id, data) => api.put(`/cases/${id}/status`, data);
cases.escalate = (id) => api.put(`/cases/${id}/escalate`);
cases.assign = (id, data) => api.put(`/cases/${id}/assign`, data);
cases.comments = (id) => api.get(`/cases/${id}/comments`);
cases.comment = (id, data) => api.post(`/cases/${id}/comments`, data);
cases.survey = (id, data) => api.post(`/cases/${id}/survey`, data);
cases.articles = (id) => api.get(`/cases/${id}/articles`);
cases.addArticle = (id, articleId) => api.post(`/cases/${id}/articles/${articleId}`);
cases.removeArticle = (id, articleId) => api.delete(`/cases/${id}/articles/${articleId}`);

export const knowledge = crud("/knowledge");
knowledge.publish = (id) => api.put(`/knowledge/${id}/publish`);

export const dashboard = {
  summary: () => api.get("/dashboard/summary"),
};

export const auth = {
  login: (data) => api.post("/auth/login", data),
  me: () => api.get("/auth/me"),
};

export const products = crud("/products");

export const priceBooks = crud("/pricebooks");
priceBooks.entries = (id) => api.get(`/pricebooks/${id}/entries`);
priceBooks.addEntry = (id, data) => api.post(`/pricebooks/${id}/entries`, data);
priceBooks.updateEntry = (id, entryId, data) =>
  api.put(`/pricebooks/${id}/entries/${entryId}`, data);
priceBooks.removeEntry = (id, entryId) =>
  api.delete(`/pricebooks/${id}/entries/${entryId}`);

export const opportunityItems = {
  list: (id) => api.get(`/opportunities/${id}/items`),
  replace: (id, data) => api.put(`/opportunities/${id}/items`, data),
};

export const quotes = crud("/quotes");
quotes.fromOpportunity = (id) => api.post(`/quotes/from-opportunity/${id}`);
quotes.items = (id) => api.get(`/quotes/${id}/items`);
quotes.replaceItems = (id, data) => api.put(`/quotes/${id}/items`, data);
quotes.discount = (id, data) => api.put(`/quotes/${id}/discount`, data);
quotes.submit = (id) => api.post(`/quotes/${id}/submit`);
quotes.accept = (id) => api.post(`/quotes/${id}/accept`);

export const approvals = {
  list: (params = {}) => api.get("/approvals", { params }),
  approve: (id, data) => api.put(`/approvals/${id}/approve`, data),
  reject: (id, data) => api.put(`/approvals/${id}/reject`, data),
};

export const users = crud("/users");
users.password = (id, data) => api.put(`/users/${id}/password`, data);

export const campaigns = crud("/campaigns");
campaigns.members = (id) => api.get(`/campaigns/${id}/members`);
campaigns.addMembers = (id, data) => api.post(`/campaigns/${id}/members`, data);
campaigns.removeMember = (id, memberId) =>
  api.delete(`/campaigns/${id}/members/${memberId}`);
campaigns.stats = (id) => api.get(`/campaigns/${id}/stats`);

export const contracts = crud("/contracts");
contracts.fromQuote = (quoteId) => api.post(`/contracts/from-quote/${quoteId}`);
contracts.activate = (id) => api.put(`/contracts/${id}/activate`);
contracts.terminate = (id, data) => api.put(`/contracts/${id}/terminate`, data);
contracts.refreshExpired = () => api.post("/contracts/refresh-expired");
contracts.plans = (id) => api.get(`/contracts/${id}/payment-plans`);
contracts.addPlan = (id, data) => api.post(`/contracts/${id}/payment-plans`, data);
contracts.payments = (id) => api.get(`/contracts/${id}/payments`);
contracts.addPayment = (id, data) => api.post(`/contracts/${id}/payments`, data);

export const forecast = {
  list: (params = {}) => api.get("/forecast", { params }),
  trend: (year) => api.get("/forecast/trend", { params: { year } }),
};

export const salesTargets = crud("/sales-targets");

export const serviceSettings = {
  slaPolicies: () => api.get("/sla-policies"),
  addSla: (data) => api.post("/sla-policies", data),
  updateSla: (id, data) => api.put(`/sla-policies/${id}`, data),
  removeSla: (id) => api.delete(`/sla-policies/${id}`),
  assignmentRules: () => api.get("/assignment-rules"),
  addAssignment: (data) => api.post("/assignment-rules", data),
  updateAssignment: (id, data) => api.put(`/assignment-rules/${id}`, data),
  removeAssignment: (id) => api.delete(`/assignment-rules/${id}`),
};

export const records = {
  history: (params) => api.get("/history", { params }),
  notes: (params) => api.get("/notes", { params }),
  addNote: (data) => api.post("/notes", data),
  attachments: (params) => api.get("/attachments", { params }),
  upload: (data) => api.post("/attachments", data, {
    headers: { "Content-Type": "multipart/form-data" },
  }),
  download: (id) => api.get(`/attachments/${id}/download`, {
    responseType: "blob",
  }),
  removeAttachment: (id) => api.delete(`/attachments/${id}`),
};

export const search = (q) => api.get("/search", { params: { q } });

export const serviceDashboard = () => api.get("/dashboard/service");

export const exportCsv = (path, params = {}) => api.get(`${path}/export`, {
  params,
  responseType: "blob",
});
