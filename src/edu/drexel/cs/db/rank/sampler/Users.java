package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;


public class Users extends ArrayList<PW> {

  public static MallowsModel model;
  
  final Ranking prefix;
  final PreferenceSet pref;
  final PreferenceConstraint cons;
  int low;
  int high;
  final double[] p;
  
  Users(Ranking prefix, PreferenceSet pref, PreferenceConstraint cons, Ranking reference) {
    this.prefix = prefix;
    this.pref = pref;
    this.cons = cons;
    
    low = 0;
    high = prefix.length();
//    Item item = reference.get(high);
//    Set<Item> higher = pref.getHigher(item);
//    Set<Item> lower = pref.getLower(item);
//    for (int j = 0; j < prefix.size(); j++) {
//      Item it = prefix.get(j);
//      if (higher.contains(it)) low = j + 1;
//      if (j < high && lower.contains(it)) high = j;
//    }

    ItemSet items = prefix.getItemSet();
    for (int id = 0; id < cons.size(); id++) {
      Boolean b = cons.get(id);
      if (b == null) continue;
      Item item = items.getItemById(id);
      int i = prefix.indexOf(item);
      if (b) low = Math.max(low, i+1);
      else high = Math.min(high, i);
    }

    if (low > high) {
      Logger.info("Low and High for %s %s: %d, %d\n%s", prefix, cons, low, high, pref);
      try { System.in.read(); } catch (IOException ex) { }
    }
    
    double sum = 0;
    int i = prefix.length();
    p = new double[high+1];                
    for (int j = low; j <= high; j++) {
      p[j] = Math.pow(model.getPhi(), i - j);
      sum += p[j];
    }
    for (int j = low; j <= high; j++) {
      p[j] = p[j] / sum;
    }
  }
  
}
