package edu.drexel.cs.db.db4pref.sampler;

import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.Sample.PW;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import edu.drexel.cs.db.db4pref.util.MathUtils;

/** Creates new sample by sampling from the current one, with replacement (for bootstrapping) */
public class Resampler {

  private Sample sample;
  
  public Resampler(Sample<? extends PreferenceSet> sample) {
    this.sample = sample;
  }
  
  /** Create random sample of size 'size', with replacement */
  public Sample<? extends PreferenceSet> resample(int size) {
    Sample<? extends PreferenceSet> resample = new Sample<PreferenceSet>(sample.getItemSet());
    for (int i = 0; i < size; i++) {
      int index = MathUtils.RANDOM.nextInt(sample.size());
      PW pw = (PW) sample.get(index);
      resample.add(pw);
    }
    return resample;
  }
  
  
  /** Create random sample of the same size as the original sample, with replacement */
  public Sample<? extends PreferenceSet> resample() {
    return resample(sample.size());
  }
  
}
