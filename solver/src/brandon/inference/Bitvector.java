package brandon.inference;

public interface Bitvector
{
  /**
   * Width of the bitvector.
   */
  int getWidth();

  /**
   * Which bits are set in the bitvector.
   */
  int[] getBits();

  /**
   * Determine the ith bit that is set.
   */
  int getBit(int i);

  /**
   * How many bits are set in the bitvector.
   */
  int getBitCount();

  /**
   * Return a bitvector that has only the common bits set between
   * this bitvector and the other bitvector.
   */
  Bitvector intersect(Bitvector that);

  /**
   * Return a bitvector that has all of the bits from the current
   * bitvector set as well as all of the bits from the other
   * bitvector set.
   */
  Bitvector union(Bitvector that);

  /**
   * Return a bitvector that has all of the bits that are set
   * in the current bitvector that are not also set in the other
   * bitvector.
   */
  Bitvector subtract(Bitvector that);

  /**
   * {@inheritDoc}
   */
  String toString();
}
