package com.igoryan.services.impl;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.igoryan.model.EdgeWithWeight;
import com.igoryan.model.Path;
import com.igoryan.services.KShortestPathService;
import java.util.List;
import org.junit.Test;

public class KShortestPathServiceImplTest {

  private final KShortestPathService<String> shortestPathService = new KShortestPathServiceImpl<>();

  @Test
  public void calculate() {

    final MutableNetwork<String, EdgeWithWeight<String>> graph = NetworkBuilder.directed()
        .allowsParallelEdges(true)
        .expectedNodeCount(4)
        .build();
    graph.addNode("a");
    graph.addNode("b");
    graph.addEdge("a", "b", new EdgeWithWeight<>("ab", 1));
    graph.addEdge("a", "b", new EdgeWithWeight<>("ab", 2));
    final List<Path<String>> result = shortestPathService.calculate(graph, "a", "b", 2);
    assertThat(result, hasSize(2));
  }
}