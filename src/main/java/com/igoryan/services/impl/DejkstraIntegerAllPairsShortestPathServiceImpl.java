package com.igoryan.services.impl;

import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.BaseIntegerNode;
import com.igoryan.model.ShortestPathResult;
import com.igoryan.services.IntegerAllPairsShortestPathService;
import com.igoryan.services.IntegerRelaxationService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import lombok.NonNull;

public class DejkstraIntegerAllPairsShortestPathServiceImpl<N extends BaseIntegerNode>
    implements IntegerAllPairsShortestPathService<N> {

  private final Map<ValueGraph<N, Integer>, Map<EndpointPair<N>, ShortestPathResult<N>>>
      graphToShortestPaths =
      new HashMap<>();
  private final IntegerRelaxationService<N> relaxationService;

  public DejkstraIntegerAllPairsShortestPathServiceImpl(
      final IntegerRelaxationService<N> relaxationService) {
    this.relaxationService = relaxationService;
  }

  public void calculate(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N source) {
    final Map<EndpointPair<N>, ShortestPathResult<N>> vertexPairToShortestPath =
        graphToShortestPaths.putIfAbsent(graph, new HashMap<>());
    init(graph, source);
    final Set<N> calculated = new HashSet<>(graph.nodes().size());
    final PriorityQueue<N> queue = new PriorityQueue<>(graph.nodes());
    while (!queue.isEmpty()) {
      N u = queue.poll();
      calculated.add(u);
      graph.adjacentNodes(u).forEach(node -> relaxationService
          .relax(u, node, graph.edgeValueOrDefault(u, node, Integer.MAX_VALUE)));
    }
    calcShortestPath(source, graph, calculated, vertexPairToShortestPath);
  }

  protected void init(@NonNull ValueGraph<N, Integer> graph, @NonNull N source) {
    graph.nodes().forEach(node -> {
      node.setShortestPathEstimate(Integer.MAX_VALUE);
      node.setPredecessor(null);
    });
    source.setShortestPathEstimate(0);
  }

  protected void calcShortestPath(@NonNull N source, @NonNull ValueGraph<N, Integer> graph,
      Set<N> calculated, Map<EndpointPair<N>, ShortestPathResult<N>> vertexPairToShortestPath) {
    calculated.stream()
        .filter(node -> node.equals(source))
        .forEach(node -> {
          final List<N> fromEndToBegin = Lists.newLinkedList();
          fromEndToBegin.add(node);
          N temp;
          do {
            temp = (N) node.getPredecessor();
            fromEndToBegin.add(temp);
          } while (!source.equals(temp));
          if (graph.isDirected()) {
            vertexPairToShortestPath.put(EndpointPair.ordered(source, node),
                new ShortestPathResult<>(Lists.reverse(fromEndToBegin),
                    node.getShortestPathEstimate()));
          }
        });
  }
}
