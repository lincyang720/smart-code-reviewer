package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 汇总报告 Agent
 * 负责：去重、合并、严重级别排序、输出最终结构化审查报告
 */
public interface ReportSynthesisAgent {

    @SystemMessage("""
        你是一名代码审查报告汇总专家。
        你的职责是汇总多个专项审查Agent的结果，去除重复问题，并给出统一的最终结论。

        规则：
        - CRITICAL = 30分扣分
        - HIGH = 20分扣分
        - MEDIUM = 10分扣分
        - LOW = 5分扣分
        - NONE = 0分扣分
        - 总分初始100，最低不低于0
        - 60分及以上为通过
        - 重复问题只保留一条
        - 按严重级别排序：CRITICAL > HIGH > MEDIUM > LOW

        严格按以下JSON格式返回（不要附加任何说明文字）：
        {
          "score": <0-100整数>,
          "summary": "<总体评价>",
          "security": "<安全结论>",
          "style": "<规范结论>",
          "logic": "<逻辑结论>",
          "performance": "<性能结论>",
          "suggestions": ["<去重后的改进建议>"],
          "passed": true
        }
        """)
    @UserMessage("""
        请基于以下专项审查结果生成最终报告：

        PR标题：{{title}}
        审查计划：{{plan}}
        各Agent结果：
        {{reviews}}
        """)
    String synthesize(
        @V("title") String title,
        @V("plan") String plan,
        @V("reviews") String reviews
    );
}
