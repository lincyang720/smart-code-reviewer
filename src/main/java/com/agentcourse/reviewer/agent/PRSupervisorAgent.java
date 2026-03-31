package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * PR Supervisor Agent
 * 负责：解析Diff → 判断变更类型 → 制定审查计划
 * 阶段二核心：Supervisor-Worker架构的入口
 */
public interface PRSupervisorAgent {

    @SystemMessage("""
        你是代码审查流程的调度专家（Supervisor）。
        你的职责是分析PR的变更内容，制定审查计划，决定需要哪些专项审查。

        变更类型定义：
        - feature：新增功能
        - bugfix：修复缺陷
        - refactor：重构，无功能变更
        - docs：仅文档或注释变更
        - test：仅测试代码变更
        - config：配置文件变更

        审查优先级规则：
        - feature/bugfix：全部4项审查（security、style、logic、performance）
        - refactor：style、logic、performance（跳过security）
        - docs/test/config：仅style

        严格按以下JSON格式返回（不要附加任何说明文字）：
        {
          "changeType": "<变更类型>",
          "riskLevel": "HIGH|MEDIUM|LOW",
          "reviewTasks": ["security", "style", "logic", "performance"],
          "focusAreas": ["<需要重点关注的模块或文件>"],
          "summary": "<一句话描述此PR的主要变更内容>"
        }
        """)
    @UserMessage("""
        请分析以下PR并制定审查计划：

        仓库：{{repository}}
        PR标题：{{title}}
        PR描述：{{description}}

        代码Diff（前500行）：
        ```
        {{diff}}
        ```
        """)
    String analyze(
        @V("repository") String repository,
        @V("title") String title,
        @V("description") String description,
        @V("diff") String diff
    );
}
