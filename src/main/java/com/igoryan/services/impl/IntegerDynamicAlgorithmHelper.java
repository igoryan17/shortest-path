package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.DataStructure;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.ShortestPathResult;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
      Map<ValueGraph<N, Integer>, Map<EndpointPair<N>, DataStructure<N>>> graphToDataStructure) {
    graphToDataStructure.putIfAbsent(graph, new HashMap<>());
    final Map<EndpointPair<N>, DataStructure<N>> vertexPairToDataStructure =
        graphToDataStructure.get(graph);
    final Map<EndpointPair<N>, ShortestPathResult<N>> result = dejkstraAllPairsShortestPathService
        .calculateAndGetResult(graph);
    // TODO: optimize it
    for (EndpointPair<N> endpointPair : result.keySet()) {
      final ShortestPathResult<N> shortestPathResult = result.get(endpointPair);
      final List<N> shortestPath = shortestPathResult.getShortestPath();
      vertexPairToDataStructure.compute(endpointPair, (k, dataStructure) -> {
        if (dataStructure == null) {
          dataStructure = new DataStructure<>(k);
        }
        dataStructure.setShortestPath(shortestPath);
        dataStructure.getLocallyShortestPath().add(shortestPath);
        return dataStructure;
      });
      if (shortestPath.isEmpty()) {
        // TODO: it's unavailable handle and test it
        continue;
      }
      final N firstNode = shortestPath.get(0);
      final N lastNode = shortestPath.get(shortestPath.size() - 1);
      if (shortestPath.size() > 2) {
        handleRightSubShortestPath(graph, vertexPairToDataStructure, endpointPair, shortestPath,
            lastNode);
        handleLeftSubShortestPath(graph, vertexPairToDataStructure, endpointPair, shortestPath,
            firstNode);
      }
      // check left side
      handleLeftExtension(graph, vertexPairToDataStructure, result, shortestPath,
          firstNode);
      // check right side
      handleRightExtension(graph, vertexPairToDataStructure, result, shortestPath, lastNode);
    }
  }

  /**
   * if shortest path is (A, B, C) then add it to set L* of BC vertex pair
   *
   * @param graph                     source graph
   * @param vertexPairToDataStructure data structure storage
   * @param endpointPair              vertex pair with start and end of shortest path
   * @param shortestPath              shortest path - chain of vertex
   * @param lastNode                  last element in shortest path
   */
  private void handleRightSubShortestPath(final ValueGraph<N, Integer> graph,
      final Map<EndpointPair<N>, DataStructure<N>> vertexPairToDataStructure,
      final EndpointPair<N> endpointPair, final List<N> shortestPath, final N lastNode) {
    final N secondNode = shortestPath.get(1);
    final EndpointPair<N> subPathVertexKey = vertexPair(secondNode, lastNode, graph);
    vertexPairToDataStructure.compute(subPathVertexKey, (k, dataStructure) -> {
      if (dataStructure == null) {
        dataStructure = new DataStructure<>(k);
      }
      dataStructure.getLeftExtensionOfShortestPaths().put(endpointPair, shortestPath);
      return dataStructure;
    });
  }

  /**
   * if shortest path is (A, B, C) then add it to set R* of BC vertex pair
   *
   * @param graph                     source graph
   * @param vertexPairToDataStructure data structure storage
   * @param endpointPair              vertex pair with start and end of shortest path
   * @param shortestPath              shortest path - chain of vertex
   * @param firstNode                 last element in shortest path
   */
  private void handleLeftSubShortestPath(final ValueGraph<N, Integer> graph,
      final Map<EndpointPair<N>, DataStructure<N>> vertexPairToDataStructure,
      final EndpointPair<N> endpointPair, final List<N> shortestPath, final N firstNode) {
    final N beforeLastNode = shortestPath.get(shortestPath.size() - 2);
    final EndpointPair<N> subPathVertexKey = vertexPair(firstNode, beforeLastNode, graph);
    vertexPairToDataStructure.compute(subPathVertexKey, (k, dataStructure) -> {
      if (dataStructure == null) {
        dataStructure = new DataStructure<>(k);
      }
      dataStructure.getRightExtensionOfShortestPaths().put(endpointPair, shortestPath);
      return dataStructure;
    });
  }

  /**
   * if shortest path is (A, B, C) then iterate in each successor D of C and
   * add to R(A, C) and P(A, D) path (A, B, C, D) if (A, B, C, D) local shortest path
   *
   * @param graph                     source path
   * @param vertexPairToDataStructure data structure storage
   * @param result                    calculated shortest paths storage
   * @param shortestPath              shortest path - chain of vertex
   * @param lastNode                  last element in shortest path
   */
  private void handleRightExtension(final ValueGraph<N, Integer> graph,
      final Map<EndpointPair<N>, DataStructure<N>> vertexPairToDataStructure,
      final Map<EndpointPair<N>, ShortestPathResult<N>> result,
      final List<N> shortestPath, final N lastNode) {
    final LinkedList<N> localShortestCandidate = new LinkedList<>(shortestPath);
    for (N node : graph.successors(lastNode)) {
      localShortestCandidate.addLast(node);
      final N first = localShortestCandidate.pollFirst();
      final N afterFirst = localShortestCandidate.getFirst();
      final EndpointPair<N> mustShortest = vertexPair(afterFirst, node, graph);
      final EndpointPair<N> localCandidate = vertexPair(first, node, graph);
      final ShortestPathResult<N> mustBeShortestWithEqualWay = result.get(mustShortest);
      if (mustBeShortestWithEqualWay.getShortestPath().equals(localShortestCandidate)) {
        localShortestCandidate.addFirst(first);
        // add as local shortest
        vertexPairToDataStructure.compute(localCandidate, (k, dataStructure) -> {
          if (dataStructure == null) {
            dataStructure = new DataStructure<>(k);
          }
          dataStructure.getLocallyShortestPath().add(localShortestCandidate);
          return dataStructure;
        });
        final EndpointPair<N> sourcePathPair = vertexPair(first, lastNode, graph);
        // add as right extension
        vertexPairToDataStructure
            .compute(sourcePathPair, (k, dataStructure) -> {
              if (dataStructure == null) {
                dataStructure = new DataStructure<>(k);
              }
              dataStructure.getRightExtensionOfLocallyShortestPaths()
                  .put(localCandidate, localShortestCandidate);
              return dataStructure;
            });
        if (localShortestCandidate.equals(result.get(sourcePathPair).getShortestPath())) {
          vertexPairToDataStructure.compute(sourcePathPair, (k, dataStructure) -> {
            if (dataStructure == null) {
              dataStructure = new DataStructure<>(k);
            }
            dataStructure.getRightExtensionOfShortestPaths()
                .put(vertexPair(first, node, graph), localShortestCandidate);
            return dataStructure;
          });
        }
      }
    }
  }

  /**
   * if shortest path is (B, C, D) then iterate in each predecessor A of B and add to L(B, D)
   * and P(A, D) path (A, B, C, D) if (A, B, C, D) local shortest path
   *
   * @param graph                     source graph
   * @param vertexPairToDataStructure data structure storage
   * @param result                    calculated shortest paths storage
   * @param shortestPath              shortest path - chain of vertex
   * @param firstNode                 first element in shortest path
   */
  private void handleLeftExtension(final ValueGraph<N, Integer> graph,
      final Map<EndpointPair<N>, DataStructure<N>> vertexPairToDataStructure,
      final Map<EndpointPair<N>, ShortestPathResult<N>> result,
      final List<N> shortestPath, final N firstNode) {
    final LinkedList<N> localShortestCandidate = new LinkedList<>(shortestPath);
    for (N node : graph.predecessors(firstNode)) {
      localShortestCandidate.addFirst(node);
      // check that left without last shortest
      final N last = localShortestCandidate.pollLast();
      final N beforeLast = localShortestCandidate.getLast();

      final EndpointPair<N> mustShortest = vertexPair(node, beforeLast, graph);
      final EndpointPair<N> localCandidate = vertexPair(node, last, graph);
      final ShortestPathResult<N> mustBeShortestWithEqualsWay = result.get(mustShortest);
      if (mustBeShortestWithEqualsWay.getShortestPath().equals(localShortestCandidate)) {
        localShortestCandidate.addLast(last);
        // add as local shortest
        vertexPairToDataStructure.compute(localCandidate, (k, dataStructure) -> {
          if (dataStructure == null) {
            dataStructure = new DataStructure<>(k);
          }
          dataStructure.getLocallyShortestPath().add(localShortestCandidate);
          return dataStructure;
        });
        // add as left extension
        final EndpointPair<N> sourcePathPair = vertexPair(firstNode, last, graph);
        vertexPairToDataStructure
            .compute(sourcePathPair, (k, dataStructure) -> {
              if (dataStructure == null) {
                dataStructure = new DataStructure<>(k);
              }
              dataStructure.getLeftExtensionOfLocallyShortestPaths()
                  .put(localCandidate, localShortestCandidate);
              return dataStructure;
            });
        if (localShortestCandidate.equals(result.get(sourcePathPair).getShortestPath())) {
          vertexPairToDataStructure.compute(sourcePathPair, (k, dataStructure) -> {
            if (dataStructure == null) {
              dataStructure = new DataStructure<>(k);
            }
            dataStructure.getLeftExtensionOfShortestPaths()
                .put(vertexPair(node, last, graph), localShortestCandidate);
            return dataStructure;
          });
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
  private EndpointPair<N> vertexPair(N source, N target, ValueGraph<N, Integer> graph) {
    return graph.isDirected()
        ? EndpointPair.ordered(source, target)
        : EndpointPair.unordered(source, target);
  }
}