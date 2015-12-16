
package com.rankst.reconstruct;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.histogram.Histogram;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/** Uses Affinity Propagation to find the central ranking */
public class DirectReconstructorAP extends DirectReconstructor {
  
  @Override
  protected Ranking reconstructCenter(Sample sample) {
    ElementSet elements = sample.getElements();
    int n = elements.size();
    
    Histogram<Ranking> hist = new Histogram<Ranking>(sample, sample.getWeights());
    Map<Ranking, Double> map = hist.getMap();
    List<Ranking> rankings = new ArrayList<Ranking>();
    rankings.addAll(map.keySet());

    /* Normalization parameters */
    double normDiagonal = hist.getMostFrequentCount();
    int normDistance = n * (n-1) / 2;
    
    /* Create a similarity matrix */
    double[][] matrix = new double[rankings.size()][rankings.size()];
    for (int i = 0; i < matrix.length; i++) {
      Ranking ranking = rankings.get(i);
      
      // Preference
      matrix[i][i] = 1d * map.get(ranking) / normDiagonal - 2;      
      matrix[i][i] *= 2;

      // Similarities
      for (int j = i+1; j < matrix.length; j++) {
        double d = KendallTauRankingDistance.between(ranking, rankings.get(j));        
        matrix[i][j] = matrix[j][i] = -1d * d / normDistance;
      }
    }
    
    
    /* Run Affinity Propagation */
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    Apro apro = new Apro(provider, 8, false);    
    apro.run(100);
    
    return vote(map, rankings, apro);
  }
  
  private Ranking vote(Map<Ranking, Double> map, List<Ranking> rankings, Apro apro) {
    int[] exemplars = apro.getExemplars();
    Histogram<Ranking> hist = new Histogram<Ranking>();
    for (int i = 0; i < exemplars.length; i++) {
      Ranking r = rankings.get(exemplars[i]);
      hist.add(r, map.get(r));
    }
    return hist.getMostFrequent();
  }
  
}
