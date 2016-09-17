package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.Arrays;

public class Range {

  public final int low;
  public final int high;

  public Range(Variable var) {
    int low = Integer.MAX_VALUE;
    int high = Integer.MIN_VALUE;
    for (int val : var.getValues()) {
      if (val < 0) continue;
      low = Integer.min(val, low);
      high = Integer.max(val, high);
    }
    this.low = low;
    this.high = high;
  }

  public int size() {
    return high - low + 1;
  }
 

  @Override
  public String toString() {
    return "[" + low + ", " + high + ']';
  }
}
