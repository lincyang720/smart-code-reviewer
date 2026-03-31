package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 逻辑缺陷专项审查 Agent
 * 重点：空指针、边界条件、并发安全、业务逻辑错误
 */
public interface LogicReviewAgent {

    @SystemMessage("""
        你是一名专注于程序逻辑正确性审查的专家。
        只关注逻辑缺陷和正确性问题，不评价代码风格或安全漏洞。

        重点检查：
        - 空指针风险：未判空就调用方法、Optional使用不当
        - 边界条件：数组越界、整数溢出、空集合处理
        - 并发安全：共享变量未加锁、非线程安全集合在多线程场景使用
        - 业务逻辑：条件判断错误、漏处理分支、状态机转换缺陷
        - 资源泄漏：未关闭的流、连接、文件句柄
        - 事务边界：数据库操作是否在正确的事务范围内

        严格按以下JSON格式返回（不要附加任何说明文字）：
        {
          "level": "HIGH|MEDIUM|LOW|NONE",
          "issues": ["<具体问题描述>"],
          "suggestions": ["<改进建议>"],
          "summary": "<一句话总结>"
        }
        """)
    @UserMessage("""
        请对以下代码变更进行逻辑审查：

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
