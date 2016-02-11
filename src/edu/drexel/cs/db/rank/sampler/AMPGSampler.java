package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.List;

public class AMPGSampler {

  protected MallowsModel model;

  public AMPGSampler(MallowsModel model) {
    this.model = model;
    Users.model = model;
  }

  public Sample sample(Sample sample) {
    Ranking reference = model.getCenter();
    Level1 level1 = new Level1(reference, sample);
    for (int i = 1; i < reference.size(); i++) {
      Item item = reference.get(i);
      Level2 prev = level1.getLevel2(i-1);
      Level2 next = level1.getLevel2(i);
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
          
          // dodaj r u slede'i nivo'
          next.add(pw, r);
        }
      }
    }
    return null;
  }
  
  
}
