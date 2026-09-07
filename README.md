# 智华 CRM

一个面向销售与客户服务团队的轻量 CRM，参考 Salesforce Sales Cloud 与 Service Cloud 的对象模型，覆盖线索到回款的销售全流程、工单与知识库的服务全流程，以及报价审批、销售预测、市场活动与基于角色的权限控制。

## 技术栈

| 层 | 技术 |
|---|---|
| 后端 | JDK 8、Spring Boot 2.7.18、Spring Data JPA、Spring Security + JWT（jjwt 0.11）、springdoc-openapi |
| 数据库 | 默认 H2 文件库（`backend/data/crm`），可切换 MySQL 8（`mysql` profile） |
| 前端 | Vue 3、Vue Router 4、Vite 6、Element Plus、ECharts、Axios |
| 测试 | JUnit 5 单元/集成测试、`scripts/smoke.sh` 全链路冒烟 |

## 功能概览

按三期迭代交付，全部已合并到 `main`：

**一期 · 销售与服务基础**
- 线索管理与一键转化（客户 + 联系人 + 商机）
- 客户 360 视图、联系人、商机阶段管道与看板
- 活动（电话 / 会议 / 邮件 / 任务）
- 服务工单：状态流转、优先级、升级、评论时间线
- 知识库文章与发布流程
- 仪表盘：新增线索、进行中商机、本月赢单金额、超期工单、商机管道 / 线索来源 / 工单状态图

**二期 · 报价、审批与权限**
- 产品目录、价格手册与价格条目、商机产品行项目
- 报价单与报价行项目，报价折扣触发审批规则，审批通过后可接受
- 用户登录（JWT）、4 种角色的 RBAC、用户管理

**三期 · 营销、合同回款、预测与服务运营**
- 市场活动与活动成员，线索来源归因
- 合同、回款计划与回款记录；合同激活联动商机；过期合同每日 01:00 自动刷新
- 销售目标与销售预测（按阶段加权、趋势）
- SLA 策略、工单自动分派规则、满意度调查；超期工单每 5 分钟自动升级
- 字段变更历史、备注、附件（记录动态时间线）
- 全局搜索（客户 / 联系人 / 线索 / 商机 / 工单 / 合同 / 知识）与列表 CSV 导出

## 对象映射

| Salesforce 对象 | 本系统模块 |
|---|---|
| Lead | 线索，支持转化为客户、联系人、商机 |
| Account | 客户及客户 360 视图 |
| Contact | 联系人 |
| Opportunity / OpportunityLineItem | 商机阶段、销售管道与商机产品 |
| Activity | 电话、会议、邮件与任务 |
| Case | 服务工单、SLA、升级、评论 |
| Knowledge | 知识文章与发布 |
| Product | 产品目录 |
| PriceBook / PriceBookEntry | 价格手册与产品价格条目 |
| Quote / QuoteLineItem | 报价单与报价行项目 |
| Approval | 报价审批规则与审批申请 |
| User | 用户、角色与 JWT 登录 |
| Campaign / CampaignMember | 市场活动与活动成员 |
| Contract / PaymentPlan / PaymentRecord | 合同、回款计划与回款记录 |
| SalesTarget / Forecast | 销售目标与销售预测 |
| SlaPolicy / AssignmentRule / CaseSurvey | SLA、工单分派与满意度 |
| FieldHistory / Note / Attachment | 字段历史、备注与附件动态 |
| Search / CSV Export | 全局搜索与列表导出 |

## 目录结构

```
backend/                       Spring Boot 后端（包 com.mengzhihua.crm）
  ├─ sales/                    线索、客户、联系人、商机、活动、产品、价格手册、报价
  ├─ service/                  工单、知识库、SLA、分派规则、满意度
  ├─ approval/                 审批规则与审批申请
  ├─ auth/                     用户、角色、JWT 登录
  ├─ marketing/                市场活动
  ├─ contract/                 合同与回款
  ├─ forecast/                 销售目标与预测
  ├─ record/                   字段历史、备注、附件
  ├─ search/                   全局搜索
  ├─ dashboard/                仪表盘统计
  ├─ common/ config/           Result/PageResult、BizException、枚举、安全与调度配置
  └─ DataInitializer.java      演示数据初始化
frontend/                      Vue 3 前端
  └─ src/views/                dashboard、leads、accounts、contacts、opportunities、activities、
                               cases、knowledge、products、pricebooks、quotes、approvals、users、
                               campaigns、contracts、forecast、settings
scripts/smoke.sh               全链路冒烟脚本
```

## 快速开始

环境要求：JDK 8、Maven 3.6+、Node.js 18+。

```bash
# 后端（首次启动自动建表并写入演示数据）
cd backend && mvn spring-boot:run

# 前端
cd frontend && npm install && npm run dev
```

- 前端：`http://localhost:5173`（开发模式下 `/api` 代理到后端）
- 后端：`http://localhost:8080`
- Swagger：`http://localhost:8080/swagger-ui.html`
- H2 控制台：`http://localhost:8080/h2-console`（JDBC URL `jdbc:h2:file:./data/crm`，用户 `sa`，密码为空）

### 默认账号

| 用户名 | 密码 | 角色 |
|---|---|---|
| admin | admin123 | 管理员 |
| manager | 123456 | 销售经理 |
| sales | 123456 | 销售代表 |
| service | 123456 | 服务专员 |

### 切换 MySQL

修改 `backend/src/main/resources/application-mysql.yml` 中的连接信息后：

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

## 配置说明

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `server.port` | 8080 | 后端端口 |
| `crm.upload-dir` | `./data/uploads` | 附件保存目录，单个附件最大 20MB |
| `crm.jwt.secret` | 内置示例值 | JWT 签名密钥，生产环境务必替换 |
| `crm.jwt.expire-hours` | 12 | 登录令牌有效期 |
| `crm.scheduling.enabled` | true | 定时任务开关（过期合同刷新、超期工单升级） |

schema 变更或需要重置演示数据时执行 `rm -rf backend/data` 后重新启动。`test` profile 不执行演示数据初始化，也不启用定时任务。

## API 概览

所有接口位于 `/api` 前缀下，除 `/api/auth/login` 外均需 `Authorization: Bearer <token>`。响应统一为 `{"code":0,"message":"成功","data":...}`，列表接口返回 `PageResult`（`records`/`total`/`page`/`size`），默认按创建时间倒序。

| 前缀 | 说明 |
|---|---|
| `/api/auth`、`/api/users` | 登录、当前用户、用户管理 |
| `/api/leads`、`/api/accounts`、`/api/contacts`、`/api/opportunities`、`/api/activities` | 销售核心对象 |
| `/api/opportunities/{id}/items` | 商机产品行项目 |
| `/api/products`、`/api/pricebooks`、`/api/quotes` | 产品、价格手册、报价 |
| `/api/approval-rules`、`/api/approvals` | 审批规则与审批申请 |
| `/api/cases`、`/api/knowledge` | 工单与知识库 |
| `/api/sla-policies`、`/api/assignment-rules` | SLA 与分派规则 |
| `/api/campaigns`、`/api/contracts` | 市场活动、合同与回款 |
| `/api/sales-targets`、`/api/forecast` | 销售目标与预测 |
| `/api/history`、`/api/notes`、`/api/attachments` | 字段历史、备注、附件 |
| `/api/search` | 全局搜索 |
| `/api/dashboard` | 销售与服务仪表盘 |
| `/api/{leads,accounts,contacts,opportunities,cases,contracts}/export` | CSV 导出 |

完整接口与参数见 Swagger。

## 权限矩阵

| 角色 | 权限范围 |
|---|---|
| ADMIN | 全部模块 |
| SALES_MANAGER | 销售、产品价格、报价审批、市场活动、合同、预测、销售目标、仪表盘；服务与知识只读 |
| SALES_REP | 销售、报价、合同读写；产品价格、市场活动、预测只读；服务与知识只读 |
| SERVICE_AGENT | 工单、知识、活动读写；客户联系人只读；仪表盘 |

## 测试

```bash
# 后端单元/集成测试
cd backend && mvn test

# 前端构建检查
cd frontend && npm run build

# 全链路冒烟（需先启动后端）：登录、线索转化、商机推进、报价审批、工单流转、
# 市场活动、合同回款、预测、搜索、导出
bash scripts/smoke.sh
```

## 后续规划

通知中心、报表中心、多租户、多级审批流程与操作审计。
