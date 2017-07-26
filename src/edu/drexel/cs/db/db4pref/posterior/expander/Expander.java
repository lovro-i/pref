package edu.drexel.cs.db.db4pref.posterior.expander;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.gm.HasseDiagram;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.Span;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/** Abstract expander that all Expanders should implement */
public abstract class Expander {

  protected final MallowsModel model;
  protected Map<Item, Integer> referenceIndex;
  
  protected final PreferenceSet pref;
  protected MutablePreferenceSet tc;
  
  private int maxItem;
  protected final Map<Item, Span> spans = new HashMap<>();
  
  protected ExpanderListener listener;
  
  public Expander(MallowsModel model, PreferenceSet pref) {
    this.model = model;
    this.pref = pref.clone();
    this.referenceIndex = model.getCenter().getIndexMap();
    init();
  }
  
  public void init() {
    this.tc = pref.transitiveClosure();
    this.calculateSpans();
    this.maxItem = calcMaxItem();
  }
  
  public MallowsModel getModel() {
    return model;
  }
  
  public PreferenceSet getPreferenceSet() {
    return pref;
  }
  
  public MutablePreferenceSet getTransitiveClosure() {
    return tc;
  }
  
  public Span getSpan(Item item) {
    return spans.get(item);
  }
  
  /** Number of items that have to be tracked at step */
  public int getWidth(int step) {
    int w = 0;
    for (Span span: spans.values()) {
      if (span.from <= step && step <= span.to && span.from != span.to) w++;
    }
    return w;
  }
  
  public List<Item> getTrackedItems(int step) {
    List<Item> tracked = new ArrayList<Item>();
    for (Entry<Item, Span> entry: spans.entrySet()) {
      Span span = entry.getValue();
      if (span.from <= step && step <= span.to && span.from != span.to) {
        tracked.add(entry.getKey());
      }
    }
    return tracked;
  }
 
  public void setListener(ExpanderListener listener) {
    this.listener = listener;
  }
  
  /** Index of the item in the reference (center) ranking */
  public int getReferenceIndex(Item item) {
    return referenceIndex.get(item);
  }
  
  public void calculateSpans() {
    spans.clear();
    for (Item item: pref.getItems()) {
      int from = referenceIndex.get(item);
      Span span = new Span(from, from);
      spans.put(item, span);
    }

    HasseDiagram hasse = new HasseDiagram(pref);
    hasse.add(model.getCenter());

    for (Preference p: hasse.getPreferenceSet()) {
      Item higher = p.higher;
      Item lower = p.lower;
      int higherIdx = referenceIndex.get(higher);
      int lowerIdx = referenceIndex.get(lower);
      if (spans.get(higher).to < lowerIdx) {
        spans.get(higher).setTo(lowerIdx);
      }
      if (spans.get(lower).to < higherIdx) {
        spans.get(lower).setTo(higherIdx);
      }
    }
  }

  @Deprecated
  public void calculateSpansBackup() {
    spans.clear();
    for (Item item: pref.getItems()) {
      int from = referenceIndex.get(item);
      Span span = new Span(from, from);
      spans.put(item, span);
    }

    Ranking reference = model.getCenter();
    HasseDiagram hasse = new HasseDiagram(pref);
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
  public int getMaxItem() {
    return maxItem;
  }
    
  private int calcMaxItem() {
    int i = 0;
    for (Item item: pref.getItems()) {
      i = Math.max(i, referenceIndex.get(item));
    }
    return i;
  }
  
  
  private HashMap<Span, Double> probs = new HashMap<>();
  
  /** Calculate the probability of the item being inserted at the given position. Directly from the Mallows model */
  public double probability(int itemIndex, int position) {
    Span span = new Span(itemIndex, position);
    Double p = probs.get(span);
    if (p != null) return p;
    
    double phi = getModel().getPhi();
    double p1 = Math.pow(phi, Math.abs(itemIndex - position)) * (1 - phi) / (1 - Math.pow(phi, itemIndex + 1));
    probs.put(span, p1);
    return p1;
  }
  
  public abstract double expand() throws Exception;
  
  public abstract double expand(State state) throws Exception;
  
}
