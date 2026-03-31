# 阶段二迭代说明：Multi-Agent 架构落地

## 本阶段目标

将阶段一的单Agent代码审查，升级为 Supervisor + Worker 的多Agent协作架构。

## 已完成内容

### 1. 新增 4 个专项 Worker Agent
- `SecurityReviewAgent`：专注 SQL注入、XSS、硬编码密钥等安全问题
- `StyleReviewAgent`：专注命名规范、方法长度、魔法值等代码规范问题
- `LogicReviewAgent`：专注空指针、边界条件、并发安全等逻辑问题
- `PerformanceReviewAgent`：专注 N+1 查询、低效算法、不必要对象创建等性能问题

### 2. 新增 `PRSupervisorAgent`
负责分析 PR Diff、识别变更类型（feature / bugfix / refactor / docs / test / config），并生成审查计划。

### 3. 新增 `MultiAgentReviewService`
使用 `CompletableFuture` 并行调度 Worker Agent，汇总所有专项结果。

### 4. 新增 `ReportSynthesisAgent`
负责统一汇总 4 个 Agent 的结果，去重问题并输出最终审查报告。

### 5. GitHub Webhook 升级
- 增加 `X-Hub-Signature-256` HMAC-SHA256 签名校验
- 增加异步处理骨架 `GitHubWebhookService`
- 增加 PR comment 回写占位能力

## 当前项目结构新增文件

- `agent/PRSupervisorAgent.java`
- `agent/SecurityReviewAgent.java`
- `agent/StyleReviewAgent.java`
- `agent/LogicReviewAgent.java`
- `agent/PerformanceReviewAgent.java`
- `agent/ReportSynthesisAgent.java`
- `model/ReviewPlan.java`
- `model/AgentReviewResult.java`
- `service/MultiAgentReviewService.java`
- `service/GitHubWebhookService.java`
- `config/AsyncConfig.java`

## 架构演进说明

### 阶段一
```text
Controller -> CodeReviewService -> 单个 CodeReviewAgent -> ReviewResult
```

### 阶段二
```text
Controller -> MultiAgentReviewService
              -> PRSupervisorAgent -> ReviewPlan
              -> 并行调度 4 个 Worker Agent
              -> ReportSynthesisAgent -> ReviewResult
```

## 下一步（阶段三）
- 集成 ChromaDB
- 实现团队规范记忆检索
- 审查历史写入 PostgreSQL
- 基于历史审查结果做规范学习
