package edu.drexel.cs.db.rank.sampler;

import cern.colt.Arrays;
import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.List;

public class AMPGSampler {

  protected MallowsModel model;
  // protected int threshold = 10000;

  public AMPGSampler(MallowsModel model) {
    this.model = model;
    Users.model = model;
  }

  public RankingSample sample(RankingSample sample) {
    Sample tcs = sample.transitiveClosure();
    return sample(tcs);
  }
    
    
  public RankingSample sample(Sample tcs) {  
    Ranking reference = model.getCenter();
    long start = System.currentTimeMillis();
    
    Level0 level1 = new Level0(reference, tcs);
    Logger.info("[G-AMP] Pre-processing done in %d ms", System.currentTimeMillis() - start);
    
    RankingSample out = new RankingSample(model.getItemSet());
    
    long timeLevel1Add = 0;
    long timeLevel1Get = 0;
    long timeInsert = 0;
    start = System.currentTimeMillis();
    for (int i = 1; i < reference.size(); i++) {
      Item item = reference.get(i);
      Level1 prev = level1.getLevel1(i-1);
      Level1 next = level1.getLevel1(i);
//      Logger.info("[Item %d]\n%s", i, prev);
//      try { System.in.read(); } catch (IOException ex) { }
      long t1 = System.currentTimeMillis();
      List<Users> groups = prev.getGroups();
      timeLevel1Get += System.currentTimeMillis() - t1;
      
      for (Users users: groups) {
        
//        if (users.size() < threshold) {
//          // Logger.info("Small group: %d", users.size());
//          // just finish them
//          for (PW pw: users) {
//            
//            Ranking r = new Ranking(users.prefix);            
//            for (int in = i; in < reference.size(); in++) {
//              Item ite = reference.get(in);
//              int low = 0;
//              int high = in;
//
//              Set<Item> higher = pw.p.getHigher(ite);
//              Set<Item> lower = pw.p.getLower(ite);
//              for (int j = 0; j < r.size(); j++) {
//                Item it = r.get(j);
//                if (higher.contains(it)) low = j + 1;
//                if (lower.contains(it) && j < high) high = j;
//              }
//
//              if (low == high) {
//                r.addAt(low, ite);
//              }
//              else {        
//                double sum = 0;
//                double[] p = new double[high+1];                
//                for (int j = low; j <= high; j++) {
//                  p[j] = Math.pow(model.getPhi(), in - j);
//                  sum += p[j];
//                }
//
//                double flip = MathUtils.RANDOM.nextDouble();
//                double ps = 0;
//                for (int j = low; j <= high; j++) {
//                  ps += p[j] / sum;
//                  if (ps > flip || j == high) {
//                    r.addAt(j, ite);
//                    break;
//                  }
//                }
//              }
//            }
//            // Logger.info("Finished single %s to %s", users.prefix, r);
//            out.add(r, pw.w);            
//          }
//          continue;
//        }
        
        
        for (PW pw: users) {
          long t2 = System.currentTimeMillis();
          Ranking r = new Ranking(users.prefix);
          if (users.low == users.high) {
            r.addAt(users.low, item);
          }
          else {
            double flip = MathUtils.RANDOM.nextDouble();
            double ps = 0;
            for (int j = users.low; j <= users.high; j++) {
              ps += users.p[j];
              if (ps > flip || j == users.high) {
                r.addAt(j, item);
                break;
              }
            }
          }
          timeInsert += System.currentTimeMillis() - t2;
          
          if (r.size() == users.prefix.size()) {
            Logger.info("ERROR 1: %d %s, %s, %d, %d\n%s\n%s\n%s", i, users.prefix, r, users.low, users.high, Arrays.toString(users.p), users.cons, users.pref);
          }
          
          if (r.size() == reference.size()) out.add(r, pw.w);
          else if (next != null) {
            long st = System.currentTimeMillis();
            next.add(pw, r);
            timeLevel1Add += System.currentTimeMillis() - st;
          }
          else Logger.info("ERROR 2: %d %s", i, r);
        }
      }
    }
    Logger.info("[G-AMP] Inserting done in %d ms; level1.add: %d ms; level1.getGroups: %d ms; timeInsert: %d ms", System.currentTimeMillis() - start, timeLevel1Add, timeLevel1Get, timeInsert);
    return out;
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.3);
    RankingSample sample1 = MallowsUtils.sample(model, 100000);
    RankingSample sample = new RankingSample(items);
    for (PW<Ranking> pw: sample1) {
      sample.add(pw.p, Math.random());
      
    }
    Filter.remove(sample, 0.2);
    // System.out.println(sample);
    Sample tcs = sample.transitiveClosure();
    System.out.println("Start");
    
    {      
      long start = System.currentTimeMillis();
      AMPSampler sampler = new AMPSampler(model);
      RankingSample out = sampler.sample(tcs);      
      // System.out.println(out);
      Logger.info("[AMP] %d items, %d users in %d ms", items.size(), out.size(), System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      AMPGSampler sampler = new AMPGSampler(model);
      RankingSample out = sampler.sample(tcs);
      // System.out.println(out);
      Logger.info("[G-AMP] %d items, %d users in %d ms", items.size(), out.size(), System.currentTimeMillis() - start);
      
      for (Ranking r: out.rankings()) {
        if (r.size() < items.size()) System.out.println("ERROR: "+r);
      }
    }
  }
  
}
