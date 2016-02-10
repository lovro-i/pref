package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AMPGroups extends ArrayList<AMPGroup> {

  private Ranking reference;
  private Sample out;
  private Map<Ranking, List<AMPGroup>> byPrefix = new HashMap<Ranking, List<AMPGroup>>();
  
  
  public AMPGroups(Ranking reference) {
    this.reference = reference;
    
  }
  
  public AMPGroups(Ranking reference, Sample sample) {
    this.reference = reference;
    ItemSet items = sample.getItemSet();
    Ranking prefix = new Ranking(items);
    prefix.add(reference.get(0));
    for (RW rw: sample) {
      this.add(rw.r, rw.w, prefix);
    }
  } 
  
  public Sample getSample() {
    return out;
  }
  
  public void add(PreferenceSet pref, double w, Ranking prefix) {
    if (prefix.size () > reference.size()) throw new IllegalArgumentException("Prefix too long");
    
    if (prefix.size() == reference.size()) {
      if (out == null) out = new Sample(reference.getItemSet());
      out.add(prefix, w);
      if (!prefix.isConsistent(pref)) Logger.info("ERROR: %s sampled to %s", pref, prefix);
      return;
    }
    
    List<AMPGroup> groups = byPrefix.get(prefix);
    if (groups != null) {
      for (AMPGroup group: groups) {
        if (group.add(pref, w, prefix)) return;
      }
    }
    else {
      groups = new ArrayList<AMPGroup>();
      byPrefix.put(prefix, groups);
    }
    
    AMPGroup group = new AMPGroup(reference, pref, w, prefix);
    this.add(group);
    groups.add(group);
  }
  
  public void add(AMPGroup group, Ranking prefix) {
    PreferenceSample sample = group.getSample();
    for (PW pref: sample) {
      this.add(pref.p, pref.w, prefix);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (AMPGroup group: this) {
      sb.append(group).append('\n');
    }
    return sb.toString();
  }

  
}
