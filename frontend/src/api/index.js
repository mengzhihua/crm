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
