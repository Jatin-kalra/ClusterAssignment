package com.example.clusterGame.Model.Response;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Data
@Component
public class PlayResponse {
    BigDecimal totalWinnings;
    String gameState;
    List<PayStep> paySteps;
}
