package edu.drexel.cs.db.rank.sampler.other;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Map from constraints of index to Level3 (which then contains prefixes) */
class Level1 {

  private final Ranking reference;
  private final int index;
  private final Map<PreferenceConstraint, Level2> map = new HashMap<PreferenceConstraint, Level2>(); 
  private final Map<PW, PreferenceConstraint> users = new HashMap<PW, PreferenceConstraint>();
  
  Level1(Ranking reference, Sample<? extends PreferenceSet> sample, int index) {
    this.reference = reference;
    this.index = index;

    for (PW pw: sample) {
      PreferenceConstraint cons = new PreferenceConstraint(pw.p, reference, index+1);
      // Logger.info("Level1 %d created cons %s", index, cons);
      Level2 level2 = map.get(cons);
      if (level2 == null) {
        level2 = new Level2(reference, cons);
        map.put(cons, level2);
      }
      users.put(pw, cons);
    }
  }
  
  void add(PW pw, Ranking prefix) {
    PreferenceConstraint cons = users.get(pw);
    Level2 level3 = map.get(cons);
    level3.add(pw, prefix);
  }

  public List<Users> getGroups() {
    List<Users> groups = new ArrayList<Users>();
    for (Level2 level3: map.values()) {
      groups.addAll(level3.getUsers());
    }
    return groups;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Level2 for index ").append(index).append("\n");
    for (Level2 l3: map.values()) {
      sb.append("   ").append(l3).append("\n");
    }
    return sb.toString();
  }
}
