package com.rankst.mixture;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.histogram.Histogram;
import com.rankst.util.Logger;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MallowsMixtureReconstructor {

  
  
  public MallowsMixtureModel reconstruct(Sample sample) {
    Histogram<Ranking> hist = new Histogram<Ranking>();
    hist.add(sample);
    Map<Ranking, Double> map = hist.getMap();
    List<Ranking> rankings = new ArrayList<Ranking>();
    rankings.addAll(map.keySet());
    System.out.println(String.format("There are %d different rankings out of %d total rankings", rankings.size(), sample.size()));
    
    /* Normalization parameters */
    double normDiagonal = hist.getMostFrequentCount();
    int n = sample.getElements().size();
    int normDistance = n * (n-1) / 2;
    
    // !! WORKING HERE
    
    /* Create a distance matrix */
    long start = System.currentTimeMillis();
    double[][] matrix = new double[rankings.size()][rankings.size()];
    for (int i = 0; i < matrix.length; i++) {
      Ranking ranking = rankings.get(i);
      
      // Preference
      matrix[i][i] = 1d * map.get(ranking) / normDiagonal - 1;
      matrix[i][i] *= 100;

      // Similarities
      for (int j = i+1; j < matrix.length; j++) {
        double d = KendallTauRankingDistance.between(ranking, rankings.get(j));        
        matrix[i][j] = -1d * d / normDistance;
        matrix[j][i] = -1d * d / normDistance;
      }
    }
    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis()-start);
    
    
    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    Apro apro = new Apro(provider, 8, false);    
    apro.run(100);


    /* Get exemplars */
    Set<Integer> exemplars = apro.getExemplarSet();
    List<Ranking> centers = new ArrayList<Ranking>();
    for (Integer eid: exemplars) {
      Ranking center = rankings.get(eid);
      centers.add(center);
    }
    
    return null; // !! CONTINUE
  }
  
  
}
