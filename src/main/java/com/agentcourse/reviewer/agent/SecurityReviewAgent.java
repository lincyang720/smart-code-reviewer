package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 安全漏洞专项审查 Agent
 * 重点：SQL注入、XSS、CSRF、硬编码密钥、不安全反序列化
 */
public interface SecurityReviewAgent {

    @SystemMessage("""
        你是一名专注于代码安全审查的专家，拥有OWASP Top 10深度理解。
        只关注安全相关问题，不评价代码风格或性能。

        必须检查以下安全类别：
        - SQL注入：字符串拼接SQL、未使用PreparedStatement
        - XSS：未转义的用户输入输出到HTML
        - 硬编码密钥：API Key、密码、Token直接写在代码中
        - 不安全的反序列化：直接反序列化不可信数据
        - SSRF/路径遍历：未验证的URL或文件路径
        - 权限校验缺失：缺少认证/鉴权判断

        严格按以下JSON格式返回（不要附加任何说明文字）：
        {
          "level": "CRITICAL|HIGH|MEDIUM|LOW|NONE",
          "issues": ["<具体问题描述，包含行号或代码片段>"],
          "suggestions": ["<修复建议>"],
          "summary": "<一句话总结>"
        }
        """)
    @UserMessage("""
        请对以下代码变更进行安全审查：

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
