package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.MutablePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import java.util.Random;

/**
 * Class that stores information for each item in the ItemSet about its missing
 * probability
 */
public class MissingProbabilities {

  private static Random random = new Random();

  private ItemSet items;
  private double[] miss;

  public MissingProbabilities(ItemSet items, double[] miss) {
    this.items = items;
    this.miss = new double[items.size()];
    for (int i = 0; i < this.miss.length; i++) {
      this.miss[i] = miss[i];
    }
  }

  /**
   * create the missing statistics from the sample
   */
  public MissingProbabilities(RankingSample sample) {
    this.items = sample.getItemSet();
    this.miss = new double[items.size()];

    int[] counts = new int[items.size()];
    for (PW<Ranking> pw : sample) {
      for (Item e : pw.p.getItems()) {
        counts[e.getId()] += pw.w;
      }
    }

    int ss = sample.size();
    for (int i = 0; i < counts.length; i++) {
      miss[i] = 1d * (ss - counts[i]) / ss;
    }
  }

  // Linear: firstPoint, lastPoint, missingP = (lastPoint-firstPoint)*(k-1)/(n-1) + firstpoint
  public static MissingProbabilities uniform(ItemSet items, double p) {
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      miss[i] = p;
    }
    return new MissingProbabilities(items, miss);
  }

  // Linear: firstPoint, lastPoint, missingP = (lastPoint-firstPoint)*(k-1)/(n-1) + firstpoint
  public static MissingProbabilities linear(ItemSet items, double firstPoint, double lastPoint) {
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    double missingIncreasingRate = (lastPoint - firstPoint) / (itemsSize - 1);
    for (int i = 0; i < itemsSize; i++) {
      miss[i] = missingIncreasingRate * i + firstPoint;
    }
    return new MissingProbabilities(items, miss);
  }

  // Geometric: p, missingP = 1 - (1-p)^(k-1)*p
  public static MissingProbabilities geometric(ItemSet items, double p) {
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      miss[i] = 1 - Math.pow(1 - p, i) * p;
    }
    return new MissingProbabilities(items, miss);
  }

  // Exponential: lambda, missingP = 1 - lambda*e^(-lambda*k)
  public static MissingProbabilities exponential(ItemSet items, double lambda) {
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      miss[i] = 1 - lambda * Math.pow(Math.E, (-lambda * i));
    }
    return new MissingProbabilities(items, miss);
  }

  /**
   * Remove items randomly from the ranking with specified probabilities
   */
  public void remove(Ranking ranking) {
    for (int i = ranking.size() - 1; i >= 0; i--) {
      Item e = ranking.get(i);
      double flip = random.nextDouble();
      if (flip < get(e)) {
        ranking.remove(i);
      }
    }
  }

  /**
   * Remove preferences randomly from this PreferenceSet Each preference should
   * be removed with probability that either item1 or item2 is removed For
   * PreferenceSets, you should remove a preference with probability that either
   * of them is removed. That is, the pair (item1 > item2) remains in the set
   * with probability (1 - pMissing(item1) * (1 - pMissing(item2)).
   *
   * @param prefs is an instance of a MutablePreferenceSet
   */
  public void remove(MutablePreferenceSet prefs) {
    int itemsSize = prefs.getItemSet().size();
    for (int i = 0; i < itemsSize - 1; i++) {
      for (int j = i + 1; j < itemsSize; j++) {
        Item e1 = this.items.get(i);
        Item e2 = this.items.get(j);
        if (prefs.contains(e1, e2)) {
          double flip = random.nextDouble();
          double missingPairProbability = 1 - (1 - this.get(e1)) * (1 - this.get(e2));
          if (flip < missingPairProbability) {
            prefs.remove(e1, e2);
          }
        } else if (prefs.contains(e2, e1)) {
          double flip = random.nextDouble();
          double missingPairProbability = 1 - (1 - this.get(e1)) * (1 - this.get(e2));
          if (flip < missingPairProbability) {
            prefs.remove(e2, e1);
          }
        }
      }
    }
  }

  /**
   * Remove preferences randomly from the sample with specified probabilities
   *
   * @param sample
   */
  public void remove(Sample<? extends PreferenceSet> sample) {
    for (PreferenceSet ps : sample.preferenceSets()) {
//      remove(ps);
    }
  }

  public double get(Item e) {
    return miss[e.getId()];
  }

  public double get(int id) {
    return miss[id];
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < miss.length; i++) {
      Item e = items.get(i);
      sb.append(e).append(": ").append(miss[i]).append("\n");
    }
    return sb.toString();
  }

  public static void main(String[] args) {
//    ItemSet items = new ItemSet(10);
//    RankingSample sample = MallowsUtils.sample(items.getRandomRanking(), 0.3, 2000);
//    Filter.remove(sample, 0.3);
//
//    MissingProbabilities m = new MissingProbabilities(sample);
//    System.out.println(m);

    ItemSet items = new ItemSet(4);
    DensePreferenceSet prefs = new DensePreferenceSet(items);
    Item a = items.get(0);
    Item b = items.get(1);
    Item c = items.get(2);
    Item d = items.get(3);
    prefs.add(b, a);
    prefs.add(c, b);
    prefs.add(d, c);
    System.out.println("Before transitive closure:\n" + prefs.toString());
    MutablePreferenceSet tc = prefs.transitiveClosure();
    System.out.println("After tc, before missing:\n" + tc.toString());
    MissingProbabilities m = MissingProbabilities.uniform(items, 0.3);
    m.remove(tc);
    System.out.println("After missing:\n" + tc.toString());

    System.out.println("\nNow let's test missing probabilities of linear, geometric and exponential:");
    MissingProbabilities mLinear = MissingProbabilities.linear(items, 0.1, 0.9);
    System.out.println("Linear:\n" + mLinear);
    MissingProbabilities mGeometric = MissingProbabilities.geometric(items, 0.6);
    System.out.println("Geometric:\n" + mGeometric);
    MissingProbabilities mExponential = MissingProbabilities.exponential(items, 0.7);
    System.out.println("Exponential:\n" + mExponential);
  }
}
