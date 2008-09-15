package brandon.inference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class PrecomputedBitvectorFactory implements BitvectorFactory
{
  /**
   * Width of the bitvectors this factory creates.
   */
  private final int width;

  /**
   * Mapping of the encoding of a bitvector to the bitvector object.
   */
  private final Bitvector[] bitvectors;

  /**
   * Precomputed instruction cache for intersections.
   */
  private final Bitvector[][] intersections;

  /**
   * Precomputed instruction cache for unions.
   */
  private final Bitvector[][] unions;

  /**
   * Precomputed instruction cache for differences.
   */
  private final Bitvector[][] differences;

  /**
   * Mapping of an encoded bitvector with a single bit set, to the value of that
   * set bit.  Note: The bits are 1-based instead of 0-based.  E.g. 0100 -> 3.
   */
  private final Map<Integer, Integer> bitValues;

  PrecomputedBitvectorFactory(int width)
  {
    // Don't want to consume too much memory with the precomputation caches
    assert width < 20;

    this.width = width;

    bitValues = new HashMap<Integer, Integer>();
    for(int i = 0; i < width; i++) {
      bitValues.put(1 << i, i);
    }

    // Number of bitvectors of the specified width
    int n = (1 << width);

    bitvectors = new Bitvector[n];
    for(int i = 0; i < n; i++) {
      int[] bits = calculateBits(i);
      bitvectors[i] = new PrecomputedBitvector(i, bits);
    }

    intersections = new Bitvector[n][n];
    unions = new Bitvector[n][n];
    differences = new Bitvector[n][n];

    for(int i = 0; i < n; i++) {
      for(int j = 0; j < n; j++) {
        intersections[i][j] = bitvectors[i & j];
        unions[i][j] = bitvectors[i | j];
        differences[i][j] = bitvectors[i & ~j];
      }
    }
  }

  /**
   * Encode the given bit into a bitvector.
   */
  public Bitvector encode(int bit)
  {
    assert 0 <= bit && bit < width;
    return bitvectors[1 << bit];
  }

  public Bitvector encode(int[] bits)
  {
    assert bits.length != 0;
    Bitvector union = encode(bits[0]);
    for(int i = 1; i < bits.length; i++) {
      union = union.union(encode(bits[i]));
    }
    return union;
  }

  public Bitvector getNone()
  {
    return bitvectors[0];
  }

  public Bitvector getAll()
  {
    return bitvectors[bitvectors.length - 1];
  }

  /**
   * Caclulate which bits are set in the encoded representation of a bitvector.
   * Note: The bits are represented as 1-based not 0-based.
   */
  private int[] calculateBits(int encoded)
  {
    int[] bits = new int[Integer.bitCount(encoded)];
    for(int i = 0; i < bits.length; i++) {
      int mask = Integer.lowestOneBit(encoded);
      bits[i] = bitValues.get(mask);
      encoded &= ~mask;
    }

    return bits;
  }

  /**
   * Precomputed bitvector class.  Doesn't do any computation of its own, it
   * looks everything up in precomputed caches of results.
   */
  private final class PrecomputedBitvector implements Bitvector
  {
    private final int encoded;
    private final int[] bits;

    public PrecomputedBitvector(int encoded, int[] bits)
    {
      this.encoded = encoded;
      this.bits = bits;
    }

    public int getWidth()
    {
      return width;
    }

    public int[] getBits()
    {
      return bits;
    }

    public int getBitCount()
    {
      return bits.length;
    }

    public Bitvector intersect(Bitvector b)
    {
      return intersections[encoded][cast(b).encoded];
    }

    public Bitvector union(Bitvector b)
    {
      return unions[encoded][cast(b).encoded];
    }

    public Bitvector subtract(Bitvector b)
    {
      return differences[encoded][cast(b).encoded];
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < width; i++) {
        if((encoded & (1 << i)) != 0) {
          sb.append('1');
        } else {
          sb.append('0');
        }
      }

      return sb.toString();
    }

    private PrecomputedBitvector cast(Bitvector b)
    {
      assert b instanceof PrecomputedBitvector;
      assert getWidth() == b.getWidth();
      return (PrecomputedBitvector) b;
    }
  }

  /**
   * Test case.
   */
  public static void main(String[] args)
  {
    PrecomputedBitvectorFactory factory = new PrecomputedBitvectorFactory(5);

    Bitvector all = factory.getAll();
    assert all.getWidth() == 5;
    assert all.getBitCount() == 5;
    assert Arrays.equals(all.getBits(), new int[] { 0, 1, 2, 3, 4 });

    Bitvector none = factory.getNone();
    assert none.getWidth() == 5;
    assert none.getBitCount() == 0;
    assert Arrays.equals(none.getBits(), new int[] {});

    Bitvector zero = factory.encode(0);
    assert zero.getWidth() == 5;
    assert zero.getBitCount() == 1;
    assert Arrays.equals(zero.getBits(), new int[] { 0 });

    Bitvector one = factory.encode(1);
    assert one.getWidth() == 5;
    assert one.getBitCount() == 1;
    assert Arrays.equals(one.getBits(), new int[] { 1 });

    // Intersection
    assert all.intersect(all) == all;
    assert all.intersect(none) == none;
    assert all.intersect(zero) == zero;
    assert all.intersect(one) == one;
    assert none.intersect(all) == none;
    assert none.intersect(none) == none;
    assert none.intersect(zero) == none;
    assert none.intersect(one) == none;
    assert zero.intersect(all) == zero;
    assert zero.intersect(none) == none;
    assert zero.intersect(zero) == zero;
    assert zero.intersect(one) == none;
    assert one.intersect(all) == one;
    assert one.intersect(none) == none;
    assert one.intersect(zero) == none;
    assert one.intersect(one) == one;

    // Union
    assert all.union(all) == all;
    assert all.union(none) == all;
    assert all.union(zero) == all;
    assert all.union(one) == all;
    assert none.union(all) == all;
    assert none.union(none) == none;
    assert none.union(zero) == zero;
    assert none.union(one) == one;
    assert zero.union(all) == all;
    assert zero.union(none) == zero;
    assert zero.union(zero) == zero;
    assert zero.union(one) == factory.encode(new int[] { 0, 1 });
    assert one.union(all) == all;
    assert one.union(none) == one;
    assert one.union(zero) == factory.encode(new int[] { 0, 1 });
    assert one.union(one) == one;

    // Subtraction
    assert all.subtract(all) == none;
    assert all.subtract(none) == all;
    assert all.subtract(zero) == factory.encode(new int[] { 1, 2, 3, 4 });
    assert all.subtract(one) == factory.encode(new int[] { 0, 2, 3, 4 });
    assert none.subtract(all) == none;
    assert none.subtract(none) == none;
    assert none.subtract(zero) == none;
    assert none.subtract(one) == none;
    assert zero.subtract(all) == none;
    assert zero.subtract(none) == zero;
    assert zero.subtract(zero) == none;
    assert zero.subtract(one) == zero;
    assert one.subtract(all) == none;
    assert one.subtract(none) == one;
    assert one.subtract(zero) == one;
    assert one.subtract(one) == none;
  }
}
