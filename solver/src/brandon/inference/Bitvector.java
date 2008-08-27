package brandon.inference;

import java.util.HashMap;
import java.util.Map;

public final class Bitvector
{
  public static final Bitvector ALL;
  public static final Bitvector NONE;

  /**
   * Width of a bitvector.
   */
  private static final int WIDTH = 9;

  /**
   * All of the possible bitvectors of WIDTH.
   */
  private static final Bitvector[] ALL_BITVECTORS = new Bitvector[1 << WIDTH];

  /**
   * All of the possible intersections of bitvectors.
   */
  private static final Bitvector[][] INTERSECTIONS = new Bitvector[ALL_BITVECTORS.length][ALL_BITVECTORS.length];

  /**
   * All of the possible differences of bitvectors.
   */
  private static final Bitvector[][] DIFFERENCES = new Bitvector[ALL_BITVECTORS.length][ALL_BITVECTORS.length];

  /**
   * Mapping of an encoded bitvector with a single bit set, to the value of the set bit.
   */
  private static final Map<Integer, Integer> BIT_VALUES = new HashMap<Integer, Integer>();

  static {
    for(int i = 0; i < WIDTH; i++) {
      BIT_VALUES.put(1 << i, i + 1);
    }

    for(int i = 0; i < ALL_BITVECTORS.length; i++) {
      ALL_BITVECTORS[i] = new Bitvector(i);
    }

    for(int i = 0; i < ALL_BITVECTORS.length; i++) {
      for(int j = 0; j < ALL_BITVECTORS.length; j++) {
        INTERSECTIONS[i][j] = ALL_BITVECTORS[i & j];
        DIFFERENCES[i][j] = ALL_BITVECTORS[i & ~j];
      }
    }

    NONE = ALL_BITVECTORS[0];
    ALL = ALL_BITVECTORS[ALL_BITVECTORS.length - 1];
  }

  private static Bitvector get(int value)
  {
    assert value < (1 << WIDTH);
    return ALL_BITVECTORS[value];
  }

  public static Bitvector encode(int value)
  {
    assert 1 <= value && value <= WIDTH;
    return ALL_BITVECTORS[1 << (value - 1)];
  }

  private final int encoded;
  private final int[] bits;

  private Bitvector(int encoded)
  {
    this.encoded = encoded;

    bits = new int[Integer.bitCount(encoded)];
    for(int i = 0; i < bits.length; i++) {
      int mask = Integer.lowestOneBit(encoded);
      bits[i] = BIT_VALUES.get(mask);
      encoded &= ~mask;
    }
  }

  public int[] getBits()
  {
    return bits;
  }

  public int getBitCount()
  {
    return bits.length;
  }

  public Bitvector intersect(Bitvector that)
  {
    return INTERSECTIONS[encoded][that.encoded];
  }

  public Bitvector subtract(Bitvector that)
  {
    return DIFFERENCES[encoded][that.encoded];
  }

  public static void main(String[] args)
  {
    Bitvector zero = Bitvector.get(0);
    assert zero.getBitCount() == 0 : zero.getBitCount();

    Bitvector one = Bitvector.get(1);
    assert one.getBitCount() == 1 : one.getBitCount();
    assert one.getBits()[0] == 1 : one.getBits()[0];

    Bitvector ff = Bitvector.get(0xff);
    assert ff.getBitCount() == 8 : ff.getBitCount();
    for(int i = 0; i < 8; i++) {
      assert ff.getBits()[i] == i + 1 : ff.getBits()[i];
    }

    // Intersect
    assert zero.intersect(zero) == zero;
    assert zero.intersect(one) == zero;
    assert zero.intersect(ff) == zero;
    assert one.intersect(one) == one;
    assert one.intersect(ff) == one;
    assert ff.intersect(ff) == ff;

    // Subtract
    assert zero.subtract(zero) == zero;
    assert zero.subtract(one) == zero;
    assert zero.subtract(ff) == zero;
    assert one.subtract(zero) == one;
    assert one.subtract(one) == zero;
    assert one.subtract(ff) == zero;
    assert ff.subtract(zero) == ff;
    assert ff.subtract(one) == Bitvector.get(0xfe);
    assert ff.subtract(ff) == zero;

    // Encode
    assert Bitvector.encode(1).getBits()[0] == 1;
    assert Bitvector.encode(2).getBits()[0] == 2;
    assert Bitvector.encode(3).getBits()[0] == 3;
    assert Bitvector.encode(4).getBits()[0] == 4;
    assert Bitvector.encode(5).getBits()[0] == 5;
    assert Bitvector.encode(6).getBits()[0] == 6;
    assert Bitvector.encode(7).getBits()[0] == 7;
    assert Bitvector.encode(8).getBits()[0] == 8;
    assert Bitvector.encode(9).getBits()[0] == 9;
  }
}