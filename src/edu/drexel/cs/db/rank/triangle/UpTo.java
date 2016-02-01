package edu.drexel.cs.db.rank.triangle;

import edu.drexel.cs.db.rank.core.Item;
import edu.drexel.cs.db.rank.core.Ranking;
import java.util.Map;

/** Returns info on ranking projection up to item max:
 * 
 * UpTo.previous Previous item in projection before the looked up one. Null if the looked up is first, or the looked up was not found in the incomplete ranking
 * UpTo.position Position of the looked up item in the projection ranking. -1 if not found
 */
public class UpTo {

  /** Previous item in projection before the looked up one. Null if the looked up is first, or the looked up was not found in the incomplete ranking */
  public final Item previous;
  
  /** position of the looked up item in the projection ranking, -1 if not found */
  public final int position;
  
  public UpTo(Ranking ranking, int max, Map<Item, Integer> referenceIndex) {
    int pos = -1;
    Item prev = null;
    boolean found = false;
    for (int i=0; i<ranking.size(); i++) {
      Item e = ranking.get(i);
      int index = referenceIndex.get(e);
      if (index <= max) pos++;
      if (index == max) {
        found = true;
        break;
      }
      if (index < max && pos != -1) prev = e;
    }
    this.previous = found ? prev : null;
    this.position = found ? pos : -1;
  }
  
}
