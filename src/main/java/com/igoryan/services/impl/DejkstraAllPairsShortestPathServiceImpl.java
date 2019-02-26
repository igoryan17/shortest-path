package com.igoryan.services.impl;

import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.ShortestPathResult;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerRelaxationService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import lombok.NonNull;

public class DejkstraAllPairsShortestPathServiceImpl<N extends IntegerBaseNode>
    implements IntegerDejkstraAllPairsShortestPathService<N> {

  private final Map<ValueGraph<N, Integer>, Map<EndpointPair<N>, ShortestPathResult<N>>>
      graphToShortestPaths =
      new HashMap<>();
  private final IntegerRelaxationService<N> relaxationService;

  public DejkstraAllPairsShortestPathServiceImpl(
      final IntegerRelaxationService<N> relaxationService) {
    this.relaxationService = relaxationService;
  }

  @Override
  public void calculate(@NonNull final ValueGraph<N, Integer> graph) {
    graph.nodes().forEach(node -> calculate(graph, node));
    final Map<EndpointPair<N>, ShortestPathResult<N>> vertexPairToShortestPath =
        graphToShortestPaths.get(graph);
  }

  public void calculate(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N source) {
    graphToShortestPaths.putIfAbsent(graph, new HashMap<>());
    final Map<EndpointPair<N>, ShortestPathResult<N>> vertexPairToShortestPath =
        graphToShortestPaths.get(graph);
    init(graph, source);
    final Set<N> calculated = new HashSet<>(graph.nodes().size());
    final PriorityQueue<N> queue = new PriorityQueue<>(graph.nodes());
    while (!queue.isEmpty()) {
      N u = queue.poll();
      calculated.add(u);
      graph.successors(u).forEach(node -> relaxationService
          .relax(u, node, graph.edgeValueOrDefault(u, node, Integer.MAX_VALUE)));
    }
    calcShortestPath(source, graph, calculated, vertexPairToShortestPath);
  }

  @Override
  public Map<EndpointPair<N>, ShortestPathResult<N>> getNodePairToShortestPath(
      @NonNull final ValueGraph<N, Integer> graph) {
    return graphToShortestPaths.get(graph);
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
        .filter(node -> !node.equals(source))
        .filter(node -> node.getShortestPathEstimate() != Integer.MAX_VALUE)
        .forEach(node -> {
          final LinkedList<N> shortestPath = Lists.newLinkedList();
          shortestPath.addFirst(node);
          N temp = node;
          while (temp.getPredecessor() != null) {
            shortestPath.addFirst((N) temp.getPredecessor());
            temp = (N) temp.getPredecessor();
          }
          if (graph.isDirected()) {
            vertexPairToShortestPath.put(EndpointPair.ordered(source, node),
                new ShortestPathResult<>(new ArrayList<>(shortestPath), node.getShortestPathEstimate()));
          }
        });
  }
}
