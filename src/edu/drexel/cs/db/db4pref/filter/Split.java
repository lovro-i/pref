package edu.drexel.cs.db.db4pref.filter;

import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import java.util.ArrayList;
import java.util.List;

/** For splitting a sample into train and test, and similar */
public class Split {

  
  /** Randomly splits the sample into two. Each ranking goes to the first one with the given probability */
  public static List<Sample<? extends PreferenceSet>> twoFold(Sample<? extends PreferenceSet> sample, double first) {
    List<Sample<? extends PreferenceSet>> samples = new ArrayList<Sample<? extends PreferenceSet>>();
    samples.add(new Sample(sample.getItemSet()));
    samples.add(new Sample(sample.getItemSet()));
    for (PW pw: sample) {
      double flip = MathUtils.RANDOM.nextDouble();
      if (flip < first) samples.get(0).add(pw);
      else samples.get(1).add(pw);
      
    }
    return samples;
  }
  
  public static List<RankingSample> twoFold(RankingSample sample, double first) {
    List<RankingSample> samples = new ArrayList<RankingSample>();
    samples.add(new RankingSample(sample.getItemSet()));
    samples.add(new RankingSample(sample.getItemSet()));
    for (PW pw: sample) {
      double flip = MathUtils.RANDOM.nextDouble();
      if (flip < first) samples.get(0).add(pw);
      else samples.get(1).add(pw);
      
    }
    return samples;
  }
  
  /** Randomly splits the sample into n (more or less) equally sized samples. */
  public static List<Sample<PreferenceSet>> nFold(Sample<? extends PreferenceSet> sample, int n) {
    List<Sample<PreferenceSet>> samples = new ArrayList<Sample<PreferenceSet>>();
    for (int i = 0; i < n; i++) {
      samples.add(new Sample(sample.getItemSet()));
    }
    
    for (PW pw: sample) {
      int index = MathUtils.RANDOM.nextInt(n);
      samples.get(index).add(pw);
    }
    return samples;
  }
  
}
