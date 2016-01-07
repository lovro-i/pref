package edu.drexel.cs.db.rank.distance;

import edu.drexel.cs.db.rank.entity.Ranking;



public interface RankingDistance {

  public double distance(Ranking rank1, Ranking rank2);
}
