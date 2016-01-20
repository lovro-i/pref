package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.distance.RankingDistance;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.util.Logger;
import java.util.ArrayList;
import java.util.List;

/** Textual report of the reconstructed Mixture of Mallows */
public class MallowsMixtureComparator {
  
  private final MallowsMixtureModel originalModel;
  private final RankingDistance dist = new KendallTauDistance();

  
  public MallowsMixtureComparator(MallowsMixtureModel original) {
    this.originalModel = original;
  }
  
  public void compare(MallowsMixtureModel model) {
    compareCenters(model.getCenters());
  }
  
  public void compareCenters(List<Ranking> centers) {
    List<Ranking> originalCenters = originalModel.getCenters();
    List<Ranking> closest = new ArrayList<Ranking>();
    
    for (int i = 0; i < originalCenters.size(); i++) {
      if (centers.isEmpty()) break;
      
      int index = findClosest(originalCenters.get(i), centers);
      Ranking c = centers.remove(index);
      closest.add(c);
    }
    
    // Compare number of clusters
    if (originalCenters.size() == closest.size()) {
      Logger.info("[GOOD] The number of models (centers) is equal");
    }
    else if (originalCenters.size() > closest.size()) {
      Logger.info(String.format("[WARNING] There are more original models (centers) than the reconstructed ones: %d > %d", originalCenters.size(), closest.size()));
    }
    else {
      Logger.info(String.format("[WARNING] There are more reconstucted centers than the original ones: %d > %d", closest.size(), originalCenters.size()));
    }
    
    // Paired centers
    Logger.info("Paired centers (original, reconstructed, distance):");
    for (int i = 0; i < Math.max(originalCenters.size(), closest.size()); i++) {
      Ranking oc = i < originalCenters.size() ? originalCenters.get(i) : null;
      Ranking rc = i < closest.size() ? closest.get(i) : null;
      
      StringBuilder sb = new StringBuilder();
      if (oc == null) sb.append("-");
      else sb.append(oc);
      sb.append(", ");
      if (rc == null) sb.append("-");
      else sb.append(rc);
      sb.append(", ");
      if (oc != null && rc != null) sb.append(dist.distance(oc, rc));
      else sb.append("-");
      
      Logger.info(sb.toString());
    }
  }
  
  /** Returns the index of the closest one in the list */
  private int findClosest(Ranking center, List<Ranking> centers) {
    int minIndex = -1;
    double minDist = Double.POSITIVE_INFINITY;
    
    for (int i = 0; i < centers.size(); i++) {      
      double d = dist.distance(center, centers.get(i));
      if (d < minDist) {
        minDist = d;
        minIndex = i;
      }
    }
    
    return minIndex;
  }
  
}
