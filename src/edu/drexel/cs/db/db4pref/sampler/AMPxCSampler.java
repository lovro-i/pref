package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.triangle.ConfidentTriangle;
import edu.drexel.cs.db.db4pref.triangle.TriangleRow;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** AMP sampler extension that uses the combination of AMP and information from the sample */
public class AMPxCSampler extends MallowsSampler {

  protected List<ConfidentTriangle> triangles = new ArrayList<ConfidentTriangle>();
  protected List<ConfidentTriangle> copies = new ArrayList<ConfidentTriangle>();
  protected double alpha;
  protected boolean[] dynamic;
  
  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param alpha 
   */
  public AMPxCSampler(MallowsModel model, double alpha, Sample... samples) {
    super(model);
    if (alpha < 0) throw new IllegalArgumentException("Rate must be greater or equal zero");
    this.alpha = alpha;
    for (Sample sample: samples) {
      ConfidentTriangle triangle = new ConfidentTriangle(model.getCenter(), sample);
      triangles.add(triangle);
      copies.add(triangle.clone());
    }
    dynamic = new boolean[samples.length];
  }


  public RankingSample sample(PreferenceSet pref, int count) {
    RankingSample out = new RankingSample(pref.getItemSet());
    for (int i = 0; i < count; i++) {
      Ranking r = sample(pref);
      out.add(r);
      dynamicUpdate(r, 1);
    }
    return out;
  }
  
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample(Sample<? extends PreferenceSet> sample) {
    RankingSample out = new RankingSample(sample.getItemSet());
    for (Sample.PW pw: sample) {
      Ranking r = sample(pw.p);
      out.add(r, pw.w);
      dynamicUpdate(r, pw.w);
    }
    return out;
  }
  
  public final void setTrainingSample(int index, Sample sample) {
    this.triangles.set(index, new ConfidentTriangle(model.getCenter(), sample));
  }
  
  public void setDynamic(int index, boolean dyn) {
    this.dynamic[index] = dyn;
  }
  
  public void reset(int index) {
    ConfidentTriangle src = copies.get(index);
    ConfidentTriangle dst = triangles.get(index);
    dst.clone(src);
  }
  
  public void setAlpha(double alpha) {
    this.alpha = alpha;
  }
  
  @Override
  public Ranking sample(PreferenceSet v) {
    if (v instanceof Ranking) return this.sample((Ranking) v); // @todo: this line may be removed 
    
    Ranking reference = model.getCenter();
    Ranking r = new Ranking(model.getItemSet());
    PreferenceSet tc = v.transitiveClosure();
    
    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.length(); i++) {
      item = reference.get(i);
      int low = 0;
      int high = i;
      
      Set<Item> higher = tc.getHigher(item);
      Set<Item> lower = tc.getLower(item);
      for (int j = 0; j < r.length(); j++) {
        Item it = r.get(j);
        if (higher.contains(it)) low = j + 1;
        if (lower.contains(it) && j < high) high = j;
      }
            
      sampleItem(r, i, item, low, high); 
    }
    return r;
  }
  
  private void dynamicUpdate(PreferenceSet pref, double w) {
    for (int i = 0; i < dynamic.length; i++) {
      if (dynamic[i]) triangles.get(i).add(pref, w);
    }
  }
  
  /** Add one item to the ranking between low and high */
  private void sampleItem(Ranking r, int i, Item item, int low, int high) {
    if (low == high) {
      r.add(low, item);
      return;
    }
    
    double[] p = new double[high+1];
    
    double[] pAmp = new double[high+1];
    for (int j = low; j <= high; j++) {
      pAmp[j] = Math.pow(model.getPhi(), i - j);
    }
    MathUtils.normalize(pAmp, 1d);
    
    
    for (ConfidentTriangle triangle: triangles) {
      TriangleRow row = triangle.getRow(i);
      double count = row.getCount(low, high+1);
    
      double beta = count / (alpha + count); // how much should the sample be favored
      for (int j = low; j <= high; j++) {
        double onep = pAmp[j];
        if (beta > 0) onep = (1 - beta) * onep + beta * row.getCount(j) / count;
        p[j] += onep;
      }
    }

    if (triangles.isEmpty()) {
      p = pAmp;
    }
    else {
      for (int j = 0; j < triangles.size(); j++) {
        p[j] = p[j] / triangles.size();
      }
    }
    
    double flip = MathUtils.RANDOM.nextDouble();
    double ps = 0;
    for (int j = low; j <= high; j++) {
      ps += p[j];
      if (ps > flip || j == high) {
        r.add(j, item);
        break;
      }
    }
  }
  

  public Ranking sample(Ranking v) {
    Ranking reference = model.getCenter();
    Map<Item, Integer> map = v.getIndexMap();
    Ranking r = new Ranking(model.getItemSet());
    
    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.length(); i++) {
      item = reference.get(i);
      int low, high;
      
      Integer ci = map.get(item);
      if (ci == null) {
        low = 0;
        high = i;
      }
      else {
        low = 0;
        high = i;
        
        for (int j = 0; j < r.length(); j++) {
          Item t = r.get(j);
          Integer ti = map.get(t);
          if (ti == null) continue;
          if (ti < ci) low = j + 1;
          if (ti > ci && j < high) high = j;
        }
      }
      
      sampleItem(r, i, item, low, high);
    }
    return r;
  }

  
}
