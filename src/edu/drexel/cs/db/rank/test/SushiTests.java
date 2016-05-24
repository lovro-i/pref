package edu.drexel.cs.db.rank.test;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.RankingSample;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.datasets.Sushi;
import edu.drexel.cs.db.rank.filter.Split;
import edu.drexel.cs.db.rank.sampler.MallowsUtils;
import edu.drexel.cs.db.rank.distance.KL;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureModel;
import edu.drexel.cs.db.rank.mixture.MallowsMixtureReconstructor;
import edu.drexel.cs.db.rank.core.PairwisePreferenceMatrix;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;
import edu.drexel.cs.db.rank.util.FileUtils;
import edu.drexel.cs.db.rank.util.Logger;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;


public class SushiTests {

  /** outputs (maxClusters, clusters, KL, LL weighted mean, LL max, ideal KL, ide */
  public static void clusters() throws IOException, Exception {
    File folder = new File("C:\\Projects\\Rank\\Data\\sushi");
    File data = new File(folder, "sushi3a.csv");    
    Sushi sushi = new Sushi(data);
    
    File outFile = new File(folder, "sushi.clusters.ideal.csv");
    boolean header = !outFile.exists();
    PrintWriter out = FileUtils.append(outFile);
    if (header) {
      out.println("# When a model is reconstructed, calculate log likelihoods and KL divergence from the sample created by THAT model");
      out.println("# max_required_clusters, obtained_clusters, kl_divergence, log_likelihood_weighted_mean, log_likelihood_max_model, ideal_kl, ideal_ll_mean, ideal_ll_max");
      out.println("# Created by edu.drexel.cs.db.rank.test.SushiTests.clusters()");
      out.println("# " + new Date());
    }
      
      
    int rep = 0;
    int[] maxClusters = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 30, 40, 50, 60, 80, 100, 120, 150, 200 };
    
    
    while (rep < 100) {
      rep++;
      
      for (int max: maxClusters) {
        Logger.info("Rep %d, maxClusters %d", rep, max);
        
        // split
        List<RankingSample> split = Split.twoFold(sushi.getSample(), 0.7);        
        Sample<Ranking> trainSample = split.get(0);
        Sample<Ranking> testSample = split.get(1);
        Logger.info("Sushi dataset split into %d train and %d test rankings", trainSample.size(), testSample.size());

        // reconstruct
        MallowsReconstructor single = new CompleteReconstructor();
        MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single, max);
        MallowsMixtureModel model = reconstructor.reconstruct(trainSample);          
        
        // measure
        PairwisePreferenceMatrix testPPM = new PairwisePreferenceMatrix(testSample);
        RankingSample modelSample = MallowsUtils.sample(model, 50000);
        PairwisePreferenceMatrix modelPPM = new PairwisePreferenceMatrix(modelSample);
        double kl = KL.divergence(testPPM, modelPPM);
        double llw = model.getLogLikelihoodMean(testSample);
        double llm = model.getLogLikelihoodMax(testSample);
        
        
        // ideal
        double idealKL = KL.divergence(modelPPM, modelPPM);
        double idealLLW = model.getLogLikelihoodMean(modelSample);
        double idealLLM = model.getLogLikelihoodMax(modelSample);
        
        // write
        out.println(String.format("%d\t%d\t%f\t%f\t%f\t%f\t%f\t%f", max, model.size(), kl, llw, llm, idealKL, idealLLW, idealLLM));
        out.flush();
      }
    }
    
    out.close();
  }
  
  public static void main(String[] args) throws Exception {
    clusters();
  }
  
}
