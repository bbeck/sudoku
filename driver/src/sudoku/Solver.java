package sudoku;

/**
 * Interface for a sudoku solver.  The solver will be passed a board in its
 * <code>solve</code> method, and is expected to modify that board into a
 * valid solution.
 */
public interface Solver {
  /**
   * Value of a missing cell.
   */
  static final int MISSING = -1;

  /**
   * Solve the given board.  The inputted integer array should be overwritten
   * with the solution to the sudoku.
   *
   * @param board The sudoku board to solve.
   */
  void solve(int[][] board);
}
