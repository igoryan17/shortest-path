package com.igoryan.services;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.BaseIntegerNode;
import com.igoryan.model.ShortestPathResult;
import java.util.Map;
import lombok.NonNull;

public interface IntegerAllPairsShortestPathService<N extends BaseIntegerNode> {

  void calculate(@NonNull ValueGraph<N, Integer> graph);

  void calculate(@NonNull ValueGraph<N, Integer> graph, @NonNull N node);

  Map<EndpointPair<N>, ShortestPathResult<N>> getNodePairToShortestPath(
      @NonNull ValueGraph<N, Integer> graph);
}
