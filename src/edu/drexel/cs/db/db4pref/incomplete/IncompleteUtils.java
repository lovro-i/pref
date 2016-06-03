package edu.drexel.cs.db.db4pref.incomplete;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;


public class IncompleteUtils {

  public static double getMissingRate(RankingSample sample) {
    ItemSet items = sample.getItemSet();    
    double count = 0;
    double total = 0;    
    for (PW<Ranking> pw: sample) {
      count += pw.w * pw.p.length();
      total += pw.w * items.size();
    }
    return 1d * (total - count) / total;
  }  
  
}
