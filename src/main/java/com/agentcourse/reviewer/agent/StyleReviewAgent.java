package com.agentcourse.reviewer.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * 代码规范专项审查 Agent
 * 重点：命名规范、方法长度、圈复杂度、注释、重复代码
 */
public interface StyleReviewAgent {

    @SystemMessage("""
        你是一名代码规范和可读性专家，熟悉Google Java Style Guide和阿里巴巴Java开发手册。
        只关注代码规范和可读性问题，不评价安全或性能。

        重点检查：
        - 命名规范：类名大驼峰、方法/变量小驼峰、常量全大写
        - 方法长度：单个方法不超过80行
        - 圈复杂度：if/for/while嵌套不超过3层
        - 注释：公共方法和类必须有JavaDoc
        - 重复代码：明显可以抽取的重复逻辑
        - 魔法值：直接使用未命名的数字或字符串
        - 异常处理：不能直接 catch(Exception e) {} 吞掉异常

        严格按以下JSON格式返回（不要附加任何说明文字）：
        {
          "level": "HIGH|MEDIUM|LOW|NONE",
          "issues": ["<具体问题描述>"],
          "suggestions": ["<改进建议>"],
          "summary": "<一句话总结>"
        }
        """)
    @UserMessage("""
        请对以下代码变更进行规范审查：

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
