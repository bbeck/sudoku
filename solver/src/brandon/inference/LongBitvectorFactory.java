package brandon.inference;

public class LongBitvectorFactory implements BitvectorFactory
{
  private static final BitvectorFactory FACTORY = Bitvectors.getFactory(9);
  private static final int[] OFFSETS = { 0, 9, 18, 27, 36, 45, 54, 63, 72 };

  private final LongBitvector none;
  private final LongBitvector all;

  public LongBitvectorFactory()
  {
    none = new LongBitvector(new Bitvector[] {
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
        FACTORY.getNone(),
    });

    all = new LongBitvector(new Bitvector[] {
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
        FACTORY.getAll(),
    });
  }

  public Bitvector encode(int bit)
  {
    int div = bit / 9;
    int mod = bit % 9;

    Bitvector[] vs = new Bitvector[9];
    vs[0] = (div == 0) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[1] = (div == 1) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[2] = (div == 2) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[3] = (div == 3) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[4] = (div == 4) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[5] = (div == 5) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[6] = (div == 6) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[7] = (div == 7) ? FACTORY.encode(mod) : FACTORY.getNone();
    vs[8] = (div == 8) ? FACTORY.encode(mod) : FACTORY.getNone();

    return new LongBitvector(vs);
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
    return none;
  }

  public Bitvector getAll()
  {
    return all;
  }

  private final class LongBitvector implements Bitvector
  {
    private final Bitvector[] vectors;
    private final int[] bits;
    private final int width;

    public LongBitvector(Bitvector[] vs)
    {
      vectors = vs;

      bits = new int[vs[0].getBitCount() + vs[1].getBitCount() + vs[2].getBitCount() +
          vs[3].getBitCount() + vs[4].getBitCount() + vs[5].getBitCount() +
          vs[6].getBitCount() + vs[7].getBitCount() + vs[8].getBitCount()];

      width = vs[0].getWidth() + vs[1].getWidth() + vs[2].getWidth() +
          vs[3].getWidth() + vs[4].getWidth() + vs[5].getWidth() +
          vs[6].getWidth() + vs[7].getWidth() + vs[8].getWidth();

      int count = 0;
      for(int i = 0; i < vs.length; i++) {
        Bitvector v = vs[i];

        for(int bit : v.getBits()) {
          bits[count++] = OFFSETS[i] + bit;
        }
      }
    }

    public int getWidth()
    {
      return width;
    }

    public int[] getBits()
    {
      return bits;
    }

    public int getBit(int i)
    {
      return bits[i];
    }

    public int getBitCount()
    {
      return bits.length;
    }

    public Bitvector intersect(Bitvector b)
    {
      LongBitvector that = cast(b);
      return new LongBitvector(new Bitvector[] {
          vectors[0].intersect(that.vectors[0]),
          vectors[1].intersect(that.vectors[1]),
          vectors[2].intersect(that.vectors[2]),
          vectors[3].intersect(that.vectors[3]),
          vectors[4].intersect(that.vectors[4]),
          vectors[5].intersect(that.vectors[5]),
          vectors[6].intersect(that.vectors[6]),
          vectors[7].intersect(that.vectors[7]),
          vectors[8].intersect(that.vectors[8]),
      });
    }

    public Bitvector union(Bitvector b)
    {
      LongBitvector that = cast(b);
      return new LongBitvector(new Bitvector[] {
          vectors[0].union(that.vectors[0]),
          vectors[1].union(that.vectors[1]),
          vectors[2].union(that.vectors[2]),
          vectors[3].union(that.vectors[3]),
          vectors[4].union(that.vectors[4]),
          vectors[5].union(that.vectors[5]),
          vectors[6].union(that.vectors[6]),
          vectors[7].union(that.vectors[7]),
          vectors[8].union(that.vectors[8]),
      });
    }

    public Bitvector subtract(Bitvector b)
    {
      LongBitvector that = cast(b);
      return new LongBitvector(new Bitvector[] {
          vectors[0].subtract(that.vectors[0]),
          vectors[1].subtract(that.vectors[1]),
          vectors[2].subtract(that.vectors[2]),
          vectors[3].subtract(that.vectors[3]),
          vectors[4].subtract(that.vectors[4]),
          vectors[5].subtract(that.vectors[5]),
          vectors[6].subtract(that.vectors[6]),
          vectors[7].subtract(that.vectors[7]),
          vectors[8].subtract(that.vectors[8]),
      });
    }

    @Override
    public String toString()
    {
      return vectors[0].toString() + " " +
          vectors[1].toString() + " " +
          vectors[2].toString() + " " +
          vectors[3].toString() + " " +
          vectors[4].toString() + " " +
          vectors[5].toString() + " " +
          vectors[6].toString() + " " +
          vectors[7].toString() + " " +
          vectors[8].toString();
    }

    private LongBitvector cast(Bitvector b)
    {
      assert b instanceof LongBitvector;
      assert getWidth() == b.getWidth();
      return (LongBitvector) b;
    }
  }
}
