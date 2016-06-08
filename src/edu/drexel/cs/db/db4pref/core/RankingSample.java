package edu.drexel.cs.db.db4pref.core;

/** Sample of rankings. Can be weighted if rankings are added through add(Ranking ranking, double weight) */
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
