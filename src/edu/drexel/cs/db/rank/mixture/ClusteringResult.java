package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

public class ClusteringResult implements Serializable {

  public final Map<PreferenceSet, PreferenceSet> exemplars;
  public final Map<PreferenceSet, Sample<PreferenceSet>> samples;

  ClusteringResult(Map<PreferenceSet, PreferenceSet> exemplars, Map<PreferenceSet, Sample<PreferenceSet>> samples) {
    this.exemplars = exemplars;
    this.samples = samples;
  }

  public ItemSet getItemSet() {
    for (PreferenceSet r : exemplars.keySet()) {
      return r.getItemSet();
    }
    for (PreferenceSet r : samples.keySet()) {
      return r.getItemSet();
    }
    return null;
  }

  public PreferenceSet getExemplar(PreferenceSet pref) {
    return exemplars.get(pref);
  }
  
  public Collection<Sample<PreferenceSet>> getSamples() {
    return samples.values();
  }
}
