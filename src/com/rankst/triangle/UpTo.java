package com.rankst.triangle;

import com.rankst.entity.Element;
import com.rankst.entity.Ranking;
import java.util.Map;

/** Returns info on ranking projection up to element max:
 * 
 * UpTo.previous Previous element in projection before the looked up one. Null if the looked up is first, or the looked up was not found in the incomplete ranking
 * UpTo.position Position of the looked up element in the projection ranking. -1 if not found
 */
public class UpTo {

  public final Element previous;
  public final int position;
  
  public UpTo(Ranking ranking, int max, Map<Element, Integer> referenceIndex) {
    int pos = -1;
    Element prev = null;
    boolean found = false;
    for (int i=0; i<ranking.size(); i++) {
      Element e = ranking.get(i);
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
