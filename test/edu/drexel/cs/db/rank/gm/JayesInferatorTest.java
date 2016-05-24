/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.incomplete.MissingProbabilities;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.MutablePreferenceSet;
import edu.drexel.cs.db.rank.preference.Preference;
import edu.drexel.cs.db.rank.technion.Expander;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hping
 */
public class JayesInferatorTest {

  public JayesInferatorTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  /**
   * Test of getProbability method, of class JayesInferator.
   */
  @Test
  public void testGetProbability() {
    System.out.println("getProbability");

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
    JayesInferator inferator = new JayesInferator(gm);
    double probabilityGivenByJayes = inferator.getProbability();

    Expander expander = new Expander(model);
    double probabilityGivenByDynamicAlgorithm = expander.getProbability(v);
    
    System.out.println("\n****** Test Case ******");
    System.out.format("user constraint v is: %s\n", v);
    System.out.format("probability from Jayes: %f\n", probabilityGivenByJayes);
    System.out.format("probability from dynamic algorithm: %f\n", probabilityGivenByDynamicAlgorithm);
    
    assertEquals(probabilityGivenByDynamicAlgorithm, probabilityGivenByJayes,0.00000001);
  }
}
