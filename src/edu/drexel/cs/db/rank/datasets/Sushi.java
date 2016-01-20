package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Split;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.measure.KullbackLeibler;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureCompactor;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PPMDistance;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.plot.ScatterPlot;
import edu.drexel.cs.db.rank.preference.PairwisePreferenceMatrix;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class Sushi {

  private Sample sample;
  
  public Sushi(File file) throws IOException {    
    this.sample = new SampleLoader(file, false).getSample();
  }
  
  public Sample getSample() {
    return sample;
  }
  
  
  
  /** MallowsMixtureModel from the whole sushi dataset */
  public void firstTest() throws Exception {
    Logger.info("%d sushi rankings loaded", sample.size());
    
    // Reconstruct
    long start = System.currentTimeMillis();
    MallowsReconstructor single = new CompleteReconstructor();
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 20);
    MallowsMixtureModel model = reconstructor.reconstruct(sample);
    double distance1 = PPMDistance.distance(sample, MallowsUtils.sample(model, 100000));
    Logger.info("----------[ Reconstructed Mixture ]-----------------------------");
    Logger.info(model);
    Logger.info("Model distance: %.4f\n", distance1);
    
    // Compact
//    MallowsMixtureCompactor compactor = new MallowsMixtureCompactor();
//    MallowsMixtureModel compact = compactor.compact(model);
//    double distance2 = PPMDistance.distance(sample, MallowsUtils.sample(compact, 100000));
//    Logger.info("----------[ Compacted Mixture ]-----------------------------");
//    Logger.info(compact);
//    Logger.info("Model distance: %.4f\n", distance2);    
//    Logger.info("Reconstructed in %.1f sec", 0.001 * (System.currentTimeMillis() - start));
    
    double distanceFromGrim = PPMDistance.distance(MallowsUtils.sample(getGrimModel(), 100000), MallowsUtils.sample(model, 100000));    
    Logger.info("Distance from GRIM model: %.4f", distanceFromGrim);
    
    double grimDistance = PPMDistance.distance(MallowsUtils.sample(getGrimModel(), 100000), sample);    
    Logger.info("Distance OF GRIM model to the sample: %.4f", grimDistance);
  }
  
  /** Divide sushi into train and test, and check... */
  public void secondTest() throws Exception {
    Logger.info("%d sushi rankings loaded", sample.size());
    
    double split = 0.7;
    List<Sample> splits = Split.twoFold(sample, split);
    Logger.info("Splitting the sample intro train (%.2f) and test (%.2f)", split, 1-split);
    
    // Reconstruct
    MallowsReconstructor single = new CompleteReconstructor();
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);
    MallowsMixtureModel model = reconstructor.reconstruct(splits.get(0));    
    PairwisePreferenceMatrix modelPPM = new PairwisePreferenceMatrix(MallowsUtils.sample(model, 50000));
    
    
    Logger.info("----------[ Reconstructed Mixture %d ]-----------------------------", reconstructor.getMaxClusters());
    Logger.info(model);
    


    Logger.info("----------[ Distances ]-----------------------------");
    PairwisePreferenceMatrix testPPM = new PairwisePreferenceMatrix(splits.get(1));
    
    Logger.info("[KL Divergence] True: test sample (1500 rankings); Model: our model: %.4f", KullbackLeibler.divergence(testPPM, modelPPM));
    Logger.info("[KL Divergence] True: test sample (1500 rankings); Model: GRIM model: %.4f", KullbackLeibler.divergence(testPPM, new PairwisePreferenceMatrix(MallowsUtils.sample(getGrimModel(), 50000))));
    Logger.info("(lower is better)");
    
    Logger.info("Log Likelihood of test sample being created with our model: %.4f", model.getLogLikelihood(splits.get(1)));
    Logger.info("Log Likelihood of test sample being created with GRIM model: %.4f", getGrimModel().getLogLikelihood(splits.get(1)));
    Logger.info("(higher is better)");
  }
  
  
  
  
  
  /** Goodness of fit depending on maxClusters */
  public void thirdTest() throws Exception {
    Logger.info("%d sushi rankings loaded", sample.size());
    
    
    File results = new File("C:\\Projects\\Rank\\Results.3\\Sushi.results.2.txt");
    PrintWriter out = FileUtils.append(results);
    MallowsReconstructor single = new CompleteReconstructor();
    
    for (int rep = 0; rep < 10; rep++) {
      double split = 0.7;
      List<Sample> splits = Split.twoFold(sample, split);
      Sample trainSample = splits.get(0);
      Sample testSample = splits.get(1);
      Logger.info("Splitting the sample intro train (%.2f) and test (%.2f)", split, 1-split);
      
      
      // One model
      MallowsModel one = single.reconstruct(trainSample);
      double dOne = PPMDistance.distance(testSample, MallowsUtils.sample(one, 50000));
      Logger.info("Rep %d, maxClusters %d, models %d, distance %.4f", rep, 1, 1, dOne);
      out.println(String.format("%d,%d,%.4f", 1, 1, dOne));
      out.flush();
      
      int[] maxClusters = { 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 30, 40, 50, 60, 80, 100, 200, 300, 400, 500 };
      for (int mc: maxClusters) {        
        MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, mc);
        MallowsMixtureModel model = reconstructor.reconstruct(trainSample);    
        double distance = PPMDistance.distance(testSample, MallowsUtils.sample(model, 50000));
        Logger.info("Rep %d, maxClusters %d, models %d, distance %.4f", rep, mc, model.size(), distance);
        out.println(String.format("%d,%d,%.4f", mc, model.size(), distance));
        out.flush();
      }      
    }
    out.close();
  }
  
  public static void plotThird() throws IOException {
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File results = new File(folder, "Sushi.results.2.txt");
    File png = new File(folder, "Sushi.results.2.png");
    
    ScatterPlot scatterPlot = new ScatterPlot(results);
    scatterPlot.setXIndex(1);
    scatterPlot.setYIndex(2);
    scatterPlot.setTitle("Sushi Mixture of Mallows");
    scatterPlot.setXLabel("Models in the mixture");
    scatterPlot.setYLabel("Model distance");
    
    scatterPlot.plot(png);
  }
  
  
  /**
    [Model 2] Center = 0-1-3-6-7-2-8-9-5-4, phi = 0.74, weight = 15
    [Model 3] Center = 4-7-1-5-0-2-3-8-6-9, phi = 0.61, weight = 17
    [Model 4] Center = 7-2-0-8-3-1-6-9-5-4, phi = 0.64, weight = 18
    [Model 5] Center = 7-4-2-5-1-8-0-3-6-9, phi = 0.61, weight = 16
    [Model 6] Center = 7-4-5-0-2-3-8-1-6-9, phi = 0.62, weight = 18

   * @return 
   */
  public static MallowsMixtureModel getGrimModel() {
    ItemSet items = new ItemSet(10);
    MallowsMixtureModel model = new MallowsMixtureModel(items);

    Ranking c1 = Ranking.fromStringById(items, "7-5-2-1-8-0-6-3-9-4");
    model.add(new MallowsModel(c1, 0.66), 17);
    Ranking c2 = Ranking.fromStringById(items, "0-1-3-6-7-2-8-9-5-4");
    model.add(new MallowsModel(c2, 0.74), 15);
    Ranking c3 = Ranking.fromStringById(items, "4-7-1-5-0-2-3-8-6-9");
    model.add(new MallowsModel(c3, 0.61), 17);
    Ranking c4 = Ranking.fromStringById(items, "7-2-0-8-3-1-6-9-5-4");
    model.add(new MallowsModel(c4, 0.64), 18);
    Ranking c5 = Ranking.fromStringById(items, "7-4-2-5-1-8-0-3-6-9");
    model.add(new MallowsModel(c5, 0.61), 16);
    Ranking c6 = Ranking.fromStringById(items, "7-4-5-0-2-3-8-1-6-9");
    model.add(new MallowsModel(c6, 0.62), 18);

    return model;
  }
  
  public static void main(String[] args) throws Exception {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File data = new File(folder, "sushi3a.csv");    
    
    Sushi sushi = new Sushi(data);
    sushi.secondTest();
  }
  
}
