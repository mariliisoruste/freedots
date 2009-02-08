/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

import org.delysid.freedots.Braille;
import org.delysid.freedots.Util;

public final class AugmentedFraction extends Fraction {
  private int dots;

  private int normalNotes, actualNotes;

  public AugmentedFraction(int numerator, int denominator, int dots) {
    this(numerator, denominator, dots, 1, 1);
  }
  public AugmentedFraction(int numerator, int denominator, int dots,
                           int normalNotes, int actualNotes) {
    super(numerator, denominator);
    simplify();
    this.dots = dots;
    this.normalNotes = normalNotes;
    this.actualNotes = actualNotes;    
  }
  public AugmentedFraction(Fraction duration) {
    this(duration.getNumerator(), duration.getDenominator(), 0);
    if (denominator == 2 || denominator == 4 || denominator == 8 ||
        denominator == 16 || denominator == 32 || denominator == 64 ||
        denominator == 128 || denominator == 256) {
      for (int dot = 10; dot > 0; dot--) {
        if (numerator == (int)(Math.pow(2, dot+1)-1)) {
          numerator -= 1;
          dots += 1;
          simplify();
        }
      }
    }
    if (denominator == 1) {
      if (numerator == 3) {
        numerator = 2;
        dots += 1;
      } else if (numerator == 6) {
        numerator = 4;
        dots += 1;
      } else if (numerator == 7) {
        numerator = 4;
        dots += 2;
      }
    }
  }
  public int getDots() { return dots; }

  @Override
  public float toFloat() {
    final float undottedValue = super.toFloat();
    float rest = undottedValue;
    for (int dot = 0; dot < dots; dot++) rest /= 2.;
    return (((undottedValue * 2) - rest) * (float)normalNotes) / (float)actualNotes;    
  }

  @Override
  public Fraction basicFraction() {
    Fraction undotted = new Fraction(numerator, denominator);
    Fraction rest = new Fraction(numerator, denominator);
    for (int i = 0; i < dots; i++) {
      rest = rest.divide(new Fraction(2, 1));
    }
    Fraction basicFraction = undotted.multiply(new Fraction(2, 1)).subtract(rest).multiply(new Fraction(normalNotes, 1)).divide(new Fraction(actualNotes, 1));
    return basicFraction;
  }

  public String toBrailleString(AbstractPitch pitch) {
    String braille = "";
    int log = Util.log2(denominator);
    if (pitch != null) {
      int[] stepDots = { 145, 15, 124, 1245, 125, 24, 245 };
      int[] denomDots = { 36, 3, 6, 0 };
      braille += Braille.unicodeBraille(
                   Braille.dotsToBits(stepDots[pitch.getStep()])
                 | Braille.dotsToBits(denomDots[log > 3? log-4: log]));
    } else { /* Rest */
      int[] restDots = { 134, 136, 1236, 1346 };
      braille += Braille.unicodeBraille(Braille.dotsToBits(restDots[log > 3? log-4: log]));
    }

    for (int dot = 0; dot < dots; dot++) braille += Braille.dot;

    return braille;
  }
}
