package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.util.MathUtils;

/** Creates new sample by sampling from the current one, with replacement */
public class Resampler {

  private Sample sample;
  
  public Resampler(Sample sample) {
    this.sample = sample;
  }
  
  /** Create random sample of size 'size', with replacement */
  public Sample resample(int size) {
    Sample resample = new Sample(sample.getItemSet());
    for (int i = 0; i < size; i++) {
      int index = MathUtils.RANDOM.nextInt(sample.size());
      RW rw = sample.get(index);
      resample.add(rw);
    }
    return resample;
  }
  
  
  /** Create random sample of the same size as the original sample, with replacement */
  public Sample resample() {
    return resample(sample.size());
  }
  
}
