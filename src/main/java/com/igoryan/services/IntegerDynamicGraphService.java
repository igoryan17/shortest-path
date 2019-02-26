package com.igoryan.services;

import com.google.common.graph.ValueGraph;
import com.igoryan.model.IntegerBaseNode;
import java.util.List;
import lombok.NonNull;

public interface IntegerDynamicGraphService<N extends IntegerBaseNode> {

  void update(@NonNull ValueGraph<N, Integer> graph, @NonNull N u, @NonNull N v, int newWeight);

  long distance(@NonNull ValueGraph<N, Integer> graph, @NonNull N src, @NonNull N dst);

  List<N> path(@NonNull ValueGraph<N, Integer> graph, @NonNull N src, @NonNull N dst);
}
