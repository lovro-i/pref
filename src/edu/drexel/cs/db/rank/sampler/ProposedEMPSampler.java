package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;

public class ProposedEMPSampler extends MallowsSampler {

  public ProposedEMPSampler(MallowsModel model) {
    super(model);
  }

  public Ranking sample(Ranking v) {
    Ranking reference = model.getCenter();
    Ranking r = new Ranking(v);
    
    
    
    for (int i = 0; i < reference.size(); i++) {
      Item sigmai = reference.get(i);

      if (!r.contains(sigmai)) {
        Ranking rr = reference.project(r.getItems());
        double dPrevItems = KendallTauDistance.between(rr, r); // distance between r and reference's projection to r (d_previous_items)
        double sum = 0;
        double[] p = new double[r.size() + 1];
        for (int j = 0; j <= r.size(); j++) {          
          int dInsertion = Math.abs(j - i);
          int dDelta = 0;
          for (int k = 0; k < r.size(); k++) {
            Item e = rr.get(k);
            if (k < j && reference.isHigher(sigmai, e)) {
              dDelta++;
            } else if (k > j && reference.isHigher(e, sigmai)) {
              dDelta++;
            }
          }
          double dOverall = dPrevItems + dInsertion + dDelta; 
          p[j] = Math.pow(model.getPhi(), dOverall);
          sum += p[j];
        }
        double flip = MathUtils.RANDOM.nextDouble();
        double ps = 0;
        for (int j = 0; j <= r.size(); j++) {
          ps += p[j] / sum;
          if (ps >= flip) {
            r.addAt(j, sigmai);
//            System.out.format("Insert %s at %d, new ranking is %s\n",sigmai.toString(),j,r.toString());
            break;
          }
        }
      }
    }
    return r;
  }

  @Override
  public Ranking sample(PreferenceSet pref) {
    if (pref instanceof Ranking) {
      return sample((Ranking) pref);
    } else {
      throw new UnsupportedOperationException("Rankings only are supported");
    }
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    Ranking v = new Ranking(items);
    v.add(items.get(3));
    v.add(items.get(7));
    v.add(items.get(5));
    System.out.println(v);

    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.3);
    ProposedEMPSampler sampler = new ProposedEMPSampler(model);
    RankingSample sample = sampler.sample(v, 10);
    System.out.println(sample);
  }
}
