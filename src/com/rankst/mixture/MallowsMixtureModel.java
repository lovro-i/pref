package com.rankst.mixture;

import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.model.MallowsModel;
import com.rankst.util.Utils;
import java.util.ArrayList;
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
    return this;
  }

  /** @return index of a random model */
  int getRandomModel() {
    double p = Utils.random.nextDouble() * sumWeights;
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
      sb.append("[Model ").append(i+1).append("] Center = ").append(model.getCenter());
      sb.append(", phi = ").append(model.getPhi());
      sb.append(", weight = ").append(weights.get(i) / sumWeights);
      sb.append("\n");
    }
    return sb.toString();
  }
  
}
