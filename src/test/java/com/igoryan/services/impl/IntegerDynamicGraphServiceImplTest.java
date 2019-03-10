package com.igoryan.services.impl;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.GraphWrapper;
import com.igoryan.model.Node;
import com.igoryan.model.WeightUpdating;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import com.igoryan.services.IntegerRelaxationService;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class IntegerDynamicGraphServiceImplTest {

  private final IntegerDejkstraAllPairsShortestPathService<Node>
      dejkstraAllPairsShortestPathService = new DejkstraAllPairsShortestPathServiceImpl<>(
      new IntegerRelaxationService<Node>() {
      });
  private final IntegerDynamicAlgorithmHelper<Node> dynamicAlgorithmHelper =
      new IntegerDynamicAlgorithmHelper<>(dejkstraAllPairsShortestPathService);
  private final IntegerDynamicGraphService<Node> dynamicGraphService =
      new IntegerDynamicGraphServiceImpl<>(dynamicAlgorithmHelper);

  @Test
  public void testTwoVertexes() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(2)
        .build();
    final GraphWrapper<Node> graphWrapper = new GraphWrapper<>("1", graph);
    final Node a = new Node("a");
    final Node b = new Node("b");
    final long weightAB = 1;
    final WeightUpdating<Node> weightUpdating =
        new WeightUpdating<>(a, Collections.emptyMap(),
            Collections.singletonMap(b, (int) weightAB));
    graph.addNode(a);
    graph.addNode(b);
    dynamicGraphService.update(graphWrapper, weightUpdating);
    assertThat(dynamicGraphService.path(graphWrapper, a, b), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
  }

  @Test
  public void testThreeVertexes() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<Node> graphWrapper = new GraphWrapper<>("1", graph);
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    final long weightAB = 1;
    final long weightAC = 2;
    final Map<Node, Integer> outgoingUpdate = new HashMap<>(2);
    outgoingUpdate.put(b, (int) weightAB);
    outgoingUpdate.put(c, (int) weightAC);
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    final WeightUpdating<Node> weightUpdating =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingUpdate);
    dynamicGraphService.update(graphWrapper, weightUpdating);
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
  }

  @Test
  public void testThreeVertexesWithAlternativeWay() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<Node> graphWrapper = new GraphWrapper<>("1", graph);
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    final long weightAC = 1;
    final long weightAB = 2;
    final long weightBC = 3;
    final WeightUpdating<Node> weightUpdatingC =
        new WeightUpdating<>(c, Collections.singletonMap(b, (int) weightBC),
            Collections.emptyMap());
    dynamicGraphService.update(graphWrapper, weightUpdatingC);
    final Map<Node, Integer> outgoingToWeight = new HashMap<>(2);
    outgoingToWeight.put(c, (int) weightAC);
    outgoingToWeight.put(b, (int) weightAB);
    final WeightUpdating<Node> weightUpdatingA =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingToWeight);
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    // check a -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, b, c), contains(b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, b, c), is(weightBC));
  }

  @Test
  public void testThreeVertex() {
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<Node> graphWrapper = new GraphWrapper<>("1", graph);
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    final long weightAB = 2;
    final long weightBC = 3;
    dynamicGraphService
        .update(graphWrapper, new WeightUpdating<>(b, Collections.singletonMap(a, (int) weightAB),
            Collections.emptyMap()));
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(c, Collections.singletonMap(b, (int) weightBC),
            Collections.emptyMap()));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, b, c), contains(b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, b, c), is(weightBC));
    // check a -> b -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c), contains(a, b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAB + weightBC));
  }

  @Test
  public void testFourVertex() {
    // graph is a -> c, a -> b -> d, c -> b
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(4)
        .build();
    final GraphWrapper<Node> graphWrapper = new GraphWrapper<>("1", graph);
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    final Node d = new Node("d");
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    final long weightAC = 1;
    final long weightAB = 2;
    final long weightCB = 3;
    final long weightBD = 4;
    final Map<Node, Integer> outgoingUpdate = new HashMap<>(2);
    outgoingUpdate.put(b, (int) weightAB);
    outgoingUpdate.put(c, (int) weightAC);
    final WeightUpdating<Node> weightUpdatingA =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingUpdate);
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    final WeightUpdating<Node> weightUpdatingC =
        new WeightUpdating<>(c, Collections.singletonMap(a, (int) weightAC),
            Collections.singletonMap(b, (int) weightCB));
    dynamicGraphService.update(graphWrapper, weightUpdatingC);
    final WeightUpdating<Node> weightUpdatingD =
        new WeightUpdating<>(d, Collections.singletonMap(b, (int) weightBD),
            Collections.emptyMap());
    dynamicGraphService.update(graphWrapper, weightUpdatingD);
    // check path a -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
    // check path a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check path c -> b
    assertThat(dynamicGraphService.path(graphWrapper, c, b), contains(c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, c, b), is(weightCB));
    // check path a -> b -> d
    assertThat(dynamicGraphService.path(graphWrapper, a, d), contains(a, b, d));
    assertThat(dynamicGraphService.distance(graphWrapper, a, d), is(weightAB + weightBD));
  }

  @Test
  public void testFourVertexAllPairs() {
    //     c
    //    / \
    //   a - b
    //    \ /
    //     d
    final MutableValueGraph<Node, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(4)
        .build();
    final GraphWrapper<Node> graphWrapper = new GraphWrapper<>("1", graph);
    final Node a = new Node("a");
    final Node b = new Node("b");
    final Node c = new Node("c");
    final Node d = new Node("d");
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    final long weightAB = 4;
    final long weightAC = 1;
    final long weightCB = 2;
    final long weightAD = 10;
    final long weightBD = 5;
    final Map<Node, Integer> outgoingUpdateA = new HashMap<>(3);
    outgoingUpdateA.put(c, (int) weightAC);
    outgoingUpdateA.put(b, (int) weightAB);
    outgoingUpdateA.put(d, (int) weightAD);
    final WeightUpdating<Node> weightUpdatingA =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingUpdateA);
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(c, Collections.singletonMap(a, (int) weightAC),
            Collections.singletonMap(b, (int) weightCB)));
    final Map<Node, Integer> incomingUpdateB = new HashMap<>(2);
    incomingUpdateB.put(c, (int) weightCB);
    incomingUpdateB.put(a, 4);
    final WeightUpdating<Node> weightUpdatingB =
        new WeightUpdating<>(b, incomingUpdateB, Collections.singletonMap(d, (int) weightBD));
    dynamicGraphService.update(graphWrapper, weightUpdatingB);
    final Map<Node, Integer> incomingUpdateD = new HashMap<>(2);
    incomingUpdateD.put(a, (int) weightAD);
    incomingUpdateD.put(b, (int) weightBD);
    dynamicGraphService
        .update(graphWrapper, new WeightUpdating<>(d, incomingUpdateD, Collections.emptyMap()));
    // check a -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
    // check c -> b
    assertThat(dynamicGraphService.path(graphWrapper, c, b), contains(c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, c, b), is(weightCB));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b), contains(a, c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAC + weightCB));
    // check a -> b -> d
    assertThat(dynamicGraphService.path(graphWrapper, a, d), contains(a, c, b, d));
    assertThat(dynamicGraphService.distance(graphWrapper, a, d),
        is(weightAC + weightCB + weightBD));
    // check b -> d
    assertThat(dynamicGraphService.path(graphWrapper, b, d), contains(b, d));
    assertThat(dynamicGraphService.distance(graphWrapper, b, d), is(weightBD));
  }
}