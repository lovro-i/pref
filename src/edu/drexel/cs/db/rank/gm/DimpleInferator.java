package edu.drexel.cs.db.rank.gm;


import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.DiscreteFactor;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.junctiontree.JunctionTreeSolver;
import com.analog.lyric.dimple.solvers.minsum.MinSumSolver;
import com.analog.lyric.dimple.solvers.particleBP.ParticleBPSolver;
import com.analog.lyric.dimple.solvers.sumproduct.SumProductSolver;
import edu.drexel.cs.db.rank.core.ItemSet;
import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.gm.Variable.Row;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.technion.Expander;
import edu.drexel.cs.db.rank.util.Logger;
import edu.drexel.cs.db.rank.util.MathUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Uses Dimple library to perform inference on the previously created GraphicalModel
 */
public class DimpleInferator {

  private final GraphicalModel gm;
  private final Map<Variable, Discrete> variables = new HashMap<Variable, Discrete>();
  private FactorGraph graph;

  public DimpleInferator(GraphicalModel gm) {
    this.gm = gm; 
  }

  public void build() {
    graph = new FactorGraph();

    // Create discrete variables
    for (Variable var : gm.getVariables()) {
      Range range = new Range(var);      
      DiscreteDomain domain = DiscreteDomain.range(range.low, range.high);
      Discrete discrete = new Discrete(domain);
      variables.put(var, discrete);
    }

    // Create factors
    for (Variable var : gm.getVariables()) {
      // DimpleSparseFactor dsf = new DimpleSparseFactor(var);
      // graph.addFactor(dsf.getSparseTable(), dsf.getWeights(), dsf.getVariables());

      MallowsFactorFunction factor = new MallowsFactorFunction(var);
      graph.addFactor(factor, factor.getVariables());
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

    @Override
    public String toString() {
      return "FactorFunction for " + var;
    }
  }
  
  
    /*
  From: Accelarating Inference: towards a full language, compiler and hardware stack
  "Sparse factors: specify only those factor function values which are nonzero. Improves performance by orders
  of magnitude when some variables are deterministic functions of others.
  
  
   */
  private class DimpleSparseFactor {

    private final Variable var;
    

    public DimpleSparseFactor(Variable var) {
      this.var = var;
    }

    public double[] getWeights() {
      double[] weights = new double[var.rows.size()];
      for (int i = 0; i < weights.length; i++) {
        weights[i] = var.rows.get(i).p;        
      }
      return weights;
    }

    public int[][] getSparseTable() {
      int[][] sparseFactor = new int[var.rows.size()][];
      for (int i = 0; i < sparseFactor.length; i++) {
        sparseFactor[i] = var.rows.get(i).getValues();        
      }
      return sparseFactor;
    }
    
    // returns the variables associated with this RVs CPT
    // as Descrite Dimple objects
    // Lovro: I assume that the ordering of vals in the row correspond to the 
    // ordering of the parents in the parents list
    private Discrete[] getVariables() {
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
      double[] weights = getWeights();
      for (int[] row : getSparseTable()) {
        for (int val : row)
          sb.append(val).append('\t');
        sb.append(weights[i++]);
        sb.append('\n');
      }
      return sb.toString();
    }

  }

  public static void main(String[] args) {
    test();
  }

  public static void test() {
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    Ranking r = new Ranking(items);    
    r.add(items.getItemByTag(2));
    r.add(items.getItemByTag(4));
    // r.add(items.getItemByTag(3));
    
    
    GraphicalModel gm = new GraphicalModel(model, r);
    gm.setOneBased(true);
    gm.build();
    // gm.display();
    System.out.println(gm);

    DimpleInferator dimple = new DimpleInferator(gm);
    FactorGraph graph = dimple.getGraph();
    graph.setSolverFactory(new SumProductSolver());
    graph.setOption(BPOptions.iterations, 100);
    graph.solve();

    for (Variable var : dimple.variables.keySet()) {
      Discrete discrete = dimple.variables.get(var);
      double[] belief = discrete.getBelief();
      Logger.info("%s | %s | %s | %f", var, discrete.getDomain(), Arrays.toString(belief), MathUtils.sum(belief));
    }
    
    
    System.out.println("\n\n");
    for (Factor f: graph.getFactors()) {
      DiscreteFactor df = (DiscreteFactor) f;
      double[] belief = df.getBelief();
      int idx[][] = df.getPossibleBeliefIndices();
      System.out.println();
      for (int i = 0; i < idx.length; i++) {
        int[] is = idx[i];
        for (int j = 0; j < is.length; j++) {
          int v = 1 + (Integer) df.getDomainList().get(j).getElements()[is[j]];
          System.out.print(v+"\t");
        }
        System.out.println(belief[i]);
      }
      // Logger.info("%d x %d", idx.length, idx[0].length);
      Logger.info("----------------------------------------");
      Logger.info("Sum: %f", MathUtils.sum(belief));
    }
    System.out.println("\n\n\n");
    
    // Check the correct value with Expander
    Expander expander = new Expander(model);
    double p = expander.getProbability(r);
    Logger.info("%s: %f", r, p);    
    Logger.info("Find: " + (1-p));
  }
}