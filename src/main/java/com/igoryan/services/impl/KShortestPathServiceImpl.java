package com.igoryan.services.impl;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Network;
import com.igoryan.model.EdgeWithWeight;
import com.igoryan.model.Path;
import com.igoryan.services.KShortestPathService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class KShortestPathServiceImpl<N> implements KShortestPathService<N> {
  @Override
  public List<Path<N>> calculate(final Network<N, ? extends EdgeWithWeight> graph, final N src, final N dst,
      final int pathCount) {
    final List<Path<N>> result = new ArrayList<>(pathCount);
    final PriorityQueue<Path<N>> queue = new PriorityQueue<>(Comparator.comparing(Path::getWeight));
    queue.add(new Path<>(EndpointPair.ordered(src, src), Collections.singletonList(src), 0));
    final Map<N, AtomicInteger> dstToPathCount = new HashMap<>();
    graph.nodes().forEach(node -> dstToPathCount.put(node, new AtomicInteger()));
    while (!queue.isEmpty()) {
      Path<N> p = queue.poll();
      final N u = p.getFistAndLast().target();
      AtomicInteger count = dstToPathCount.get(u);
      int currentCount = count.incrementAndGet();
      if (u.equals(dst)) {
        result.add(p);
      }
      if (currentCount <= pathCount) {
        graph.successors(u).forEach(v -> {
          List<N> path = new ArrayList<>(p.getVertexChain().size() + 1);
          path.addAll(p.getVertexChain());
          path.add(v);
          EndpointPair<N> sv = EndpointPair.ordered(src, v);
          graph.edgesConnecting(u, v)
              .forEach(edge -> queue.add(new Path<>(sv, path, edge.getWeight())));
        });
      }
    }
    return result;
  }
}
