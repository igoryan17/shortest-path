package com.igoryan.services.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.DataStructure;
import com.igoryan.model.Node;
import com.igoryan.model.Path;
import com.igoryan.services.IntegerRelaxationService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class IntegerDynamicAlgorithmHelperTest {

  private final IntegerDynamicAlgorithmHelper<Node> dynamicAlgorithmHelper =
      new IntegerDynamicAlgorithmHelper<>(new DejkstraAllPairsShortestPathServiceImpl<>(
          new IntegerRelaxationService<Node>() {
          }));

  @Test
  public void testInitSimpleGraph() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder
        .directed()
        .expectedNodeCount(2)
        .build();
    final Node first = new Node("a");
    final Node second = new Node("b");
    final EndpointPair<Node> ab = EndpointPair.ordered(first, second);
    final List<Node> vertexesOfShortestPathAB = Lists.newArrayList(first, second);
    final Path<Node> pathAB = new Path<>(ab, vertexesOfShortestPathAB);
    final int weight = 3;
    graph.addNode(first);
    graph.addNode(second);
    graph.putEdgeValue(first, second, weight);

    final Map<ValueGraph<Node, Integer>, DataStructure<Node>> graphToDataStructure =
        new HashMap<>();
    dynamicAlgorithmHelper.init(graph, graphToDataStructure);
    assertThat(graphToDataStructure.values(), hasSize(1));
    final DataStructure<Node> dataStructure = graphToDataStructure.get(graph);
    // check shortest path
    assertThat(dataStructure.getShortestPath().keySet(), hasSize(1));
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(ab));
    assertThat(dataStructure.getShortestPath().get(ab).getVertexChain(), contains(first, second));
    // check local shortest path
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasSize(1));
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasItem(ab));
    assertThat(dataStructure.getLocallyShortestPath().get(ab), hasItem(pathAB));
    // check that other empty
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().values(), empty());
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().values(), empty());
    assertThat(dataStructure.getRightExtensionOfShortestPaths().values(), empty());
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().values(), empty());
  }

  @Test
  public void testSubShortestPath() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder
        .directed()
        .expectedNodeCount(2)
        .build();
    // graph a -> b -> c
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    // a -> b
    final EndpointPair<Node> ab = EndpointPair.ordered(a, b);
    final List<Node> vertexesOfPathAB = Lists.newArrayList(a, b);
    final Path<Node> pathAB = new Path<>(ab, vertexesOfPathAB);
    // b -> c
    final EndpointPair<Node> bc = EndpointPair.ordered(b, c);
    final List<Node> vertexesOfPathBC = Lists.newArrayList(b, c);
    final Path<Node> pathBC = new Path<>(bc, vertexesOfPathBC);
    // a -> c
    final List<Node> vertexesOfPathABC = Lists.newArrayList(a, b, c);
    final EndpointPair<Node> ac = EndpointPair.ordered(a, c);
    final Path<Node> pathABC = new Path<>(ac, vertexesOfPathABC);

    final int weightAB = 3;
    final int weightBC = 2;
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, weightAB);
    graph.putEdgeValue(b, c, weightBC);
    final Map<ValueGraph<Node, Integer>, DataStructure<Node>>
        graphToDataStructure = new HashMap<>();
    dynamicAlgorithmHelper.init(graph, graphToDataStructure);
    assertThat(graphToDataStructure.values(), hasSize(1));
    final DataStructure<Node> dataStructure = graphToDataStructure.get(graph);
    // check path <A, B>
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(ab));
    assertThat(dataStructure.getShortestPath().get(ab), is(pathAB));
    assertThat(dataStructure.getLocallyShortestPath().get(ab), hasItem(pathAB));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().keySet(), hasItem(pathAB));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().get(pathAB), hasItem(pathABC));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().keySet(), hasItem(pathAB));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().get(pathAB),
        hasItem(pathABC));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().keySet(), not(hasItem(pathAB)));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathAB)));
    // check path <B, C>
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(bc));
    assertThat(dataStructure.getShortestPath().get(bc), is(pathBC));
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasItem(bc));
    assertThat(dataStructure.getLocallyShortestPath().get(bc), hasItem(pathBC));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().keySet(), hasItem(pathBC));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().get(pathBC), hasItem(pathABC));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().keySet(), hasItem(pathBC));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().get(pathBC),
        hasItem(pathABC));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().keySet(), not(hasItem(pathBC)));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathBC)));
    // check data structure AC
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(ac));
    assertThat(dataStructure.getShortestPath().get(ac), is(pathABC));
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasItem(ac));
    assertThat(dataStructure.getLocallyShortestPath().get(ac), hasSize(1));
    assertThat(dataStructure.getLocallyShortestPath().get(ac), hasItem(pathABC));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().keySet(), not(hasItem(pathABC)));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathABC)));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().keySet(), not(hasItem(pathABC)));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathABC)));
  }

  @Test
  public void testWithLocalShortestPath() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder
        .directed()
        .expectedNodeCount(2)
        .build();
    // graph a -> b -> c; a -> c
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    // a -> b
    final List<Node> vertexesOfPathAB = Lists.newArrayList(a, b);
    final EndpointPair<Node> ab = EndpointPair.ordered(a, b);
    final Path<Node> pathAB = new Path<>(ab, vertexesOfPathAB);
    // b -> c
    final List<Node> vertexesOfPathBC = Lists.newArrayList(b, c);
    final EndpointPair<Node> bc = EndpointPair.ordered(b, c);
    final Path<Node> pathBC = new Path<>(bc, vertexesOfPathBC);
    // a -> c
    final EndpointPair<Node> ac = EndpointPair.ordered(a, c);
    final List<Node> vertexesOfPathAC = Lists.newArrayList(a, c);
    final Path<Node> pathAC = new Path<>(ac, vertexesOfPathAC);
    final List<Node> vertexesOfPathABC = Lists.newArrayList(a, b, c);
    final Path<Node> pathABC = new Path<>(ac, vertexesOfPathABC);
    final int weightAB = 3;
    final int weightBC = 2;
    final int weightAC = 1;
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, weightAB);
    graph.putEdgeValue(b, c, weightBC);
    graph.putEdgeValue(a, c, weightAC);
    final Map<ValueGraph<Node, Integer>, DataStructure<Node>> graphToDataStructure =
        new HashMap<>();
    dynamicAlgorithmHelper.init(graph, graphToDataStructure);
    assertThat(graphToDataStructure.values(), hasSize(1));
    final DataStructure<Node> dataStructure = graphToDataStructure.get(graph);
    // check path <A, B>
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(ab));
    assertThat(dataStructure.getShortestPath().get(ab), is(pathAB));
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasItem(ab));
    assertThat(dataStructure.getLocallyShortestPath().get(ab), hasSize(1));
    assertThat(dataStructure.getLocallyShortestPath().get(ab), hasItem(pathAB));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().keySet(), hasItem(pathAB));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().get(pathAB), hasSize(1));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().get(pathAB),
        hasItem(pathABC));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().keySet(), not(hasItem(pathAB)));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathAB)));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().keySet(), not(hasItem(pathAB)));
    // check path <B, C>
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(bc));
    assertThat(dataStructure.getShortestPath().get(bc), is(pathBC));
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasItem(bc));
    assertThat(dataStructure.getLocallyShortestPath().get(bc), hasSize(1));
    assertThat(dataStructure.getLocallyShortestPath().get(bc), hasItem(pathBC));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().keySet(), not(hasItem(pathBC)));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathBC)));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().keySet(), not(hasItem(pathBC)));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().keySet(), hasItem(pathBC));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().get(pathBC),
        hasItem(pathABC));
    // check data structure AC
    assertThat(dataStructure.getShortestPath().keySet(), hasItem(ac));
    assertThat(dataStructure.getShortestPath().get(ac), is(pathAC));
    assertThat(dataStructure.getLocallyShortestPath().keySet(), hasItem(ac));
    assertThat(dataStructure.getLocallyShortestPath().get(ac), hasSize(2));
    assertThat(dataStructure.getLocallyShortestPath().get(ac), hasItems(pathAC, pathABC));
    assertThat(dataStructure.getRightExtensionOfShortestPaths().keySet(), not(hasItem(pathAC)));
    assertThat(dataStructure.getRightExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathAC)));
    assertThat(dataStructure.getLeftExtensionOfShortestPaths().keySet(), not(hasItem(pathAC)));
    assertThat(dataStructure.getLeftExtensionOfLocallyShortestPaths().keySet(),
        not(hasItem(pathAC)));
  }
}