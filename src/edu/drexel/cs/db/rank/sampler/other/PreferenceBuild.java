package edu.drexel.cs.db.rank.sampler.other;

import edu.drexel.cs.db.rank.sampler.other.PreferenceConstraint;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.HashSet;
import java.util.Set;


public class PreferenceBuild {

  private final PreferenceSet pref;
  private final Ranking prefix;
  private final Ranking reference;
  private final PreferenceConstraint constraint;
  
  public PreferenceBuild(PreferenceSet pref, Ranking prefix, Ranking reference) {
    this.pref = pref;
    this.prefix = prefix;
    this.reference = reference;
    if (prefix.length() < reference.length()) this.constraint = new PreferenceConstraint(pref, reference, prefix.length());
    else constraint = null;
  }
  
  public Ranking getPrefix() {
    return prefix;
  }
  
  public PreferenceSet getPreferenceSet() {
    return pref;
  }
  
  public Ranking getReference() {
    return reference;
  }
  
  public boolean isCompatible(PreferenceSet p) {
    PreferenceConstraint pc = new PreferenceConstraint(p, reference, prefix.length());
    if (!pc.equals(constraint)) return false;
    Ranking projection = p.project(prefix.getItems());
    return prefix.equals(projection);
  }
  
  /** Does the parameter PreferenceSet have the same history as this build */
  public boolean isSupport(PreferenceSet p) {
    Set<Item> items = new HashSet<Item>(prefix.getItems());
    items.add(reference.get(prefix.length()));
    Ranking projection = p.project(items);
    if (projection == null || projection.length() < items.size()) return false;
    
    Ranking p2 = projection.project(prefix.getItems());
    return prefix.equals(p2);
  }
  
  /** Does the parameter PreferenceSet have the same history as this build, and is the next item between low and high (inclusive) */
  public boolean isSupport(PreferenceSet p, int low, int high) {
    Set<Item> items = new HashSet<Item>(prefix.getItems());
    Item nextItem = reference.get(prefix.length());
    items.add(nextItem);
    Ranking projection = p.project(items);
    if (projection == null || projection.length() < items.size()) return false;
    
    int index = projection.indexOf(nextItem);
    if (index < low || index > high) return false;
    
    Ranking p2 = projection.project(prefix.getItems());
    return prefix.equals(p2);
  }
  
  /** If the given PreferenceSet can be used as support for this build, what is its insertion index. If cannot be used, returns -1 */
  public int getInsertIndex(PreferenceSet p) {
    Set<Item> items = new HashSet<Item>(prefix.getItems());
    Item nextItem = reference.get(prefix.length());
    items.add(nextItem);
    Ranking projection = p.project(items);
    if (projection == null || projection.length() < items.size()) return -1;

    Ranking p2 = projection.project(prefix.getItems());
    if (!prefix.equals(p2)) return -1;    
    return projection.indexOf(nextItem);    
  }
  
  /** Add next element at index position */
  public PreferenceBuild addNext(int index) {
    Ranking r = new Ranking(prefix);
    Item next = reference.get(prefix.length());
    r.add(index, next);
    return new PreferenceBuild(pref, r, reference);
  }
  
  
  /** Add next element at some position with specified probabilities */
  public PreferenceBuild addNext(double[] p) {
    double sum = MathUtils.sum(p);
    
    double flip = MathUtils.RANDOM.nextDouble();
    double ps = 0;
    for (int i = 0; i < p.length; i++) {
      ps += p[i] / sum;
      if (ps > flip || i == p.length - 1) {
        return addNext(i);
      }
    }
    return null;    
  }

  @Override
  public String toString() {
    return "PreferenceBuild{" + "prefix=" + prefix + ", constraint=" + constraint + '}';
  }
  
  
}
