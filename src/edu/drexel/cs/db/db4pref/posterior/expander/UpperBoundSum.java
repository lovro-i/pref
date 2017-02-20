package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Preference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Upper bound modifier that sums the non-overlapping remaining preference pairs */
public class UpperBoundSum extends UpperBound {

  public UpperBoundSum(Expander expander) {
    super(expander);
  }

  @Override
  public double getUpperBoundModifier(int t) {
    List<Preference> prefs = new ArrayList<Preference>();
    for (Preference pref: expander.getTransitiveClosure().getPreferences()) {
      int i = expander.getReferenceIndex(pref.higher);
      int j = expander.getReferenceIndex(pref.lower);
      if ((i > j) && (i > t || j > t)) prefs.add(pref);
    }

    prefs.sort((Preference p1, Preference p2) -> {
      int i1 = expander.getReferenceIndex(p1.higher);
      int i2 = expander.getReferenceIndex(p2.higher);
      if (i1 < i2) return -1;
      if (i1 > i2) return 1;
      int j1 = expander.getReferenceIndex(p1.lower);
      int j2 = expander.getReferenceIndex(p2.lower);
      return j1 - j2;
    });
    
    int sum = 0;
    int lim = 0;
    Iterator<Preference> it = prefs.iterator();
    while (it.hasNext()) {
      Preference pref = it.next();
      int j = expander.getReferenceIndex(pref.lower);
      // i > j
      if (j < lim) it.remove();
      else {
        int i = expander.getReferenceIndex(pref.higher);
        sum += i - j;
        lim = i;
      }
    }
//    StringBuilder sb = new StringBuilder();
//    for (Preference p: prefs) {
//      sb.append(p).append(" ");
//    }
//    Logger.info("Sum: %d (%s)", sum, sb);
    return Math.pow(expander.getModel().getPhi(), sum);
  }

}
