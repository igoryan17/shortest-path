package com.igoryan.services.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItems;
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
import org.junit.Test;

public class DejkstraIntegerAllPairsShortestPathServiceImplTest {

  private final IntegerRelaxationService<Node> relaxationService =
      new IntegerRelaxationService<Node>() {
      };
  private final IntegerAllPairsShortestPathService<Node> allPairsShortestPathService =
      new DejkstraIntegerAllPairsShortestPathServiceImpl<>(relaxationService);

  @Test
  public void calculateSimpleCase() {
    // prepare graph
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder
        .directed()
        .expectedNodeCount(2)
        .build();
    final Node first = new Node("a");
    final Node second = new Node("b");
    final int weight = 3;
    graph.addNode(first);
    graph.addNode(second);
    graph.putEdgeValue(first, second, weight);
    // calculate
    allPairsShortestPathService.calculate(graph, first);
    final Map<EndpointPair<Node>, ShortestPathResult<Node>> result =
        allPairsShortestPathService.getNodePairToShortestPath(graph);
    // check
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
  public void calculateAllPairsShortestPath() {
    // prepare graph
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder
        .directed()
        .expectedNodeCount(3)
        .build();
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, 1);
    graph.putEdgeValue(b, c, 2);
    graph.putEdgeValue(a, c, 1);
    allPairsShortestPathService.calculate(graph);
    final Map<EndpointPair<Node>, ShortestPathResult<Node>> result = allPairsShortestPathService
        .getNodePairToShortestPath(graph);
    // check
    assertThat(result.keySet(), hasSize(3));
    final EndpointPair<Node> ab = EndpointPair.ordered(a, b);
    final EndpointPair<Node> bc = EndpointPair.ordered(b, c);
    final EndpointPair<Node> ac = EndpointPair.ordered(a, c);
    assertThat(result.keySet(), hasItems(ab, bc, ac));
    assertThat(result.get(ab).getWeight(), is(1));
    assertThat(result.get(bc).getWeight(), is(2));
    assertThat(result.get(ac).getWeight(), is(1));
    assertThat(result.get(ab).getShortestPath(), contains(a, b));
    assertThat(result.get(bc).getShortestPath(), contains(b, c));
    assertThat(result.get(ac).getShortestPath(), contains(a, c));
  }
}