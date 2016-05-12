package edu.drexel.cs.db.rank.gm;

import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.technion.Expander;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.recommenders.jayes.BayesNet;
import org.eclipse.recommenders.jayes.BayesNode;
import org.eclipse.recommenders.jayes.inference.IBayesInferer;
import org.eclipse.recommenders.jayes.inference.junctionTree.JunctionTreeAlgorithm;
import org.eclipse.recommenders.jayes.io.XMLBIFWriter;

/**
 * http://www.codetrails.com/blog/introduction-bayesian-networks-jayes
 */
public class JayesInferator {

  private final GraphicalModel gm;
  private final Map<Variable, BayesNode> variables = new HashMap<Variable, BayesNode>();
  private final Map<Variable, Range> ranges = new HashMap<Variable, Range>();
  private BayesNet net;

  public JayesInferator(GraphicalModel gm) {
    this.gm = gm;
  }

  public BayesNet getBayesNet() {
    if (net == null) {
      build();
    }
    return net;
  }

  public void saveXML(String filename) throws IOException {
    if (net == null) {
      build();
    }
    XMLBIFWriter w = new XMLBIFWriter();
    w.writeToFile(net, filename);
  }

  public void build() {
    net = new BayesNet();

    // Create discrete variables
    for (Variable var : gm.getVariables()) {
      BayesNode node = net.createNode(var.getId());
      Range range = new Range(var);
      for (int i = range.low; i <= range.high; i++) {
        node.addOutcome("I-" + i);
      }
      ranges.put(var, range);
      variables.put(var, node);
    }

    // Create factors
    for (Variable var : gm.getVariables()) {
      BayesNode node = variables.get(var);
      List<BayesNode> parents = new ArrayList<BayesNode>();
      Range range = ranges.get(var);
      int size = range.size();

      for (Variable parent : var.getParents()) {
        parents.add(variables.get(parent));
        size *= ranges.get(parent).size();
      }
      node.setParents(parents);

      double[] probs = new double[size];

      int idx = 0;
      if (parents.size() == 0) {
        for (int i = range.low; i <= range.high; i++) {
          probs[idx++] = var.getProbability(i);
        }
      } else if (parents.size() == 1) {
        Range r1 = ranges.get(var.parents.get(0));
        for (int i = r1.low; i <= r1.high; i++) {
          for (int j = range.low; j <= range.high; j++) {
            probs[idx++] = var.getProbability(j, i);
          }
        }
      } else if (parents.size() == 2) {
        Range r1 = ranges.get(var.parents.get(0));
        Range r2 = ranges.get(var.parents.get(1));
        for (int i = r1.low; i <= r1.high; i++) {
          for (int k = r2.low; k <= r2.high; k++) {
            for (int j = range.low; j <= range.high; j++) {
              probs[idx++] = var.getProbability(j, i, k);
            }
          }
        }
      } else {
        throw new IllegalStateException("ToDo, using recursion");
      }

      Logger.info(Arrays.toString(probs));
      node.setProbabilities(probs);

    }

    IBayesInferer inferer = new JunctionTreeAlgorithm();
    inferer.setNetwork(net);

    for (Variable var : variables.keySet()) {
      double[] beliefs = inferer.getBeliefs(variables.get(var));
      Logger.info("%s: %s | sum: %f", var, Arrays.toString(beliefs), MathUtils.sum(beliefs));
    }
  }

  public double getProbability() {
    // take any variable and return MathUtils.sum(beliefs);
    net = new BayesNet();

    // Create discrete variables
    for (Variable var : gm.getVariables()) {
      BayesNode node = net.createNode(var.getId());
      Range range = new Range(var);
      for (int i = range.low; i <= range.high; i++) {
        node.addOutcome("I-" + i);
      }
      ranges.put(var, range);
      variables.put(var, node);
    }

    // Create factors
    for (Variable var : gm.getVariables()) {
      BayesNode node = variables.get(var);
      List<BayesNode> parents = new ArrayList<BayesNode>();
      Range range = ranges.get(var);
      int size = range.size();

      for (Variable parent : var.getParents()) {
        parents.add(variables.get(parent));
        size *= ranges.get(parent).size();
      }
      node.setParents(parents);

      double[] probs = new double[size];

      int idx = 0;
      if (parents.size() == 0) {
        for (int i = range.low; i <= range.high; i++) {
          probs[idx++] = var.getProbability(i);
        }
      } else if (parents.size() == 1) {
        Range r1 = ranges.get(var.parents.get(0));
        for (int i = r1.low; i <= r1.high; i++) {
          for (int j = range.low; j <= range.high; j++) {
            probs[idx++] = var.getProbability(j, i);
          }
        }
      } else if (parents.size() == 2) {
        Range r1 = ranges.get(var.parents.get(0));
        Range r2 = ranges.get(var.parents.get(1));
        for (int i = r1.low; i <= r1.high; i++) {
          for (int k = r2.low; k <= r2.high; k++) {
            for (int j = range.low; j <= range.high; j++) {
              probs[idx++] = var.getProbability(j, i, k);
            }
          }
        }
      } else {
        throw new IllegalStateException("ToDo, using recursion");
      }

      Logger.info(Arrays.toString(probs));
      node.setProbabilities(probs);

    }

    IBayesInferer inferer = new JunctionTreeAlgorithm();
    inferer.setNetwork(net);

    Variable var = variables.keySet().iterator().next();
    double[] beliefs = inferer.getBeliefs(variables.get(var));
    double probability = MathUtils.sum(beliefs);
    
    return probability;
  }

  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);

//    Ranking r = new Ranking(items);    
//    r.add(items.getItemByTag(2));
//    r.add(items.getItemByTag(4));
//    r.add(items.getItemByTag(3));
//    r.add(items.getItemByTag(1));
//    
//    
//    GraphicalModel gm = new GraphicalModel(model, r);
//    gm.setOneBased(true);
//    gm.build();
//    System.out.println(gm);
//    
//    JayesInferator inferator = new JayesInferator(gm);
//    inferator.build();
//    
//    
//    
//    // Check the correct value with Expander (Dynamic Algorithm)
//    Expander expander = new Expander(model);
//    double p = expander.getProbability(r);
//    Logger.info("%s: %f", r, p);
    {
      // create preference set
      MapPreferenceSet v = new MapPreferenceSet(items);
      v.add(items.getItemByTag(2), items.getItemByTag(4));
      v.add(items.getItemByTag(3), items.getItemByTag(4));

      // build graphical model
      GraphicalModel gm1 = new GraphicalModel(model, v);
      gm1.setOneBased(true);
      gm1.build();
      System.out.println(gm1);

      // infer on graphical model
      JayesInferator inferator1 = new JayesInferator(gm1);
      inferator1.build();

      // and now compare it to the exact value that dynamic algorithm gives
      // iterate through v.getRankings() and sum the probabilities that expander gives for each one:
      Expander expander1 = new Expander(model);
      Ranking r1 = new Ranking(items);
      r1.add(items.getItemByTag(2));
      r1.add(items.getItemByTag(3));
      r1.add(items.getItemByTag(4));
      double p1 = expander1.getProbability(r1);

      Expander expander2 = new Expander(model);
      Ranking r2 = new Ranking(items);
      r2.add(items.getItemByTag(3));
      r2.add(items.getItemByTag(2));
      r2.add(items.getItemByTag(4));
      double p2 = expander2.getProbability(r2);

      Logger.info("%s + %s: %f", r1, r2, p1 + p2);
      
      System.out.println(inferator1.getProbability());
    }
  }

}
