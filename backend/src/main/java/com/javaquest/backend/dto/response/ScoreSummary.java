package com.javaquest.backend.dto.response;

import java.time.LocalDateTime;

public record ScoreSummary(   QuizResponse quiz,
     Integer scorePercentage,
     String badge,
    LocalDateTime completedAt) {
    
}
