package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Sample;
import java.io.Serializable;
import java.util.Map;

/** Result of preference clustering. */
public class PreferenceClusters implements Serializable {

  /** Maps each PreferenceSet to its exemplar */
  public final Map<PreferenceSet, PreferenceSet> exemplars;
  
  /** Maps each exemplar to a sample of its PreferenceSets */
  public final Map<PreferenceSet, Sample<PreferenceSet>> clusters;

  PreferenceClusters(Map<PreferenceSet, PreferenceSet> exemplars, Map<PreferenceSet, Sample<PreferenceSet>> clusters) {
    this.exemplars = exemplars;
    this.clusters = clusters;
  }

  public ItemSet getItemSet() {
    for (PreferenceSet r : exemplars.keySet()) {
      return r.getItemSet();
    }
    for (PreferenceSet r : clusters.keySet()) {
      return r.getItemSet();
    }
    return null;
  }

}
