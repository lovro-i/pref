/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.core.MutablePreferenceSet;
import edu.drexel.cs.db.db4pref.core.Preference;
import edu.drexel.cs.db.db4pref.core.Ranking;
import edu.drexel.cs.db.db4pref.filter.MissingProbabilities;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.posterior.old.FullExpander;
import edu.umass.cs.mallet.grmm.inference.BruteForceInferencer;
import edu.umass.cs.mallet.grmm.inference.Inferencer;
import edu.umass.cs.mallet.grmm.inference.JunctionTreeInferencer;
import edu.umass.cs.mallet.grmm.inference.VariableElimination;
import java.util.Set;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author hping
 */
public class GrmmInferatorTest {
  
  public GrmmInferatorTest() {
  }
  
  @BeforeClass
  public static void setUpClass() {
  }
  
  @AfterClass
  public static void tearDownClass() {
  }

  public void testGetProbability(Inferencer inferencer) {
    
    int itemSetSize = 10;
    double phi = 0.9;
    double pairwiseMissing = 0.5;

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
    GrmmInferator inferator = new GrmmInferator(gm, inferencer);
    inferator.build();
    double probabilityGivenByGrmm = inferator.getProbability();

    FullExpander expander = new FullExpander(model);
    double probabilityGivenByDynamicAlgorithm = expander.getProbability(v);
    
    System.out.println("\n****** Test Case ******");
    System.out.format("user constraint v is: %s\n", v);
    System.out.format("probability from GRMM %s: %f\n", inferencer.getClass().getSimpleName(), probabilityGivenByGrmm);
    System.out.format("probability from dynamic algorithm: %f\n", probabilityGivenByDynamicAlgorithm);
    
    assertEquals(probabilityGivenByDynamicAlgorithm, probabilityGivenByGrmm,0.0000001);
  }
 
  /**
   * Test of getProbability method, of class GrmmInferator.
   */
  // @Test
  public void testVariableElimination(){
    System.out.println("testGrmmVariableElimination");
    Inferencer inferencer = new VariableElimination();
    testGetProbability(inferencer);
  }

  /**
   * Test of getProbability method, of class GrmmInferator.
   */
  @Test
  public void testJunctionTreeInferencer(){
    System.out.println("testGrmmJunctionTreeInferencer");
    Inferencer inferencer = new JunctionTreeInferencer();
    testGetProbability(inferencer);
  }
  
  /**
   * Test of getProbability method, of class GrmmInferator.
   */
  // @Test
  public void testBruteForceInferencer(){
    System.out.println("testGrmmBruteForceInferencer");
    Inferencer inferencer = new BruteForceInferencer();
    testGetProbability(inferencer);
  }
}