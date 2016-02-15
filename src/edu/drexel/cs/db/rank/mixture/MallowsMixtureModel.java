package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample.PW;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mixture of Mallows models (with weights) */
public class MallowsMixtureModel {

  private final ItemSet itemSet;
  private final List<MallowsModel> models = new ArrayList<MallowsModel>();
  private final List<Double> weights = new ArrayList<Double>();
  
  private double sumWeights = 0;
  
  
  public MallowsMixtureModel(ItemSet items) {    
    this.itemSet = items;
  }
  
  public MallowsMixtureModel add(MallowsModel model, double weight) {
    models.add(model);
    weights.add(weight);
    sumWeights += weight;
    sort();
    return this;
  }

  public int size() {
    return models.size();
  }
  
  public MallowsModel getModel(int i) {
    return models.get(i);
  }
  
  /** Get normalized weight */
  public double getWeight(int i) {
    return weights.get(i) / sumWeights;
  }
  
  private void sort() {
    boolean dirty = true;
    while (dirty) {
      dirty = false;
      for (int i = 0; i < models.size() - 1; i++) {
        MallowsModel m1 = models.get(i);
        MallowsModel m2 = models.get(i+1);
        if (m1.getCenter().compareTo(m2.getCenter()) > 0) {
          Collections.swap(models, i, i+1);
          Collections.swap(weights, i, i+1);
          dirty = true;
        }
      }
    }
  }
  
  public RankingSample getCenterSample() {
    RankingSample sample = new RankingSample(itemSet);
    for (int i = 0; i < this.size(); i++) {
      Ranking r = this.getModel(i).getCenter();
      double w = this.getWeight(i);
      sample.add(r, w);
    }
    return sample;
  }
  
  
  /** @return index of a random model */
  int getRandomModel() {
    double p = MathUtils.RANDOM.nextDouble() * sumWeights;
    double acc = 0;
    int last = weights.size() - 1;
    for (int i = 0; i < last; i++) {
      acc += weights.get(i);
      if (acc > p) return i;
    }
    return last;
  }


  public ItemSet getItemSet() {
    return itemSet;
  }

  public List<MallowsModel> getModels() {
    return models;
  }

  public List<Double> getWeights() {
    return weights;
  }

  public List<Ranking> getCenters() {
    List<Ranking> centers = new ArrayList<Ranking>();
    for (MallowsModel model: models) {
      centers.add(model.getCenter());
    }
    return centers;
  }
  
  
  /** @return Probability of the ranking being generated by this model */
  public double getProbability(Ranking r) {
    double p = 0;
    for (int i = 0; i < this.size(); i++) {
      p += this.getWeight(i) * models.get(i).getProbability(r);      
    }
    return p;
  }
  
  public double getProbabilityMax(Ranking r) {
    double p = 0;
    for (int i = 0; i < this.size(); i++) {
      p = Math.max(p, models.get(i).getProbability(r));
    }
    return p;
  }
  
  
  public double getLogLikelihoodMean(RankingSample sample) {
    double ll = 0;
    for (PW<Ranking> rw: sample) {
      double p = getProbability(rw.p);
      ll += rw.w * Math.log(p);
    }
    return ll / sample.sumWeights();
  }
  
  public double getLogLikelihoodMax(RankingSample sample) {
    double ll = 0;
    for (PW<Ranking> rw: sample) {
      double p = getProbabilityMax(rw.p);
      ll += rw.w * Math.log(p);
    }
    return ll / sample.sumWeights();
  }

  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < models.size(); i++) {
      MallowsModel model = models.get(i);
      String s = String.format("[Model %d] Center = %s, phi = %.3f, weight = %.1f", i+1, model.getCenter(), model.getPhi(), 100d * weights.get(i) / sumWeights);
      sb.append(s).append("\n");
    }
    return sb.toString();
  }
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(10);
    MallowsModel model1 = new MallowsModel(items.getReferenceRanking(), 0.3);
    MallowsModel model2 = new MallowsModel(items.getRandomRanking(), 0.5);
    MallowsMixtureModel mix = new MallowsMixtureModel(items);
    mix.add(model1, 0.3);
    mix.add(model2, 0.7);
    
    RankingSample sample = MallowsUtils.sample(mix, 1000);
    
    System.out.println(mix.getLogLikelihoodMean(sample));
  }
  
}
