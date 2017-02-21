package edu.drexel.cs.db.db4pref.posterior.concurrent6;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.gm.HasseDiagram;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.Span;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

/** Best parallel so far */
public class Expander6 {

  final MallowsModel model;
  final Map<Item, Integer> referenceIndex;
  final int threads;
  final int mapsPerThread;
  
  private PreferenceSet pref;
  MutablePreferenceSet tc;
  private Expands6 expands;
  Map<Item, Span> spans;
  long timeout = 0; // millis

  
  /** Creates a parallelized Expander for the given Mallows model with specified number of threads */
  public Expander6(MallowsModel model, int threads, int mapsPerThread) {
    this.model = model;
    this.referenceIndex = model.getCenter().getIndexMap();
    this.threads = threads;
    this.mapsPerThread = mapsPerThread;
  }

  
  public double getProbability(PreferenceSet pref) throws TimeoutException, InterruptedException {
    expand(pref);
    return expands.getProbability();
  }
  
  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }
  
  private void calculateSpans() {
    this.spans = new HashMap<Item, Span>();
    Ranking reference = model.getCenter();
    
    for (Item item: pref.getItems()) {
      int from = referenceIndex.get(item);
      Span span = new Span(from, from);
      spans.put(item, span);
    }
    
    HasseDiagram hasse = new HasseDiagram(pref, tc);
    
    
    for (int step = 0; step < reference.length(); step++) {
      Item item = reference.get(step);
      if (pref.contains(item)) {
        hasse.add(item);
        for (Preference p: hasse.getPreferenceSet()) {
          int il = referenceIndex.get(p.lower);
          int ih = referenceIndex.get(p.higher);
          if (il < ih && ih == step) spans.get(p.lower).setTo(step);
          else if (il > ih && il == step) spans.get(p.higher).setTo(step);
        }
      }
    }
  }
  
  

  /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  /** Returns number of items that are relevant at the given step */
  private int getS(int step) {
    int s = 0;
    for (Span span: spans.values()) {
      if (step >= span.from && step <= span.to) s++;
    }
    return s;
  }
  
  public int getMaxWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w = Math.max(w, getS(i));
    }
    return w;
  }
  
  public int getSumWidth() {
    int w = 0;
    Ranking ref = model.getCenter();
    for (int i = 0; i < ref.length(); i++) {
      w += getS(i);
    }
    return w;
  }
  
  public void expand(PreferenceSet pref) throws TimeoutException, InterruptedException {
    if (pref.equals(this.pref)) {
      Logger.info("Expander already available for PreferenceSet " + pref);
      return;
    }
    
    long start = System.currentTimeMillis();
    this.pref = pref;
    this.tc = pref.transitiveClosure();
    calculateSpans();
    expands = new Expands6(this);
    expands.nullify();
    Ranking reference = model.getCenter();
    int maxIndex = getMaxItem(pref);
    
    Workers6 workers = new Workers6(threads);
    for (int i = 0; i <= maxIndex; i++) {
      if (timeout > 0 && System.currentTimeMillis() - start > timeout) throw new TimeoutException("Expander timeout");
      Item item = reference.get(i);
      
      boolean missing = !this.pref.contains(item);
      expands = expands.insert(item, missing, workers);
    }
  }

  private HashMap<Span, Double> probs = new HashMap<>();

  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  double probability(int itemIndex, int position) {
    Span span = new Span(itemIndex, position);
    Double p = probs.get(span);
    if (p != null) return p;
    
    double phi = model.getPhi();
    double p1 = Math.pow(phi, Math.abs(itemIndex - position)) * (1 - phi) / (1 - Math.pow(phi, itemIndex + 1));
    probs.put(span, p1);
    return p1;
  }
  

  
}
