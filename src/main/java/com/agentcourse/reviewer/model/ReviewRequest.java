package com.agentcourse.reviewer.model;

import lombok.Data;

/**
 * PR 代码审查请求模型
 */
@Data
public class ReviewRequest {

    /** GitHub 仓库全名，如 owner/repo */
    private String repository;

    /** Pull Request 编号 */
    private Integer prNumber;

    /** PR 标题 */
    private String title;

    /** PR 描述 */
    private String description;

    /** 代码 Diff 内容 */
    private String diff;

    /** 变更类型：feature / bugfix / refactor / docs */
    private String changeType;
}
