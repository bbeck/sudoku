package sudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Driver
{
  private static final String INPUT_FILENAME = "sudoku-inputs.txt";
  private static final String OUTPUT_FILENAME = "sudoku-outputs.txt";
  private static final int MIN_BOARDS = 1000;

  public static void main(String... args) throws IOException
  {
    if(args.length == 0) {
      System.err.println("Usage: java -jar sudoku.jar <Algorithm> [-numtrials num] [-stable] [-real]");
      System.err.println("  -numtrials will run the specified number of trials");
      System.err.println("  -stable will always run the same trials in the same order");
      System.err.println("  -real will solve every input board");
      System.exit(1);
    }

    String solverClassName = args[0];
    boolean isReal = false;
    boolean isStable = false;
    int numTrials = MIN_BOARDS;

    for(int i = 1; i < args.length; i++) {
      if("-numtrials".equals(args[i])) {
        numTrials = Integer.parseInt(args[++i]);
      } else if("-stable".equals(args[i])) {
        isStable = true;
      } else if("-real".equals(args[i])) {
        isReal = true;
      }
    }

    if(isStable && isReal) {
      System.err.println("-stable and -real cannot be combined");
      System.exit(2);
    }

    Solver solver;
    {
      long loadStart = System.nanoTime();
      try {
        Class<? extends Solver> cls = Class.forName(solverClassName).asSubclass(Solver.class);
        solver = cls.newInstance();
      } catch(Exception e) {
        System.err.printf("Unable to instantiate algorithm: %1$s\n", solverClassName);
        System.exit(3);
        solver = null;  // for the compiler
      }
      long loadEnd = System.nanoTime();

      System.out.printf("Loaded algorithm %1s: %2$f ms\n",
          solver.getClass().getName(), (loadEnd - loadStart) / (1000. * 1000.));
    }

    // Load all of the boards as well as the solutions
    List<int[][]> boards, solutions;
    {
      long loadStart = System.nanoTime();
      boards = load(ClassLoader.getSystemResourceAsStream(INPUT_FILENAME));
      solutions = load(ClassLoader.getSystemResourceAsStream(OUTPUT_FILENAME));
      long loadEnd = System.nanoTime();

      System.out.printf("Loaded %1$d boards: %2$f ms\n",
          boards.size(), (loadEnd - loadStart) / (1000. * 1000.));
      assert boards.size() == solutions.size();
    }

    // Determine the order in which to solve the boards
    int[] order;
    {
      if(isReal) {
        System.out.println("Preparing all boards for solver in random order.");
        List<Integer> indices = new ArrayList<Integer>(boards.size());
        for(int i = 0; i < boards.size(); i++) {
          indices.add(i);
        }
        Collections.shuffle(indices);

        order = new int[boards.size()];
        for(int i = 0; i < boards.size(); i++) {
          order[i] = indices.get(i);
        }
      } else if(isStable) {
        System.out.printf("Preparing %1$d stably ordered boards for solver.\n", numTrials);
        order = new int[numTrials];

        for(int i = 0; i < order.length; i++) {
          order[i] = i;
        }
      } else {
        System.out.printf("Preparing %1$d randomly ordered boards for solver.\n", numTrials);
        order = new int[numTrials];

        Random rnd = new Random();
        Set<Integer> seen = new HashSet<Integer>();
        for(int i = 0; i < order.length; i++) {
          while(true) {
            int r = rnd.nextInt(boards.size());

            if(seen.add(r)) {
              order[i] = r;
              break;
            }
          }
        }
      }
    }

    // Put every board into the actuals list, when solved, they'll be mutated in place
    List<int[][]> actuals = new ArrayList<int[][]>(order.length);
    List<int[][]> expected = new ArrayList<int[][]>(order.length);
    for(int i = 0; i < order.length; i++) {
      actuals.add(boards.get(order[i]));
      expected.add(solutions.get(order[i]));
    }
    System.out.printf("Running solver on %1$d boards.\n", actuals.size());

    // Solve the baords
    int size = actuals.size();
    long solveStart = System.nanoTime();
    for(int i = 0; i < size; i++) {
      solver.solve(actuals.get(i));
    }
    long solveEnd = System.nanoTime();
    System.out.printf("Solved %1$d boards: %2$f ms\n", actuals.size(), (solveEnd - solveStart) / (1000. * 1000.));

    // Verify solutions
    boolean correct = true;
    for(int i = 0; i < size; i++) {
      correct &= checkSolution(actuals.get(i), expected.get(i));
    }

    if(!correct) {
      System.out.flush();
      System.err.flush();
      System.err.println("Incorrect solutions!");
      System.exit(4);
    }
  }

  private static boolean checkSolution(int[][] actual, int[][] expected)
  {
    for(int i = 0; i < actual.length; i++) {
      for(int j = 0; j < actual.length; j++) {
        if(actual[i][j] != expected[i][j]) {
          return false;
        }
      }
    }

    return true;
  }

  private static List<int[][]> load(InputStream in) throws IOException
  {
    List<int[][]> boards = new ArrayList<int[][]>();
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    String line;
    while((line = reader.readLine()) != null) {
      int[][] board = parseBoard(line);
      boards.add(board);
    }

    return boards;
  }

  private static int[][] parseBoard(String line)
  {
    int[][] board = new int[9][9];

    for(int i = 0; i < board.length; i++) {
      for(int j = 0; j < board.length; j++) {
        board[i][j] = parseChar(line.charAt(9 * i + j));
      }
    }
    return board;
  }

  private static int parseChar(char c)
  {
    switch(c) {
      case '1': return 1;
      case '2': return 2;
      case '3': return 3;
      case '4': return 4;
      case '5': return 5;
      case '6': return 6;
      case '7': return 7;
      case '8': return 8;
      case '9': return 9;
    }

    return Solver.MISSING;
  }
}
