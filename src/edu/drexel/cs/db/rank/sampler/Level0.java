package edu.drexel.cs.db.rank.sampler;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.preference.PreferenceSample;
import edu.drexel.cs.db.rank.preference.PreferenceSample.PW;
import java.util.HashMap;
import java.util.Map;

/** Maps from index to Level2 */
public class Level0 {

  private final Ranking reference;
  private final Map<Integer, Level1> map = new HashMap<Integer, Level1>();
  
  
  public Level0(Ranking reference, PreferenceSample sample) {
    this.reference = reference;
    build(sample);
  }
  
  private void build(PreferenceSample sample) {
    for (int i = 0; i < reference.size(); i++) {
      Level1 level1 = new Level1(reference, sample, i); 
      map.put(i, level1);
    }
    
    Level1 zero = map.get(0);
    Ranking first = new Ranking(reference.getItemSet());
    first.add(reference.get(0));
    for (PW pw: sample) {
      zero.add(pw, first);
    }
  }
  
  public Level1 getLevel2(int index) {
    return map.get(index);
  }
}
