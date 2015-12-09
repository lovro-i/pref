package com.rankst.reconstruct;

import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.KendallTauUtils;
import com.rankst.distance.RankingDistance;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.model.MallowsModel;
import com.rankst.histogram.Histogram;


public class DirectReconstructor implements MallowsReconstructor {


  public DirectReconstructor() {
  }
  
  public MallowsModel reconstruct(Sample sample) {
    Histogram<Ranking> rankHist = new Histogram(sample, sample.getWeights());
    Ranking center = rankHist.getMostFrequent();
    
    RankingDistance dist = new KendallTauRankingDistance();
    Histogram<Integer> hist = new Histogram<Integer>();
    for (int i = 0; i < sample.size(); i++) {
      int d = (int) dist.distance(center, sample.get(i));
      hist.add(d, sample.getWeight(i));
    }
    
    int n = sample.getElements().size();
    double sum = 0;
    int max = n * (n-1) / 2;
    double arr[] = new double[max+1];
    for (int i=0; i<=max; i++) {
      double b = 0;
      Double a = hist.get(i);
      if (a != null) b = a;
      
      double e = KendallTauUtils.getCount(n, i);
      double c = 0;
      if (e != 0) c = b / e;
      arr[i] = c;
      sum += c;
    }
    
    for (int i = 0; i <= max; i++) {
      arr[i] = arr[i] / sum;
    }
    
    double e = 0; // expectation
    for (int i = 0; i < arr.length; i++) {
      e += arr[i] * i;
    }
    double phi = e / (e+1);
    return new MallowsModel(center, phi);
  }
  
}
