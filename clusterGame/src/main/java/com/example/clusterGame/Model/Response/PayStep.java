package com.example.clusterGame.Model.Response;

import lombok.Data;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.util.Set;

@Data
public class PayStep {

    LinkedMultiValueMap<Integer, String> symbolGrid;
    Set<String> clusterPositions;
    BigDecimal winnings;
}
