package edu.drexel.cs.db.rank.core;

import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.util.FileUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.StringTokenizer;

/**
 * Sample of rankings. Can be weighted if rankings are added through add(Ranking
 * ranking, double weight)
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
    Ranking[] rankings = new Ranking[this.size()];
    for (int i = 0; i < rankings.length; i++) {
      rankings[i] = (Ranking) this.get(i).p;
    }
    return rankings;
  }

  public Ranking getRanking(int index) {
    return (Ranking) this.getPreferenceSet(index);
  }
  

  public Ranking getPreferenceSet(int index) {
    return (Ranking) super.getPreferenceSet(index);
  }

}
