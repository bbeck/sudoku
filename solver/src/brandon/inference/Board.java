package brandon.inference;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public final class Board
{
  public static final int N = 9;
  public static final int NUM_CELLS = N * N;
  public static final int NUM_GROUPS = N * 3;
  
  /**
   * Mapping of the groups a given cell is in.
   */
  private static final int[][] GROUPS = new int[NUM_CELLS][3];
  static {
    for(int id = 0; id < NUM_CELLS; id++) {
      GROUPS[id][0] = id % 9;
      GROUPS[id][1] = 9 + id / 9;
      GROUPS[id][2] = 18 + 3 * (id / 27) + (id / 3) % 3;
    }
  }

  /**
   * Mapping of all of the neighbors of a given cell.
   */
  private static final int[][] NEIGHBORS = new int[NUM_CELLS][20];
  static {
    for(int id = 0; id < NUM_CELLS; id++) {
      Set<Integer> neighbors = new HashSet<Integer>();
      for(int i = 0; i < GROUPS[id].length; i++) {
        int group = GROUPS[id][i];

        for(int j = 0; j < NUM_CELLS; j++) {
          for(int k = 0; k < GROUPS[j].length; k++) {
            if(GROUPS[j][k] == group) {
              neighbors.add(j);
            }
          }
        }
      }
      neighbors.remove(id);

      Iterator<Integer> iter = neighbors.iterator();
      for(int i = 0; i < 20; i++) {
        NEIGHBORS[id][i] = iter.next();
      }
    }
  }

  /**
   * Mapping of which cells are in a given group.
   */
  private static final int[][] GROUP_CELLS = new int[NUM_GROUPS][N];
  static {
    int[] indices = new int[NUM_GROUPS];
    for(int id = 0; id < NUM_CELLS; id++) {
      for(int groupid : GROUPS[id]) {
        GROUP_CELLS[groupid][indices[groupid]++] = id;
      }
    }
  }

  /**
   * Factory for creating bitvectors.
   */
  private final BitvectorFactory factory;

  /**
   * Possibilities for every cell in the board.
   */
  private final Bitvector[] possibilities;

  /**
   * Version of the board.  Incremented each time the board is mutated.
   */
  private int version;

  private Board(boolean fill)
  {
    factory = BitvectorFactory.getInstance(N);
    possibilities = new Bitvector[NUM_CELLS];

    if(fill) {
      Arrays.fill(possibilities, factory.getAll());
      version = 0;
    }
  }

  public final boolean setValue(int id, int value)
  {
    assert 0 <= id && id < NUM_CELLS : id;
    assert 1 <= value && value <= N : value;

    Bitvector valueMask = factory.encode(value);

    // Check to see if setting the specified value would cause a contradiction
    if(possibilities[id].intersect(valueMask) == factory.getNone()) {
      return false;
    }

    // Set the value of the specified cell
    possibilities[id] = valueMask;

    // Go to each neighboring cell and update their possibility lists,
    // detecting any contradictions
    int[] neighbors = getNeighbors(id);
    for(int i = 0; i < neighbors.length; i++) {
      int neighborId = neighbors[i];
      Bitvector oldPossibilityMask = possibilities[neighborId];
      Bitvector possibilityMask = oldPossibilityMask.subtract(valueMask);

      // Nothing was changed
      if(oldPossibilityMask == possibilityMask) {
        continue;
      }

      // Contradiction
      if(possibilityMask == factory.getNone()) {
        return false;
      }

      possibilities[neighborId] = possibilityMask;

      if(possibilityMask.getBitCount() == 1) {
        if(!setValue(neighborId, possibilityMask.getBits()[0])) {
          return false;
        }
      }
    }

    version++;
    return true;
  }

  public final boolean removePossibilities(int id, int[] values)
  {
    Bitvector possibilityMask = possibilities[id].subtract(factory.encode(values));
    possibilities[id] = possibilityMask;

    if(possibilityMask == factory.getNone()) {
      return false;
    }

    if(possibilityMask.getBitCount() == 1) {
      if(!setValue(id, possibilityMask.getBits()[0])) {
        return false;
      }
    }

    version++;
    return true;
  }

  public final int getCellToSearch()
  {
    int bestCount = Integer.MAX_VALUE;
    int bestId = -1;

    for(int id = 0; id < NUM_CELLS; id++) {
      int count = possibilities[id].getBitCount();
      if(1 < count && count < bestCount) {
        bestCount = count;
        bestId = id;
      }
    }

    return bestId;
  }

  public final int[] getPossibleValues(int id)
  {
    return possibilities[id].getBits();
  }

  public final int getVersion()
  {
    return version;
  }

  public final String toString()
  {
    String rowFormat = " {0} {1} {2} | {3} {4} {5} | {6} {7} {8} ";
    String spacer = "-------------------------------+-------------------------------+-------------------------------";

    String[] formats = new String[N];
    for(int i = 0; i < N; i++) {
      String[] possibilityString = new String[N];

      for(int j = 0; j < N; j++) {
        int[] possibleValues = possibilities[i*N+j].getBits();

        possibilityString[j] = "";

        for(int k = 1; k <= N; k++) {
          if(Arrays.binarySearch(possibleValues, k) >= 0) {
            possibilityString[j] += Integer.toString(k);
          } else {
            possibilityString[j] += ".";
          }
        }
      }

      formats[i] = MessageFormat.format(rowFormat, (Object[]) possibilityString);
    }

    return
        formats[0] + "\n" +
        formats[1] + "\n" +
        formats[2] + "\n" +
        spacer + "\n" +
        formats[3] + "\n" +
        formats[4] + "\n" +
        formats[5] + "\n" +
        spacer + "\n" +
        formats[6] + "\n" +
        formats[7] + "\n" +
        formats[8] + "\n";
  }

  /**
   * Construct a board from an array.
   */
  public static Board fromArray(int[][] array)
  {
    Board board = new Board(true);
    assert array.length == N;

    for(int i = 0; i < N; i++) {
      assert array[i].length == N;

      for(int j = 0; j < N; j++) {
        int value = array[i][j];
        if(value != sudoku.Solver.MISSING) {
          board.setValue(i * N + j, value);
        }
      }
    }

    return board;
  }

  /**
   * Construct a board from another board.
   */
  public static Board fromBoard(Board other)
  {
    Board board = new Board(false);
    board.version = other.version;    
    System.arraycopy(other.possibilities, 0, board.possibilities, 0, NUM_CELLS);

    return board;
  }

  /**
   * Copy the data in the specified board into the provided array.
   */
  public static void toArray(Board board, int[][] array)
  {
    assert array.length == N;

    for(int id = 0; id < NUM_CELLS; id++) {
      assert array[id].length == N;

      int value = board.possibilities[id].getBits()[0];
      array[id/N][id%N] = value;
    }
  }

  /**
   * Determine all of the neighbors of a given cell.
   */
  public static int[] getNeighbors(int id)
  {
    assert 0 <= id && id < NUM_CELLS;
    return NEIGHBORS[id];
  }

  /**
   * Determine all of the members in a given group.
   */
  public static int[] getGroupMembers(int groupid)
  {
    assert 0 <= groupid && groupid < NUM_GROUPS;
    return GROUP_CELLS[groupid];
  }
}
