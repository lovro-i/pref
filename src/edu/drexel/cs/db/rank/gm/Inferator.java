package edu.drexel.cs.db.rank.gm;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.gm.Variable.Row;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.MapPreferenceSet;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uses Dimple library to do the inference on the previously created
 * GraphicalModel
 */
public class Inferator {

  private final GraphicalModel gm;
  private final Map<Variable, Discrete> variables = new HashMap<Variable, Discrete>();
  private FactorGraph graph;

  public Inferator(GraphicalModel gm) {
    this.gm = gm;
  }

  public void build() {
    graph = new FactorGraph();

    // Create discrete variables
    for (Variable var : gm.getVariables()) {
      Range range = new Range(var);
      Discrete discrete = new Discrete(DiscreteDomain.range(range.low, range.high));
      variables.put(var, discrete);
    }

    // Create factors
    for (Variable var : gm.getVariables()) {
      DimpleSparseFactor dsf = new DimpleSparseFactor(var);
      graph.addFactor(dsf.getSparseTable(), dsf.getWeights(), dsf.getVars());

      //MallowsFactorFunction factor = new MallowsFactorFunction(var);
      //graph.addFactor(factor, factor.getVariables());
    }
  }
  
  public FactorGraph getGraph() {
    if (graph == null) build();
    return graph;
  }

  

  private class MallowsFactorFunction extends FactorFunction {

    private final Variable var;

    private MallowsFactorFunction(Variable var) {
      this.var = var;
    }

    private Discrete[] getVariables() {
      Discrete[] vars = new Discrete[var.getParents().size() + 1];
      int i = 0;
      for (Variable parent : var.getParents()) {
        vars[i++] = variables.get(parent);
      }
      vars[i] = variables.get(this.var);
      return vars;
    }

    @Override
    public double evalEnergy(Value[] values) {
      for (Row row : var.rows) {
        if (matches(row, values)) return -Math.log(row.p);
      }
      return -Math.log(0);
    }

    private boolean matches(Row row, Value[] values) {
      for (int i = 0; i < values.length - 1; i++) {
        int vr = row.vals.get(i);
        int vv = values[i].getInt();
        if (vr != vv) return false;
      }
      int vv = values[values.length - 1].getInt();
      return row.value == vv;
    }

  }

  private static class Range {

    private final int low;
    private final int high;

    public Range(Variable var) {
      int low = Integer.MAX_VALUE;
      int high = Integer.MIN_VALUE;
      for (int val : var.getValues()) {
        low = Integer.min(val, low);
        high = Integer.max(val, high);
      }
      this.low = low;
      this.high = high;
    }

  }
  
  
    /*
  From: Accelarating Inference: towards a full language, compiler and hardware stack
  "Sparse factors: specify only those factor function values which are nonzero. Improves performance by orders
  of magnitude when some variables are deterministic functions of others.
  
  
   */
  private class DimpleSparseFactor {

    private final Variable var;
    private double[] weights;
    private int[][] sparseFactor;

    public DimpleSparseFactor(Variable var) {
      this.var = var;
      List<Row> rows = var.rows;
      weights = new double[rows.size()];
      sparseFactor = new int[rows.size()][];
      int idx = 0;
      for (Row r : rows) {
        sparseFactor[idx] = r.getValues();
        weights[idx] = r.p;
        idx++;
      }
    }

    public double[] getWeights() {
      return weights;
    }

    public int[][] getSparseTable() {
      return sparseFactor;
    }
    
    //returns the variables associated with this RVs CPT
    //as Descrite Dimple objects
    //Lovro: I assume that the ordering of vals in the row correspond to the 
    //ordering of the parents in the parents list
    private Discrete[] getVars() {
      Discrete[] vars = new Discrete[var.getParents().size() + 1];
      int i = 0;
      for (Variable parent : var.getParents()) {
        vars[i++] = variables.get(parent);
      }
      vars[i] = variables.get(var);
      return vars;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      int i = 0;
      for (int[] row : sparseFactor) {
        for (int val : row)
          sb.append(val).append('\t');
        sb.append(weights[i++]);
        sb.append('\n');
      }
      return sb.toString();
    }

  }

  public static void main(String[] args) {
    testPair();

//    ItemSet items = new ItemSet(5);
//    items.tagOneBased();
//    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
//    
//    MapPreferenceSet v = new MapPreferenceSet(items);
//    v.addByTag(3, 1);
//    v.addByTag(3, 2);
//    v.addByTag(1, 5);
//    v.addByTag(2, 5);
//    
//    
//    GraphicalModel gm = new GraphicalModel(model, v);
//    gm.setOneBased(true);
//    gm.build();
//    gm.display();
//    System.out.println(gm);
//    
//    Inferator dimple = new Inferator(gm);
//    FactorGraph graph = dimple.getGraph();
//    graph.setOption(BPOptions.iterations, 100);
//    graph.solve();
//    
//    System.out.println(v);
//    
//    for (Variable var: dimple.variables.keySet()) {
//      Discrete discrete = dimple.variables.get(var);
//      DiscreteDomain domain = discrete.getDomain();
//      double[] belief = discrete.getBelief();
//      
//      System.out.print(var);
//      for (int i = 0; i < belief.length; i++) {
//        int va = (Integer) domain.getElement(i);
//        double be = belief[i];
//        System.out.print(String.format(" | p(%d) = %f", va + 1, be));
//      }
//      System.out.println();
//      
//    }
  }

  public static void testPair() {
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addByTag(2, 4);

    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();
    gm.display();
    System.out.println(gm);

    Inferator dimple = new Inferator(gm);
    FactorGraph graph = dimple.getGraph();
    graph.setOption(BPOptions.iterations, 100);
    graph.solve();

    for (Variable var : dimple.variables.keySet()) {
      Discrete discrete = dimple.variables.get(var);
      double[] belief = discrete.getBelief();
      Logger.info("%s | %s | %s | %f", var, discrete.getDomain(), Arrays.toString(belief), MathUtils.sum(belief));
    }
  }
}
