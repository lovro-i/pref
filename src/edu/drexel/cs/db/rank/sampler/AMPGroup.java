package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Utils;
import java.util.HashSet;
import java.util.Set;

/** Group of items that are consistent up to some level */
public class AMPGroup {

  private final Ranking reference;
  private final Ranking prefix;  
  private final PreferenceSample sample;
  
  public AMPGroup(Ranking reference, PreferenceSet pref, double w, Ranking prefix) {
    this.reference = reference;
    this.prefix = prefix;
    this.sample = new PreferenceSample(pref.getItemSet());
    this.sample.add(pref, w);    
  }
  
  public boolean isConsistent(PreferenceSet prefs, Ranking prefix) {
    if (!this.prefix.equals(prefix)) return false;
    Item next = reference.get(prefix.size());
    PreferenceSet ps = sample.get(0).p;
    for (int i = 0; i < prefix.size(); i++) {
      Item item = reference.get(i);
      Boolean p1 = prefs.isHigher(item, next);
      Boolean p2 = ps.isHigher(item, next);
      if (!Utils.equals(p1, p2)) return false;
    }
    return true;
  }
  
  public Ranking getPrefix() {
    return prefix;
  }
  
  public int size() {
    return sample.size();
  }
  
  public PreferenceSample getSample() {
    return sample;
  }
  
  
  /** Try to add preference set to this group, that is sampled up to prefix
   * 
   * @param prefs PreferenceSet to be added
   * @param w weight of the PreferenceSet
   * @param prefix sampled so far
   * @return true if consistent with the group and added, false otherwise
   */
  public boolean add(PreferenceSet prefs, double w, Ranking prefix) {
    if (!isConsistent(prefs, prefix)) return false;
    sample.add(prefs, w);
    return true;
  }
 
  @Override
  public String toString() {
    String s = String.format("%d users starting with %s and consistent on %s", sample.size(), prefix, reference.get(prefix.size()));
    PreferenceSet ps = sample.get(0).p;
    if (ps instanceof Ranking) {
      Set<Item> elements = new HashSet(prefix.getItems());
      elements.add(reference.get(prefix.size()));
      s += " (" + ((Ranking) ps).project(elements) + ")";
    }
    return s;
  }
  
  public static void main(String[] args) {    
    ItemSet items = new ItemSet(10);
    
    Sample sample = MallowsUtils.sample(items.getReferenceRanking(), 0.2, 1000);
    
    AMPGroups groups = new AMPGroups(items.getReferenceRanking(), sample);
    System.out.println(groups);
  }

  
  
 
}
