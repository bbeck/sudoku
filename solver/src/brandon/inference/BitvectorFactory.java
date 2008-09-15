package brandon.inference;

public interface BitvectorFactory
{
  /**
   * Encode the given bit into a bitvector.
   */
  Bitvector encode(int bit);

  /**
   * Encode the given set of bits into a bitvector.
   */
  Bitvector encode(int[] bits);

  /**
   * Get the none bitvector (no bits set).
   */
  Bitvector getNone();

  /**
   * Get the all bitvector (all bits set).
   */
  Bitvector getAll();
}
