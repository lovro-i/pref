package com.rankst.mixture;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.Ranking;
import com.rankst.model.MallowsModel;
import com.rankst.util.Logger;
import java.util.List;

/** Compacts MallowsMixtureModel */
public class MallowsMixtureCompactor {

  /** Still doing nothing, but it will... */
  public MallowsMixtureModel compact(MallowsMixtureModel model) {
    int n = model.getElements().size();
    System.out.println("Maximum distance: " + (n * (n-1) / 2));
    
    List<MallowsModel> models = model.getModels();
    for (int i = 0; i < models.size()-1; i++) {
      Ranking c1 = models.get(i).getCenter();
      double phi1 = models.get(i).getPhi();
      Logger.info("Distance from " + c1);
      for (int j = i+1 ; j < models.size(); j++) {      
        double phi2 = models.get(j).getPhi();
        double diff = Math.abs(2 * (phi1-phi2) / (phi1+phi2));
        Ranking c2 = models.get(j).getCenter();
        Logger.info("Distance: %.1f, phi_diff: %.3f", KendallTauRankingDistance.between(c1, c2), diff);
        
      }
      
    }
    
    
    return null;
  }
  
}
