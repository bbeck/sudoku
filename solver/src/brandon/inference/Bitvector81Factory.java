package brandon.inference;

import java.util.Arrays;

public class Bitvector81Factory implements BitvectorFactory
{
  private static final Bitvector[] SINGLETONS;
  static {
    SINGLETONS = new Bitvector81[81];
    for(int i = 0; i < 81; i++) {
      SINGLETONS[i] = new Bitvector81(i);
    }
  }

  private static final Bitvector NONE;
  static {
    NONE = new Bitvector81();
  }

  private static final Bitvector ALL;
  static {
    Bitvector union = SINGLETONS[0];
    for(int i = 1; i < SINGLETONS.length; i++) {
      union = union.union(SINGLETONS[i]);
    }

    ALL = union;
  }

  public Bitvector encode(int bit)
  {
    return SINGLETONS[bit];
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
    return NONE;
  }

  public Bitvector getAll()
  {
    return ALL;
  }

  private static final class Bitvector81 implements Bitvector
  {
    private static final BitvectorFactory FACTORY = new PrecomputedBitvectorFactory(9);
    private static final int[] OFFSETS = { 0, 9, 18, 27, 36, 45, 54, 63, 72 };

    private final Bitvector[] bvs;

    public Bitvector81()
    {
      this(true);
    }

    public Bitvector81(boolean fill)
    {
      bvs = new Bitvector[9];

      if(fill) {
        Arrays.fill(bvs, FACTORY.getNone());
      }
    }

    public Bitvector81(int bit)
    {
      this(true);
      bvs[bit / 9] = FACTORY.encode(bit % 9);
    }

    public int getBitCount()
    {
      int sum = 0;
      for(int i = 0; i < bvs.length; i++) {
        sum += bvs[i].getBitCount();
      }

      return sum;
    }

    public int[] getBits()
    {
      int count = 0;
      int[] bs = new int[81];

      for(int i = 0; i < bvs.length; i++) {
        int[] bits = bvs[i].getBits();
        int offset = OFFSETS[i];

        for(int j = 0; j < bits.length; j++) {
          bs[count++] = offset + bits[j];
        }
      }

      int[] bits = new int[count];
      System.arraycopy(bs, 0, bits, 0, count);
      return bits;
    }

    public int getWidth()
    {
      return 81;
    }

    public Bitvector intersect(Bitvector b)
    {
      Bitvector81 that = cast(b);
      Bitvector81 newBV = new Bitvector81(false);
      for(int i = 0; i < bvs.length; i++) {
        newBV.bvs[i] = this.bvs[i].intersect(that.bvs[i]);
      }
      return newBV;
    }

    public Bitvector union(Bitvector b)
    {
      Bitvector81 that = cast(b);
      Bitvector81 newBV = new Bitvector81(false);
      for(int i = 0; i < bvs.length; i++) {
        newBV.bvs[i] = this.bvs[i].union(that.bvs[i]);
      }
      return newBV;
    }

    public Bitvector subtract(Bitvector b)
    {
      Bitvector81 that = cast(b);
      Bitvector81 newBV = new Bitvector81(false);
      for(int i = 0; i < bvs.length; i++) {
        newBV.bvs[i] = this.bvs[i].subtract(that.bvs[i]);
      }
      return newBV;
    }

    private Bitvector81 cast(Bitvector b)
    {
      assert b instanceof Bitvector81;
      assert getWidth() == b.getWidth();
      return (Bitvector81) b;
    }

    @Override
    public String toString()
    {
      StringBuilder sb = new StringBuilder();
      for(int i = 0; i < bvs.length; i++) {
        sb.append(bvs[i].toString());
        if(i + 1 < bvs.length) {
          sb.append(' ');
        }
      }
      return sb.toString();
    }
  }
}
