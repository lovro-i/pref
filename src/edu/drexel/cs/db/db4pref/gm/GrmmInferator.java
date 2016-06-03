package edu.drexel.cs.db.db4pref.gm;

import edu.drexel.cs.db.db4pref.core.ItemSet;
import edu.drexel.cs.db.db4pref.model.MallowsModel;
import edu.drexel.cs.db.db4pref.core.MapPreferenceSet;
import edu.drexel.cs.db.db4pref.util.MathUtils;
import edu.umass.cs.mallet.base.types.LabelAlphabet;
import edu.umass.cs.mallet.grmm.inference.Inferencer;
import edu.umass.cs.mallet.grmm.inference.JunctionTreeInferencer;
import edu.umass.cs.mallet.grmm.inference.VariableElimination;
import edu.umass.cs.mallet.grmm.types.FactorGraph;
import edu.umass.cs.mallet.grmm.types.TableFactor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** http://mallet.cs.umass.edu/grmm/models.php */
public class GrmmInferator {

  private final GraphicalModel gm;
  private final Map<Variable, edu.umass.cs.mallet.grmm.types.Variable> variables = new HashMap<>();
  private final Map<Variable, Range> ranges = new HashMap<Variable, Range>();
  private FactorGraph graph;
  private Inferencer inferencer;

  public GrmmInferator(GraphicalModel gm) {
    this(gm, new JunctionTreeInferencer());
  }
  
  public GrmmInferator(GraphicalModel gm, Inferencer inferencer) {
    this.gm = gm; 
    this.inferencer = inferencer;
  }

  public FactorGraph getFactorGraph() {
    if (graph == null) build();
    return graph;
  }
  
  public void build() {
    // Create discrete variables
    for (Variable var : gm.getVariables()) {
      Range range = new Range(var);
      LabelAlphabet outs = new LabelAlphabet();
      for (int i = range.low; i <= range.high; i++) {
        outs.lookupIndex(Integer.valueOf(i), true);
      }
      edu.umass.cs.mallet.grmm.types.Variable node = new edu.umass.cs.mallet.grmm.types.Variable(outs);
      ranges.put(var, range);
      variables.put(var, node);
    }

    // Now we can create the FactorGraph
    edu.umass.cs.mallet.grmm.types.Variable[] allVars = new edu.umass.cs.mallet.grmm.types.Variable[variables.size()];
    int ix = 0;
    for (edu.umass.cs.mallet.grmm.types.Variable v: variables.values()) allVars[ix++] = v;
    graph = new FactorGraph(allVars);
    
    // Create factors
    for (Variable var : gm.getVariables()) {
      List<Variable> parents = var.getParents();
      Range range = ranges.get(var);

      int size = range.size();
      for (Variable parent: var.getParents()) {
        size *= ranges.get(parent).size();
      }

      // Calculate probabilities
      double[] probs = new double[size];
      if (parents.size() == 0) {
        int idx = 0;
        for (int i = range.low; i <= range.high; i++) {
          probs[idx++] = var.getProbability(i);
        }
      }
      else if (parents.size() == 1) {
        Range r1 = ranges.get(var.parents.get(0));
        int idx = 0;
        for (int i = r1.low; i <= r1.high; i++) {
          for (int j = range.low; j <= range.high; j++) {
            probs[idx++] = var.getProbability(j, i);
          }
        }
      }
      else if (parents.size() == 2) {
        Range r1 = ranges.get(var.parents.get(0));
        Range r2 = ranges.get(var.parents.get(1));
        int idx = 0;
        for (int i = r1.low; i <= r1.high; i++) {
          for (int k = r2.low; k <= r2.high; k++) {
            for (int j = range.low; j <= range.high; j++) {
              probs[idx++] = var.getProbability(j, i, k);
            }
          }
        }
      }
      else {
        throw new IllegalStateException("ToDo, using recursion");
      }
      
      edu.umass.cs.mallet.grmm.types.Variable[] factorVars = new edu.umass.cs.mallet.grmm.types.Variable[parents.size() + 1];
      for (int i = 0; i < parents.size(); i++) {
        factorVars[i] = variables.get(parents.get(i));
      }
      factorVars[factorVars.length-1] = variables.get(var);
      
      TableFactor factor = new TableFactor(factorVars, probs);
      graph.addFactor(factor);

    }
    
    // Inferencer inf = new JunctionTreeInferencer(); // works
    // Inferencer inf = new BruteForceInferencer();  // works
    // Inferencer inf = new TRP();                  // NullPointerException
    // Inferencer inf = new TreeBP();              // does not work (1.202785)
    // Inferencer inf = new LoopyBP();            // does not work (0)
    // Sampler sampler = new ExactSampler();     // does not work (1) 
    // Sampler sampler = new GibbsSampler();    // Exception
    // inferencer = new SamplingInferencer(sampler, 1000);
    // inferencer = new VariableElimination();
    
    inferencer.computeMarginals(graph);
    
//    for (Variable var: variables.keySet()) {
//      edu.umass.cs.mallet.grmm.types.Variable factorVar = variables.get(var);
//      TableFactor f = (TableFactor) inferencer.lookupMarginal(factorVar);
//      double[] beliefs = f.getValues();
//      Logger.info("%s: %s %f", var, Arrays.toString(beliefs), MathUtils.sum(beliefs));
//    }

  }
  
  public double getProbability() {
    if (inferencer == null) build();
    edu.umass.cs.mallet.grmm.types.Variable factorVar = variables.values().iterator().next();
    TableFactor f = (TableFactor) inferencer.lookupMarginal(factorVar);
    double[] beliefs = f.getValues();
    return MathUtils.sum(beliefs);
  }
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    // create preference set
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.add(items.getItemByTag(2), items.getItemByTag(4));
    v.add(items.getItemByTag(3), items.getItemByTag(4));

    // build graphical model
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();
    System.out.println(gm);

    // infer on graphical model
    GrmmInferator inferator = new GrmmInferator(gm, new VariableElimination());
    inferator.build();
    System.out.println(inferator.getProbability());
  }
}
