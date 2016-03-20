package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.HashSet;
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
   *
   * @param sample
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

  /**
   * Uniform missing probabilities. Items share the same missing probability.
   *
   * @param items is the ItemSet which will be assigned some probabilities for
   * each item,
   * @param p is the uniform missing probability.
   * @return the MissingProbabilities Class
   */
  public static MissingProbabilities uniform(ItemSet items, double p) {
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      miss[i] = p;
    }
    return new MissingProbabilities(items, miss);
  }

  /**
   * Uniform missing probabilities. Items share the same missing probability.
   *
   * @param center is center ranking which will be assigned some probabilities
   * for each item.
   * @param p is the uniform missing probability.
   * @return the MissingProbabilities Class
   */
  public static MissingProbabilities uniform(Ranking center, double p) {
    ItemSet items = center.getItemSet();
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      miss[i] = p;
    }
    return new MissingProbabilities(items, miss);
  }

  /**
   * Linear missing probabilities. missingP = (lastPoint-firstPoint)*(k-1)/(n-1)
   * + firstPoint
   *
   * @param center is center ranking which will be assigned some probabilities
   * for each item.
   * @param firstPoint
   * @param lastPoint
   * @return the MissingProbabilities Class
   */
  public static MissingProbabilities linear(Ranking center, double firstPoint, double lastPoint) {
    ItemSet items = center.getItemSet();
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    double missingIncreasingRate = (lastPoint - firstPoint) / (itemsSize - 1);
    for (int i = 0; i < itemsSize; i++) {
      int itemId = center.get(i).getId();
      miss[itemId] = missingIncreasingRate * i + firstPoint;
    }
    return new MissingProbabilities(items, miss);
  }

  /**
   * Geometric missing probabilities. missingP = 1 - (1-p)^(k-1)*p
   *
   * @param center is center ranking which will be assigned some probabilities
   * for each item.
   * @param p
   * @return the MissingProbabilities Class
   */
  public static MissingProbabilities geometric(Ranking center, double p) {
    ItemSet items = center.getItemSet();
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      int itemId = center.get(i).getId();
      miss[itemId] = 1 - Math.pow(1 - p, i) * p;
    }
    return new MissingProbabilities(items, miss);
  }

  // Exponential: lambda, missingP = 1 - lambda*e^(-lambda*k)
  /**
   * Exponential missing probabilities. lambda, missingP = 1 -
   * lambda*e^(-lambda*k)
   *
   * @param center is center ranking which will be assigned some probabilities
   * for each item.
   * @param p
   * @return the MissingProbabilities Class
   */
  public static MissingProbabilities exponential(Ranking center, double lambda) {
    ItemSet items = center.getItemSet();
    int itemsSize = items.size();
    double[] miss = new double[itemsSize];
    for (int i = 0; i < itemsSize; i++) {
      int itemId = center.get(i).getId();
      miss[itemId] = 1 - lambda * Math.pow(Math.E, (-lambda * i));
    }
    return new MissingProbabilities(items, miss);
  }

  /**
   * Remove items randomly from the ranking.
   *
   * @param r
   * @return
   */
  public void removeItems(Ranking r) {
    for (int i = r.size() - 1; i >= 0; i--) {
      Item e = r.get(i);
      double flip = random.nextDouble();
      if (flip < get(e)) {
        r.remove(i);
      }
    }
    if (r.size() < 2) {
      System.err.println("Length of ranking is less than 2.");
    }
  }

  /**
   * remove all edges (preferences) if an item is decided to be removed.
   *
   * @param prefs
   */
  public void removeItems(MapPreferenceSet prefs) {
    int itemsSize = prefs.getItemSet().size();
    HashSet<Item> missingItems = new HashSet<>();
    for (int i = 0; i < itemsSize; i++) {
      Item e = this.items.get(i);
      double flip = random.nextDouble();
      if (flip < get(e)) {
        missingItems.add(e);
      }
    }
    for (Item e : prefs.keySet()) {
      if (missingItems.contains(e)) {
        prefs.remove(e);
      } else {
        prefs.get(e).removeAll(missingItems);
      }
    }
  }

  public MapPreferenceSet removePreferences(Ranking r) {
    MapPreferenceSet tc = r.transitiveClosureToMap();
    removePreferences(tc);
    return tc;
  }

  /**
   * Remove pairs in PreferenceSet. remove a preference with 1 - (1 - p1) * (1 -
   * p2) (already implemented below, move it here).
   *
   * @param prefs
   */
  public void removePreferences(MapPreferenceSet prefs) {
    MapPreferenceSet cloneSet = prefs.deepCopy();
    for (Item eParent : cloneSet.keySet()) {
      for (Item eChild : cloneSet.get(eParent)) {
        double flip = random.nextDouble();
        double missingPairProbability = 1 - (1 - this.get(eParent)) * (1 - this.get(eChild));
        if (flip < missingPairProbability) {
          prefs.remove(eParent, eChild);
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

    ItemSet items = new ItemSet(5);
    Item a = items.get(0);
    Item b = items.get(1);
    Item c = items.get(2);
    Item d = items.get(3);
    Item e = items.get(4);

    MapPreferenceSet prefs = new MapPreferenceSet(items);
    prefs.add(b, a);
    prefs.add(c, b);
    prefs.add(d, c);
    prefs.add(d, e);

    System.out.println("Before transitive closure:\n" + prefs.toString());
    MapPreferenceSet tc = prefs.tempTransitiveClosure();
    System.out.println("After tc, before missing:\n" + tc.toString());
    MissingProbabilities m = MissingProbabilities.uniform(items, 0.3);
    m.removePreferences(tc);
    System.out.println("After missing:\n" + tc.toString());

    Ranking r = new Ranking(items);
    for (int i = items.size() - 1; i >= 0; i--) {
      r.add(items.get(i));
    }

    System.out.format("Now let's test missing probabilities of linear, geometric and exponential given ranking %s:\n\n", r);
    MissingProbabilities mLinear = MissingProbabilities.linear(r, 0.1, 0.9);
    System.out.println("Linear:\n" + mLinear);
    MissingProbabilities mGeometric = MissingProbabilities.geometric(r, 0.6);
    System.out.println("Geometric:\n" + mGeometric);
    MissingProbabilities mExponential = MissingProbabilities.exponential(r, 0.7);
    System.out.println("Exponential:\n" + mExponential);
  }
}
