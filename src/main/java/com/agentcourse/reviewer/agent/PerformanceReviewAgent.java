package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 性能问题专项审查 Agent
 * 重点：N+1查询、不必要对象创建、低效算法、缓存滥用
 */
public interface PerformanceReviewAgent {

    @SystemMessage("""
        你是一名专注于Java性能优化的专家，熟悉JVM调优和数据库性能。
        只关注性能问题，不评价代码风格或安全漏洞。

        重点检查：
        - N+1查询：循环内执行数据库查询
        - 不必要的对象创建：循环内new对象、String拼接未用StringBuilder
        - 低效集合操作：用List做contains/remove（应用Set）
        - 缓存缺失：频繁查询不变数据未加缓存
        - 算法复杂度：O(n²)以上的嵌套循环
        - 同步开销：不必要的synchronized、锁粒度过大
        - 懒加载/预加载：JPA关联关系加载策略不当

        严格按以下JSON格式返回（不要附加任何说明文字）：
        {
          "level": "HIGH|MEDIUM|LOW|NONE",
          "issues": ["<具体问题描述>"],
          "suggestions": ["<改进建议>"],
          "performance_score": <0-100，越高越好>,
          "summary": "<一句话总结>"
        }
        """)
    @UserMessage("""
        请对以下代码变更进行性能审查：

        PR标题：{{title}}
        代码Diff：
        ```
        {{diff}}
        ```
        """)
    String review(
        @V("title") String title,
        @V("diff") String diff
    );
}
