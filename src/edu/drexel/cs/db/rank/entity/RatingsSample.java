package edu.drexel.cs.db.rank.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/** Weighted set of Ratings */
public class RatingsSample extends HashMap<Ratings, Double> {

  private final ElementSet elements;
  private final List<Ratings> ratingss = new ArrayList<Ratings>();

  public RatingsSample(ElementSet elements) {
    this.elements = elements;
  }
  
  public ElementSet getElements() {
    return elements;
  }
  
  public double sumWeights() {
    double s = 0;
    for (Double d: this.values()) s += d;
    return s;
  }
  
  public void add(Ratings ratings, double weight) {
    Double w = this.get(ratings);
    if (w == null) {
      w = weight;
      ratingss.add(ratings);
    }
    else {
      w += weight;
    }
    this.put(ratings, w);
  }  
  
  public Ratings get(int index) {
    return ratingss.get(index);
  }
  
  public double getWeight(Ratings ratings) {
    Double w = this.get(ratings);
    if (w == null) return 0;
    return w;
  }
  
  public void add(Ratings ratings) {
    this.add(ratings, 1);
  }
  
  /** Converts RatingsSample to rankings Sample
   * @param maxPerRatings Maximum number of rankings to add (for ratings that have multiple rankings)
   */
  public Sample toSample(int maxPerRatings) {
    Sample sample = new Sample(elements);
    for (Entry<Ratings, Double> entry: this.entrySet()) {
      Sample s = entry.getKey().toSample(maxPerRatings);
      sample.addAll(s, entry.getValue());
    }
    return sample;
  }
  
}
