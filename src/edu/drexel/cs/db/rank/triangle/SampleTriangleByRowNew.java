package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SampleTriangleByRowNew extends Triangle {

  
  private final Map<Integer, TriangleRow> rows;
  private final Map<Integer, Expands> expandsMap = new ConcurrentHashMap<Integer, Expands>();
  private final List<Integer> pending = Collections.synchronizedList(new ArrayList<Integer>());
  private final List<Thread> workers = new ArrayList<Thread>();
  
  private final Sample sample;
  private int threads = Runtime.getRuntime().availableProcessors();
  
  public SampleTriangleByRowNew(Ranking reference, Sample sample) throws InterruptedException {
    super(reference); 
    this.sample = sample;
    this.buildReferenceIndexMap();
    rows = new HashMap<Integer, TriangleRow>();
    for (int i = 0; i < reference.size(); i++) {
      TriangleRow c = new TriangleRow(i);
      rows.put(i, c);
    }
    
    
    long start = System.currentTimeMillis();
    
    for (int item = 0; item < reference.size(); item++) {
      pending.clear();
      Logger.info("Item %d of %d, %d sec", item, reference.size(), (int) (0.001 * (System.currentTimeMillis() - start)));
      
      addParallel(item);
      unpendParallel(item);
    }
    
  }
  
  int nextAdd = 0;
  
  private synchronized Integer getNextAdd() {
    if (nextAdd < sample.size()) return nextAdd++;
    else return null;
  }
  
  int nextUnpend = 0;
  
  private synchronized Integer getNextUnpend() {
    if (nextUnpend < pending.size()) return pending.get(nextUnpend++);
    else return null;
  }
  
  
  private void unpendParallel(int item) throws InterruptedException {
    nextUnpend = 0;
    if (!pending.isEmpty()) {
      workers.clear();
      for (int i = 0; i < Math.min(threads, pending.size()); i++) {
        Unpender unpender = new Unpender(item);
        unpender.start();
        workers.add(unpender);
      }
      for (Thread worker: workers) worker.join();
    }
  }
  
  /** Inserts missing item into ones where this one is missing */
  private void unpend(int item) {
    for (int index: pending) {
      Expands expands = expandsMap.get(index);
      if (expands == null) {
        expands = new Expands();
        expands.nullify();
      }
      TriangleRow row = rows.get(item);
      expands = expands.insertMissing(row);
      expandsMap.put(index, expands);
    }
  }  
    
  private void unpend(int index, int item) {  
    Expands expands = expandsMap.get(index);
    if (expands == null) {
      expands = new Expands();
      expands.nullify();
    }
    TriangleRow row = rows.get(item);
    expands = expands.insertMissing(row);
    expandsMap.put(index, expands);
  }
  
  
  private class Unpender extends Thread {
   
    private int item;
    
    private Unpender(int item) {
      this.item = item;
    }
    
    @Override
    public void run() {
      while (true) {
        Integer index = getNextUnpend();
        if (index == null) return;
        unpend(index, item);
      }
    }
  }
  
  
  @Override
  public TriangleRow getRow(int i) {
    return rows.get(i);
  }
  
  public double get(int item, int pos) {
    return rows.get(item).getProbability(pos);
  }
  
  
  private void addParallel(int item) throws InterruptedException {
    nextAdd = 0;
    workers.clear();
    for (int i = 0; i < threads; i++) {
      Adder adder = new Adder(item);
      adder.start();
      workers.add(adder);
    }
    for (Thread worker: workers) worker.join();
  }
  
  
  private class Adder extends Thread {
    
    private int item;
    
    private Adder(int item) {
      this.item = item;
    }
    
    public void run() {
      while (true) {
        Integer index = getNextAdd();
        if (index == null) return;
        add(index, item);
      }
    }
  }
  
  private void add(int index, int item) {
    RW rw = sample.get(index);
    Item element = reference.get(item);
    if (!rw.r.contains(element)) {
      pending.add(index);
      return;
    }
    
    Expands expands = expandsMap.get(index);
    if (expands == null) {
      expands = new Expands();
      expands.nullify();
    }

    
    
          
//    for (int i = 0; i < item; i++) {
//      Item e = reference.get(i);
//      
//      UpTo upto = new UpTo(ranking, i, referenceIndex);
//      int pos = upto.position;
//      
//      if (pos == -1) { 
//        TriangleRow row = rows.get(i);
//        expands = expands.insertMissing(row);
//      }
//      else {
//        expands = expands.insert(e, upto.previous);      
//      }
//    }    
    
    UpTo upto = new UpTo(rw.r, item, referenceIndex);
    expands = expands.insert(element, upto.previous);    
    expandsMap.put(index, expands);
    TriangleRow row = rows.get(item);
    double[] displacements = expands.getDistribution(element);
    // Logger.info("Item %d, displacements %d", item, displacements.length);
    
    synchronized (row) {
      for (int j = 0; j < displacements.length; j++) {      
        row.inc(j, displacements[j] * rw.w);
      }
    }
    
    
  }
  
  private Map<Item, Integer> referenceIndex = new HashMap<Item, Integer>();
  
  private void buildReferenceIndexMap() {
    referenceIndex.clear();
    for (int i = 0; i < reference.size(); i++) {
      Item e = reference.get(i);
      referenceIndex.put(e, i);
    }    
  }
  
  
  @Override
  public int randomPosition(int e) {
    return rows.get(e).getRandomPosition();
  }
  
    
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < getItemSet().size(); i++) {
      sb.append(rows.get(i)).append("\n");
    }  
    return sb.toString();
  }

  
  public static void main(String[] args) throws InterruptedException {
    ItemSet items = new ItemSet(10);
    Expands.setThreshold(0.001);
    Ranking ref = items.getRandomRanking();
    Sample sample = MallowsUtils.sample(ref, 0.2, 1000);
    Filter.remove(sample, 0.3);
    
    SampleTriangleByRow t = new SampleTriangleByRow(ref, sample);
    
    
    SampleTriangle t1 = new SampleTriangle(ref, sample);
    
    
    SampleTriangleByRowNew t2 = new SampleTriangleByRowNew(ref, sample);
    System.out.println(t.equals(t1));
    System.out.println(t.equals(t2));
    System.out.println(t1.equals(t2));
    
  }
  
}
