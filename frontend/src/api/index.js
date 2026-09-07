import api from './request'
export const crud=(path)=>({list:(p={})=>api.get(path,{params:p}),get:id=>api.get(`${path}/${id}`),add:x=>api.post(path,x),update:(id,x)=>api.put(`${path}/${id}`,x),remove:id=>api.delete(`${path}/${id}`)})
export const leads=crud('/leads'); leads.convert=(id,x)=>api.post(`/leads/${id}/convert`,x)
export const accounts=crud('/accounts'); accounts.overview=id=>api.get(`/accounts/${id}/overview`)
export const contacts=crud('/contacts'); export const opportunities=crud('/opportunities'); opportunities.pipeline=()=>api.get('/opportunities/pipeline');opportunities.stage=(id,x)=>api.put(`/opportunities/${id}/stage`,x)
export const activities=crud('/activities');activities.complete=id=>api.put(`/activities/${id}/complete`)
export const cases=crud('/cases');cases.status=(id,x)=>api.put(`/cases/${id}/status`,x);cases.escalate=id=>api.put(`/cases/${id}/escalate`);cases.assign=(id,x)=>api.put(`/cases/${id}/assign`,x);cases.comments=id=>api.get(`/cases/${id}/comments`);cases.comment=(id,x)=>api.post(`/cases/${id}/comments`,x)
export const knowledge=crud('/knowledge');knowledge.publish=id=>api.put(`/knowledge/${id}/publish`)
export const dashboard={summary:()=>api.get('/dashboard/summary')}
