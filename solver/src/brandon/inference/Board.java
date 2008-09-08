package brandon.inference;

import java.text.MessageFormat;
import java.util.Arrays;

/**
 * Representation of a sudoku board.
 */
public final class Board
{
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
    factory = BitvectorFactory.getInstance(Cells.N);
    possibilities = new Bitvector[Cells.NUM_CELLS];

    if(fill) {
      Arrays.fill(possibilities, factory.getAll());
      version = 0;
    }
  }

  public final boolean setValue(int id, int value)
  {
    assert 0 <= id && id < Cells.NUM_CELLS : id;
    assert 1 <= value && value <= Cells.N : value;

    Bitvector valueMask = factory.encode(value);

    // Check to see if setting the specified value would cause a contradiction
    if(possibilities[id].intersect(valueMask) == factory.getNone()) {
      return false;
    }

    // Set the value of the specified cell
    possibilities[id] = valueMask;

    // Go to each neighboring cell and update their possibility lists,
    // detecting any contradictions
    int[] neighbors = Cells.getNeighbors(id);
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

    if(possibilityMask == factory.getNone()) {
      return false;
    }

    possibilities[id] = possibilityMask;
    
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

    for(int id = 0; id < Cells.NUM_CELLS; id++) {
      int count = possibilities[id].getBitCount();
      if(1 < count && count < bestCount) {
        bestCount = count;
        bestId = id;

        if(bestCount == 2) {
          break;
        }
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

    String[] formats = new String[Cells.N];
    for(int i = 0; i < Cells.N; i++) {
      String[] possibilityString = new String[Cells.N];

      for(int j = 0; j < Cells.N; j++) {
        int[] possibleValues = possibilities[i* Cells.N +j].getBits();

        possibilityString[j] = "";

        for(int k = 1; k <= Cells.N; k++) {
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
    assert array.length == Cells.N;

    for(int i = 0; i < Cells.N; i++) {
      assert array[i].length == Cells.N;

      for(int j = 0; j < Cells.N; j++) {
        int value = array[i][j];
        if(value != sudoku.Solver.MISSING) {
          board.setValue(i * Cells.N + j, value);
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
    System.arraycopy(other.possibilities, 0, board.possibilities, 0, Cells.NUM_CELLS);
    // TODO(bbeck): Copy other.index into board.index.
    
    return board;
  }

  /**
   * Copy the data in the specified board into the provided array.
   */
  public static void toArray(Board board, int[][] array)
  {
    assert array.length == Cells.N;

    for(int id = 0; id < Cells.NUM_CELLS; id++) {
      assert array[id].length == Cells.N;

      int value = board.possibilities[id].getBits()[0];
      array[id/ Cells.N][id% Cells.N] = value;
    }
  }
}
