package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.core.MapPreferenceSet;
import edu.drexel.cs.db.rank.core.MutablePreferenceSet;
import edu.drexel.cs.db.rank.core.Preference;
import edu.drexel.cs.db.rank.core.PreferenceSet;
import edu.drexel.cs.db.rank.incomplete.MissingProbabilities;
import java.util.Arrays;
import java.util.Set;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.junit.Test;
import static org.junit.Assert.*;

public class GraphicalModelTest {

  @Test
  public void testBatya1() {
    ItemSet items = new ItemSet(25);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addById(3, 7);
    v.addById(3, 5);
    v.addById(3, 20);
    v.addById(5, 2);

    GraphicalModel gm = new GraphicalModel(model, v);

    gm.alg2();
    // check number of variables after algorithm 2
    assertEquals(5, gm.getVariables().size());

    gm.alg3();
    // check number of variables after algorithm 3
    assertEquals(13, gm.getVariables().size());

    // check number of edges
    gm.alg4();
    assertEquals(13, gm.getVariables().size());
    assertEquals(17, gm.getEdges().size());
  }

  @Test
  public void testBatya2() {
    ItemSet items = new ItemSet(30);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addById(3, 7);
    v.addById(3, 5);
    v.addById(3, 20);
    v.addById(5, 2);
    v.addById(20, 25);
    v.addById(22, 25);

    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();

    assertEquals(24, gm.getVariables().size());
    assertEquals(31, gm.getEdges().size());
  }

  @Test
  public void testBatya3() {
    ItemSet items = new ItemSet(10);
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addById(2, 0);
    v.addById(2, 1);
    v.addById(0, 4);
    v.addById(1, 4);

    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();

    assertEquals(12, gm.getVariables().size());
    assertEquals(16, gm.getEdges().size());
  }

  @Test
  public void testGetNetworkSize() {
    System.out.println("testing getNetworkSize");

    int itemSetSize = 6;
    double phi = 0.2;
    double pairwiseMissing = 0.7;

    ItemSet items = new ItemSet(itemSetSize);
    items.tagOneBased();
    Ranking center = items.getReferenceRanking();
    MallowsModel model = new MallowsModel(center, phi);

    Ranking r = items.getRandomRanking();

    Set<Preference> vSet = r.getPreferences();
    MutablePreferenceSet v = new MapPreferenceSet(items);
    for (Preference pref : vSet) {
      v.add(pref.higher, pref.lower);
    }
    MissingProbabilities missingProbabilities = MissingProbabilities.uniformPairwise(items, pairwiseMissing);
    missingProbabilities.removePreferences(v);

    GraphicalModel gm = new GraphicalModel(model, v);
    gm.build();
    int networkSizeGivenByGM = gm.getNetworkSize();
    JayesInferator inferator = new JayesInferator(gm);
    int networkSizeGivenByJayes = getJayesNetSize(inferator.getBayesNet());
    
    System.out.format("\nTest case of networkSize:\n%s\n",v);
    System.out.format("networkSize Given by GM is %d\n",networkSizeGivenByGM);
    System.out.format("networkSize Given by Jayes is %d\n",networkSizeGivenByJayes);
    assertEquals(networkSizeGivenByJayes, networkSizeGivenByGM);
  }

  public static int getJayesNetSize(BayesNet bn) {
    int size = 0;
    for (BayesNode node : bn.getNodes()) {
      size += node.getProbabilities().length;
    }
    return size;
  }
}
