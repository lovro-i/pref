package edu.drexel.cs.db.rank.entity;

import edu.drexel.cs.db.rank.generator.FullSample;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Set of ratings of a single user */
public class Ratings extends HashMap<Element, Float> {

  private final ElementSet elements;

  public Ratings(ElementSet elements) {
    this.elements = elements;
  }
  
  
  /** Returns list of elements with the given rating */
  public List<Element> getElements(float rating) {
    List<Element> elements = new ArrayList<Element>();
    for (Element e: this.keySet()) {
      float r = this.get(e);
      if (r == rating) elements.add(e);
    }
    return elements;
  }
  
  public ElementSet getElements() {
    return elements;
  }

  /** Get groups of equally rated items */
  public List<List<Element>> getGroups() {
    Set<Float> values = new HashSet<Float>(this.values());
    Object[] vals = values.toArray();
    Arrays.sort(vals);
   
    List<List<Element>> groups = new ArrayList<List<Element>>();
    for (int i = vals.length - 1; i >= 0; i--) {
      float r = (Float) vals[i];
      groups.add(getElements(r));
    }
    
    return groups;
  }
  
  
  
  public Sample toSample() {
    List<List<Element>> groups = getGroups();    
    
    Sample sample = null;
    for (List<Element> le: groups) {
      Sample s = new FullSample(this.elements, le);
      if (sample == null) sample = s;
      else sample = sample.multiply(s);
    }
    
    sample.setWeights(1d / sample.size());
    return sample;
  }
  
  private long getSampleSize(List<List<Element>> groups) throws ArithmeticException {
    BigInteger s = BigInteger.ONE;
    for (List<Element> le: groups) {
      s = s.multiply(MathUtils.factorial(le.size()));
    }
    return s.longValueExact();
  }
  
  public Sample toSample(int max) {
    List<List<Element>> groups = getGroups();    
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
    for (List<Element> le: groups) {      
      Ranking r = new Ranking(elements);
      subrankings.add(r);
      for (Element e: le) r.add(e);      
    }

    Sample sample = new Sample(elements);
    double w = 1d / max;
    for (int i = 0; i < max; i++) {
      Ranking ranking = new Ranking(elements);
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
    List<List<Element>> groups = getGroups(); 
    
    Ranking ranking = new Ranking(elements);
    for (List<Element> le: groups) {      
      Ranking r = new Ranking(elements);
      for (Element e: le) r.add(e);      
      r.randomize();
      ranking.add(r);
    }
    
    return ranking;
  }
  
  
  public PreferenceSet toPreferences() {
    List<List<Element>> groups = getGroups();
    PreferenceSet preferences = new SparsePreferenceSet(elements);
    for (int i = 0; i < groups.size()-1; i++) {
      for (Element e1: groups.get(i)) {
        for (int j = i+1; j < groups.size(); j++) {
          for (Element e2: groups.get(j)) preferences.add(e1, e2);          
        }
      }
    }
    return preferences;
  }
  
  public static void main(String[] args) {
    ElementSet elements = new ElementSet(10);
    elements.letters();
    
    Ratings ratings = new Ratings(elements);
    ratings.put(elements.get(0), 1f); // A: 1
    ratings.put(elements.get(1), 4f); // B: 4
    ratings.put(elements.get(2), 3f); // C: 3
    ratings.put(elements.get(3), 3f); // D: 3
    ratings.put(elements.get(4), 4f); // E: 4
    ratings.put(elements.get(5), 3f); // F: 3
    ratings.put(elements.get(6), 2f); // G: 2
    // BE | CDF | G | A 
    
    Sample sample = ratings.toSample(5);
    System.out.println(sample);
    
    PreferenceSet prefs = ratings.toPreferences();
    System.out.println(prefs);
  }

  
}
