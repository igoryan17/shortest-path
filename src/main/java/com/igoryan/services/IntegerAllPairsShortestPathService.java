package com.igoryan.services;

import com.google.common.graph.ValueGraph;
import com.igoryan.model.BaseIntegerNode;
import lombok.NonNull;

public interface IntegerAllPairsShortestPathService<N extends BaseIntegerNode> {

  void calculate(@NonNull ValueGraph<N, Integer> graph, @NonNull N node);
}
