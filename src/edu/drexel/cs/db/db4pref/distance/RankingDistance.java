package edu.drexel.cs.db.db4pref.distance;

import edu.drexel.cs.db.db4pref.core.Ranking;

/** Interface to be implemented by different distance measures between rankings */
public interface RankingDistance {

  public double distance(Ranking rank1, Ranking rank2);
}
