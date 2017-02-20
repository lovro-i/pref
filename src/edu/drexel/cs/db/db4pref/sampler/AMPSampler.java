package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Item;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class AMPSampler extends MallowsPosteriorSampler {

  private Random random = new Random();
  
  public AMPSampler(MallowsModel model) {
    super(model);
  }

  // Sample multiple posteriors from user preferences pref by running AMP multiple times.
  public double[] samplePosteriors(PreferenceSet pref, int samplingRepetition) {
    double[] posteriors = new double[samplingRepetition];
    for (int i = 0; i < samplingRepetition; i++) {
      posteriors[i] = samplePosterior(pref);
    }
    return posteriors;
  }
  
    /** Returns the index of highest sigma_i in the preference set */
  private int getMaxItem(PreferenceSet pref) {
    int max = 0;
    Map<Item, Integer> referenceIndex = model.getCenter().getIndexMap();
    for (Item item: pref.getItems()) {
      max = Math.max(max, referenceIndex.get(item));
    }
    return max;
  }
  
  /** Sample one posterior from user preferences v */
  public double samplePosterior(PreferenceSet pref) {
    Ranking reference = model.getCenter();
    double phi = model.getPhi();
    PreferenceSet tc = pref.transitiveClosure();
    double posterior = 1;

    Ranking r = new Ranking(model.getItemSet());
    Item item = reference.get(0);
    r.add(item);
    Set<Item> items = new HashSet(pref.getItems());
    int max = getMaxItem(pref);
    for (int i = 1; i <= max; i++) {
      item = reference.get(i);
      int low = 0;
      int high = i;
      
      if (items.contains(item)) {
        Set<Item> higher = tc.getHigher(item);
        Set<Item> lower = tc.getLower(item);
        for (int j = 0; j < r.length(); j++) {
          Item it = r.get(j);
          if (higher.contains(it)) low = j + 1;
          if (lower.contains(it) && j < high) high = j;
        }


        int where = high;
        double sum;
        if (low == high) {
          where = low;
          sum = Math.pow(phi, i - where);
        }
        else {
          sum = 0;
          double[] p = new double[high + 1];
          for (int j = low; j <= high; j++) {
            p[j] = Math.pow(phi, i - j);
            sum += p[j];
          }

          double flip = random.nextDouble();
          double ps = 0;
          for (int j = low; j <= high; j++) {
            ps += p[j] / sum;
            if (ps > flip || j == high) {
              where = j;
              break;
            }
          }
        }

        r.add(where, item);
        double z = (1 - phi) / (1 - Math.pow(phi, i + 1));
        posterior *= z * sum;
      }
      else {
        double flip = random.nextDouble();
        double ps = 0;
        int where = high;
        double s = (1 - Math.pow(phi, i+1)) / (1 - phi);
        for (int j = low; j <= high; j++) {
          double p = Math.pow(phi, i - j) / s;
          ps += p;
          if (ps > flip || j == high) {
            where = j;
            break;
          }
        }
        r.add(where, item);
      }
    }
    return posterior;
  }
  
  
  @Override
  public Ranking sample(PreferenceSet v) {
    Ranking reference = model.getCenter();
    Ranking r = new Ranking(model.getItemSet());
    PreferenceSet tc = v.transitiveClosure();

    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.length(); i++) {
      item = reference.get(i);
      int low = 0;
      int high = i;

      Set<Item> higher = tc.getHigher(item);
      Set<Item> lower = tc.getLower(item);
      for (int j = 0; j < r.length(); j++) {
        Item it = r.get(j);
        if (higher.contains(it)) {
          low = j + 1;
        }
        if (lower.contains(it) && j < high) {
          high = j;
        }
      }

      if (low == high) {
        r.add(low, item);
      } else {
        double sum = 0;
        double[] p = new double[high + 1];
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          sum += p[j];
        }

        double flip = random.nextDouble();
        double ps = 0;
        for (int j = low; j <= high; j++) {
          ps += p[j] / sum;
          if (ps > flip || j == high) {
            r.add(j, item);
            break;
          }
        }
      }
    }
    return r;
  }

  public RankingSample sample(PreferenceSet v, int size) {
    RankingSample sample = new RankingSample(model.getItemSet());
    for (int i = 0; i < size; i++) {
      sample.add(sample(v));
    }
    return sample;
  }

  public Ranking sample(Ranking v) {
    Ranking reference = model.getCenter();
    Map<Item, Integer> map = v.getIndexMap();
    Ranking r = new Ranking(model.getItemSet());

    Item item = reference.get(0);
    r.add(item);
    for (int i = 1; i < reference.length(); i++) {
      item = reference.get(i);
      int low, high;

      Integer ci = map.get(item);
      if (ci == null) {
        low = 0;
        high = i;
      } else {
        low = 0;
        high = i;

        for (int j = 0; j < r.length(); j++) {
          Item t = r.get(j);
          Integer ti = map.get(t);
          if (ti == null) {
            continue;
          }
          if (ti < ci) {
            low = j + 1;
          }
          if (ti > ci && j < high) {
            high = j;
          }
        }
      }

      if (low == high) {
        r.add(low, item);
      } else {
        double sum = 0;
        double[] p = new double[high + 1];
        for (int j = low; j <= high; j++) {
          p[j] = Math.pow(model.getPhi(), i - j);
          sum += p[j];
        }

        double flip = random.nextDouble();
        double ps = 0;
        for (int j = low; j <= high; j++) {
          ps += p[j] / sum;
          if (ps > flip || j == high) {
            r.add(j, item);
            break;
          }
        }
      }
    }
    return r;
  }

}
