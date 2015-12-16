package com.rankst.mixture;

import cern.colt.matrix.DoubleMatrix2D;
import com.rankst.distance.KendallTauRankingDistance;
import com.rankst.distance.RankingDistance;
import com.rankst.entity.ElementSet;
import com.rankst.entity.Ranking;
import com.rankst.entity.Sample;
import com.rankst.generator.RIMRSampler;
import com.rankst.model.MallowsModel;
import com.rankst.histogram.Histogram;
import com.rankst.triangle.MallowsTriangle;
import com.rankst.util.Logger;
import com.rankst.util.MathUtils;
import com.rankst.util.Utils;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MixtureTest1 {

  public static void test() {
    int n = 10;
    ElementSet elements = new ElementSet(n);
    
    MallowsModel model1 = new MallowsModel(elements.getRandomRanking(), 0.4d);
    MallowsModel model2 = new MallowsModel(elements.getRandomRanking(), 0.4d);
    MallowsModel model3 = new MallowsModel(elements.getRandomRanking(), 0.4d);
    
    MallowsMixtureModel model = new MallowsMixtureModel(elements);
    model.add(model1, 1);
    model.add(model2, 1);
    model.add(model3, 1);
    
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    Sample sample = sampler.generate(5000);
    
    
    /* Getting all the rankings that appear in both sets, with counts */
    Histogram<Ranking> hist = new Histogram<Ranking>();
    hist.add(sample);
    Map<Ranking, Double> map = hist.getMap();
    List<Ranking> rankings = new ArrayList<Ranking>();
    rankings.addAll(map.keySet());
    Logger.info("There are %d different rankings out of %d total rankings", rankings.size(), sample.size());
    
    /* Normalization parameters */
    double normDiagonal = hist.getMostFrequentCount();
    int normDistance = n * (n-1) / 2;
    
    /* Create the similarity matrix */
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
    
    
    MallowsMixtureComparator comparator = new MallowsMixtureComparator(model);
    comparator.compareCenters(centers, new PrintWriter(System.out));
  }
  
  public static void randomTest(PrintWriter out) {
    int n = 10;
    ElementSet elements = new ElementSet(n);
    
    double phi1 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    double phi2 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    double phi3 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    
    MallowsModel model1 = new MallowsModel(elements.getRandomRanking(), phi1);
    MallowsModel model2 = new MallowsModel(elements.getRandomRanking(), phi2);
    MallowsModel model3 = new MallowsModel(elements.getRandomRanking(), phi3);        
    
    double weight1 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    double weight2 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    double weight3 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    
    MallowsMixtureModel model = new MallowsMixtureModel(elements);
    model.add(model1, weight1);
    model.add(model2, weight2);
    model.add(model3, weight3);        
    
    
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    Sample sample = sampler.generate(5000);
    
    
    out.println(String.format("[Test sample] %d rankings, %d models", sample.size(), model.getModels().size()));
    out.println(model);
    out.println("[Center distances]");
    out.println("C1 to C2: " + KendallTauRankingDistance.getInstance().distance(model1.getCenter(), model2.getCenter()));
    out.println("C1 to C3: " + KendallTauRankingDistance.getInstance().distance(model1.getCenter(), model3.getCenter()));
    out.println("C2 to C3: " + KendallTauRankingDistance.getInstance().distance(model2.getCenter(), model3.getCenter()));
    
    
    /* Getting all the rankings that appear in both sets, with counts */
    
    // !!--- Moved to MallowsMixtureReconstructor
    
    MallowsMixtureComparator comparator = new MallowsMixtureComparator(model);
    out.println("\n[Reconstruction results]");
    // comparator.compareCenters(centers, out);
  }
    
  public static void main(String[] args) throws IOException {
    // test();
    
    File folder = new File("D:\\Projects\\Rankst\\Results\\02 mixture");
    File out = new File(folder, "Mallows.mixture.results.txt");
    PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(out)));
    for (int i = 1; i <= 100; i++) {
      System.out.println("Test #" + i);
      writer.println("\n\n-------[ Test #"+i+" ]-----------------------------------------------------------\n");
      randomTest(writer);  
      writer.flush();
    }    
    writer.close();
  }
  
  @Deprecated
  public static void firstVersion() {
    int n = 10;
    ElementSet elements = new ElementSet(n);
    RankingDistance dist = new KendallTauRankingDistance();
    
    /* Input parameters*/
    Ranking center1 = elements.getReferenceRanking();
    Ranking center2 = elements.getRandomRanking();
    Ranking center3 = elements.getRandomRanking();
    // while (dist.distance(center1, center2) < n * (n-1) / 8) center2 = elements.getRandomRanking();
    // while (dist.distance(center1, center2) != 4) center2 = elements.getRandomRanking();
    
    Logger.info("Center1: %s; center2: %s; distance: %d", center1, center2, (int) dist.distance(center1, center2));
    Logger.info("Center1: %s; center3: %s; distance: %d", center1, center3, (int) dist.distance(center1, center3));
    Logger.info("Center2: %s; center3: %s; distance: %d", center2, center3, (int) dist.distance(center2, center3));
    
    double phi1 = 0.4;
    double phi2 = 0.4;
    double phi3 = 0.4;
    
    int samples1 = 1000;
    int samples2 = 1000;
    int samples3 = 1000;
    
    
    /* Creating two samples */
    MallowsTriangle triangle1 = new MallowsTriangle(center1, phi1);
    RIMRSampler sampler1 = new RIMRSampler(triangle1);
    Sample sample1 = sampler1.generate(samples1);
    
    MallowsTriangle triangle2 = new MallowsTriangle(center2, phi2);
    RIMRSampler sampler2 = new RIMRSampler(triangle2);
    Sample sample2 = sampler2.generate(samples2);
    
    MallowsTriangle triangle3 = new MallowsTriangle(center3, phi3);
    RIMRSampler sampler3 = new RIMRSampler(triangle3);
    Sample sample3 = sampler3.generate(samples3);
    
    /* Getting all the rankings that appear in both sets, with counts */
    Histogram<Ranking> hist = new Histogram<Ranking>();
    hist.add(sample1);
    hist.add(sample2);
    hist.add(sample3);
    Map<Ranking, Double> map = hist.getMap();
    List<Ranking> rankings = new ArrayList<Ranking>();
    rankings.addAll(map.keySet());
    Logger.info("There are %d different rankings out of %d total rankings", rankings.size(), samples1+samples2);
    
    double normDiagonal = hist.getMostFrequentCount();
    int normDistance = n * (n-1) / 2;
        
    /* Making a distance matrix */
    long start = System.currentTimeMillis();
    double[][] matrix = new double[rankings.size()][rankings.size()];
    for (int i = 0; i < matrix.length; i++) {
      Ranking ranking = rankings.get(i);
      matrix[i][i] = 1d * map.get(ranking) / normDiagonal - 1;
      matrix[i][i] *= 100;
      // matrix[i][i] = 0;
      for (int j = i+1; j < matrix.length; j++) {
        double d = dist.distance(ranking, rankings.get(j));        
        matrix[i][j] = -1d * d / normDistance; // + (Math.random() - 0.5) / 100;
        matrix[j][i] = -1d * d / normDistance; // + (Math.random() - 0.5) / 100;        
      }
    }
    Logger.info("Matrix %d x %d created in %d ms", matrix.length, matrix.length, System.currentTimeMillis()-start);
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        System.out.print(matrix[i][j] + " ");
      }
      System.out.println();
    }
    
//    int ic1 = rankings.indexOf(center1);
//    int ic2 = rankings.indexOf(center2);
//    Logger.info("Center1 weight: %f center2 weight: %f", matrix[ic1][ic1], matrix[ic2][ic2]);
    
    // Matrix 400 x 400 created in 167 ms
    // Matrix 4000 x 4000 created in 7277 ms
    // Matrix 10000 x 10000 created in 46088 ms
   
    
    DataProvider provider = new MatrixProvider(matrix);
    provider.addNoise();
    Apro apro = new Apro(provider, 1, false);    
    apro.run(100);

    Set<Integer> exemplars = apro.getExemplarSet();
    

    DoubleMatrix2D ar = apro.getAR();
    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        System.out.print(ar.get(i, j) + " ");
      }
      System.out.println();
    }

    
    for (Integer eid: exemplars) {
      Ranking ex = rankings.get(eid);
      int d1 = (int) dist.distance(ex, center1);
      int d2 = (int) dist.distance(ex, center2);
      int d3 = (int) dist.distance(ex, center3);
      Logger.info("Exemplar %s, min distance %d", ex, Math.min(Math.min(d1, d2), d3));
    }
    Logger.info("Exemplar count: %d", exemplars.size());
    
  }
  
  private static void testApro() {
    double[][] s = new double[3][3];
    s[0][0] = s[0][2] = s[2][0] = s[2][1] = 3;
    s[0][1] = s[1][0] = 20;
    s[1][1] = 7;
    s[1][2] = s[2][2] = 15;

    DataProvider provider = new MatrixProvider(s);
    Apro apro = new Apro(provider, 1, false);    
    apro.run(100);
    Set<Integer> exemplars = apro.getExemplarSet();
    Logger.info("Exemplar count: %d", exemplars.size());
  }
}
