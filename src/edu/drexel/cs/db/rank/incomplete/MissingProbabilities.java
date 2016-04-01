package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.MutablePreferenceSet;
import edu.drexel.cs.db.rank.preference.Preference;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.ArrayList;
import java.util.List;
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
   * Uniform missing probabilities, specified by pairwise missing rate. Items share the same missing probability.
   *
   * @param items is the ItemSet which will be assigned some probabilities for
   * each item,
   * @param pp is the uniform missing probability PAIRWISE.
   * @return the MissingProbabilities Class
   */
  public static MissingProbabilities uniformPairwise(ItemSet items, double pp) {
    double missingItemWise = 1 - Math.sqrt(1 - pp);
    return uniform(items, missingItemWise);
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
   */
  public void removeItems(Ranking r) {
    for (int i = r.length() - 1; i >= 0; i--) {
      Item e = r.get(i);
      double flip = random.nextDouble();
      if (flip < get(e)) {
        r.remove(i);
      }
    }
  }

  /**
   * Removes all preference pairs of an item if the item is decided to be removed.
   */
  public void removeItems(PreferenceSet prefs) {
    for (int i = 0; i < prefs.getItemSet().size(); i++) {
      Item e = this.items.get(i);
      double flip = random.nextDouble();
      if (flip < get(e)) {
        prefs.remove(e);
      }
    }
  }

  /**
   * Remove pairs in PreferenceSet. remove a preference with 1 - (1 - p1) * (1 -
   * p2) (already implemented below, move it here).
   *
   * @param prefs
   */
  public void removePreferences(MutablePreferenceSet prefs) {
    List<Preference> remove = new ArrayList<Preference>();
    for (Preference pref: prefs.getPreferences()) {      
      double missingPairProbability = 1 - (1 - this.get(pref.higher)) * (1 - this.get(pref.lower));
      double flip = random.nextDouble();
      if (flip < missingPairProbability) remove.add(pref);
    }
    
    for (Preference pref: remove) prefs.remove(pref);
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


}
