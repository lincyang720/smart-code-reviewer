# 阶段四迭代说明：工程化、测试与部署

## 本阶段目标

将项目从“能跑”提升到“可测试、可部署、可观测”。

## 已完成内容

### 1. 测试补充
新增：
- `TeamNormsMemoryTest`
- `GitHubWebhookServiceTest`

目标：
- 验证记忆模块可根据代码上下文召回历史规范
- 验证 GitHub Webhook 签名校验边界行为

### 2. 监控能力
已集成：
- Spring Boot Actuator
- Prometheus 指标采集
- Grafana 数据源自动配置

已开放端点：
- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

### 3. 容器化部署
新增：
- `Dockerfile`
- `docker-compose.yml`

支持一键启动以下组件：
- app
- postgres
- prometheus
- grafana

### 4. 监控配置文件
新增：
- `monitoring/prometheus.yml`
- `monitoring/grafana/provisioning/datasources/prometheus.yml`
- `monitoring/grafana/provisioning/dashboards/default.yml`

## 本阶段价值

到这里，项目已经具备课程实战展示所需的完整闭环：
1. AI代码审查能力
2. Multi-Agent 架构
3. 记忆与持久化能力
4. Webhook 接入
5. 可观测与容器化部署

## 课程展示建议

演示顺序建议：
1. 手动调用 `/api/review`
2. 展示 Supervisor + 4 Worker 架构图
3. 演示阶段三的历史规范记忆
4. 打开 `/actuator/prometheus` 展示指标
5. 启动 `docker-compose up` 展示工程化能力

## 后续可继续增强
- 用 ChromaDB 替换内存版记忆
- 增加真实 GitHub Diff 拉取与自动评论格式化
- 增加审查结果 dashboard
- 增加安全规则库和 Prompt 模板管理
