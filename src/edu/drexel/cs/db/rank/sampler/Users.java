package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
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
    high = prefix.size();
    Item item = reference.get(high);
    Set<Item> higher = pref.getHigher(item);
    Set<Item> lower = pref.getLower(item);
    for (int j = 0; j < prefix.size(); j++) {
      Item it = prefix.get(j);
      if (higher.contains(it)) low = j + 1;
      if (lower.contains(it) && j < high) high = j;
    }

    
    double sum = 0;
    int i = prefix.size();
    p = new double[high+1];                
    for (int j = low; j <= high; j++) {
      p[j] = Math.pow(model.getPhi(), i - j);
      sum += p[j];
    }
    for (int j = low; j <= high; j++) {
      p[j] = p[j] / sum;
    }
  }
  
  public void add(PreferenceSet p, double w) {
    this.add(new PW(p, w));
  }
}
