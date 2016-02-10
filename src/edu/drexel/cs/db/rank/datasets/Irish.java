package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.preference.DensePreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.io.File;
import java.io.IOException;


public class Irish {

  private Sample sample;
  
  public Irish(String filename) throws IOException {
    this(new File(filename));
  }
  
  public Irish(File data) throws IOException {
    SampleLoader loader = new SampleLoader(false, false);
    this.sample = loader.loadSample(data);
  }
  
  public Sample getRankingSample() {
    return sample;
  }
  
  public PreferenceSample getPreferenceSample() {
    PreferenceSample ps = new PreferenceSample(sample.getItemSet());
    for (RW rw: sample) {
      ps.add(DensePreferenceSet.fromTopKRanking(rw.r), rw.w);
    }
    return ps;
  }

  
  public static void main(String[] args) throws IOException {
    String data = "C:\\Projects\\Rank\\Data\\irish\\ranking\\dnorth.txt";
    Irish irish = new Irish(data);
    System.out.println(irish.getPreferenceSample());
  }
}
