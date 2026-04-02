package com.agentcourse.reviewer.service;

import java.util.List;

public interface TeamNormsMemory {

    void rememberNorm(String normDescription, String example);

    List<String> retrieveRelevantNorms(String codeContext);

    List<String> dumpAll();
}
