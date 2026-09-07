# 智华 CRM

一个面向销售与客户服务团队的轻量 CRM，参考 Salesforce Sales Cloud 与 Service Cloud 的对象模型，覆盖销售、服务、产品价格、报价审批与简化 RBAC。

## 对象映射

| CRM 对象 | 本系统模块 |
|---|---|
| Lead | 线索，支持转化为客户、联系人、商机 |
| Account | 客户及客户 360 视图 |
| Contact | 联系人 |
| Opportunity | 商机阶段与销售管道 |
| Activity | 电话、会议、邮件与任务 |
| Case | 服务工单、SLA、升级、评论 |
| Knowledge | 知识文章与发布 |
| Product | 产品目录 |
| PriceBook / PriceBookEntry | 价格手册与产品价格条目 |
| Quote / QuoteLineItem | 报价单与报价行项目 |
| Approval | 报价审批规则与审批申请 |
| User | 用户、角色与 JWT 登录 |

## 目录结构

`backend/` Spring Boot 2.7.18 + JPA + H2；`frontend/` Vue 3 + Vite + Element Plus；`scripts/smoke.sh` 全链路冒烟脚本。

## 启动

```bash
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev
```

后端默认 `http://localhost:8080`，前端默认 `http://localhost:5173`。Swagger 地址：`http://localhost:8080/swagger-ui.html`，H2 控制台：`http://localhost:8080/h2-console`。schema 变更后可执行 `rm -rf backend/data`。

启动后端后运行：

```bash
bash scripts/smoke.sh
```

## 默认账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| manager | 123456 | 销售经理 |
| sales | 123456 | 销售代表 |
| service | 123456 | 服务专员 |

## 权限矩阵

| 角色 | 权限范围 |
|---|---|
| ADMIN | 全部模块 |
| SALES_MANAGER | 销售、产品价格、报价审批、仪表盘；服务与知识只读 |
| SALES_REP | 销售、报价读写；产品价格只读；服务与知识只读 |
| SERVICE_AGENT | 工单、知识、活动读写；客户联系人只读；仪表盘 |

## 后续规划

通知中心、报表导出、多租户、报价审批的更复杂多级流程与审计能力。
