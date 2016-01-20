package edu.drexel.cs.db.rank.datasets;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.rating.RatingsSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Split;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.incomplete.QuickIncompleteReconstructor;
import edu.drexel.cs.db.rank.loader.RatingsLoader;
import edu.drexel.cs.db.rank.measure.KullbackLeibler;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureRatingsReconstructor;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PairwisePreferenceMatrix;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.triangle.Expands;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;
import java.io.IOException;
import java.util.List;


public class MovieLens {
    
  private File data;
  
  public MovieLens(File data) {
    this.data = data;
  }
  
  
  public RatingsSample getRatingsSample() throws IOException {
    RatingsSample sample = new RatingsLoader(data).getRatingsSample();
    Logger.info("MovieLens dataset loaded: %d users, %d movies", sample.size(), sample.getItems().size());
    return sample;
  }
  
  public Sample getSample() throws IOException {
    return getRatingsSample().toSample(1);
  }

  public ItemSet getItems() throws IOException {
    return getRatingsSample().getItems();
  }
  
//    [Model 1] Center = 923-858-750-527-904-318-2858-1221-1193-912-50-296-1136-608-2324-1247-908-1213-1252-1198, phi = 0.98, weight = 24
//    [Model 2] Center = 858-750-923-912-260-50-1198-1136-904-913-541-1193-1206-924-908-296-1221-1252-1208-318, phi = 0.98, weight = 23
//    [Model 3] Center = 1198-858-527-904-260-318-912-2762-1219-923-1234-50-2028-1221-593-919-750-1387-110-1200, phi = 0.98, weight = 21
//    [Model 4] Center = 318-2324-1198-527-260-2571-2762-1234-356-50-110-3147-1291-2028-1197-1196-593-1704-2918-1307, phi = 0.98, weight = 19
//    [Model 5] Center = 50-318-527-2324-2804-1288-2858-2762-296-1197-593-858-356-2959-608-2918-1394-2028-1704-2571, phi = 0.97, weight = 13  
  public MallowsMixtureModel getGrimModel() throws IOException {
    ItemSet items = getItems();
    MallowsMixtureModel model = new MallowsMixtureModel(items);

    Ranking c1 = Ranking.fromStringByTag(items, "923-858-750-527-904-318-2858-1221-1193-912-50-296-1136-608-2324-1247-908-1213-1252-1198");
    model.add(new MallowsModel(c1, 0.98), 24);
    Ranking c2 = Ranking.fromStringByTag(items, "858-750-923-912-260-50-1198-1136-904-913-541-1193-1206-924-908-296-1221-1252-1208-318");
    model.add(new MallowsModel(c2, 0.98), 23);
    Ranking c3 = Ranking.fromStringByTag(items, "1198-858-527-904-260-318-912-2762-1219-923-1234-50-2028-1221-593-919-750-1387-110-1200");
    model.add(new MallowsModel(c3, 0.98), 21);
    Ranking c4 = Ranking.fromStringByTag(items, "318-2324-1198-527-260-2571-2762-1234-356-50-110-3147-1291-2028-1197-1196-593-1704-2918-1307");
    model.add(new MallowsModel(c4, 0.98), 19);
    Ranking c5 = Ranking.fromStringByTag(items, "50-318-527-2324-2804-1288-2858-2762-296-1197-593-858-356-2959-608-2918-1394-2028-1704-2571");
    model.add(new MallowsModel(c5, 0.97), 13);
    
    return model;
  }
  
  public void secondTest() throws Exception {
    RatingsSample sample = getRatingsSample();
    Logger.info("%d movielens rankings loaded", sample.size());
    
    double split = 0.66;
    List<RatingsSample> splits = Split.twoFold(sample, split);
    Logger.info("Splitting the sample intro train (%.2f) and test (%.2f)", split, 1-split);
    
    // Reconstruct
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    Expands.setThreshold(0.001);
    MallowsReconstructor single = new QuickIncompleteReconstructor(new File(folder, "incomplete.quick.train.arff"), 3);
    MallowsMixtureRatingsReconstructor reconstructor = new MallowsMixtureRatingsReconstructor(single, 10);
    reconstructor.setMaxRankings(1);
    MallowsMixtureModel model = reconstructor.reconstruct(splits.get(0));    
    PairwisePreferenceMatrix modelPPM = new PairwisePreferenceMatrix(MallowsUtils.sample(model, 50000));
    
    
    Logger.info("----------[ Reconstructed Mixture %d ]-----------------------------", reconstructor.getMaxClusters());
    Logger.info(model);
    


    Logger.info("----------[ Distances ]-----------------------------");
    PairwisePreferenceMatrix testPPM = new PairwisePreferenceMatrix(splits.get(1));
    
    Logger.info("[KL Divergence] True: test sample (1500 rankings); Model: our model: %.4f", KullbackLeibler.divergence(testPPM, modelPPM));
    // Logger.info("[KL Divergence] True: test sample (1500 rankings); Model: GRIM model: %.4f", KullbackLeibler.divergence(testPPM, new PairwisePreferenceMatrix(MallowsUtils.sample(getGrimModel(), 50000))));
    Logger.info("(lower is better)");
    
    // Logger.info("Log Likelihood of test sample being created with our model: %.4f", model.getLogLikelihood(splits.get(1)));
    // Logger.info("Log Likelihood of test sample being created with GRIM model: %.4f", getGrimModel().getLogLikelihood(splits.get(1)));
    // Logger.info("(higher is better)");
  }
  
  
  public static void main(String[] args) throws IOException, Exception {
    File data = new File("C:\\Projects\\Rank\\Papers\\prefaggregation\\Mallows_Model\\datasets\\movielens\\ratings_top_200.csv");
    MovieLens movieLens = new MovieLens(data);
    movieLens.secondTest();
  }
  
}
