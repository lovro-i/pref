package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.entity.ElementSet;
import edu.drexel.cs.db.rank.entity.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import java.io.File;

public class QuickIncompleteTest {

  public static void main(String[] args) throws Exception {
    ElementSet elements = new ElementSet(100);
    Sample sample = MallowsUtils.sample(elements.getRandomRanking(), 0.4, 5000);
    Filter.remove(sample, 0.4);

    File arff = new File("C:\\Projects\\Rank\\Results.3\\temp.arff");
    arff.delete();
    QuickIncompleteReconstructor rec = new QuickIncompleteReconstructor(arff, 4);
    MallowsModel model = rec.reconstruct(sample);
    System.out.println(model);
  }
}
