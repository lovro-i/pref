package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Map from constraints of index to Level3 (which then contains prefixes) */
class Level2 {

  private final Ranking reference;
  private final int index;
  private final Map<PreferenceConstraint, Level3> map = new HashMap<PreferenceConstraint, Level3>(); 
  
  Level2(Ranking reference, Sample sample, int index) {
    this.reference = reference;
    this.index = index;
    build(sample);
  }

  private void build(Sample sample) {
    for (Ranking r: sample.rankings()) {
      PreferenceConstraint cons = new PreferenceConstraint(r, reference, index);
      Level3 level3 = map.get(cons);
      if (level3 == null) {
        level3 = new Level3(reference, cons, index);
        map.put(cons, level3);
      }
    }
  }

  void add(PreferenceSet pref, double w, Ranking prefix) {
    PreferenceConstraint cons = new PreferenceConstraint(pref, reference, prefix.size());
    Level3 level3 = map.get(cons);
    level3.add(pref, w, prefix);
  }

  public List<Users> getGroups() {
    List<Users> groups = new ArrayList<Users>();
    for (Level3 level3: map.values()) {
      groups.addAll(level3.getUsers());
    }
    return groups;
  }
}
