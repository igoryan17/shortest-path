package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.igoryan.model.GraphWrapper;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.Path;
import com.igoryan.model.WeightUpdating;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import java.util.List;

public class DejkstraIntegerDynamicGraphServiceImpl<N extends IntegerBaseNode>
    implements IntegerDynamicGraphService<N> {

  private final IntegerDejkstraAllPairsShortestPathService<N> dejkstraAllPairsShortestPathService;

  public DejkstraIntegerDynamicGraphServiceImpl(
      final IntegerDejkstraAllPairsShortestPathService<N> dejkstraAllPairsShortestPathService) {
    this.dejkstraAllPairsShortestPathService = dejkstraAllPairsShortestPathService;
  }

  @Override
  public void update(final GraphWrapper<N> graphWrapper,
      final WeightUpdating<N> weightUpdating) {
    final N v = weightUpdating.getNode();
    weightUpdating.getIncomingNodeToNewWeight()
        .forEach((u, w) -> graphWrapper.getGraph().putEdgeValue(u, v, w));
    weightUpdating.getOutgoingNodeToNewWeight()
        .forEach((u, w) -> graphWrapper.getGraph().putEdgeValue(v, u, w));
    dejkstraAllPairsShortestPathService.calculate(graphWrapper.getGraph());
  }

  @Override
  public long distance(final GraphWrapper<N> graphWrapper,
      final N src, final N dst) {
    return dejkstraAllPairsShortestPathService.getNodePairToShortestPath(graphWrapper.getGraph())
        .get(graphWrapper.getGraph().isDirected()
            ? EndpointPair.ordered(src, dst)
            : EndpointPair.unordered(src, dst))
        .getWeight();
  }

  @Override
  public Path<N> path(final GraphWrapper<N> graphWrapper, final N src, final N dst) {
    final EndpointPair<N> vertexes = graphWrapper.getGraph().isDirected()
        ? EndpointPair.ordered(src, dst)
        : EndpointPair.unordered(src, dst);
    List<N> path = dejkstraAllPairsShortestPathService
        .getNodePairToShortestPath(graphWrapper.getGraph())
        .get(vertexes)
        .getShortestPath();
    return new Path<>(vertexes, path);
  }

  @Override
  public void init(final GraphWrapper<N> graphWrapper) {
    dejkstraAllPairsShortestPathService.calculate(graphWrapper.getGraph());
  }
}
