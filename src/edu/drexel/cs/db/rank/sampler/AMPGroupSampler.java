package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.Set;


public class AMPGroupSampler {

  protected MallowsModel model;
  
  public AMPGroupSampler(MallowsModel model) {
    this.model = model;
  }
  
//  public Ranking sample(PreferenceSet v) {
//    Ranking reference = model.getCenter();
//    Ranking r = new Ranking(model.getItemSet());
//    DensePreferenceSet tc = v.transitiveClosure();
//    
//    Item item = reference.get(0);
//    r.add(item);
//    for (int i = 1; i < reference.size(); i++) {
//      item = reference.get(i);
//      int low = 0;
//      int high = i;
//      
//      Set<Item> higher = tc.getHigher(item);
//      Set<Item> lower = tc.getLower(item);
//      for (int j = 0; j < r.size(); j++) {
//        Item it = r.get(j);
//        if (higher.contains(it)) low = j + 1;
//        if (lower.contains(it) && j < high) high = j;
//      }
//            
//      if (low == high) {
//        r.addAt(low, item);
//      }
//      else {        
//        double sum = 0;
//        double[] p = new double[high+1];                
//        for (int j = low; j <= high; j++) {
//          p[j] = Math.pow(model.getPhi(), i - j);
//          sum += p[j];
//        }
//        
//        double flip = MathUtils.RANDOM.nextDouble();
//        double ps = 0;
//        for (int j = low; j <= high; j++) {
//          ps += p[j] / sum;
//          if (ps > flip || j == high) {
//            r.addAt(j, item);
//            break;
//          }
//        }
//      }
//    }
//    return r;
//  }
//  
//  public Sample sample(PreferenceSet v, int size) {
//    Sample sample = new Sample(model.getItemSet());
//    for (int i = 0; i < size; i++) {
//      sample.add(sample(v));      
//    }
//    return sample;
//  }
  

  
  public Sample sample(Sample sample) {
    Sample out = new Sample(sample.getItemSet());
    Ranking reference = model.getCenter();
    
    AMPGroups groups = new AMPGroups(reference, sample);
    AMPGroups nextGroups;
    
    for (int i = 1; i < reference.size(); i++) {
      long start = System.currentTimeMillis();
      long timeSingle = 0;
      long timeGroup = 0;
      final Item item = reference.get(i);      
      //try { System.in.read(); } catch (IOException ex) { }
      nextGroups = new AMPGroups(reference);
      for (AMPGroup group: groups) {        
        Ranking prefix = group.getPrefix();
        PreferenceSample s = group.getSample();
        
        
        if (s.size() < 100000) {
          // just finish these ones
          
          long t1 = System.currentTimeMillis();
          for (PW pw: s) {
            Ranking r = new Ranking(prefix);
            // Logger.info("Finishing %s %s", r, tc);
            for (int in = i; in < reference.size(); in++) {
              Item ite = reference.get(in);
              int low = 0;
              int high = in;

              Set<Item> higher = pw.p.getHigher(ite);
              Set<Item> lower = pw.p.getLower(ite);
              for (int j = 0; j < r.size(); j++) {
                Item it = r.get(j);
                if (higher.contains(it)) low = j + 1;
                if (lower.contains(it) && j < high) high = j;
              }

              if (low == high) {
                r.addAt(low, ite);
              }
              else {        
                double sum = 0;
                double[] p = new double[high+1];                
                for (int j = low; j <= high; j++) {
                  p[j] = Math.pow(model.getPhi(), in - j);
                  sum += p[j];
                }

                double flip = MathUtils.RANDOM.nextDouble();
                double ps = 0;
                for (int j = low; j <= high; j++) {
                  ps += p[j] / sum;
                  if (ps > flip || j == high) {
                    r.addAt(j, ite);
                    break;
                  }
                }
              }
            }

            out.add(r, pw.w);
          }
          timeSingle += System.currentTimeMillis() - t1;
        }
        else {
          // continue the agony
          long tg = System.currentTimeMillis();
          PreferenceSet tc = s.get(0).p;
          int low = 0;
          int high = i;

          Set<Item> higher = tc.getHigher(item);
          Set<Item> lower = tc.getLower(item);
          for (int j = 0; j < prefix.size(); j++) {
            Item it = prefix.get(j);
            if (higher.contains(it)) low = j + 1;
            if (lower.contains(it) && j < high) high = j;
          }

          
          if (low == high) {
            Ranking r = new Ranking(prefix);
            r.addAt(low, item);
            //Logger.info("Whole group add " + group.size());
            nextGroups.add(group, r);
          }
          else {        
            double sum = 0;
            double[] p = new double[high+1];                
            for (int j = low; j <= high; j++) {
              p[j] = Math.pow(model.getPhi(), i - j);
              sum += p[j];
            }
            
            //Logger.info("Common calculation, single add for " + s.size());
            
            for (PW pw: s) {
              Ranking r = new Ranking(prefix);
              double flip = MathUtils.RANDOM.nextDouble();
              double ps = 0;
              for (int j = low; j <= high; j++) {
                ps += p[j] / sum;
                if (ps > flip || j == high) {
                  r.addAt(j, item);
                  nextGroups.add(pw.p, pw.w, r);
                  break;
                }
              }
            }
          }
          timeGroup += System.currentTimeMillis() - tg;
        }
        
      } // for groups   
      
      long sum = 0;
      double avg = 0;
      int min = Integer.MAX_VALUE;
      int max = Integer.MIN_VALUE;
      for (AMPGroup group: groups) {
        sum += group.size();
        min = Math.min(min, group.size());
        max = Math.max(max, group.size());
      }
      avg = 1d * sum / groups.size();
      if (groups.isEmpty()) Logger.info("Item %s | Groups: %d", item, groups.size());
      else Logger.info("Item %s | Groups: %d | Distinct users: %d | Min / Avg / Max per group: %d / %.1f / %d | Time singles: %d ms | Time groups: %d ms | Total time: %d ms", item, groups.size(), sum, min, avg, max, timeSingle, timeGroup, System.currentTimeMillis() - start);
      groups = nextGroups;      
    }
    
    Sample gs = groups.getSample(); 
    if (gs != null) { 
      out.addAll(gs);
      Logger.info("Added %d", gs.size());
    }
    return out;
  }
 
  
  public static void main(String[] args) {    
    ItemSet items = new ItemSet(10);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    Sample sample = MallowsUtils.sample(model, 1000000);
    Filter.remove(sample, 0.5);
    
    {      
      long start = System.currentTimeMillis();
      AMPSampler sampler = new AMPSampler(model);
      Sample out = sampler.sample(sample);      
      // System.out.println(out);
      Logger.info("Done %d in %d ms", out.size(), System.currentTimeMillis() - start);
    }
    
    {      
      Sample in = new Sample(items);
      for (RW rw: sample) {
        in.addWeight(rw.r, rw.w);
      }
      System.out.println("Packed size: " + in.size());
      
      long start = System.currentTimeMillis();
      AMPSampler sampler = new AMPSampler(model);
      Sample out = sampler.sample(in);      
      // System.out.println(out);
      Logger.info("Done %d in %d ms", out.size(), System.currentTimeMillis() - start);
    }
    
    {
      long start = System.currentTimeMillis();
      AMPGroupSampler sampler = new AMPGroupSampler(model);
      Sample out = sampler.sample(sample);
      // System.out.println(out);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
      int c = 0;
      Logger.info("Sum Weights: %f", out.sumWeights());
      for (RW rw: out) {
        // if (c < 30) System.out.println(rw.r + " " + rw.w);
        c++;
        if (rw.r.size() != items.size()) Logger.info("ERROR: " + rw.r);
      }
    }
    
//    System.out.println(sample);
//    System.out.println();
//    System.out.println(out);


//    Ranking r1 = Ranking.fromStringById(items, "1-3-6-9-7-8");
//    Ranking r2 = Ranking.fromStringById(items, "0-1-3-2-4-6-9-5-7-8");
//    System.out.println(r2.isConsistent(r1));
    
  }
  
}
