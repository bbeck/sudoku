package brandon.inference;

import java.util.Arrays;

public final class Solver implements sudoku.Solver
{
  private static final boolean OUTPUT = false;

  public final void solve(int[][] array)
  {
    Board unsolved = Board.fromArray(array);
    if(OUTPUT) {
      System.out.println("unsolved:");
      System.out.println(unsolved);
      System.out.println();
    }

    Board solved = solve(unsolved);
    if(OUTPUT) {
      System.out.println("solved:");
      System.out.println(solved);
      System.out.println();
    }

    Board.toArray(solved, array);
    if(OUTPUT) {
      System.out.println("array:");
      for(int i = 0; i < Board.N; i++) {
        for(int j = 0; j < Board.N; j++) {
          System.out.print(array[i][j] + " ");
        }
        System.out.println();
      }
      System.out.println();
    }
  }

  private final Board solve(Board board)
  {
    return infer(board);
  }

  private final Board search(Board board)
  {
    int id = board.getCellToSearch();
    if(id == -1) {
      // Nothing left to search, we're done!
      return board;
    }

    int[] values = board.getPossibleValues(id);
    for(int value : values) {
      Board copy = Board.fromBoard(board);
      if(!copy.setValue(id, value)) {
        continue;
      }

      copy = infer(copy);
      if(copy != null) {
        return copy;
      }
    }

    // Nothing worked, backtrack
    return null;
  }

  private final Board infer(Board board)
  {
    boolean simplified;

    do {
      int version = board.getVersion();
      Board other = inferHiddenSingles(board);
      if(other == null) {
        return null;
      }

      simplified = other.getVersion() != version;
    } while(simplified);

    return search(board);
  }

  // Keep a record of which cells have which possible values (only keep one around)
  final int infer_length = 10;
  final int[] infer_possibilities = new int[infer_length];
  final int[] infer_counts = new int[infer_length*Board.NUM_GROUPS];
  private final Board inferHiddenSingles(Board board)
  {
    Arrays.fill(infer_counts, 0);

    // Find all hidden singles (cells that are the only possible place for a given value)
    for(int groupid = 0; groupid < Board.NUM_GROUPS; groupid++) {
      int[] members = Board.getGroupMembers(groupid);
      for(int id : members) {
        int[] values = board.getPossibleValues(id);

        // Only deal with this cell if its value hasn't already been fixed
        if(values.length > 1) {
          for(int value : values) {
            infer_possibilities[value] = id;
            infer_counts[groupid * infer_length + value]++;
          }
        }
      }

      for(int value = 1; value < infer_length; value++) {
        if(infer_counts[groupid * infer_length + value] == 1) {
          if(!board.setValue(infer_possibilities[value], value)) {
            return null;
          }
        }
      }
    }

    return board;
  }
}
