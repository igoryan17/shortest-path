package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.DataStructure;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.Path;
import com.igoryan.model.ShortestPathResult;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import lombok.NonNull;

public class IntegerDynamicAlgorithmHelper<N extends IntegerBaseNode> {

  private final IntegerDejkstraAllPairsShortestPathService<N> dejkstraAllPairsShortestPathService;

  public IntegerDynamicAlgorithmHelper(
      final IntegerDejkstraAllPairsShortestPathService<N> dejkstraAllPairsShortestPathService) {
    this.dejkstraAllPairsShortestPathService = dejkstraAllPairsShortestPathService;
  }

  /**
   * calculate shortest paths and fill data structure
   *
   * @param graph                source graph
   * @param graphToDataStructure storage of data structure
   */
  public void init(ValueGraph<N, Integer> graph,
      Map<ValueGraph<N, Integer>, DataStructure<N>> graphToDataStructure) {
    final DataStructure<N> dataStructure = graphToDataStructure
        .computeIfAbsent(graph, k -> new DataStructure<>());
    final Map<EndpointPair<N>, ShortestPathResult<N>> result = dejkstraAllPairsShortestPathService
        .calculateAndGetResult(graph);
    // TODO: optimize it
    // check left side
    // check right side
    result.forEach((endpointPair, shortestPathResult) -> {
      final List<N> shortestPathVertexes = shortestPathResult.getShortestPath();
      final Path<N> shortestPath = new Path<>(endpointPair, shortestPathVertexes);
      dataStructure.getShortestPath()
          .computeIfAbsent(endpointPair, k -> new HashSet<>())
          .add(new Path<>(endpointPair, shortestPathVertexes));
      dataStructure.getLocallyShortestPath()
          .computeIfAbsent(endpointPair, k -> new HashSet<>())
          .add(shortestPath);
      if (shortestPathVertexes.isEmpty()) {
        // TODO: it's unavailable handle and test it
        throw new RuntimeException(String.format("empty shortest path of %s", endpointPair));
      }
      if (shortestPathVertexes.size() > 2) {
        handleRightSubShortestPath(graph, dataStructure, shortestPath);
        handleLeftSubShortestPath(graph, dataStructure, shortestPath);
      }
      handleLeftExtension(graph, dataStructure, result, shortestPath);
      handleRightExtension(graph, dataStructure, result, shortestPath);
    });
  }

  /**
   * if shortest path is (A, B, C) then add it to set L* of BC vertex pair
   *
   * @param graph        source graph
   * @param shortestPath shortest path - chain of vertex
   */
  private void handleRightSubShortestPath(@NonNull final ValueGraph<N, Integer> graph,
      @NonNull DataStructure<N> dataStructure, @NonNull Path<N> shortestPath) {
    final Path<N> subShortest = rightSubPath(graph, shortestPath);
    dataStructure.getLeftExtensionOfShortestPaths()
        .computeIfAbsent(subShortest, k -> new HashSet<>())
        .add(shortestPath);
    dataStructure.getLeftExtensionOfLocallyShortestPaths()
        .computeIfAbsent(subShortest, k -> new HashSet<>())
        .add(shortestPath);
  }

  /**
   * if shortest path is (A, B, C) then add it to set R* of BC vertex pair
   *
   * @param graph        source graph
   * @param shortestPath shortest path - chain of vertex
   */
  private void handleLeftSubShortestPath(@NonNull final ValueGraph<N, Integer> graph,
      @NonNull DataStructure<N> dataStructure, @NonNull Path<N> shortestPath) {
    final Path<N> subShortest = leftSubPath(graph, shortestPath);
    dataStructure.getRightExtensionOfShortestPaths()
        .computeIfAbsent(subShortest, k -> new HashSet<>())
        .add(shortestPath);
    dataStructure.getRightExtensionOfLocallyShortestPaths()
        .computeIfAbsent(subShortest, k -> new HashSet<>())
        .add(shortestPath);
  }

  /**
   * if shortest path is (A, B, C) then iterate in each successor D of C and
   * add to R(A, C) and P(A, D) path (A, B, C, D) if (A, B, C, D) local shortest path
   *
   * @param graph        source path
   * @param result       calculated shortest paths storage
   * @param shortestPath shortest path - chain of vertex
   */
  private void handleRightExtension(@NonNull final ValueGraph<N, Integer> graph,
      @NonNull final DataStructure<N> dataStructure,
      @NonNull final Map<EndpointPair<N>, ShortestPathResult<N>> result,
      @NonNull final Path<N> shortestPath) {
    final List<N> shortestPathVertexes = shortestPath.getVertexChain();
    final N lastNode = shortestPathVertexes.get(shortestPathVertexes.size() - 1);
    final LinkedList<N> localShortestCandidate = new LinkedList<>(shortestPathVertexes);
    for (N node : graph.successors(lastNode)) {
      localShortestCandidate.addLast(node);
      final N first = localShortestCandidate.pollFirst();
      final N afterFirst = localShortestCandidate.getFirst();
      final EndpointPair<N> rightShortestVertexes = vertexPair(afterFirst, node, graph);
      final EndpointPair<N> localCandidateVertexes = vertexPair(first, node, graph);
      final ShortestPathResult<N> rightShortestPathResult = result.get(rightShortestVertexes);
      if (localShortestCandidate.equals(rightShortestPathResult.getShortestPath())) {
        localShortestCandidate.addFirst(first);
        final Path<N> localShortestPath =
            new Path<>(localCandidateVertexes, localShortestCandidate);
        // add as local shortest
        dataStructure.getLocallyShortestPath()
            .computeIfAbsent(localCandidateVertexes, k -> new HashSet<>())
            .add(localShortestPath);
        // add as right extension
        dataStructure.getRightExtensionOfLocallyShortestPaths()
            .computeIfAbsent(shortestPath, k -> new HashSet<>())
            .add(localShortestPath);
        if (localShortestCandidate
            .equals(result.get(localCandidateVertexes).getShortestPath())) {
          dataStructure.getRightExtensionOfShortestPaths()
              .computeIfAbsent(shortestPath, k -> new HashSet<>())
              .add(localShortestPath);
        }
      }
    }
  }

  /**
   * if shortest path is (B, C, D) then iterate in each predecessor A of B and add to L(B, D)
   * and P(A, D) path (A, B, C, D) if (A, B, C, D) local shortest path
   *
   * @param graph        source graph
   * @param result       calculated shortest paths storage
   * @param shortestPath shortest path - chain of vertex
   */
  private void handleLeftExtension(@NonNull final ValueGraph<N, Integer> graph,
      @NonNull final DataStructure<N> dataStructure,
      @NonNull final Map<EndpointPair<N>, ShortestPathResult<N>> result,
      @NonNull final Path<N> shortestPath) {
    final List<N> shortestPathVertexes = shortestPath.getVertexChain();
    final N firstNode = shortestPathVertexes.get(0);
    final LinkedList<N> localShortestCandidate = new LinkedList<>(shortestPathVertexes);
    for (N node : graph.predecessors(firstNode)) {
      localShortestCandidate.addFirst(node);
      // check that left without last shortest
      final N last = localShortestCandidate.pollLast();
      final N beforeLast = localShortestCandidate.getLast();
      final EndpointPair<N> leftShortestVertexes = vertexPair(node, beforeLast, graph);
      final EndpointPair<N> localCandidateVertexes = vertexPair(node, last, graph);
      final ShortestPathResult<N> leftShortestPathResult = result.get(leftShortestVertexes);
      if (localShortestCandidate.equals(leftShortestPathResult.getShortestPath())) {
        localShortestCandidate.addLast(last);
        final Path<N> localShortestPath =
            new Path<>(localCandidateVertexes, localShortestCandidate);
        // add as local shortest
        dataStructure.getLocallyShortestPath()
            .computeIfAbsent(localCandidateVertexes, k -> new HashSet<>())
            .add(localShortestPath);
        // add as left extension
        dataStructure.getLeftExtensionOfLocallyShortestPaths()
            .computeIfAbsent(shortestPath, k -> new HashSet<>())
            .add(localShortestPath);
        if (localShortestCandidate.equals(result.get(localCandidateVertexes).getShortestPath())) {
          dataStructure.getLeftExtensionOfShortestPaths()
              .computeIfAbsent(shortestPath, k -> new HashSet<>())
              .add(localShortestPath);
        }
      }
    }
  }

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
        sourcePath.getVertexChain().subList(1, sourcePath.getVertexChain().size() - 1);
    return new Path<>(vertexPair(result.get(0), result.get(result.size() - 1), graph), result);
  }

  Path<N> leftSubPath(@NonNull ValueGraph<N, Integer> graph, @NonNull Path<N> sourcePath) {
    if (sourcePath.getVertexChain().size() == 2) {
      final N sourceAndTarget = sourcePath.getFistAndLast().source();
      return new Path<>(vertexPair(sourceAndTarget, sourceAndTarget, graph),
          Collections.singletonList(sourcePath.getVertexChain().get(0)));
    }
    final List<N> result =
        sourcePath.getVertexChain().subList(0, sourcePath.getVertexChain().size() - 2);
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