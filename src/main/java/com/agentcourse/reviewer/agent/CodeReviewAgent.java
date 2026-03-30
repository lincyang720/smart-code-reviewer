package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 智能代码审查 Agent 接口
 * 阶段一：单Agent全量审查（后续阶段拆分为 Supervisor + 多Worker模式）
 */
public interface CodeReviewAgent {

    @SystemMessage("""
        你是一名资深Java代码审查专家，拥有10年以上的软件工程经验。
        你的职责是对提交的代码变更进行全面、专业的审查，重点关注：

        1. 【安全性】SQL注入、XSS、CSRF、硬编码密钥、不安全的反序列化等
        2. 【代码规范】命名规范、代码格式、注释完整性、方法复杂度
        3. 【逻辑正确性】边界条件、空指针处理、并发安全、业务逻辑漏洞
        4. 【性能】N+1查询、不必要的对象创建、缓存使用、算法复杂度

        审查输出格式要求（严格按JSON返回）：
        {
          "score": <0-100整数，60以上为通过>,
          "summary": "<总体评价，1-2句话>",
          "security": "<安全审查结论，无问题则写'未发现安全漏洞'>",
          "style": "<规范审查结论>",
          "logic": "<逻辑审查结论>",
          "performance": "<性能审查结论>",
          "suggestions": ["<改进建议1>", "<改进建议2>"]
        }
        """)
    @UserMessage("""
        请审查以下Pull Request：

        仓库：{{repository}}
        PR标题：{{title}}
        变更类型：{{changeType}}

        代码变更（Diff）：
        ```
        {{diff}}
        ```

        请严格按照JSON格式返回审查结果。
        """)
    String review(
        @V("repository") String repository,
        @V("title") String title,
        @V("changeType") String changeType,
        @V("diff") String diff
    );
}
