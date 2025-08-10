/*
This is the simulation code as per given game data
please update simulationCycle & printCycle variables as per your requirements
*/



import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public class ClusterSimulation {
    static String[] Symbol = {"H1","H2","H3","H4","L5","L6","L7","L8","WR","BLOCKER"};
    //       "           {"H1","H2","H3","H4","L5","L6","L7","L8","WR","BLOCKER"}

    static int[] PAY_21 ={  10 ,  9 ,  9 ,  7 ,  5 ,  5 ,  5 ,  5 ,  0 ,   0     };
    static int[] PAY_17 ={  8  ,  7 ,  7 ,  6 ,  4 ,  4 ,  4 ,  4 ,  0 ,   0     };
    static int[] PAY_13 ={  7  ,  6 ,  6 ,  5 ,  3 ,  3 ,  3 ,  3 ,  0 ,   0     };
    static int[] PAY_9 = {  6  ,  5 ,  5 ,  4 ,  2 ,  2 ,  2 ,  2 ,  0 ,   0     };
    static int[] PAY_5 = {  5  ,  4 ,  4 ,  3 ,  1 ,  1 ,  1 ,  1 ,  0 ,   0     };
    static int MATRIX_ROW =8;
    static int MATRIX_COL =8;
    static BigDecimal totalBet = BigDecimal.ZERO;
    static BigDecimal BASE_BET = BigDecimal.valueOf(10);
    static BigDecimal spinWin ;
    static BigDecimal totalWin  = BigDecimal.ZERO;
    static final String WILD ="WR";
    static final String BLOCKER ="BLOCKER";
    static final int simulationCycle =100_000_000;
    static final int printCycle =1000_000;
    static Boolean[][] boolMatrix;
    static Random rn = new Random();

    private static String[][] createGrid() {
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

    public static void createBooleanGrid(String[][] matrix) {
        boolMatrix = new Boolean[MATRIX_COL][MATRIX_ROW];

        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                boolMatrix[row][col] = BLOCKER.equals(matrix[row][col]);
            }
        }
    }

    public static void winMethod(String element, int clusterSize) {
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

    public static void checkNeighbourhood(String[][] matrix, Stack<String> stack, String element, Set<String> burstingPositions) {
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

    public static Set<String> findCluster(String[][] matrix) {
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

    private static void changeWildFromTrueToFalse(String[][] matrix) {
        for (int col = 0; col < MATRIX_COL; col++) {
            for (int row = 0; row < MATRIX_ROW; row++) {
                if (WILD.equals(matrix[col][row])) boolMatrix[col][row] = false;
            }
        }
    }

    private static void clusterBuster(Set<String> burstingPositions, String[][] matrix) {
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

    private static void dropSymbols(String[][] matrix) {
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

    private static void refillBlankPositions(String[][] matrix) {
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

    public static void print_Matrix(String[][] arr) {  //---------PRINT MATRIX
        for (String[] strings : arr) {
            for (String string : strings) System.out.print(string + "      ");
            System.out.println("     ");
        }
    }

    public static void main(String [] args){
        for (int cycle = 1; cycle <= simulationCycle; cycle++) {
            spinWin = BigDecimal.ZERO;
            totalBet = totalBet.add(BASE_BET);
            String[][] matrix = createGrid();
            createBooleanGrid(matrix);
            Set<String> burstingPositions = findCluster(matrix);        //Find all Clusters in Grid & award win respectively
            boolean hasCluster = !burstingPositions.isEmpty();
            while(hasCluster){
                clusterBuster(burstingPositions,matrix); // bursting all winning positions along with BLOCKER positions
                dropSymbols(matrix); //dropping symbols present above onto blank positions
                refillBlankPositions(matrix); // refilling Grid with new symbols using different weights
                createBooleanGrid(matrix);
                burstingPositions = findCluster(matrix);
                hasCluster = !burstingPositions.isEmpty();
            }
            totalWin = totalWin.add(spinWin);
            BigDecimal totalRTP = totalWin.divide(totalBet, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));

            if(cycle%printCycle==0){
                System.out.println("Cycle # "+cycle+"  |  Total RTP : "+totalRTP);
            }
        }
    }
}

/*Cycle # 100000000  |  Total RTP: 58.82%*/
