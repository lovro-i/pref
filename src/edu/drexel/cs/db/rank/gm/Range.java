package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.util.Logger;
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
  
  @Deprecated
  public Integer[] toArray(boolean dummy) {
    int size = high - low + 1;
    if (dummy) size += 1;
    Integer[] a = new Integer[size];
    int idx = 0;
    for (int i = low; i <= high; i++) {
      a[idx++] = i;
    }

    if (idx < a.length) a[idx++] = -1;
    Logger.info("Range [%d, %d] %s", low, high, Arrays.toString(a));
    return a;
  }

  @Override
  public String toString() {
    return "[" + low + ", " + high + ']';
  }
}
