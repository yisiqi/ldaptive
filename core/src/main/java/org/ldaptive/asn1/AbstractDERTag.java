/* See LICENSE for licensing and NOTICE for copyright. */
package org.ldaptive.asn1;

/**
 * Abstract base class for custom DER tag types.
 *
 * @author  Middleware Services
 */
public abstract class AbstractDERTag implements DERTag
{

  /** Tag number. */
  private final int tagNo;

  /** Flag indicating whether value is primitive or constructed. */
  private final boolean constructed;


  /**
   * Creates a new tag with given tag number.
   *
   * @param  number  Tag number.
   * @param  isConstructed  True for constructed tag, false otherwise.
   */
  public AbstractDERTag(final int number, final boolean isConstructed)
  {
    tagNo = number;
    constructed = isConstructed;
  }


  /** {@inheritDoc} */
  @Override
  public int getTagNo()
  {
    return tagNo;
  }


  /** {@inheritDoc} */
  @Override
  public boolean isConstructed()
  {
    return constructed;
  }


  /** {@inheritDoc} */
  @Override
  public int getTagByte()
  {
    return constructed ? tagNo | ASN_CONSTRUCTED : tagNo;
  }


  /** {@inheritDoc} */
  @Override
  public String toString()
  {
    return String.format("%s(%s)", name(), tagNo);
  }
}