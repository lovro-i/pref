package edu.drexel.cs.db.rank.reconstruct;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.KendallTauUtils;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.math.Polynomial;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.Arrays;

/** Reconstructs phi from the known (reconstructed) center and the sample finding the root of the polynomial
  * If the center is not specified, uses CenterReconstructor */
public class PolynomialReconstructor implements MallowsReconstructor {

  
  /** Normalization factor for n items */
  public static Polynomial z(int n) {
    Polynomial z = new Polynomial(1d);
    for (int i = 1; i < n; i++) {
      double[] a = new double[i+1];
      Arrays.fill(a, 1d);
      Polynomial f = new Polynomial(a);
      z = z.mul(f);
    }
    return z;
  }
  
  
  /** Polynomial of size n(n-1)/2 with number of rankings at distance d, times d */
  private static Polynomial c(int n) {
    int N = n * (n-1) / 2;
    double[] c = new double[N+1];
    for (int i = 0; i < c.length; i++) {
      c[i] = i * KendallTauUtils.getCount(n, i);
    }
    return new Polynomial(c);
  }
  

  @Override
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample) {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }
  
  
  @Override
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample, Ranking center) {
    double sumd = 0;
    for (PW pw: sample) {
      sumd += pw.w * KendallTauDistance.getInstance().distance(center, pw.p);
    }
    double meand = sumd / sample.sumWeights();
    
    int n = sample.getItemSet().size();
    Polynomial left = z(n).mul(meand);
    Polynomial right = c(n);
    
    Polynomial solve = left.sub(right);
    double phi = solve.root(0d, 1d, 0.00001d);
    return new MallowsModel(center, phi);
  }

  public static void main(String[] args) {
    double phi = 0.2;
    long start = System.currentTimeMillis();
    ItemSet items = new ItemSet(200);
    MallowsModel model = new MallowsModel(items.getRandomRanking(), phi);
    RankingSample sample = MallowsUtils.sample(model, 1000);
    PolynomialReconstructor rec = new PolynomialReconstructor();
    MallowsModel m2 = rec.reconstruct(sample);
    Logger.info("Reconstructed %f in %d ms", m2.getPhi(), System.currentTimeMillis() - start);
  }
  
}
