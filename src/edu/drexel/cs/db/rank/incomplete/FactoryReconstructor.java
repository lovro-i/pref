package edu.drexel.cs.db.rank.incomplete;

import edu.drexel.cs.db.rank.core.Ranking;
import edu.drexel.cs.db.rank.core.Sample;
import edu.drexel.cs.db.rank.model.MallowsModel;
import edu.drexel.cs.db.rank.preference.PreferenceSet;
import edu.drexel.cs.db.rank.reconstruct.CenterReconstructor;
import edu.drexel.cs.db.rank.reconstruct.MallowsReconstructor;

/** Encapsulates other reconstructors. You specify the type in constructor, and it creates a new one on each reconstruct() call */
public class FactoryReconstructor implements MallowsReconstructor {
  
  private final String reconstructorName;
  private final int maxIterationEM;
  private final int alphaInAMPx;
  private final double initialPhi;

  public FactoryReconstructor(String reconstructorName, int maxClusters, int maxIterationEM) {
    this(reconstructorName, maxClusters, maxIterationEM, 1, 0.5d);
  }
  
  public FactoryReconstructor(String reconstructorName, int maxClusters, int maxIterationEM, int alphaInAMPx, double initialPhi) {
    this.reconstructorName = reconstructorName;
    this.maxIterationEM = maxIterationEM;
    this.alphaInAMPx = alphaInAMPx;
    this.initialPhi = initialPhi;
  }

  /** Creates a required reconstructor */
  private MallowsReconstructor getReconstructor(Ranking center) {
    MallowsModel initialModel = new MallowsModel(center, initialPhi);
    EMReconstructor reconstructor = null;
    if (reconstructorName.equals("AMP")) {
      reconstructor = new AMPReconstructor(initialModel, maxIterationEM);
    } else if (reconstructorName.equals("AMPx")) {
      reconstructor = new AMPxReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-I")) {
      reconstructor = new AMPxIReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-D")) {
      reconstructor = new AMPxDReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-DI")) {
      reconstructor = new AMPxDIReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-D-I")) {
      reconstructor = new HybridReconstructor(initialModel, maxIterationEM, alphaInAMPx, false);
    } else if (reconstructorName.equals("AMPx-D-DI")) {
      reconstructor = new HybridReconstructor(initialModel, maxIterationEM, alphaInAMPx, true);
    } else if (reconstructorName.equals("AMPx-N")) {
      reconstructor = new AMPxNReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    } else if (reconstructorName.equals("AMPx-IN")) {
      reconstructor = new AMPxINReconstructor(initialModel, maxIterationEM, alphaInAMPx);
    }
    else {
      throw new IllegalArgumentException("Unknown reconstructorName");
    }
    return reconstructor;
  }

  @Override
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample) throws Exception {
    Ranking center = CenterReconstructor.reconstruct(sample);
    return this.reconstruct(sample, center);
  }

  @Override
  public MallowsModel reconstruct(Sample<? extends PreferenceSet> sample, Ranking center) throws Exception {
    MallowsReconstructor reconstructor = getReconstructor(center);
    return reconstructor.reconstruct(sample, center);
  }
  
}
