
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 *
 * @author pbharat
 */
public class game {

    public static Integer[][] board;
    public static Integer size;
    public static Integer typesOfFruits;
    public static long time;
    public static Integer depthLimit;
    public static long start;

    public static void main(String[] args) {
        //System.out.println("Started");
        start = getUserTime();
        long startClock = System.currentTimeMillis();
        readInput();
        Integer[] result = minimax(copyBoard(board), true, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
        writeOutput(result[0], result[1]);
        System.out.println("Time taken: " + (getUserTime() - start) + " nanos");
        System.out.println("Clock time taken: " + (System.currentTimeMillis() - startClock) + " millis");

    }

    public static void readInput() {
        String path = "input.txt";
        try (FileReader fileReader = new FileReader(path); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line = null;
            if (null != (line = bufferedReader.readLine())) {
                size = Integer.parseInt(line);
            }
            if (null != (line = bufferedReader.readLine())) {
                typesOfFruits = Integer.parseInt(line);
            }
            if (null != (line = bufferedReader.readLine())) {
                Double remainingTime = Double.parseDouble(line);
                remainingTime *= 1000;
                remainingTime -= 150;
                time = remainingTime.longValue();
            }
            board = new Integer[size][size];
            for (Integer i = 0; i < size && null != (line = bufferedReader.readLine()); i++) {
                for (Integer j = 0; j < size; j++) {
                    board[i][j] = Character.compare('*', line.charAt(j)) == 0 ? -1 : Character.getNumericValue(line.charAt(j));
                }
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public static void writeOutput(Integer row, Integer column) {
        //System.out.println("Row - " + row + " column - " + column);
        ArrayList<String> stars = new ArrayList<>();
        stars.add(cellValue(row, column));
        Integer[][] explored = new Integer[size][size];
        for (Integer[] i : explored) {
            Arrays.fill(i, 0);
        }
        explored[row][column] = 1;
        Integer count = findAdjacentCount(row, column, board, 1, explored, stars);
        Integer[][] result = gravity(board, stars);
        String cell = cellValue(row, column);
        String path = "output.txt";
        try (FileWriter fileWriter = new FileWriter(path); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(cell);
            bufferedWriter.newLine();
            for (Integer i = 0; i < size; i++) {
                StringBuilder r = new StringBuilder();
                for (Integer j = 0; j < size; j++) {
                    String value = "";
                    if (-1 == result[i][j]) {
                        value = "*";
                    } else {
                        value = String.valueOf(result[i][j]);
                    }
                    r.append(value);
                }
                bufferedWriter.write(r.toString());
                bufferedWriter.newLine();
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    }

    public static String cellValue(Integer i, Integer j) {
        return String.valueOf((char) (65 + j)) + (i + 1);
    }

    public static Integer[] boardPositions(String cellValue) {
        Integer[] position = new Integer[2];
        position[1] = cellValue.charAt(0) - 65;
        position[0] = Integer.parseInt(cellValue.substring(1)) - 1;
        return position;
    }

    public static Integer[][] copyBoard(Integer[][] current) {
        Integer[][] copy = new Integer[size][size];
        for (Integer i = 0; i < size; i++) {
            for (Integer j = 0; j < size; j++) {
                copy[i][j] = current[i][j];
            }
        }
        return copy;
    }

    public static Integer[] minimax(Integer[][] board, boolean isMaximum, Integer depth, Integer alpha, Integer beta, Integer score) {
        Integer[] result = new Integer[3];
        ArrayList<Branch> branches = new ArrayList<>();
        ArrayList<Integer> scores = new ArrayList<>();
        Integer[][] explored = new Integer[size][size];
        for (Integer[] row : explored) {
            Arrays.fill(row, 0);
        }
        for (Integer i = 0; i < size; i++) {
            for (Integer j = 0; j < size; j++) {
                if (board[i][j] != -1 && explored[i][j] == 0) {
                    explored[i][j] = 1;
                    String cell = cellValue(i, j);
                    ArrayList<String> branchStars = new ArrayList<>();
                    branchStars.add(cell);
                    Integer value = findAdjacentCount(i, j, board, 1, explored, branchStars);
                    branches.add(new Branch(value, cell, branchStars));
                }
            }
        }
        Collections.sort(branches);  // descending order at maximizer level
        if (!isMaximum) {
            Collections.reverse(branches);  // ascending order at minimizer level
        }

        long spent = (getUserTime() - start) / 1000000;
        if (time < 0 || spent >= time) {
            if (depth == 0) {
                Integer[] position = boardPositions(branches.get(0).getPosition());
                result[0] = position[0];
                result[1] = position[1];
                return result;
            } else {
                result[2] = score;
                return result;
            }
        }

        Integer branchingFactor = branches.size();
        if (depth == 0) {
            //System.out.println("Branching factor - " + branchingFactor);
            if (branchingFactor > 200) {
                depthLimit = 2;
            } else if (branchingFactor <= 200 && branchingFactor > 70) {
                depthLimit = 3;
            } else {
                depthLimit = 4;
            }
        }
        if (branchingFactor == 0 || depth > depthLimit) {
            result[0] = result[1] = -1;
            result[2] = score;
            return result;
        }
        Integer bestBranchIndex = 0;
        Integer bestScore;
        if (isMaximum) {
            bestScore = Integer.MIN_VALUE;
        } else {
            bestScore = Integer.MAX_VALUE;
        }

        for (Branch branch : branches) {
            Integer[][] newBoard = copyBoard(board);
            Integer branchScore = score;
            newBoard = gravity(newBoard, branch.getBranchStars());
            Integer[] child = minimax(newBoard, !isMaximum, depth + 1, alpha, beta, branchScore);
            scores.add(child[2]);
            if (isMaximum) {
                if (bestScore < child[2]) {
                    bestScore = child[2];
                    bestBranchIndex = scores.size() - 1;
                }
                if (alpha < bestScore) {
                    alpha = bestScore;
                }
                if (beta <= alpha) {
                    break;
                }
            } else {
                if (bestScore > child[2]) {
                    bestScore = child[2];
                    bestBranchIndex = scores.size() - 1;
                }
                if (beta > bestScore) {
                    beta = bestScore;
                }
                if (beta <= alpha) {
                    break;
                }
            }
        }
        Integer[] position = boardPositions(branches.get(bestBranchIndex).getPosition());
        result[0] = position[0];
        result[1] = position[1];
        result[2] = bestScore;
        return result;
    }

    public static Integer findAdjacentCount(Integer row, Integer colummn, Integer[][] current, Integer count, Integer[][] explored, ArrayList<String> branch) {
        if (current[row][colummn] == -1) {
            return count;
        }
        if (row < 0 || colummn < 0 || row >= size || colummn >= size) {
            return count;
        }
        Integer value = current[row][colummn];
        String bottom = cellValue(row + 1, colummn);
        if (row + 1 >= 0 && row + 1 < size && explored[row + 1][colummn] != 1 && value == current[row + 1][colummn]) {
            count++;
            explored[row + 1][colummn] = 1;
            branch.add(bottom);
            count = findAdjacentCount(row + 1, colummn, current, count, explored, branch);
        }
        String top = cellValue(row - 1, colummn);
        if (row - 1 >= 0 && row - 1 < size && explored[row - 1][colummn] != 1 && value == current[row - 1][colummn]) {
            count++;
            explored[row - 1][colummn] = 1;
            branch.add(top);
            count = findAdjacentCount(row - 1, colummn, current, count, explored, branch);
        }
        String left = cellValue(row, colummn - 1);
        if (colummn - 1 >= 0 && colummn - 1 < size && explored[row][colummn - 1] != 1 && value == current[row][colummn - 1]) {
            count++;
            explored[row][colummn - 1] = 1;
            branch.add(left);
            count = findAdjacentCount(row, colummn - 1, current, count, explored, branch);
        }
        String right = cellValue(row, colummn + 1);
        if (colummn + 1 >= 0 && colummn + 1 < size && explored[row][colummn + 1] != 1 && value == current[row][colummn + 1]) {
            count++;
            explored[row][colummn + 1] = 1;
            branch.add(right);
            count = findAdjacentCount(row, colummn + 1, current, count, explored, branch);
        }
        return count;
    }

    public static Integer[][] gravity(Integer[][] board, ArrayList<String> stars) {
        if (stars.isEmpty()) {
            return board;
        }
        for (String star : stars) {
            Integer[] position = boardPositions(star);
            board[position[0]][position[1]] = -1;
        }
        for (Integer j = 0; j < size; j++) {
            ArrayList<Integer> fruits = new ArrayList<>();
            for (Integer i = size - 1; i >= 0; i--) {
                if (board[i][j] != -1) {
                    fruits.add(board[i][j]);
                }
            }
            Integer row = size - 1;
            for (Integer value : fruits) {
                board[row][j] = value;
                row--;
            }
            for (Integer i = row; i >= 0; i--) {
                board[i][j] = -1;
            }
        }
        return board;
    }

    public static long getUserTime() {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        return bean.isCurrentThreadCpuTimeSupported()
                ? bean.getCurrentThreadUserTime() : 0L;
    }

}

class Branch implements Comparable<Branch> {

    private Integer count;
    private String position;
    private ArrayList<String> branchStars = new ArrayList<>();

    public Branch() {

    }

    public Branch(Integer count, String postion, ArrayList<String> branchStars) {
        this.count = count;
        this.position = postion;
        this.branchStars = branchStars;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public ArrayList<String> getBranchStars() {
        return branchStars;
    }

    public void setBranchStars(ArrayList<String> branchStars) {
        this.branchStars = branchStars;
    }

    @Override
    public String toString() {
        return "Branch{" + "count=" + count + ", position=" + position + '}';
    }

    @Override
    public int compareTo(Branch another) {
//        return this.count - another.getCount(); //ascending
        return another.getCount() - this.count;
    }

}
