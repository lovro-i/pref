package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Ranking;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.filter.Split;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.loader.SampleLoader;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureCompactor;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.ppm.PPMDistance;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.plot.ScatterPlot;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;


public class Sushi {

  /** MallowsMixtureModel from the whole sushi dataset */
  public static void first() throws Exception {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File file = new File(folder, "sushi3a.csv");
    
    Sample sample = new SampleLoader(file, false).getSample();
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
  
  
  public static void second() throws Exception {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File file = new File(folder, "sushi3a.csv");
    
    Sample sample = new SampleLoader(file, false).getSample();
    Logger.info("%d sushi rankings loaded", sample.size());
    
    double split = 0.8;
    List<Sample> splits = Split.twoFold(sample, split);
    Logger.info("Splitting the sample intro train (%.2f) and test (%.2f)", split, 1-split);
    
    // Reconstruct
    long start = System.currentTimeMillis();
    MallowsReconstructor single = new CompleteReconstructor();
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, 10);
    MallowsMixtureModel model = reconstructor.reconstruct(splits.get(0));    
    Logger.info("----------[ Reconstructed Mixture %d ]-----------------------------", reconstructor.getMaxClusters());
    Logger.info(model);
    
    double distance = PPMDistance.distance(splits.get(1), MallowsUtils.sample(model, 100000));
    Logger.info("Model distance from the test sample: %.4f\n", distance);
  }
  
  /** Goodness of fit depending on maxClusters */
  public static void third() throws Exception {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File data = new File(folder, "sushi3a.csv");
    
    Sample sample = new SampleLoader(data, false).getSample();
    Logger.info("%d sushi rankings loaded", sample.size());
    
    
    File results = new File("C:\\Projects\\Rank\\Results.3\\Sushi.results.2.txt");
    PrintWriter out = FileUtils.append(results);
    MallowsReconstructor single = new CompleteReconstructor();
    
    for (int rep = 0; rep < 10; rep++) {
      double split = 0.8;
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
  
  public static MallowsMixtureModel getGrimModel() {
    ElementSet elements = new ElementSet(10);
    MallowsMixtureModel model = new MallowsMixtureModel(elements);

//    [Model 1] Center = 7-5-2-1-8-0-6-3-9-4, phi = 0.66, weight = 17
//    [Model 2] Center = 0-1-3-6-7-2-8-9-5-4, phi = 0.74, weight = 15
//    [Model 3] Center = 4-7-1-5-0-2-3-8-6-9, phi = 0.61, weight = 17
//    [Model 4] Center = 7-2-0-8-3-1-6-9-5-4, phi = 0.64, weight = 18
//    [Model 5] Center = 7-4-2-5-1-8-0-3-6-9, phi = 0.61, weight = 16
//    [Model 6] Center = 7-4-5-0-2-3-8-1-6-9, phi = 0.62, weight = 18

    Ranking c1 = Ranking.fromString(elements, "7-5-2-1-8-0-6-3-9-4");
    model.add(new MallowsModel(c1, 0.66), 17);
    Ranking c2 = Ranking.fromString(elements, "0-1-3-6-7-2-8-9-5-4");
    model.add(new MallowsModel(c2, 0.74), 15);
    Ranking c3 = Ranking.fromString(elements, "4-7-1-5-0-2-3-8-6-9");
    model.add(new MallowsModel(c3, 0.61), 17);
    Ranking c4 = Ranking.fromString(elements, "7-2-0-8-3-1-6-9-5-4");
    model.add(new MallowsModel(c4, 0.64), 18);
    Ranking c5 = Ranking.fromString(elements, "7-4-2-5-1-8-0-3-6-9");
    model.add(new MallowsModel(c5, 0.61), 16);
    Ranking c6 = Ranking.fromString(elements, "7-4-5-0-2-3-8-1-6-9");
    model.add(new MallowsModel(c6, 0.62), 18);

    return model;
  }
  
  public static void main(String[] args) throws Exception {
    first();
    // second();
    // third();
    // plotThird();
    
  }
  
}
