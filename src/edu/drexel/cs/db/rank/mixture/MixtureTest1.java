package edu.drexel.cs.db.rank.mixture;

import edu.drexel.cs.db.rank.distance.KendallTauDistance;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.SmartReconstructor;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import fr.lri.tao.apro.ap.Apro;
import fr.lri.tao.apro.data.DataProvider;
import fr.lri.tao.apro.data.MatrixProvider;
import java.io.File;
import java.io.IOException;
import java.util.Set;


public class MixtureTest1 {

  
  
  public static void randomTest() throws Exception {
    int n = 10;
    ItemSet items = new ItemSet(n);
    
    double phi1 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    double phi2 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    double phi3 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    double phi4 = MathUtils.RANDOM.nextDouble() * 0.6 + 0.2;
    
    MallowsModel model1 = new MallowsModel(items.getRandomRanking(), phi1);
    MallowsModel model2 = new MallowsModel(items.getRandomRanking(), phi2);
    MallowsModel model3 = new MallowsModel(items.getRandomRanking(), phi3);        
    MallowsModel model4 = new MallowsModel(items.getRandomRanking(), phi4);        
    
    double weight1 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    double weight2 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    double weight3 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    double weight4 = MathUtils.RANDOM.nextDouble() * 0.8 + 0.2;
    
    MallowsMixtureModel model = new MallowsMixtureModel(items);
    model.add(model1, weight1);
    model.add(model2, weight2);
    model.add(model3, weight3);        
    // model.add(model4, weight4);        
    
    
    MallowsMixtureSampler sampler = new MallowsMixtureSampler(model);
    Sample sample = sampler.generate(6000);
    Filter.remove(sample, 0.15);
    
    
    Logger.info("[Test sample] %d items, %d rankings, %d models", n, sample.size(), model.getModels().size());
    Logger.info(model);
    Logger.info("[Center distances]");
    Logger.info("C1 to C2: " + KendallTauDistance.getInstance().distance(model1.getCenter(), model2.getCenter()));
    Logger.info("C1 to C3: " + KendallTauDistance.getInstance().distance(model1.getCenter(), model3.getCenter()));
    Logger.info("C2 to C3: " + KendallTauDistance.getInstance().distance(model2.getCenter(), model3.getCenter()));
    
    
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    File arff = new File(folder, "incomplete.train.arff");
    SmartReconstructor single = new SmartReconstructor(arff, 0);
    MallowsMixtureReconstructor reconstructor = new MallowsMixtureReconstructor(single);
    MallowsMixtureModel rec = reconstructor.reconstruct(sample);
    
    
    Logger.info("-----[ Original ]------------------------");
    Logger.info(model);
    Logger.info("-----[ Reconstructed ]-------------------");
    Logger.info(rec);
    Logger.info("-----[ Compacted ]-----------------------");
    
    MallowsMixtureCompactor compactor = new MallowsMixtureCompactor();
    compactor.compact(rec);
    
//    MallowsMixtureComparator comparator = new MallowsMixtureComparator(model);
//    Logger.info("\n[Reconstruction results]");
//    comparator.compare(rec);
  }
    
  public static void main(String[] args) throws IOException, Exception {
    File folder = new File("C:\\Projects\\Rank\\Results.3");
    randomTest();
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
