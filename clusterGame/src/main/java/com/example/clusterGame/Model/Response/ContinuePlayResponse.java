package com.example.clusterGame.Model.Response;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Data
@Component
public class ContinuePlayResponse {
    BigDecimal totalWinnings;
    String gameState;
    boolean featureWon;
}
