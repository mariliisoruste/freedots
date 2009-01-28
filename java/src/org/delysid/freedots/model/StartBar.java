/* -*- c-basic-offset: 2; -*- */
package org.delysid.freedots.model;

public class StartBar extends VerticalEvent {
  public StartBar(Fraction offset) { super(offset); }

  int staffCount;
  public int getStaffCount() { return staffCount; }
  public void setStaffCount(int staffCount) { this.staffCount = staffCount; }

  boolean newSystem = false;
  public boolean getNewSystem() { return newSystem; }
  public void setNewSystem(boolean newSystem) { this.newSystem = newSystem; }

  int endingStart = 0;
  public int getEndingStart() { return endingStart; }
  public void setEndingStart(int endingStart) { this.endingStart = endingStart; }
}
