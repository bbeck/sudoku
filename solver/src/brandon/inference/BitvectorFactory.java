package brandon.inference;

import java.util.HashMap;
import java.util.Map;

public class BitvectorFactory
{
  private static final Map<Integer, BitvectorFactory> FACTORIES = new HashMap<Integer, BitvectorFactory>();

  public static synchronized BitvectorFactory getInstance(int width)
  {
    BitvectorFactory factory = FACTORIES.get(width);
    if(factory == null) {
      factory = new BitvectorFactory(width);
      FACTORIES.put(width, factory);
    }

    return factory;
  }

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

  private BitvectorFactory(int width)
  {
    // Don't want to consume too much memory with the precomputation caches
    assert width < 20;

    this.width = width;

    bitValues = new HashMap<Integer, Integer>();
    for(int i = 0; i < width; i++) {
      bitValues.put(1 << i, i + 1);
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
    assert 1 <= bit && bit <= width;
    return bitvectors[1 << (bit - 1)];
  }

  /**
   * Encode the given set of bits into a bitvector.
   */
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
    return bitvectors[bitvectors.length-1];
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

    private PrecomputedBitvector cast(Bitvector b)
    {
      assert b instanceof PrecomputedBitvector;
      assert getWidth() == b.getWidth();
      return (PrecomputedBitvector) b;
    }
  }

  public static void main(String... args)
  {
    BitvectorFactory factory = BitvectorFactory.getInstance(9);

    assert factory.getNone().getBitCount() == 0;
    assert factory.getAll().getBitCount() == 9;
    
    for(int i = 1; i <= 9; i++) {
      Bitvector b = factory.encode(i);
      assert b.getWidth() == 9;
      assert b.getBitCount() == 1;
      assert b.getBits()[0] == i;
    }

    for(int i = 1; i <= 9; i++) {
      for(int j = i+1; j <= 9; j++) {
        Bitvector b = factory.encode(new int[] { i, j });
        int[] bits = b.getBits();

        assert b.getWidth() == 9;
        assert b.getBitCount() == 2;
        assert bits.length == 2;
        assert bits[0] == i || bits[1] == i;
        assert bits[0] == j || bits[1] == j;
        assert bits[0] != bits[1];
      }
    }

    Bitvector none = factory.getNone();
    Bitvector all = factory.getAll();
    Bitvector one = factory.encode(1);

    assert none.intersect(none) == none;
    assert none.intersect(all) == none;
    assert none.intersect(one) == none;
    assert all.intersect(none) == none;
    assert all.intersect(all) == all;
    assert all.intersect(one) == one;
    assert one.intersect(none) == none;
    assert one.intersect(all) == one;
    assert one.intersect(one) == one;

    assert none.union(none) == none;
    assert none.union(all) == all;
    assert none.union(one) == one;
    assert all.union(none) == all;
    assert all.union(all) == all;
    assert all.union(one) == all;
    assert one.union(none) == one;
    assert one.union(all) == all;
    assert one.union(one) == one;

    assert none.subtract(none) == none;
    assert none.subtract(all) == none;
    assert none.subtract(one) == none;
    assert all.subtract(none) == all;
    assert all.subtract(all) == none;
    assert all.subtract(one) == factory.encode(new int[] { 2, 3, 4, 5, 6, 7, 8, 9 });
    assert one.subtract(none) == one;
    assert one.subtract(all) == none;
    assert one.subtract(one) == none;


    Bitvector a = BitvectorFactory.getInstance(9).encode(1);
    Bitvector b = BitvectorFactory.getInstance(10).encode(1);
    assert a != b;

    try {
      a.intersect(b);
      assert false;
    } catch(AssertionError e) {
      // expected
    }

    try {
      a.union(b);
      assert false;
    } catch(AssertionError e) {
      // expected
    }

    try {
      a.union(b);
      assert false;
    } catch(AssertionError e) {
      // expected
    }
  }
}
