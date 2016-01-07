package edu.drexel.cs.db.rank.kemeny;

import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;

public interface Kemenizator {

  public Ranking kemenize(Sample sample, Ranking start);
  
}
