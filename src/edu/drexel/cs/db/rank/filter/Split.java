package edu.drexel.cs.db.rank.filter;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.rating.Ratings;
import edu.drexel.cs.db.rank.rating.RatingsSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.ArrayList;
import java.util.List;

/** For splitting a sample into train and test, and similar */
public class Split {

  /** Randomly splits the sample into two, whereas the former contains the specified part of the rankings */
  public static List<Sample> twoFold(Sample sample, double first) {
    List<Sample> samples = new ArrayList<Sample>();
    samples.add(new Sample(sample.getItemSet()));
    samples.add(new Sample(sample.getItemSet()));
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i).r;
      double w = sample.getWeight(i);
      double flip = MathUtils.RANDOM.nextDouble();
      if (flip < first) samples.get(0).add(r, w);
      else samples.get(1).add(r, w);
      
    }
    return samples;
  }
  
  public static List<RatingsSample> twoFold(RatingsSample sample, double first) {
    List<RatingsSample> samples = new ArrayList<RatingsSample>();
    samples.add(new RatingsSample(sample.getItems()));
    samples.add(new RatingsSample(sample.getItems()));
    for (int i = 0; i < sample.size(); i++) {
      Ratings r = sample.get(i);
      double w = sample.getWeight(r);
      double flip = MathUtils.RANDOM.nextDouble();
      if (flip < first) samples.get(0).add(r, w);
      else samples.get(1).add(r, w);
      
    }
    return samples;
  }
  
  
  /** Randomly splits the sample into n (more or less) equally sized samples. */
  public static List<Sample> nFold(Sample sample, int n) {
    List<Sample> samples = new ArrayList<Sample>();
    for (int i = 0; i < n; i++) 
      samples.add(new Sample(sample.getItemSet()));      
    
    for (int i = 0; i < sample.size(); i++) {
      Ranking r = sample.get(i).r;
      double w = sample.getWeight(i);
      int index = MathUtils.RANDOM.nextInt(n);
      samples.get(index).add(r, w);
    }
    return samples;
  }
  
}
