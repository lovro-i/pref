package edu.drexel.cs.db.db4pref.datasets;

import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.loader.SampleLoader;
import edu.drexel.cs.db.db4pref.core.DensePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Sample;
import edu.drexel.cs.db.db4pref.core.PreferenceSet;
import java.io.File;
import java.io.IOException;

/** Class used for loading and accessing Irish dataset */
public class Irish {

  private RankingSample sample;
  
  /** Load Irish dataset from the file with specified filename */
  public Irish(String filename) throws IOException {
    this(new File(filename));
  }
  
  /** Load Irish dataset from the specified file */
  public Irish(File data) throws IOException {
    SampleLoader loader = new SampleLoader(false, false, true);
    this.sample = loader.loadSample(data);
  }
  
  public RankingSample getRankingSample() {
    return sample;
  }
  
  public Sample<PreferenceSet> transitiveClosure() {
    return sample.transitiveClosure();
  }


  
  public static void main(String[] args) throws IOException {
    String data = "C:\\Projects\\Rank\\Data\\irish\\ranking\\dnorth.txt";
    Irish irish = new Irish(data);
    System.out.println(irish.transitiveClosure());
  }
}
