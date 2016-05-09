package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.triangle.Insertions;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.ArrayList;
import java.util.List;

/** AMPx variant that immediately updates the Insertion Probability Matrix with a newly sampled ranking */
public class AMPxSParallelSampler extends AMPxSampler {

  private final Insertions[] insertions;
  private final int threads;
  
  /** Very low rate (close to zero) favors sample information.
   * High rate (close to positive infinity) favors AMP.
   * 
   * @param model
   * @param sample
   * @param alpha 
   */
  public AMPxSParallelSampler(MallowsModel model, Sample<? extends PreferenceSet> sample, double alpha, int threads) {
    super(model, null, alpha);
    insertions = new Insertions[sample.size()];
    int idx = 0;
    for (PW<? extends PreferenceSet> pw: sample) {
      Insertions ins = new Insertions(pw.p, model.getCenter());
      insertions[idx++] = ins;
      triangle.add(ins, pw.w);
    }
    this.threads = threads;
  }
  
  public AMPxSParallelSampler(MallowsModel model, Sample<? extends PreferenceSet> sample, double alpha) {
    this(model, sample, alpha, Runtime.getRuntime().availableProcessors());
  }
  
  public RankingSample sample(PreferenceSet pref, int count) {
    throw new IllegalArgumentException("Not supported");
  }
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample2(Sample<? extends PreferenceSet> sample) {
    if (sample.size() != insertions.length) throw new IllegalStateException("Sample sizes do not match");
    
    RankingSample out = new RankingSample(sample.getItemSet());
    for (int i = 0; i < insertions.length; i++) {
      PW<? extends PreferenceSet> pw = sample.get(i);
      Ranking r = sample(pw.p);
      out.add(r, pw.w);
      
//      Insertions prev = insertions[i];
//      triangle.sub(prev, pw.w);
//      Insertions next = new Insertions(r, model.getCenter());
//      triangle.add(next, pw.w);
//      insertions[i] = next;
      
      double damp = 1; // - this.model.getPhi();
      triangle.sub(insertions[i], damp * pw.w);
      insertions[i].set(r, model.getCenter());
      triangle.add(insertions[i], pw.w);


    }

    return out;
  }
  
  private Sample<? extends PreferenceSet> sample;
  private int idx;
  private RankingSample out;
  
  /** Create new sample with completions of the rankings in the input one */
  public RankingSample sample(Sample<? extends PreferenceSet> sample) {
    if (sample.size() != insertions.length) throw new IllegalStateException("Sample sizes do not match");
    
    this.sample = sample;
    this.out = new RankingSample(sample.getItemSet());
    for (int i = 0; i < insertions.length; i++) out.add((PW) null);
    this.idx = 0;
    
    // start threads
    List<Worker> workers = new ArrayList<Worker>();
    for (int i = 0; i < this.threads; i++) {
      Worker worker = new Worker();
      worker.start();
      workers.add(worker);
    }

    // join threads
    try { for (Worker worker: workers) worker.join(); }
    catch (InterruptedException e) { Logger.error(e); }
    
    return out;
  }
  

  
  private synchronized void set(int index, Ranking r) {
    double w = sample.getWeight(index);
    out.set(index, r, w);
    triangle.sub(insertions[index], w);
    insertions[index].set(r, model.getCenter());
    triangle.add(insertions[index], w);
  }
  
  private synchronized int getNext() {
    if (idx < insertions.length) return idx++;
    else return -1;
  }
  
  private synchronized int setAndGetIndex(int index, Ranking r) {
    set(index, r);
    if (idx < insertions.length) return idx++;
    else return -1;
  }
  
  private int nextId = 1;
  
  private class Worker extends Thread {
    
    private int id;

    private Worker() {
      this.id = nextId++;
    }
    
    public void run() {
      int index = getNext();
      int count = 0;
      while (index > -1) {
        PreferenceSet pref = sample.getPreferenceSet(index);
        Ranking r = sample(pref);
        count++;
        index = setAndGetIndex(index, r);
      }
      // Logger.info("Worker %d did %d out of %d rankings", id, count, sample.size());
    }
  }
  
}

