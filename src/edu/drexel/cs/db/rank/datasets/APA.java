package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.core.Sample;
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
  
  public Sample<DensePreferenceSet> transitiveClosure() {
    return sample.transitiveClosure();
  }

  
  public static void main(String[] args) throws IOException {
    String data = "C:\\Projects\\Rank\\Data\\apa\\ranking\\APA_2000.csv";
    APA apa = new APA(data);
    System.out.println(apa.getRankingSample());
  }
}
