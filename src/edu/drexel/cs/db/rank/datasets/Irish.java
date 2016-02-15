package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.io.File;
import java.io.IOException;


public class Irish {

  private RankingSample sample;
  
  public Irish(String filename) throws IOException {
    this(new File(filename));
  }
  
  public Irish(File data) throws IOException {
    SampleLoader loader = new SampleLoader(false, false, true);
    this.sample = loader.loadSample(data);
  }
  
  public RankingSample getRankingSample() {
    return sample;
  }
  
  public Sample<DensePreferenceSet> transitiveClosure() {
    return sample.transitiveClosure();
  }


  
  public static void main(String[] args) throws IOException {
    String data = "C:\\Projects\\Rank\\Data\\irish\\ranking\\dnorth.txt";
    Irish irish = new Irish(data);
    System.out.println(irish.transitiveClosure());
  }
}
