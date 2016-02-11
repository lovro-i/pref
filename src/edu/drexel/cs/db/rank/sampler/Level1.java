package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.RW;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import java.util.HashMap;
import java.util.Map;

/** Maps from index to Level2 */
public class Level1 {

  private final Ranking reference;
  private final Map<Integer, Level2> map = new HashMap<Integer, Level2>();
  
  
  public Level1(Ranking reference, Sample sample) {
    this.reference = reference;
    build(sample);
  }
  
  private void build(Sample sample) {
    for (int i = 0; i < reference.size(); i++) {
      Level2 level2 = new Level2(reference, sample, i); 
      map.put(i, level2);
    }
    
    Level2 zero = map.get(0);
    Ranking first = new Ranking(reference.getItemSet());
    first.add(reference.get(0));
    for (RW rw: sample) {
      zero.add(new PW(rw.r, rw.w), first);
    }
  }
  
  public Level2 getLevel2(int index) {
    return map.get(index);
  }
}
