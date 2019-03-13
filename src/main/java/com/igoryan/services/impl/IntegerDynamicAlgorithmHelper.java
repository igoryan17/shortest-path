package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.NonNull;

public class IntegerDynamicAlgorithmHelper<N> {
  /**
   * @param source source vertex
   * @param target target vertex
   * @param graph  source graph
   * @return vertex pair depend on type of graph
   */
  EndpointPair<N> vertexPair(N source, N target, ValueGraph<N, Integer> graph) {
    return graph.isDirected()
        ? EndpointPair.ordered(source, target)
        : EndpointPair.unordered(source, target);
  }

  Path<N> rightSubPath(@NonNull ValueGraph<N, Integer> graph, @NonNull Path<N> sourcePath) {
    if (sourcePath.getVertexChain().size() == 2) {
      final N sourceAndTarget = sourcePath.getFistAndLast().target();
      return new Path<>(vertexPair(sourceAndTarget, sourceAndTarget, graph),
          Collections.singletonList(sourcePath.getVertexChain().get(1)));
    }
    final List<N> result =
        sourcePath.getVertexChain().subList(1, sourcePath.getVertexChain().size());
    return new Path<>(vertexPair(result.get(0), result.get(result.size() - 1), graph), result);
  }

  Path<N> leftSubPath(@NonNull ValueGraph<N, Integer> graph, @NonNull Path<N> sourcePath) {
    if (sourcePath.getVertexChain().size() == 2) {
      final N sourceAndTarget = sourcePath.getFistAndLast().source();
      return new Path<>(vertexPair(sourceAndTarget, sourceAndTarget, graph),
          Collections.singletonList(sourcePath.getVertexChain().get(0)));
    }
    final List<N> result =
        sourcePath.getVertexChain().subList(0, sourcePath.getVertexChain().size() - 1);
    return new Path<>(vertexPair(result.get(0), result.get(result.size() - 1), graph), result);
  }

  long weight(@NonNull ValueGraph<N, Integer> graph, @NonNull Path<N> path) {
    final List<N> vertexes = path.getVertexChain();
    if (vertexes.size() < 2) {
      return 0L;
    }
    long result = 0;
    for (int i = 0; i < path.getVertexChain().size() - 1; i++) {
      result += graph.edgeValueOrDefault(vertexes.get(i), vertexes.get(i + 1), 0);
    }
    return result;
  }

  Path<N> addAsFirst(@NonNull ValueGraph<N, Integer> graph, @NonNull N node,
      @NonNull Path<N> path) {
    final EndpointPair<N> vertexPair = vertexPair(node, path.getFistAndLast().target(), graph);
    final List<N> vertexChain = new ArrayList<>(path.getVertexChain().size() + 1);
    vertexChain.add(node);
    vertexChain.addAll(path.getVertexChain());
    return new Path<>(vertexPair, vertexChain);
  }

  Path<N> addAsLast(@NonNull ValueGraph<N, Integer> graph, @NonNull N node, @NonNull Path<N> path) {
    final EndpointPair<N> vertexPair = vertexPair(path.getFistAndLast().source(), node, graph);
    final List<N> vertexChain = new ArrayList<>(path.getVertexChain().size() + 1);
    vertexChain.addAll(path.getVertexChain());
    vertexChain.add(node);
    return new Path<>(vertexPair, vertexChain);
  }
}