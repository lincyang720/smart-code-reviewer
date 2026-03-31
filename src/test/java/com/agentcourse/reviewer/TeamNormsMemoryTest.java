package com.agentcourse.reviewer;

import com.agentcourse.reviewer.service.TeamNormsMemory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeamNormsMemoryTest {

    @Test
    void shouldRetrieveRelevantNormsByCodeContext() {
        TeamNormsMemory memory = new TeamNormsMemory();
        memory.rememberNorm("查询数据库时必须使用 PreparedStatement，禁止字符串拼接 SQL", "SELECT * FROM users WHERE id=" + " + id");
        memory.rememberNorm("Controller 层禁止直接操作 Repository", "controller -> repository");

        List<String> result = memory.retrieveRelevantNorms("String sql = \"SELECT * FROM users WHERE id=\" + id;");

        assertThat(result).isNotEmpty();
        assertThat(result.get(0)).contains("PreparedStatement");
    }
}
