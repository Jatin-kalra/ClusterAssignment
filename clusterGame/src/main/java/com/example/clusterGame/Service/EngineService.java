package com.example.clusterGame.Service;

import com.example.clusterGame.Model.Request.ContinuePlayRequest;
import com.example.clusterGame.Model.Request.PlayRequest;
import com.example.clusterGame.Model.Response.ContinuePlayResponse;
import com.example.clusterGame.Model.Response.PayStep;
import com.example.clusterGame.Model.Response.PlayResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.util.*;

@Service
public class EngineService {

    @Autowired
    ContinuePlayResponse continuePlayResponse;

    @Autowired
    PlayResponse playResponse;

    final String[] Symbol = {"H1","H2","H3","H4","L5","L6","L7","L8","WR","BL"};
    final int[] PAY_21 ={  10 ,  9 ,  9 ,  7 ,  5 ,  5 ,  5 ,  5 ,  0 ,   0     };
    final int[] PAY_17 ={  8  ,  7 ,  7 ,  6 ,  4 ,  4 ,  4 ,  4 ,  0 ,   0     };
    final int[] PAY_13 ={  7  ,  6 ,  6 ,  5 ,  3 ,  3 ,  3 ,  3 ,  0 ,   0     };
    final int[] PAY_9 = {  6  ,  5 ,  5 ,  4 ,  2 ,  2 ,  2 ,  2 ,  0 ,   0     };
    final int[] PAY_5 = {  5  ,  4 ,  4 ,  3 ,  1 ,  1 ,  1 ,  1 ,  0 ,   0     };
    final int MATRIX_ROW =8;
    final int MATRIX_COL =8;
    final BigDecimal BASE_BET = BigDecimal.valueOf(10);
    final String WILD ="WR";
    final String BLOCKER ="BL";
    final String IN_PROGRESS ="IN_PROGRESS";
    final String DONE ="DONE";
    Boolean[][] boolMatrix;
    BigDecimal spinWin = BigDecimal.ZERO;
    static Random rn = new Random();
    private String[][] createGrid() {
        String[][] matrix = new String[MATRIX_COL][MATRIX_ROW];

        NavigableMap<Integer, String> weights = new TreeMap<>();
        weights.put(0,    "H1");
        weights.put(100,  "H2");
        weights.put(200,  "H3");
        weights.put(300,  "H4");
        weights.put(400,  "L5");
        weights.put(500,  "L6");
        weights.put(600,  "L7");
        weights.put(700,  "L8");
        weights.put(800,  WILD);
        weights.put(900,  BLOCKER);

        int weightRange = 1000;         // Max weight range based on map keys

        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                int rand = rn.nextInt(weightRange);
                matrix[row][col] = weights.floorEntry(rand).getValue();
            }
        }

        return matrix;
    }

    public void createBooleanGrid(String[][] matrix) {
        boolMatrix = new Boolean[MATRIX_COL][MATRIX_ROW];

        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                boolMatrix[row][col] = BLOCKER.equals(matrix[row][col]);
            }
        }
    }

    public void winMethod(String element, int clusterSize) {
        int symbolIndex = -1;

        for (int i = 0; i < Symbol.length; i++) {   // Find index of the symbol
            if (element.equals(Symbol[i])) {
                symbolIndex = i;
                break;
            }
        }

        if (symbolIndex == -1) {  // Symbol not found
            System.out.println("Unknown symbol: " + element);
            return;
        }
        int win;
        if (clusterSize >= 21) win = PAY_21[symbolIndex];
        else if (clusterSize >= 17) win = PAY_17[symbolIndex];
        else if (clusterSize >= 13) win = PAY_13[symbolIndex];
        else if (clusterSize >= 9) win = PAY_9[symbolIndex];
        else win = PAY_5[symbolIndex];

        // Add win to total
        spinWin = spinWin.add(BigDecimal.valueOf(win));

    }

    public void checkNeighbourhood(String[][] matrix, Stack<String> stack, String element, Set<String> burstingPositions) {
        List<String> cluster = new ArrayList<>();
        int clusterSize = 0;

        while (!stack.isEmpty()) {
            String[] parts = stack.pop().split(",");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            if (row < 0 || row >= MATRIX_ROW || col < 0 || col >= MATRIX_COL || boolMatrix[row][col]) continue;
            String symbol = matrix[row][col];
            if (!symbol.equals(element) && !symbol.equals(WILD)) continue;

            boolMatrix[row][col] = true;
            cluster.add(col + "," + row);
            clusterSize++;

            stack.push(row + "," + (col - 1)); // up
            stack.push(row + "," + (col + 1)); // down
            stack.push((row - 1) + "," + col); // left
            stack.push((row + 1) + "," + col); // right
        }
        if (clusterSize > 4) {
            winMethod(element, clusterSize);
            burstingPositions.addAll(cluster);
        }
    }

    public Set<String> findCluster(String[][] matrix) {
        Set<String> burstingPositions = new HashSet<>();

        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                changeWildFromTrueToFalse(matrix); // changing wild from true to false so other clusters can use them in case of a wild is visited by another cluster.

                if (boolMatrix[row][col] || WILD.equals(matrix[row][col])) continue; // Skip already visited cells and wilds as starting points

                Stack<String> stack = new Stack<>();
                stack.push(row + "," + col);
                String element = matrix[row][col];

                checkNeighbourhood(matrix, stack, element, burstingPositions);
            }
        }
        return burstingPositions;
    }

    private void changeWildFromTrueToFalse(String[][] matrix) {
        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                if (WILD.equals(matrix[col][row])) boolMatrix[col][row] = false;
            }
        }
    }

    private void listUpdate(Set<String> burstingPositions, String[][] matrix){
        Set<String> blockerPositions = new HashSet<>();
        for (String x : burstingPositions) {
            int col = Integer.parseInt(x.split(",")[0]);
            int row = Integer.parseInt(x.split(",")[1]);
            if(col>0 && BLOCKER.equals(matrix[row][col - 1])) blockerPositions.add((col-1)+","+row);
            if(col< MATRIX_COL -1 && BLOCKER.equals(matrix[row][col + 1])) blockerPositions.add((col+1)+","+row);
            if(row>0 && BLOCKER.equals(matrix[row - 1][col])) blockerPositions.add(col+","+(row-1));
            if(row< MATRIX_ROW -1 && BLOCKER.equals(matrix[row + 1][col])) blockerPositions.add(col+","+(row+1));
        }
        burstingPositions.addAll(blockerPositions);
    }

    private void clusterBuster(Set<String> burstingPositions, String[][] matrix) {
        for (String pos : burstingPositions) {
            String[] parts = pos.split(",");
            int col = Integer.parseInt(parts[0]);
            int row = Integer.parseInt(parts[1]);

            matrix[row][col] = "__";
            if (col > 0 && BLOCKER.equals(matrix[row][col - 1])) matrix[row][col - 1] = "__";
            if (col < MATRIX_COL - 1 && BLOCKER.equals(matrix[row][col + 1])) matrix[row][col + 1] = "__";
            if (row > 0 && BLOCKER.equals(matrix[row - 1][col])) matrix[row - 1][col] = "__";
            if (row < MATRIX_ROW - 1 && BLOCKER.equals(matrix[row + 1][col])) matrix[row + 1][col] = "__";
        }
    }

    private void dropSymbols(String[][] matrix) {
        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = MATRIX_ROW - 1; row >= 0; row--) {
                if ("__".equals(matrix[row][col])) {
                    for (int k = row - 1; k >= 0; k--) {           // Finding the next non-blank symbol above
                        if (!"__".equals(matrix[k][col])) {
                            matrix[row][col] = matrix[k][col];
                            matrix[k][col] = "__";
                            break;
                        }
                    }
                }
            }
        }
    }

    private void refillBlankPositions(String[][] matrix) {
        NavigableMap<Integer, String> weights = new TreeMap<>();
        weights.put(0,    "H1");
        weights.put(100,  "H2");
        weights.put(200,  "H3");
        weights.put(300,  "H4");
        weights.put(400,  "L5");
        weights.put(500,  "L6");
        weights.put(600,  "L7");
        weights.put(700,  "L8");
        weights.put(800,  WILD);

        int maxWeight = 900;

        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                if ("__".equals(matrix[row][col])) {
                    int rand = rn.nextInt(maxWeight);
                    matrix[row][col] = weights.floorEntry(rand).getValue();
                }
            }
        }
    }

    public void validate(BigDecimal betAmount) throws Exception{
        if (betAmount.compareTo(BASE_BET)!=0) {
            throw new Exception("Invalid bet amount");
        }
    }

    public PayStep responseSetup(String[][] matrix, Set<String> burstingPositions,BigDecimal accumulatedWin){
        PayStep payStep = new PayStep();
        LinkedMultiValueMap<Integer, String> symbolGrid = new LinkedMultiValueMap<>();
        for (int i = 0; i < MATRIX_COL; i++) {
            List<String> column = new ArrayList<>();
            for (int j = 0; j < MATRIX_ROW; j++) {
                column.add(matrix[j][i]);
            }
            symbolGrid.put(i,column);
        }
        payStep.setSymbolGrid(symbolGrid);
        payStep.setClusterPositions(burstingPositions);
        payStep.setWinnings(spinWin.subtract(accumulatedWin));

        return payStep;
    }

    public PlayResponse gameRound(PlayRequest request) throws Exception{
        validate(request.getBetAmount());
        List<PayStep> paySteps = new ArrayList<>();
        spinWin = BigDecimal.ZERO;
        String[][] matrix = createGrid();
        boolean BaseBuster = false;
        createBooleanGrid(matrix);
        Set<String> burstingPositions;
        burstingPositions = findCluster(matrix);
        if(!burstingPositions.isEmpty()) {
            BaseBuster =true;
            listUpdate(burstingPositions,matrix);
        }
        BigDecimal accumulatedWin = BigDecimal.ZERO;
        PayStep initialPayStep = responseSetup(matrix,burstingPositions,accumulatedWin);
        paySteps.add(initialPayStep);
        while(BaseBuster){
            accumulatedWin = spinWin;
            clusterBuster(burstingPositions,matrix);
            dropSymbols(matrix);
            refillBlankPositions(matrix);
            createBooleanGrid(matrix);
            burstingPositions = findCluster(matrix);
            listUpdate(burstingPositions,matrix);
            System.out.println(burstingPositions);
            PayStep cascadePayStep = responseSetup(matrix,burstingPositions,accumulatedWin);
            paySteps.add(cascadePayStep);
            BaseBuster = !burstingPositions.isEmpty();
        }
        playResponse.setPaySteps(paySteps);
        playResponse.setTotalWinnings(spinWin);
        if(spinWin.compareTo(BigDecimal.ZERO)>0)playResponse.setGameState(IN_PROGRESS);
        else playResponse.setGameState(DONE);
        return playResponse;
    }

    public ContinuePlayResponse roundResult(ContinuePlayRequest request) throws Exception {
        if(!request.getGameState().equals(IN_PROGRESS)) throw new Exception("Invalid Game State");
        if(request.isGambleOption()) {
            if(rn.nextInt(2)==1){
                continuePlayResponse.setFeatureWon(true);
                continuePlayResponse.setTotalWinnings(request.getTotalWinnings().multiply(BigDecimal.valueOf(2)));
                continuePlayResponse.setGameState(DONE);
            }else{
                continuePlayResponse.setFeatureWon(false);
                continuePlayResponse.setGameState(DONE);
                continuePlayResponse.setTotalWinnings(BigDecimal.ZERO);}
        }else{
            continuePlayResponse.setFeatureWon(false);
            continuePlayResponse.setGameState(DONE);
            continuePlayResponse.setTotalWinnings(request.getTotalWinnings());
        }
        return continuePlayResponse;
    }
}
