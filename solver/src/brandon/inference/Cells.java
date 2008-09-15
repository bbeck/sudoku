package brandon.inference;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper class to keep track of cell and group relationships.
 */
public final class Cells
{
  /**
   * Number of possible cell values.
   */
  public static final int N = 9;

  /**
   * Number of cells on a board.
   */
  public static final int NUM_CELLS = N * N;

  /**
   * Number of groups on a board.
   */
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
  private static final Bitvector[] NEIGHBORS = new Bitvector[NUM_CELLS];
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

      int[] bits = new int[neighbors.size()];
      int count = 0;
      for(Integer bit : neighbors) {
        bits[count++] = bit;
      }

      BitvectorFactory factory = Bitvectors.getFactory(NUM_CELLS);
      NEIGHBORS[id] = factory.encode(bits);
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
   * Determine all of the neighbors of a given cell.
   */
  public static Bitvector getNeighbors(int id)
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

  /**
   * Singleton.
   */
  private Cells()
  {
  }
}
