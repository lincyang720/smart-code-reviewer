# 阶段三迭代说明：记忆与持久化

## 本阶段目标

为智能代码审查系统补充“跨PR学习能力”和“审查历史沉淀能力”，让系统从一次性审查升级为可持续进化的团队代码规范助手。

## 已完成内容

### 1. 团队规范记忆模块 `TeamNormsMemory`
- 当前实现为**内存版记忆模块**
- 提供两个核心能力：
  - `rememberNorm(normDescription, example)`：记住规范
  - `retrieveRelevantNorms(codeContext)`：根据当前代码上下文检索历史规范
- 实现方式保留了“向量检索接口形态”，后续可以平滑升级为 **ChromaDB / PGVector / Redis Vector**

### 2. 审查历史持久化
新增：
- `ReviewRecord`：审查记录实体
- `ReviewRecordRepository`：JPA 仓库
- `ReviewHistoryService`：负责保存审查结果

存储字段包括：
- repository / prNumber / title
- changeType / riskLevel
- score / passed
- security/style/logic/performance 各维度结论
- suggestionsJson
- retrievedNormsJson
- createdAt

### 3. 多Agent流程集成记忆
在 `MultiAgentReviewService` 中新增了：
1. **审查前检索历史规范**
2. **将规范参考追加到当前审查上下文**
3. **审查后自动保存历史结果**
4. **从本次建议中学习新的团队规范**

这意味着系统已经具备基础的“审查经验积累”能力。

## 当前版本的设计取舍

### 为什么先做内存版，而不是直接上 ChromaDB？
原因有三点：
1. 课程实战需要分阶段递进，先跑通接口和流程
2. 当前重点是让学员理解“记忆模块如何嵌入多Agent架构”
3. 后续阶段接入 ChromaDB 时，只需要替换 `TeamNormsMemory` 的内部实现，不影响业务调用层

也就是说，现在完成的是：
- **架构预埋**
- **接口先行**
- **实现可替换**

## 架构演进

### 阶段二
```text
Supervisor -> Worker Agents -> ReportSynthesisAgent
```

### 阶段三
```text
Supervisor -> 检索团队规范 -> Worker Agents
            -> 汇总报告 -> 保存审查历史
            -> 从结果中学习新规范
```

## 下一阶段（阶段四）
- 编写集成测试
- Docker Compose 启动完整依赖
- 监控指标暴露
- Grafana 面板
- 准备课程演示脚本
