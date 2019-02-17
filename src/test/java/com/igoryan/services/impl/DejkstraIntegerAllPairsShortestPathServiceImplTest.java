package com.igoryan.services.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.Node;
import com.igoryan.model.ShortestPathResult;
import com.igoryan.services.IntegerAllPairsShortestPathService;
import com.igoryan.services.IntegerRelaxationService;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class DejkstraIntegerAllPairsShortestPathServiceImplTest {

  private final IntegerRelaxationService<Node> relaxationService =
      new IntegerRelaxationService<Node>() {
      };
  private final IntegerAllPairsShortestPathService<Node> allPairsShortestPathService =
      new DejkstraIntegerAllPairsShortestPathServiceImpl<>(relaxationService);
  private final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
      .expectedNodeCount(2)
      .build();
  private final Node first = new Node("a");
  private final Node second = new Node("b");
  private final int weight = 3;

  @Before
  public void fillGraph() {
    graph.addNode(first);
    graph.addNode(second);
    graph.putEdgeValue(first, second, weight);
  }

  @Test
  public void calculate() {
    allPairsShortestPathService.calculate(graph, first);
    final Map<EndpointPair<Node>, ShortestPathResult<Node>> result =
        allPairsShortestPathService.getNodePairToShortestPath(graph);
    assertThat(result.keySet(), hasSize(1));
    final EndpointPair<Node> endpointPair = result.keySet().iterator().next();
    assertThat(endpointPair.source(), is(first));
    assertThat(endpointPair.target(), is(second));
    assertThat(result.values(), hasSize(1));
    final ShortestPathResult<Node> shortestPathResult = result.get(endpointPair);
    assertThat(shortestPathResult.getWeight(), is(weight));
    assertThat(shortestPathResult.getShortestPath(), contains(first, second));
  }

  @Test
  public void init() {
  }

  @Test
  public void calcShortestPath() {
  }
}