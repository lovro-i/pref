package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/** Sample of rankings. Can be weighted if rankings are added through add(Ranking ranking, double weight)
 * 
 */
public class RankingSample extends Sample<Ranking> {

  public RankingSample(ItemSet itemSet) {
    super(itemSet);
  }
  
  public RankingSample(RankingSample sample) {
    super(sample);
  } 
  
  public Ranking[] rankings() {
    return preferenceSets();
  }
  
  
  /** Creates a sample of cartesian products; the first half of each ranking is from this sample, the second if from Sample s */
  public RankingSample multiply(Sample<Ranking> s) {
    RankingSample sample = new RankingSample(this.itemSet);
    for (PW<Ranking> pw1: this) {
      for (PW<Ranking> pw2: s) {
        Ranking r = new Ranking(pw1.p);
        r.append(pw2.p);
        sample.add(r, pw1.w * pw2.w);
      }
    }
    return sample;
  }


  public void save(PrintWriter out) {
    out.println(this.itemSet.size());
    for (PW pw: this) {
      out.print(pw.p);
      out.print("\t");
      out.print(pw.w);
      out.println();
    }    
  }
  
  public void save(File file) throws IOException {
    PrintWriter out = FileUtils.write(file);
    save(out);
    out.close();
  }


}
