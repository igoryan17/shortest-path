package com.igoryan.services.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.DataStructure;
import com.igoryan.model.Node;
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
    final int weight = 3;
    graph.addNode(first);
    graph.addNode(second);
    graph.putEdgeValue(first, second, weight);

    final Map<ValueGraph<Node, Integer>, Map<EndpointPair<Node>, DataStructure<Node>>>
        graphToDataStructure = new HashMap<>();
    dynamicAlgorithmHelper.init(graph, graphToDataStructure);
    assertThat(graphToDataStructure.values(), hasSize(1));
    final Map<EndpointPair<Node>, DataStructure<Node>> result = graphToDataStructure.get(graph);
    assertThat(result.values(), hasSize(1));
    final DataStructure<Node> dataStructure = result.values().iterator().next();
    assertThat(dataStructure.getShortestPath(), contains(first, second));
    assertThat(dataStructure.getLocallyShortestPath(), empty());
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
    final List<Node> pathAB = Lists.newArrayList(a, b);
    final List<Node> pathBC = Lists.newArrayList(b, c);
    final List<Node> pathAC = Lists.newArrayList(a, b, c);
    final EndpointPair<Node> ab = EndpointPair.ordered(a, b);
    final EndpointPair<Node> bc = EndpointPair.ordered(b, c);
    final EndpointPair<Node> ac = EndpointPair.ordered(a, c);
    final int weightAB = 3;
    final int weightBC = 2;
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, weightAB);
    graph.putEdgeValue(b, c, weightBC);
    final Map<ValueGraph<Node, Integer>, Map<EndpointPair<Node>, DataStructure<Node>>>
        graphToDataStructure = new HashMap<>();
    dynamicAlgorithmHelper.init(graph, graphToDataStructure);
    assertThat(graphToDataStructure.values(), hasSize(1));
    final Map<EndpointPair<Node>, DataStructure<Node>> result = graphToDataStructure.get(graph);
    assertThat(result.values(), hasSize(3));
    assertThat(result.keySet(), hasItems(ab, bc, ac));
    // check data structure AB
    final DataStructure<Node> dataStructureAB = result.get(ab);
    assertThat(dataStructureAB.getShortestPath(), is(pathAB));
    assertThat(dataStructureAB.getLocallyShortestPath(), hasItem(pathAB));
    assertThat(dataStructureAB.getRightExtensionOfShortestPaths().keySet(), hasSize(1));
    assertThat(dataStructureAB.getRightExtensionOfShortestPaths().values(), hasItem(pathAC));
    assertThat(dataStructureAB.getRightExtensionOfLocallyShortestPaths().keySet(), hasSize(1));
    assertThat(dataStructureAB.getRightExtensionOfLocallyShortestPaths().values(), hasItem(pathAC));
    assertThat(dataStructureAB.getLeftExtensionOfShortestPaths().values(), empty());
    assertThat(dataStructureAB.getLeftExtensionOfLocallyShortestPaths().values(), empty());
    // check data structure BC
    final DataStructure<Node> dataStructureBC = result.get(bc);
    assertThat(dataStructureBC.getShortestPath(), is(pathBC));
    assertThat(dataStructureBC.getLocallyShortestPath(), hasItem(pathBC));
    assertThat(dataStructureBC.getLeftExtensionOfShortestPaths().keySet(), hasSize(1));
    assertThat(dataStructureBC.getLeftExtensionOfShortestPaths().values(), hasItem(pathAC));
    assertThat(dataStructureBC.getLeftExtensionOfLocallyShortestPaths().keySet(), hasSize(1));
    assertThat(dataStructureBC.getLeftExtensionOfLocallyShortestPaths().values(), hasItem(pathAC));
    assertThat(dataStructureBC.getRightExtensionOfShortestPaths().keySet(), empty());
    assertThat(dataStructureBC.getRightExtensionOfLocallyShortestPaths().keySet(), empty());
    // check data structure AC
    final DataStructure<Node> dataStructureAC = result.get(ac);
    assertThat(dataStructureAC.getShortestPath(), is(pathAC));
    assertThat(dataStructureAC.getLocallyShortestPath(), hasItem(pathAC));
    assertThat(dataStructureAC.getRightExtensionOfShortestPaths().keySet(), empty());
    assertThat(dataStructureAC.getRightExtensionOfLocallyShortestPaths().keySet(), empty());
    assertThat(dataStructureAC.getLeftExtensionOfShortestPaths().keySet(), empty());
    assertThat(dataStructureAC.getLeftExtensionOfLocallyShortestPaths().keySet(), empty());
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
    final List<Node> pathAB = Lists.newArrayList(a, b);
    final List<Node> pathBC = Lists.newArrayList(b, c);
    final List<Node> pathAC = Lists.newArrayList(a, c);
    final List<Node> pathABC = Lists.newArrayList(a, b, c);
    final EndpointPair<Node> ab = EndpointPair.ordered(a, b);
    final EndpointPair<Node> bc = EndpointPair.ordered(b, c);
    final EndpointPair<Node> ac = EndpointPair.ordered(a, c);
    final int weightAB = 3;
    final int weightBC = 2;
    final int weightAC = 1;
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.putEdgeValue(a, b, weightAB);
    graph.putEdgeValue(b, c, weightBC);
    graph.putEdgeValue(a, c, weightAC);
    final Map<ValueGraph<Node, Integer>, Map<EndpointPair<Node>, DataStructure<Node>>>
        graphToDataStructure = new HashMap<>();
    dynamicAlgorithmHelper.init(graph, graphToDataStructure);
    assertThat(graphToDataStructure.values(), hasSize(1));
    final Map<EndpointPair<Node>, DataStructure<Node>> result = graphToDataStructure.get(graph);
    assertThat(result.values(), hasSize(3));
    assertThat(result.keySet(), hasItems(ab, bc, ac));
    // check data structure AB
    final DataStructure<Node> dataStructureAB = result.get(ab);
    assertThat(dataStructureAB.getShortestPath(), is(pathAB));
    assertThat(dataStructureAB.getLocallyShortestPath(), hasItem(pathAB));
    assertThat(dataStructureAB.getRightExtensionOfLocallyShortestPaths().keySet(), hasSize(1));
    assertThat(dataStructureAB.getRightExtensionOfLocallyShortestPaths().values(),
        hasItem(pathABC));
    assertThat(dataStructureAB.getRightExtensionOfLocallyShortestPaths().values(), hasItem(pathABC));
    assertThat(dataStructureAB.getRightExtensionOfShortestPaths().keySet(), empty());
    assertThat(dataStructureAB.getLeftExtensionOfLocallyShortestPaths().keySet(), empty());
    assertThat(dataStructureAB.getLeftExtensionOfShortestPaths().keySet(), empty());
    // check data structure BC
    final DataStructure<Node> dataStructureBC = result.get(bc);
    assertThat(dataStructureBC.getShortestPath(), is(pathBC));
    assertThat(dataStructureBC.getLocallyShortestPath(), hasSize(1));
    assertThat(dataStructureBC.getLocallyShortestPath(), hasItem(pathBC));
    assertThat(dataStructureBC.getRightExtensionOfShortestPaths().keySet(), empty());
    assertThat(dataStructureBC.getRightExtensionOfLocallyShortestPaths().keySet(), empty());
    assertThat(dataStructureBC.getLeftExtensionOfShortestPaths().keySet(), empty());
    assertThat(dataStructureBC.getLeftExtensionOfLocallyShortestPaths().keySet(), hasSize(1));
    assertThat(dataStructureBC.getLeftExtensionOfLocallyShortestPaths().values(), hasItem(pathABC));
    // check data structure AC
    final DataStructure<Node> dataStructureAC = result.get(ac);
    assertThat(dataStructureAC.getShortestPath(), is(pathAC));
    assertThat(dataStructureAC.getLocallyShortestPath(), hasSize(2));
    assertThat(dataStructureAC.getLocallyShortestPath(), hasItems(pathAC, pathABC));
    assertThat(dataStructureAC.getRightExtensionOfShortestPaths().values(), empty());
    assertThat(dataStructureAC.getRightExtensionOfLocallyShortestPaths().values(), empty());
    assertThat(dataStructureAC.getLeftExtensionOfShortestPaths().values(), empty());
    assertThat(dataStructureAC.getLeftExtensionOfLocallyShortestPaths().values(), empty());
  }
}