package edu.drexel.cs.db.db4pref.data.datasets;

import edu.drexel.cs.db.db4pref.data.SampleLoader;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.core.DensePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import java.io.File;
import java.io.IOException;

/** Class used for loading and accessing APA dataset */
public class APA {

  private RankingSample sample;
  
  /** Load APA dataset from the filename */
  public APA(String filename) throws IOException {
    this(new File(filename));
  }
  
  /** Load APA dataset from the file */
  public APA(File data) throws IOException {
    SampleLoader loader = new SampleLoader(false, true, true, false);
    this.sample = loader.loadSample(data);
  }
  
  public RankingSample getRankingSample() {
    return sample;
  }
  
  public Sample<PreferenceSet> transitiveClosure() {
    return sample.transitiveClosure();
  }

  
  public static void main(String[] args) throws IOException {
    String data = "C:\\Projects\\Rank\\Data\\apa\\ranking\\APA_2000.csv";
    APA apa = new APA(data);
    System.out.println(apa.getRankingSample());
  }
}
