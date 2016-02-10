package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.triangle.TriangleRow;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Deprecated
public class RankingGroup {

  private final Ranking reference;
  private final int index;
  private final Map<Item, Integer> indexMap;
  private final List<Ranking> rankings = new ArrayList<Ranking>();
  private Ranking projection;
  
  public RankingGroup(Ranking reference) {
    this(reference, 0);
  }
  
  public RankingGroup(Ranking reference, int index) {
    this.reference = reference;
    this.index = index;
    this.indexMap = reference.getIndexMap();
  }
  
  public RankingGroup(Ranking reference, Sample sample) {
    this(reference);
    for (RW rw: sample) {
      this.add(rw.r);
    }
  }
  
  /** Adds the PreferenceSet to this group if it is consistent with the group.
   * @param pref PreferenceSet to add
   * @return true if the pref is constistent and added, false otherwise
   */
  public boolean add(Ranking r) {
    if (rankings.isEmpty()) {
      rankings.add(r);
      projection = project(r, index);
      return true;
    }
      
    if (isConsistent(r)) {
      rankings.add(r);
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
    return rankings.size();
  }
  
  @Override
  public String toString() {
    if (rankings.isEmpty()) return String.format("Empty set; reference ranking %s, index %d", reference, index);
    StringBuilder sb = new StringBuilder();
    sb.append(size()).append(" users with ranking starting ");
    sb.append(project(rankings.get(0), index));
    TriangleRow row = getRow();
    if (row != null) sb.append("; ").append(getRow());
    return sb.toString();
  }
  
  public boolean isConsistent(Ranking r) {
    if (rankings.isEmpty()) return false;
    Ranking p = project(r, index);
    return projection.equals(p);
  }
  
  public Ranking project(Ranking r, int idx) {
    Ranking p = new Ranking(r.getItemSet());
    for (Item i: r.getItems()) {
      if (indexMap.get(i) <= idx) p.add(i);
    }
    return p;
  }
  
  /** Are pref1 and pref2 consistent up to index idx */
  public boolean areConsistent(Ranking r1, Ranking r2, int idx) {
    Ranking p1 = project(r1, idx);
    Ranking p2 = project(r2, idx);
    return p1.equals(p2);
  }
  
  
  public TriangleRow getRow() {
    if (index == reference.size() - 1) return null;
    TriangleRow row = new TriangleRow(index+1);
    Item item = reference.get(index+1);
    
    List<RankingGroup> groups = this.split();
    if (groups != null) {
      for (RankingGroup group: groups) {
        row.inc(group.projection.indexOf(item), group.rankings.size());
      }
    }
    
    return row;
  }
  
  private List<RankingGroup> groups;
  
  public List<RankingGroup> split() {
    if (index == reference.size() - 1) return null;
    if (groups != null) return groups;
    
    groups = new ArrayList<RankingGroup>();       
    for (Ranking r: rankings) {
      
      // check if the ranking has the next item
      if (!r.contains(reference.get(index+1))) continue;
      
      // check if it can be added to some of the existing groups
      boolean added = false;
      for (RankingGroup group: groups) {
        if (group.add(r)) {
          added = true;
          break;
        }
      }
      
      // if not added, create a new group
      if (!added) {
        RankingGroup group = new RankingGroup(reference, index+1);
        group.add(r);
        groups.add(group);
      }
    }
    return groups;
  }
  
  
  private static void indent(int i, StringBuilder sb) {
    for (int j = 0; j < i; j++) {
      sb.append("  | ");
    }
  }
  
  private static void write(RankingGroup group, StringBuilder sb) {
    indent(group.getIndex(), sb);
    sb.append(group).append('\n');
    if (group.getIndex() < group.getReference().size() - 1) {
      List<RankingGroup> splits = group.split();
      for (RankingGroup g: splits) {
        write(g, sb);
      }
    }    
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(6);
    Ranking ref = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(ref, 0.2);
    Sample sample = MallowsUtils.sample(model, 1000);
    //Filter.remove(sample, 0.2);
    
    RankingGroup group = new RankingGroup(ref, sample);

    
    StringBuilder sb = new StringBuilder();
    write(group, sb);
    System.out.println(sb.toString());
    
    
    
//    Ranking r1 = new Ranking(items);
//    r1.add(items.get(0));
//    r1.add(items.get(1));
//    r1.add(items.get(2));
//    r1.add(items.get(3));
//    r1.add(items.get(4));
//    r1.add(items.get(5));
//    
//    
//    Ranking r2 = new Ranking(items);
//    r2.add(items.get(0));
//    r2.add(items.get(1));
//    r2.add(items.get(3));
//    r2.add(items.get(4));
//    r2.add(items.get(5));
//    
//    RankingGroup g1 = new RankingGroup(ref);
//    g1.add(r1);
//    g1.add(r2);
//    
//    List<RankingGroup> gs = g1.split();
//    System.out.println(gs.size());
//    System.out.println(gs.get(0).size());
//    System.out.println(gs);
//    
//    List<RankingGroup> gss = gs.get(0).split();
//    System.out.println(gss.size());
//    
//    sb = new StringBuilder();
//    write(g1, sb);
//    System.out.println(sb.toString());
    
    
  }
  
}
