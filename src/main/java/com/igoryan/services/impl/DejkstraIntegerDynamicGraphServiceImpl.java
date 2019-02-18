package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.BaseIntegerNode;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import java.util.List;

public class DejkstraIntegerDynamicGraphServiceImpl<N extends BaseIntegerNode>
    implements IntegerDynamicGraphService<N> {

  private final IntegerDejkstraAllPairsShortestPathService<N> dejkstraAllPairsShortestPathService;

  public DejkstraIntegerDynamicGraphServiceImpl(
      final IntegerDejkstraAllPairsShortestPathService<N> dejkstraAllPairsShortestPathService) {
    this.dejkstraAllPairsShortestPathService = dejkstraAllPairsShortestPathService;
  }

  @Override
  public void update(final ValueGraph<N, Integer> graph,
      final N u, final N v, final int newWeight) {
    ((MutableValueGraph) graph).putEdgeValue(u, v, newWeight);
    dejkstraAllPairsShortestPathService.calculate(graph);
  }

  @Override
  public long distance(final ValueGraph<N, Integer> graph,
      final N src, final N dst) {
    return dejkstraAllPairsShortestPathService.getNodePairToShortestPath(graph)
        .get(graph.isDirected() ? EndpointPair.ordered(src, dst) : EndpointPair.unordered(src, dst))
        .getWeight();
  }

  @Override
  public List<N> path(final ValueGraph<N, Integer> graph, final N src, final N dst) {
    return dejkstraAllPairsShortestPathService.getNodePairToShortestPath(graph)
        .get(graph.isDirected() ? EndpointPair.ordered(src, dst) : EndpointPair.unordered(src, dst))
        .getShortestPath();
  }
}
