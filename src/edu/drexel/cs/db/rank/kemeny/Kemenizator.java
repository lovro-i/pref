package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;

/** Interface implemented by different implementations of kemenizators */
public interface Kemenizator {

  public Ranking kemenize(Sample sample, Ranking start);
  
}
