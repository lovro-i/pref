package edu.drexel.cs.db.rank.entity;

import edu.drexel.cs.db.rank.generator.FullSample;
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

  private List<List<Element>> getGroups() {
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
    
    return sample;
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
    
    Ratings ratings = new Ratings(elements);
    ratings.put(elements.get(0), 1f); // A: 1
    ratings.put(elements.get(1), 4f); // B: 4
    ratings.put(elements.get(2), 3f); // C: 3
    ratings.put(elements.get(3), 3f); // D: 3
    ratings.put(elements.get(4), 4f); // E: 4
    ratings.put(elements.get(5), 3f); // F: 3
    ratings.put(elements.get(6), 2f); // G: 2
    // BE | CDF | G | A 
    
    Sample sample = ratings.toSample();
    System.out.println(sample);
    
    PreferenceSet prefs = ratings.toPreferences();
    System.out.println(prefs);
  }
}
