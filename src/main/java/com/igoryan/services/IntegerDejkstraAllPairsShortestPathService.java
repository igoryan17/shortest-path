package com.igoryan.services;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.ShortestPathResult;
import java.util.Map;
import lombok.NonNull;

public interface IntegerDejkstraAllPairsShortestPathService<N extends IntegerBaseNode> {

  default Map<EndpointPair<N>, ShortestPathResult<N>> calculateAndGetResult(
      @NonNull ValueGraph<N, Integer> graph) {
    calculate(graph);
    return getNodePairToShortestPath(graph);
  }

  void calculate(@NonNull ValueGraph<N, Integer> graph);

  void calculate(@NonNull ValueGraph<N, Integer> graph, @NonNull N node);

  Map<EndpointPair<N>, ShortestPathResult<N>> getNodePairToShortestPath(
      @NonNull ValueGraph<N, Integer> graph);
}
