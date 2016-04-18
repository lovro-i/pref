package edu.drexel.cs.db.rank.gm;

import cern.colt.Arrays;
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
import java.util.HashMap;
import java.util.Map;

/** Uses Dimple library to do the inference on the previously created GraphicalModel */
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
    for (Variable var: gm.getVariables()) {
      Range range = new Range(var);
      Discrete discrete = new Discrete(DiscreteDomain.range(range.low, range.high));
      variables.put(var, discrete);
    }
    
    // Create factors
    for (Variable var: gm.getVariables()) {
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
      for (Variable parent: var.getParents()) {
        vars[i++] = variables.get(parent);
      }
      vars[i] = variables.get(this.var);
      return vars;
    }
    
    @Override
    public double evalEnergy(Value[] values) {
      for (Row row: var.rows) {
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

    public Range(Variable var){
      int low = Integer.MAX_VALUE;
      int high = Integer.MIN_VALUE;
      for (int val: var.getValues()) {
        low = Integer.min(val, low);
        high = Integer.max(val, high);
      }
      this.low = low;
      this.high = high;
    }

  }
  
  
  public static void main(String[] args) {
    ItemSet items = new ItemSet(5);
    items.tagOneBased();
    MallowsModel model = new MallowsModel(items.getReferenceRanking(), 0.2);
    
    MapPreferenceSet v = new MapPreferenceSet(items);
    v.addByTag(3, 1);
    v.addByTag(3, 2);
    v.addByTag(1, 5);
    v.addByTag(2, 5);
    
    
    GraphicalModel gm = new GraphicalModel(model, v);
    gm.setOneBased(true);
    gm.build();
    gm.display();
    System.out.println(gm);
    
    Inferator dimple = new Inferator(gm);
    FactorGraph graph = dimple.getGraph();
    graph.setOption(BPOptions.iterations, 100);
    graph.solve();
    
    for (Variable var: dimple.variables.keySet()) {
      Discrete discrete = dimple.variables.get(var);
      double[] belief = discrete.getBelief();
      Logger.info("%s | %s | %s | %f", var, discrete.getDomain(), Arrays.toString(belief), MathUtils.sum(belief));
    }
  }
}
