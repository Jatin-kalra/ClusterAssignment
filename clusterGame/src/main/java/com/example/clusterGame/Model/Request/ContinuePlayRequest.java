package com.example.clusterGame.Model.Request;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContinuePlayRequest {
    String gameState;
    boolean gambleOption;
    BigDecimal totalWinnings;
}
