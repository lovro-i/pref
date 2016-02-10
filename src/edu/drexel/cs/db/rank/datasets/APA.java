package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import java.io.File;
import java.io.IOException;


public class APA {

  private Sample sample;
  
  public APA(String filename) throws IOException {
    this(new File(filename));
  }
  
  public APA(File data) throws IOException {
    SampleLoader loader = new SampleLoader(false, true, true, false);
    this.sample = loader.loadSample(data);
  }
  
  public Sample getRankingSample() {
    return sample;
  }
  
  public PreferenceSample getPreferenceSample() {
    PreferenceSample ps = new PreferenceSample(sample.getItemSet());
    for (Sample.RW rw: sample) {
      ps.add(DensePreferenceSet.fromTopKRanking(rw.r), rw.w);
    }
    return ps;
  }

  
  public static void main(String[] args) throws IOException {
    String data = "C:\\Projects\\Rank\\Data\\apa\\ranking\\APA_2000.csv";
    APA apa = new APA(data);
    System.out.println(apa.getRankingSample());
  }
}
