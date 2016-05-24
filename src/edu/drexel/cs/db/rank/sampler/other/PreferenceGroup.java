package edu.drexel.cs.db.rank.sampler.other;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.DensePreferenceSet;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.util.Utils;
import java.util.ArrayList;
import java.util.List;

/** Contains PreferenceSets that have the same preferences up to the given index */
@Deprecated
public class PreferenceGroup {

  private final Ranking reference;
  private final int index;
  private final List<PreferenceSet> prefs = new ArrayList<PreferenceSet>();
  
  public PreferenceGroup(Ranking reference) {
    this(reference, 0);
  }
  
  public PreferenceGroup(Ranking reference, int index) {
    this.reference = reference;
    this.index = index;
  }
  
  /** Adds the PreferenceSet to this group if it is consistent with the group.
   * @param pref PreferenceSet to add
   * @return true if the pref is constistent and added, false otherwise
   */
  public boolean add(PreferenceSet pref) {
    if (prefs.isEmpty() || isConsistent(pref)) {
      prefs.add(pref);
      return true;
    }
    return false;
  }
  
  public int getIndex() {
    return index;
  }
  
  public Ranking getReference() {
    return reference;
  }
  
  public int size() {
    return prefs.size();
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(size()).append(" users with sequence ");
    for (int i = 0; i <= index; i++) {
      if (i > 0) sb.append("-");
      sb.append(reference.get(i));
    }
    return sb.toString();
  }
  
  public boolean isConsistent(PreferenceSet pref) {
    if (prefs.isEmpty()) return false;
    return areConsistent(pref, prefs.get(0));
  }
  
  public boolean areConsistent(PreferenceSet pref1, PreferenceSet pref2) {
    for (int i = 1; i <= index; i++) {
      if (!areConsistent(pref1, pref2, index)) return false;
    }

//    for (int i = 0; i < index; i++) {
//      Item item1 = reference.get(i);
//      for (int j = i+1; j <= index; j++) {
//        Item item2 = reference.get(j);
//        Boolean b1 = pref1.isHigher(item1, item2);
//        Boolean b2 = pref2.isHigher(item1, item2);
//        if (!eq(b1, b2)) return false;
//      }
//    }
    
    
//    for (int i = 0; i <= index; i++) {
//      boolean c = areConsistent(pref1, pref2, i);
//      if (!c) return false;
//    }

    return true;
  }

  
  public List<PreferenceGroup> split() {
    if (index == reference.length() - 1) throw new UnsupportedOperationException("Cannot split futher");
    
    List<PreferenceGroup> groups = new ArrayList<PreferenceGroup>();
    for (PreferenceSet pref: prefs) {
      
      // check if it can be added to some of the existing groups
      boolean added = false;
      for (PreferenceGroup group: groups) {
        if (group.add(pref)) {
          added = true;
          break;
        }
      }
      
      // if not added, create a new group
      if (!added) {
        PreferenceGroup group = new PreferenceGroup(reference, index+1);
        group.add(pref);
        groups.add(group);
      }
    }
    return groups;
  }
  
  
  /** Are pref1 and pref2 consistent over item at idx */
  public boolean areConsistent(PreferenceSet pref1, PreferenceSet pref2, int idx) {
    Item item1 = reference.get(idx);
    for (int i = 0; i < idx; i++) {
      Item item2 = reference.get(i);
      Boolean b1 = pref1.isPreferred(item1, item2);
      Boolean b2 = pref2.isPreferred(item1, item2);
      if (!Utils.equals(b1, b2)) return false;
    }
    return true;
  }
  

  
  private static void indent(int i, StringBuilder sb) {
    for (int j = 0; j < i; j++) {
      sb.append("  ");
    }
  }
  
  private static void write(PreferenceGroup group, StringBuilder sb) {
    indent(group.getIndex(), sb);
    sb.append(group).append('\n');
    if (group.getIndex() < group.getReference().length() - 1) {
      List<PreferenceGroup> splits = group.split();
      for (PreferenceGroup g: splits) {
        write(g, sb);
      }
    }    
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    Ranking ref = items.getReferenceRanking();
    
    PreferenceGroup group = new PreferenceGroup(ref);
    
    // a0 b1  c2  d3  e4
    
    DensePreferenceSet user1 = new DensePreferenceSet(items);
    user1.add(items.get(0), items.get(1));
    user1.add(items.get(1), items.get(2));
    user1.add(items.get(1), items.get(4));
    user1.add(items.get(4), items.get(3));
    
    
    DensePreferenceSet user2 = new DensePreferenceSet(items);
    user2.add(items.get(0), items.get(1));
    user2.add(items.get(1), items.get(4));
    user2.add(items.get(4), items.get(3));
    
    
    group.add(user1);
    group.add(user2);
    
//    Ranking r1 = new Ranking(items);
//    r1.add(items.get(0));
//    r1.add(items.get(1));
//    r1.add(items.get(2));
//    r1.add(items.get(3));
//    r1.add(items.get(4));
//    r1.add(items.get(5));
//    
//    Ranking r2 = new Ranking(items);
//    r2.add(items.get(0));
//    r2.add(items.get(1));
//    r2.add(items.get(2));
//    r2.add(items.get(4));
//    r2.add(items.get(5));
//    r2.add(items.get(3));
//    
//    group.add(DensePreferenceSet.fromRanking(r1));
//    group.add(DensePreferenceSet.fromRanking(r2));
    
    StringBuilder sb = new StringBuilder();
    write(group, sb);
    System.out.println(sb.toString());
    
    
    // System.out.println(group.areConsistent(DensePreferenceSet.fromRanking(r1), DensePreferenceSet.fromRanking(r2)));
    
    
//    MallowsModel model = new MallowsModel(ref, 0.1);
//    Sample sample = MallowsUtils.sample(model, 10);
//    PreferenceGroup group = new PreferenceGroup(ref, 2);
//    group.add(DensePreferenceSet.fromRanking(ref));
//    for (Ranking r: sample) {
//      boolean added = group.add(DensePreferenceSet.fromRanking(r));
//      Logger.info("%s added: %s", r, added);
//    }
  }
}
