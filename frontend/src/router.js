import {createRouter,createWebHistory} from 'vue-router'
import Dashboard from './views/Dashboard.vue'
import EntityList from './views/EntityList.vue'
import Cases from './views/Cases.vue'
const routes=[{path:'/',redirect:'/dashboard'},{path:'/dashboard',component:Dashboard},{path:'/leads',component:()=>import('./views/Leads.vue')},{path:'/accounts',component:EntityList,props:{type:'accounts'}},{path:'/contacts',component:EntityList,props:{type:'contacts'}},{path:'/opportunities',component:EntityList,props:{type:'opportunities'}},{path:'/activities',component:EntityList,props:{type:'activities'}},{path:'/cases',component:Cases},{path:'/knowledge',component:EntityList,props:{type:'knowledge'}}]
export default createRouter({history:createWebHistory(),routes})
