package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.IOException;
import java.util.List;

public class AMPGSampler {

  protected MallowsModel model;

  public AMPGSampler(MallowsModel model) {
    this.model = model;
    Users.model = model;
  }

  public Sample sample(Sample sample) {
    Ranking reference = model.getCenter();
    PreferenceSample tcs = sample.transitiveClosure();
    Level0 level1 = new Level0(reference, tcs);
    Sample out = new Sample(sample.getItemSet());
    for (int i = 1; i < reference.size(); i++) {
      Item item = reference.get(i);
      Level1 prev = level1.getLevel2(i-1);
      Level1 next = level1.getLevel2(i);
//      Logger.info("[Item %d]\n%s", i, prev);
//      try { System.in.read(); } catch (IOException ex) { }
      List<Users> groups = prev.getGroups();
      for (Users users: groups) {
        for (PW pw: users) {
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
          
          if (r.size() == reference.size()) { out.add(r, pw.w); System.out.println(next); }
          else next.add(pw, r);
        }
      }
    }
    return out;
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    Sample sample = MallowsUtils.sample(model, 10);
    Filter.remove(sample, 0.3);
    System.out.println(sample);
    
//    {      
//      long start = System.currentTimeMillis();
//      AMPSampler sampler = new AMPSampler(model);
//      Sample out = sampler.sample(sample);      
//      System.out.println(out);
//      Logger.info("Done %d in %d ms", out.size(), System.currentTimeMillis() - start);
//    }
    
    {
      long start = System.currentTimeMillis();
      AMPGSampler sampler = new AMPGSampler(model);
      Sample out = sampler.sample(sample);
      System.out.println(out);
      Logger.info("Done in %d ms", System.currentTimeMillis() - start);
    }
  }
  
}
