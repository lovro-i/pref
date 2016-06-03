package edu.drexel.cs.db.db4pref.mixture;

import edu.drexel.cs.db.db4pref.distance.KendallTauDistance;
import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.core.RankingSample;
import edu.drexel.cs.db.db4pref.kemeny.BubbleTableKemenizator;
import edu.drexel.cs.db.db4pref.kemeny.Kemenizator;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.util.Logger;
import java.util.ArrayList;
import java.util.List;

/** Compacts MallowsMixtureModel by merging very similar models within.
  * Models are considered 'similar' if phi distance is below phiThreshold, and center distance relative to max distance is below centerThreshold
  */
public class MallowsMixtureCompactor {
  
  private double phiThreshold = 0.05;
  private double centerThreshold = 0.05;
  
  
  /** Creates compactor with default values (0.05, 0.05) */
  public MallowsMixtureCompactor() {    
  }
  
  /** Creates compactor with the same value for phi and center threshold */
  public MallowsMixtureCompactor(double threshold) {
    this(threshold, threshold);  
  }
  
  /** Creates compactor with custom values for phi and center threshold */
  public MallowsMixtureCompactor(double centerThreshold, double phiThreshold) {
    this.centerThreshold = centerThreshold;
    this.phiThreshold = phiThreshold;
  }

  /** Still doing nothing, but it will... */
  public MallowsMixtureModel compact(MallowsMixtureModel model) {
    ItemSet items = model.getItemSet();
    int n = items.size();
    long maxDist = n * (n-1) / 2;
    
    List<MallowsMixtureModel> clusters = new ArrayList<MallowsMixtureModel>();
    List<Double> weights = new ArrayList<Double>();
    
    for (int i = 0; i < model.size(); i++) {
      MallowsModel modelToAdd = model.getModel(i);
      double weightToAdd = model.getWeight(i);
      
      boolean found = false;
      for (int c = 0; c < clusters.size(); c++) {
        MallowsMixtureModel candidateModels = clusters.get(c);
        for (int j = 0; j < candidateModels.size(); j++) {
          MallowsModel candidate = candidateModels.getModel(j);
          if (Math.abs(modelToAdd.getPhi() - candidate.getPhi()) < phiThreshold && KendallTauDistance.between(candidate.getCenter(), modelToAdd.getCenter()) / maxDist < centerThreshold) {
            found = true;
            break;
          }
        }
        
        if (found) {
          candidateModels.add(modelToAdd, weightToAdd);
          double ww = weights.get(c) + weightToAdd;
          weights.set(c, ww);
          break;
        }
      }
      
      if (!found) {
        MallowsMixtureModel newMixture = new MallowsMixtureModel(items);
        newMixture.add(modelToAdd, weightToAdd);
        clusters.add(newMixture);
        weights.add(weightToAdd);
      }
    }

    
    Kemenizator kemenizator = new BubbleTableKemenizator();
    MallowsMixtureModel compacted = new MallowsMixtureModel(items);
    for (int i = 0; i < clusters.size(); i++) {
      MallowsMixtureModel cluster = clusters.get(i);
      if (cluster.size() == 1) {
        compacted.add(cluster.getModel(0), weights.get(i));
      }
      else {
        RankingSample centers = cluster.getCenterSample();
        Ranking newCenter = kemenizator.kemenize(centers, centers.get(0).p);
        double phi = weightedPhi(cluster);
        MallowsModel newModel = new MallowsModel(newCenter, phi);
        compacted.add(newModel, weights.get(i));
      }
    }
    
    return compacted;
  }
  
  
  private double weightedPhi(MallowsMixtureModel model) {
    double sumPhi = 0;
    double sumWeight = 0;
    for (int i = 0; i < model.size(); i++) {
      sumPhi += model.getModel(i).getPhi() * model.getWeight(i);
      sumWeight += model.getWeight(i);
    }
    return sumPhi / sumWeight;
  }
  
}
