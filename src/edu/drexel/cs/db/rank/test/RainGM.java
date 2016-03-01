package edu.drexel.cs.db.rank.test;

import cern.colt.Arrays;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.options.BPOptions;


public class RainGM {

  
  public static class CloudyToSprinkler extends FactorFunction {

    @Override
    public double evalEnergy(Value[] values) {
      boolean cloudy = values[0].getBoolean();
      boolean sprinkler = values[1].getBoolean();
      double p;
      if (cloudy && sprinkler) p = 0.1;
      else if (cloudy && !sprinkler) p = 0.9;
      else if (!cloudy && sprinkler) p = 0.5;
      else p = 0.5;
      return -Math.log(p);
    }
    
  }
  
  public static class CloudyToRain extends FactorFunction {

    @Override
    public double evalEnergy(Value[] values) {
      boolean cloudy = values[0].getBoolean();
      boolean rain = values[1].getBoolean();
      double p;
      if (cloudy && rain) p = 0.8;
      else if (cloudy && !rain) p = 0.2;
      else if (!cloudy && rain) p = 0.2;
      else p = 0.8;
      return -Math.log(p);
    }
    
  }
  
  public static class GrassFunction extends FactorFunction {

    @Override
    public double evalEnergy(Value[] values) {
      boolean sprinkler = values[0].getBoolean();
      boolean rain = values[1].getBoolean();
      boolean grass = values[2].getBoolean();
      double p;
      if (sprinkler && rain) p = grass ? 0.99 : 0.01;
      else if (sprinkler && !rain) p = grass ? 0.9 : 0.1;
      else if (!sprinkler && rain) p = grass ? 0.9 : 0.1;
      else p = grass ? 0.01 : 0.99;
      return -Math.log(p);
    }
    
  }
  
  public static void main(String[] args) {
    FactorGraph graph = new FactorGraph();
    
    Discrete cloudy = new Discrete(DiscreteDomain.bool());
    Discrete rain = new Discrete(DiscreteDomain.bool());
    Discrete sprinkler = new Discrete(DiscreteDomain.bool());
    Discrete wetGrass = new Discrete(DiscreteDomain.bool());
    
    graph.addFactor(new CloudyToSprinkler(), cloudy, sprinkler);
    graph.addFactor(new CloudyToRain(), cloudy, rain);
    graph.addFactor(new GrassFunction(), sprinkler, rain, wetGrass);
    
    cloudy.setInput(0.5, 0.5);
    wetGrass.setFixedValue(true); // we want to check the probability of if the grass is wet
    
    graph.setOption(BPOptions.iterations, 20);
    graph.solve();
    
    double[] belief = rain.getBelief(); // distribution of rain if wetGrass is set to true
    System.out.println(Arrays.toString(belief));
  }
}
