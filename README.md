# Smart Code Reviewer

一个面向 Java Agent 架构师训练营的智能代码审查实战项目。

## 项目定位

这个项目不是一次性写完的 Demo，而是按照课程节奏逐步迭代出来的工程化样例，覆盖：
- AI 代码审查
- Supervisor + Worker 多 Agent 架构
- 团队规范记忆
- 审查历史持久化
- GitHub Webhook 接入
- Prometheus / Grafana 可观测性
- Docker Compose 部署

## 核心架构

```text
ReviewController
  ├─ /api/review -> MultiAgentReviewService
  └─ /api/webhook/github -> GitHubWebhookService
                                   ├─ GitHubPullRequestService 拉取 PR diff
                                   ├─ MultiAgentReviewService 执行审查
                                   └─ ReviewCommentFormatter 回写 PR 评论
```

### Multi-Agent 流程

```text
PR Diff
  -> PRSupervisorAgent 生成审查计划
  -> 并行执行 Security / Style / Logic / Performance 4 个 Worker Agent
  -> ReportSynthesisAgent 汇总结果
  -> ReviewHistoryService 保存审查历史
  -> TeamNormsMemory 记忆与召回团队规范
```

## 技术栈

- Java 17
- Spring Boot 3.2.5
- LangChain4j
- OpenAI / GPT-4o
- Spring Data JPA
- H2 / PostgreSQL
- GitHub API
- Spring Boot Actuator
- Prometheus + Grafana
- Docker Compose

## 环境变量

最少需要：

```bash
OPENAI_API_KEY=your-key
```

Webhook / GitHub 自动评论相关：

```bash
GITHUB_TOKEN=your-github-token
GITHUB_WEBHOOK_SECRET=your-webhook-secret
```

团队规范记忆默认支持切换到 ChromaDB：

```bash
TEAM_NORMS_PROVIDER=in-memory
# 或 chromadb
CHROMADB_BASE_URL=http://localhost:8000
CHROMADB_COLLECTION=team-norms
```

如果使用 `docker-compose.yml`，默认会启动 ChromaDB，并把应用的 `TEAM_NORMS_PROVIDER` 设为 `chromadb`。

## 本地启动

### 1. 启动应用

```bash
mvn spring-boot:run
```

### 2. 手动调用审查接口

```bash
curl -X POST http://localhost:8080/api/review \
  -H 'Content-Type: application/json' \
  -d '{
    "repository": "demo/repo",
    "prNumber": 1,
    "title": "fix: prevent SQL injection",
    "description": "replace string concatenation with prepared statement",
    "diff": "- String sql = \"SELECT * FROM users WHERE id=\" + id;\n+ PreparedStatement ps = conn.prepareStatement(\"SELECT * FROM users WHERE id=?\");",
    "changeType": "bugfix"
  }'
```

### 3. Webhook 接口

```text
POST /api/webhook/github
```

当前支持的 GitHub PR 动作：
- opened
- synchronize
- reopened

流程：
1. 校验 `X-Hub-Signature-256`
2. 拉取 PR diff
3. 调用多 Agent 审查
4. 生成 Markdown 评论
5. 回写到 GitHub PR

## Docker Compose

```bash
docker-compose up --build
```

包含服务：
- app: 8080
- postgres: 5432
- prometheus: 9090
- grafana: 3000
- chromadb: 8000

## 监控端点

- `/actuator/health`
- `/actuator/info`
- `/actuator/metrics`
- `/actuator/prometheus`

## 课程迭代文档

项目已经配套以下阶段说明：
- `phase-2-iteration-notes.md`
- `phase-3-iteration-notes.md`
- `phase-4-iteration-notes.md`

## 当前限制

- ChromaDB 路径已预留，但默认仍使用内存版 TeamNormsMemory
- PR 评论目前为顶层 Markdown 评论，还没有做到逐行 review comment
- 真实 GitHub 集成依赖有效的 `GITHUB_TOKEN`

## 后续可继续增强

- 完善 ChromaDB 检索与写入策略
- 增加逐文件 / 逐行 PR review comment
- 增加审查结果 dashboard
- 引入更细粒度的规则模板管理
