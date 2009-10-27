/* -*- c-basic-offset: 2; -*- */
import org.delysid.freedots.model.AugmentedFraction;
import org.delysid.freedots.model.Fraction;

public class TestAugmentedFraction extends junit.framework.TestCase {
  public void testDotInference() {
    AugmentedFraction af;
    Fraction f;

    f = new Fraction(2, 1);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 0);

    f = new Fraction(3, 1);
    af = new AugmentedFraction(f);
    assertEquals("nominator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 1);

    f = new Fraction(7, 2);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 2);

    f = new Fraction(15, 4);
    af = new AugmentedFraction(new Fraction(15, 4));
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 3);

    f = new Fraction(31, 8);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 2);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 4);


    f = new Fraction(1, 1);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 0);

    f = new Fraction(3, 2);
    af = new AugmentedFraction(f);
    assertEquals("nominator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 1);

    f = new Fraction(7, 4);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 2);

    f = new Fraction(15, 8);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 3);

    f = new Fraction(31, 16);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 1);
    assertEquals("dots of "+f, af.getDots(), 4);


    f = new Fraction(1, 2);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 0);

    f = new Fraction(3, 4);
    af = new AugmentedFraction(f);
    assertEquals("nominator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 1);

    f = new Fraction(7, 8);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 2);

    f = new Fraction(15, 16);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 3);

    f = new Fraction(31, 32);
    af = new AugmentedFraction(f);
    assertEquals("numerator of "+f, af.getNumerator(), 1);
    assertEquals("denominator of "+f, af.getDenominator(), 2);
    assertEquals("dots of "+f, af.getDots(), 4);

    f = new Fraction(1, 4);
    af = new AugmentedFraction(1, 4, 1);
    assertFalse("1/4 != 1/4.", f.equals(af));

    f = new Fraction(1, 24);
    af = new AugmentedFraction(1, 16, 0, 4, 6);
    assertTrue("1/16 (6 in 4) == 1/24", f.equals(af));
    assertEquals("1/16 (6 in 4) at 48 divisions", af.toInteger(48), 8);

    Fraction f1 = new Fraction(4, 1);
    Fraction f2 = new Fraction(4, 1);
    assertEquals("4/1.getLog() == 0", f1.getLog(), 0);
    f2.setFromLog(f2.getLog());
    assertEquals("4/1 == 4/1 -> 0 -> 4/1", f1, f2);
    f1 = new Fraction(1, 16);
    f2 = new Fraction(1, 16);
    assertEquals("1/16.getLog() == 6", f1.getLog(), 6);
    f2.setFromLog(f2.getLog());
    assertEquals("1/16 == 1/16 -> 6 -> 1/16", f1, f2);

    af = new AugmentedFraction(1, 8, 1);
    assertEquals("1/8. log == 5", af.getLog(), 5);
  }
  public void testEquality() {
    AugmentedFraction af1 = new AugmentedFraction(1, 8, 0, 3, 2);
    AugmentedFraction af2 = new AugmentedFraction(1, 8, 0, 3, 2);
    assertTrue("1/8 (3 in 2) == 1/8 (3 in 2)", af1.equals(af2));
  }
}