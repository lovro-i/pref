package edu.drexel.cs.db.rank.generator;

import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.Utils;

/** Creates new sample from the current one, with replacement */
public class Resampler {

  private Sample sample;
  
  public Resampler(Sample sample) {
    this.sample = sample;
  }
  
  /** Create random sample of size 'size', with replacement */
  public Sample resample(int size) {
    Sample resample = new Sample(sample.getElements());
    for (int i = 0; i < size; i++) {
      int index = MathUtils.RANDOM.nextInt(sample.size());
      Ranking r = sample.get(index);
      resample.add(r, sample.getWeight(index));
    }
    return resample;
  }
  
  
  /** Create random sample of the same size as the original sample, with replacement */
  public Sample resample() {
    return resample(sample.size());
  }
  
}
