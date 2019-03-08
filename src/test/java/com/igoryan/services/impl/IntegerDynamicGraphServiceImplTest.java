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
}