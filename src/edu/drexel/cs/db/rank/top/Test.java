package edu.drexel.cs.db.rank.top;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.filter.SampleCompleter;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.generator.RIMRSampler;
import edu.drexel.cs.db.rank.incomplete.BetterIncompleteReconstructor;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.reconstruct.CompleteReconstructor;
import edu.drexel.cs.db.rank.triangle.SampleTriangle;
import edu.drexel.cs.db.rank.util.MathUtils;


public class Test {

  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(10);
    Ranking center = items.getRandomRanking();
    Sample sample = MallowsUtils.sample(center, 0.2, 10000);
    Filter.top(sample, 5, items.size());
    System.out.println(sample);
    
//    SampleTriangle t1 = new SampleTriangle(center, sample);
//    RIMRSampler sampler1 = new RIMRSampler(t1);
//    Sample resample1 = sampler1.generate(5000);
//    
//    TopSampleTriangle t2 = new TopSampleTriangle(center, sample);
//    RIMRSampler sampler2 = new RIMRSampler(t2);
//    Sample resample2 = sampler2.generate(5000);
//    
//    CompleteReconstructor reconstructor = new CompleteReconstructor();
//    MallowsModel model1 = reconstructor.reconstruct(resample1);
//    MallowsModel model2 = reconstructor.reconstruct(resample2);
//    System.out.println(center);
//    System.out.println(model1);
//    System.out.println(model2);


//     Sample sample2 = MallowsUtils.sample(center, 0.3, 10000);
//     Tops tops = new Tops(sample);
//     System.out.println(tops);
//     tops.remove(sample2);
//     Tops tops2 = new Tops(sample2);
//     System.out.println(tops2);

    TopIncompleteReconstructor rec = new TopIncompleteReconstructor(true, false, false, 20, 4);
    MallowsModel old = rec.reconstruct(sample);
    System.out.println(old);

//    TopSampleCompleter completer = new TopSampleCompleter(sample);
//    CompleteReconstructor reconstructor = new CompleteReconstructor();
//    double[] boots = new double[20];
//    for (int i = 0; i < boots.length; i++) {
//      Sample resample = completer.complete();
//      MallowsModel model = reconstructor.reconstruct(resample);
//      boots[i] = model.getPhi();
//    }
//    double phi = MathUtils.mean(boots);
//    System.out.println("phi = " + phi);
  }
}
