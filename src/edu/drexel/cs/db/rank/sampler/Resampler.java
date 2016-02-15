package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.MathUtils;

/** Creates new sample by sampling from the current one, with replacement */
public class Resampler<PS extends PreferenceSet> {

  private Sample sample;
  
  public Resampler(Sample<PS> sample) {
    this.sample = sample;
  }
  
  /** Create random sample of size 'size', with replacement */
  public Sample<PS> resample(int size) {
    Sample<PS> resample = new Sample<PS>(sample.getItemSet());
    for (int i = 0; i < size; i++) {
      int index = MathUtils.RANDOM.nextInt(sample.size());
      PW pw = (PW) sample.get(index);
      resample.add(pw);
    }
    return resample;
  }
  
  
  /** Create random sample of the same size as the original sample, with replacement */
  public Sample<PS> resample() {
    return resample(sample.size());
  }
  
}
