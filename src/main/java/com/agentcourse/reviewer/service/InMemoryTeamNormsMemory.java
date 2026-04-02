package com.agentcourse.reviewer.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 默认团队规范记忆实现。
 */
public class InMemoryTeamNormsMemory implements TeamNormsMemory {

    private final List<NormEntry> norms = new CopyOnWriteArrayList<>();

    @Override
    public void rememberNorm(String normDescription, String example) {
        if (normDescription == null || normDescription.isBlank()) {
            return;
        }
        norms.add(new NormEntry(normDescription, example == null ? "" : example));
    }

    @Override
    public List<String> retrieveRelevantNorms(String codeContext) {
        if (codeContext == null || codeContext.isBlank() || norms.isEmpty()) {
            return List.of();
        }

        Set<String> queryTerms = tokenize(codeContext);
        return norms.stream()
            .map(norm -> new ScoredNorm(norm, score(norm, queryTerms)))
            .filter(scored -> scored.score() > 0)
            .sorted(Comparator.comparingInt(ScoredNorm::score).reversed())
            .limit(5)
            .map(scored -> scored.norm().description())
            .collect(Collectors.toList());
    }

    @Override
    public List<String> dumpAll() {
        return new ArrayList<>(norms.stream().map(NormEntry::description).toList());
    }

    private int score(NormEntry norm, Set<String> queryTerms) {
        String text = (norm.description() + " " + norm.example()).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : queryTerms) {
            if (term.length() >= 3 && text.contains(term)) {
                score++;
            }
        }
        return score;
    }

    private Set<String> tokenize(String input) {
        return List.of(input.toLowerCase(Locale.ROOT).split("[^a-zA-Z0-9_\\u4e00-\\u9fa5]+"))
            .stream()
            .filter(token -> !token.isBlank())
            .collect(Collectors.toSet());
    }

    private record NormEntry(String description, String example) {
    }

    private record ScoredNorm(NormEntry norm, int score) {
    }
}
