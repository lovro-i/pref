package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.filter.Filter;
import edu.drexel.cs.db.rank.generator.MallowsUtils;
import edu.drexel.cs.db.rank.model.MallowsModel;
import java.io.File;

@Deprecated
public class QuickIncompleteTest {

  public static void main(String[] args) throws Exception {
    ItemSet items = new ItemSet(100);
    Sample sample = MallowsUtils.sample(items.getRandomRanking(), 0.4, 5000);
    Filter.remove(sample, 0.4);

    File arff = new File("C:\\Projects\\Rank\\Results.3\\temp.arff");
    arff.delete();
    QuickIncompleteReconstructor rec = new QuickIncompleteReconstructor(arff, 4);
    MallowsModel model = rec.reconstruct(sample);
    System.out.println(model);
  }
}
