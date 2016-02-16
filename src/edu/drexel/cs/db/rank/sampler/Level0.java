package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import java.util.HashMap;
import java.util.Map;

/** Maps from index to Level2 */
public class Level0 {

  private final Ranking reference;
  private final Map<Integer, Level1> map = new HashMap<Integer, Level1>();
  
  
  public Level0(Ranking reference, Sample<? extends PreferenceSet> sample) {
    this.reference = reference;

    // initialize Level1s
    for (int i = 0; i < reference.size()-1; i++) {
      Level1 level1 = new Level1(reference, sample, i); 
      map.put(i, level1);
    }
    
    // initialize level1 zero
    Level1 zero = map.get(0);
    Ranking firstItem = new Ranking(reference.getItemSet());
    firstItem.add(reference.get(0));
    for (PW pw: sample) {
      zero.add(pw, firstItem);
    }
  }
  
  public Level1 getLevel1(int index) {
    return map.get(index);
  }
}
