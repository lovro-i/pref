package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.util.MathUtils;
import edu.drexel.cs.db.rank.util.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Mixture of Mallows models (with weights) */
public class MallowsMixtureModel {

  private final ElementSet elements;
  private final List<MallowsModel> models = new ArrayList<MallowsModel>();
  private final List<Double> weights = new ArrayList<Double>();
  
  private double sumWeights = 0;
  
  
  public MallowsMixtureModel(ElementSet elements) {    
    this.elements = elements;
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
  
  public double getWeight(int i) {
    return weights.get(i);
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
  
  public Sample getCenterSample() {
    Sample sample = new Sample(elements);
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


  public ElementSet getElements() {
    return elements;
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
  
}
