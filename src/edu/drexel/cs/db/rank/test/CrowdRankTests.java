package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.datasets.CrowdRank;
import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.incomplete.IncompleteReconstructor;
import edu.drexel.cs.db.rank.incomplete.QuickIncompleteReconstructor;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;
import java.io.IOException;


public class CrowdRankTests {

  /** Reconstructs each hit separately */
  public static void testOne() throws IOException, Exception {
    File data = new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\datasets\\crowdrank\\hit_uid_ranking.csv");
    CrowdRank crowdRank = new CrowdRank(data);
    
    System.out.println("There are " + crowdRank.getHitCount() + " hits in the dataset");
    System.out.println("There are " + crowdRank.getFullSample().size() + " rankings in the full dataset");
    
    
    for (int hit = 1; hit <= crowdRank.getHitCount(); hit++) {
      Sample sample = crowdRank.getHitSample(hit);
      Logger.info("Reconstructing hit %d with %d rankings in the sample", hit, sample.size());
      
      // IncompleteReconstructor single = new IncompleteReconstructor(2);
      QuickIncompleteReconstructor single = new QuickIncompleteReconstructor(2);
      single.setBootstraps(30);
      MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);
      
      MallowsMixtureModel reconstructedModel = reconstructor.reconstruct(sample);
      Logger.info("Reconstructed model for hit %d is", hit);
      Logger.info(reconstructedModel);
      
      Logger.info("There are %d clusters in the reconstructed model", reconstructedModel.size());
      Logger.info("The center of the first Mallows is %s", reconstructedModel.getModel(0).getCenter());
      if (reconstructedModel.size() > 1) {
        Ranking center0 = reconstructedModel.getModel(0).getCenter();
        Ranking center1 = reconstructedModel.getModel(1).getCenter();
        Logger.info("Kendall tau distance between the first and the second center is %.0f", KendallTauDistance.between(center0, center1));
      }
      break;
    }
    
  }
  
  public static void main(String[] args) throws Exception {
    testOne();
  }
}
