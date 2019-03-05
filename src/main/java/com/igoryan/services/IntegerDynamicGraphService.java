package com.igoryan.services;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.WeightUpdating;
import java.util.List;
import lombok.NonNull;

public interface IntegerDynamicGraphService<N extends IntegerBaseNode> {

  void update(MutableValueGraph<N, Integer> graph, @NonNull WeightUpdating<N> weightUpdating);

  long distance(@NonNull ValueGraph<N, Integer> graph, @NonNull N src, @NonNull N dst);

  List<N> path(@NonNull ValueGraph<N, Integer> graph, @NonNull N src, @NonNull N dst);
}
