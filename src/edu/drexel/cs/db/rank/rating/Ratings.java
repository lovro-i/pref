package edu.drexel.cs.db.rank.rating;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.SparsePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.sampler.FullSample;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Set of ratings of a single user */
public class Ratings extends HashMap<Item, Float> {

  private final ItemSet items;

  public Ratings(ItemSet itemSet) {
    this.items = itemSet;
  }
  
  
  /** Returns list of items with the given rating */
  public List<Item> getItems(float rating) {
    List<Item> items = new ArrayList<Item>();
    for (Item e: this.keySet()) {
      float r = this.get(e);
      if (r == rating) items.add(e);
    }
    return items;
  }
  
  public ItemSet getItemSet() {
    return items;
  }

  /** Get groups of equally rated items */
  public List<List<Item>> getGroups() {
    Set<Float> values = new HashSet<Float>(this.values());
    Object[] vals = values.toArray();
    Arrays.sort(vals);
   
    List<List<Item>> groups = new ArrayList<List<Item>>();
    for (int i = vals.length - 1; i >= 0; i--) {
      float r = (Float) vals[i];
      groups.add(getItems(r));
    }
    
    return groups;
  }
  
  
  
  public RankingSample toSample() {
    List<List<Item>> groups = getGroups();    
    
    RankingSample sample = null;
    for (List<Item> le: groups) {
      RankingSample s = new FullSample(this.items, le);
      if (sample == null) sample = s;
      else sample = sample.multiply(s);
    }
    
    double w = 1d / sample.size();
    RankingSample wd = new RankingSample(items);
    for (PW pw: sample) {
      wd.add(pw);
    }
    return wd;
  }
  
  private long getSampleSize(List<List<Item>> groups) throws ArithmeticException {
    BigInteger s = BigInteger.ONE;
    for (List<Item> le: groups) {
      s = s.multiply(MathUtils.factorial(le.size()));
    }
    return s.longValueExact();
  }
  
  public RankingSample toSample(int max) {
    List<List<Item>> groups = getGroups();    
    boolean shrink;
    try {
      long ss = getSampleSize(groups);
      shrink = ss > max;
    }
    catch (ArithmeticException e) {
      shrink = true;
    }    
    if (!shrink) return toSample();
    
    
    List<Ranking> subrankings = new ArrayList<Ranking>();
    for (List<Item> le: groups) {      
      Ranking r = new Ranking(items);
      subrankings.add(r);
      for (Item e: le) r.add(e);      
    }

    RankingSample sample = new RankingSample(items);
    double w = 1d / max;
    for (int i = 0; i < max; i++) {
      Ranking ranking = new Ranking(items);
      for (Ranking r: subrankings) {
        r.randomize();
        ranking.add(r);
      }
      sample.add(ranking, w);
    }
    return sample;
  }
  
  /** Create one random ranking from this ratings */
  public Ranking toRanking() {
    List<List<Item>> groups = getGroups(); 
    
    Ranking ranking = new Ranking(items);
    for (List<Item> le: groups) {      
      Ranking r = new Ranking(items);
      for (Item e: le) r.add(e);      
      r.randomize();
      ranking.add(r);
    }
    
    return ranking;
  }
  
  
  public PreferenceSet toPreferences() {
    List<List<Item>> groups = getGroups();
    SparsePreferenceSet preferences = new SparsePreferenceSet(items);
    for (int i = 0; i < groups.size()-1; i++) {
      for (Item e1: groups.get(i)) {
        for (int j = i+1; j < groups.size(); j++) {
          for (Item e2: groups.get(j)) preferences.add(e1, e2);          
        }
      }
    }
    return preferences;
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    items.letters();
    
    Ratings ratings = new Ratings(items);
    ratings.put(items.get(0), 1f); // A: 1
    ratings.put(items.get(1), 4f); // B: 4
    ratings.put(items.get(2), 3f); // C: 3
    ratings.put(items.get(3), 3f); // D: 3
    ratings.put(items.get(4), 4f); // E: 4
    ratings.put(items.get(5), 3f); // F: 3
    ratings.put(items.get(6), 2f); // G: 2
    // BE | CDF | G | A 
    
    RankingSample sample = ratings.toSample(5);
    System.out.println(sample);
    
    PreferenceSet prefs = ratings.toPreferences();
    System.out.println(prefs);
  }

  
}
