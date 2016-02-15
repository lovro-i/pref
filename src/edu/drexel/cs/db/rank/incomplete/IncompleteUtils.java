package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;


public class IncompleteUtils {

  public static double getMissingRate(RankingSample sample) {
    ItemSet items = sample.getItemSet();    
    double count = 0;
    double total = 0;    
    for (PW<Ranking> pw: sample) {
      count += pw.w * pw.p.size();
      total += pw.w * items.size();
    }
    return 1d * (total - count) / total;
  }  
  
}
