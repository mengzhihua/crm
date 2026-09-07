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
