# 智华 CRM

一个面向销售与客户服务团队的轻量 CRM，参考 Salesforce Sales Cloud 与 Service Cloud 的对象模型，覆盖线索、客户、联系人、商机、活动、服务工单与知识库。

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

## 后续规划

报价单、产品与价格手册、审批流、角色权限、通知中心、报表导出与多租户能力。

## 发布包（单 JAR 成品）

把前端生产构建打进后端可执行 JAR，解压即可运行：

```bash
bash scripts/package-release.sh
unzip release/crm-1.0.0.zip
cd crm-1.0.0
./start.sh
```

浏览器访问 `http://127.0.0.1:8091`。默认账号（如启用登录）`admin / admin123`。

十二套系统可同时启动，端口互不冲突：OMS 8081 / WMS 8082 / TMS 8083 / BMS 8084 / SAP 8085 / OA 8086 / SRM 8087 / BOM 8088 / INV 8089 / IR 8090 / CRM 8091 / DMS 8092。

打 GitHub Release：在默认分支合并后执行 `git tag v1.0.0 && git push origin v1.0.0`，Actions 会上传 zip。
