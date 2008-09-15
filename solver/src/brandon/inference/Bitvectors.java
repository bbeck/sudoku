package brandon.inference;

/**
 * Helper class to keep track of bitvectors and their factories.
 */
public final class Bitvectors
{
  private static final BitvectorFactory[] FACTORIES = new BitvectorFactory[100];

  public static BitvectorFactory getFactory(int numBits)
  {
    BitvectorFactory factory = FACTORIES[numBits];
    if(factory == null) {
      factory = newFactory(numBits);
      FACTORIES[numBits] = factory;
    }

    return factory;
  }

  private static BitvectorFactory newFactory(int numBits)
  {
    if(numBits < 20) {
      return new PrecomputedBitvectorFactory(numBits);
    }

    if(numBits == 81) {
      return new Bitvector81Factory();
    }

    throw new IllegalArgumentException("invalid number of bits: " + numBits);
  }

  /**
   * Singleton.
   */
  private Bitvectors()
  {
  }
}
