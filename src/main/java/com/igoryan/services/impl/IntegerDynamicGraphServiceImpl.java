package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.DataStructure;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.services.IntegerDynamicGraphService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class IntegerDynamicGraphServiceImpl<N extends IntegerBaseNode> implements IntegerDynamicGraphService<N> {

  private final Map<ValueGraph<N, Integer>, Map<EndpointPair<N>, DataStructure<N>>> graphToDataStructure = new HashMap<>();
  private final IntegerDynamicAlgorithmHelper<N> dynamicAlgorithmHelper;

  public IntegerDynamicGraphServiceImpl(
      final IntegerDynamicAlgorithmHelper<N> dynamicAlgorithmHelper) {
    this.dynamicAlgorithmHelper = dynamicAlgorithmHelper;
  }

  @Override
  public void update(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N u,
      @NonNull final N v,
      final int newWeight) {

  }

  @Override
  public long distance(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N src,
      @NonNull final N dst) {
    return 0;
  }

  @Override
  public List<N> path(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N src,
      @NonNull final N dst) {
    return null;
  }
}
