package com.igoryan.services.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.GraphWrapper;
import com.igoryan.model.WeightUpdating;
import com.igoryan.services.IntegerDynamicGraphService;
import com.igoryan.util.GraphGenerator;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class IntegerDynamicGraphServiceImplTest {

  private final IntegerDynamicAlgorithmHelper<String> dynamicAlgorithmHelper =
      new IntegerDynamicAlgorithmHelper<>();
  private final IntegerDynamicGraphService<String> dynamicGraphService =
      new IntegerDynamicGraphServiceImpl<>(dynamicAlgorithmHelper);

  @Test
  public void testTwoVertexes() {
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(2)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final long weightAB = 1;
    final WeightUpdating<String> weightUpdating =
        new WeightUpdating<>(a, Collections.emptyMap(),
            singletonMap(b, (int) weightAB));
    graph.addNode(a);
    graph.addNode(b);
    dynamicGraphService.update(graphWrapper, weightUpdating);
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
  }

  @Test
  public void testCycle() {
    //  a <- b
    //  a -> b
    //      /
    //     c
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final long weightAB = 1;
    final long weightBA = 5;
    final long weightCB = 3;
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    final WeightUpdating<String> weightUpdatingA =
        new WeightUpdating<>(a, singletonMap(b, (int) weightBA),
            singletonMap(b, (int) weightAB));
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    final Map<String, Integer> incomingUpdate = new HashMap<>(2);
    incomingUpdate.put(c, (int) weightCB);
    incomingUpdate.put(a, (int) weightAB);
    final Map<String, Integer> outgoingUpdate = new HashMap<>(1);
    outgoingUpdate.put(a, (int) weightBA);
    final WeightUpdating<String> weightUpdatingB =
        new WeightUpdating<>(b, incomingUpdate, outgoingUpdate);
    dynamicGraphService.update(graphWrapper, weightUpdatingB);
    // a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // b -> a
    assertThat(dynamicGraphService.path(graphWrapper, b, a).getVertexChain(), contains(b, a));
    assertThat(dynamicGraphService.distance(graphWrapper, b, a), is(weightBA));
    // c -> b
    assertThat(dynamicGraphService.path(graphWrapper, c, b).getVertexChain(), contains(c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, c, b), is(weightCB));
    // c -> a
    assertThat(dynamicGraphService.path(graphWrapper, c, a).getVertexChain(), contains(c, b, a));
    assertThat(dynamicGraphService.distance(graphWrapper, c, a), is(weightCB + weightBA));
  }

  @Test
  public void testThreeVertexes() {
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final long weightAB = 1;
    final long weightAC = 2;
    final Map<String, Integer> outgoingUpdate = new HashMap<>(2);
    outgoingUpdate.put(b, (int) weightAB);
    outgoingUpdate.put(c, (int) weightAC);
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    final WeightUpdating<String> weightUpdating =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingUpdate);
    dynamicGraphService.update(graphWrapper, weightUpdating);
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
  }

  @Test
  public void testThreeVertexesWithAlternativeWay() {
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final long weightAC = 1;
    final long weightAB = 2;
    final long weightBC = 3;
    final WeightUpdating<String> weightUpdatingC =
        new WeightUpdating<>(c, singletonMap(b, (int) weightBC),
            Collections.emptyMap());
    dynamicGraphService.update(graphWrapper, weightUpdatingC);
    final Map<String, Integer> outgoingToWeight = new HashMap<>(2);
    outgoingToWeight.put(c, (int) weightAC);
    outgoingToWeight.put(b, (int) weightAB);
    final WeightUpdating<String> weightUpdatingA =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingToWeight);
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    // check a -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, b, c).getVertexChain(), contains(b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, b, c), is(weightBC));
  }

  @Test
  public void testThreeVertex() {
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final long weightAB = 2;
    final long weightBC = 3;
    dynamicGraphService
        .update(graphWrapper, new WeightUpdating<>(b, singletonMap(a, (int) weightAB),
            Collections.emptyMap()));
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(c, singletonMap(b, (int) weightBC),
            Collections.emptyMap()));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, b, c).getVertexChain(), contains(b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, b, c), is(weightBC));
    // check a -> b -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAB + weightBC));
  }

  @Test
  public void testFourVertex() {
    // graph is a -> c, a -> b -> d, c -> b
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(4)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final String d = "d";
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    final long weightAC = 1;
    final long weightAB = 2;
    final long weightCB = 3;
    final long weightBD = 4;
    final Map<String, Integer> outgoingUpdate = new HashMap<>(2);
    outgoingUpdate.put(b, (int) weightAB);
    outgoingUpdate.put(c, (int) weightAC);
    final WeightUpdating<String> weightUpdatingA =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingUpdate);
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    final WeightUpdating<String> weightUpdatingC =
        new WeightUpdating<>(c, singletonMap(a, (int) weightAC),
            singletonMap(b, (int) weightCB));
    dynamicGraphService.update(graphWrapper, weightUpdatingC);
    final WeightUpdating<String> weightUpdatingD =
        new WeightUpdating<>(d, singletonMap(b, (int) weightBD),
            Collections.emptyMap());
    dynamicGraphService.update(graphWrapper, weightUpdatingD);
    // check path a -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
    // check path a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check path c -> b
    assertThat(dynamicGraphService.path(graphWrapper, c, b).getVertexChain(), contains(c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, c, b), is(weightCB));
    // check path a -> b -> d
    assertThat(dynamicGraphService.path(graphWrapper, a, d).getVertexChain(), contains(a, b, d));
    assertThat(dynamicGraphService.distance(graphWrapper, a, d), is(weightAB + weightBD));
  }

  @Test
  public void testFourVertexAllPairs() {
    //     c
    //    / \
    //   a - b
    //    \ /
    //     d
    final MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(4)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("1", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final String d = "d";
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    final long weightAB = 4;
    final long weightAC = 1;
    final long weightCB = 2;
    final long weightAD = 10;
    final long weightBD = 5;
    final Map<String, Integer> outgoingUpdateA = new HashMap<>(3);
    outgoingUpdateA.put(c, (int) weightAC);
    outgoingUpdateA.put(b, (int) weightAB);
    outgoingUpdateA.put(d, (int) weightAD);
    final WeightUpdating<String> weightUpdatingA =
        new WeightUpdating<>(a, Collections.emptyMap(), outgoingUpdateA);
    dynamicGraphService.update(graphWrapper, weightUpdatingA);
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(c, singletonMap(a, (int) weightAC),
            singletonMap(b, (int) weightCB)));
    final Map<String, Integer> incomingUpdateB = new HashMap<>(2);
    incomingUpdateB.put(c, (int) weightCB);
    incomingUpdateB.put(a, 4);
    final WeightUpdating<String> weightUpdatingB =
        new WeightUpdating<>(b, incomingUpdateB, singletonMap(d, (int) weightBD));
    dynamicGraphService.update(graphWrapper, weightUpdatingB);
    final Map<String, Integer> incomingUpdateD = new HashMap<>(2);
    incomingUpdateD.put(a, (int) weightAD);
    incomingUpdateD.put(b, (int) weightBD);
    dynamicGraphService
        .update(graphWrapper, new WeightUpdating<>(d, incomingUpdateD, Collections.emptyMap()));
    // check a -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAC));
    // check c -> b
    assertThat(dynamicGraphService.path(graphWrapper, c, b).getVertexChain(), contains(c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, c, b), is(weightCB));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, c, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAC + weightCB));
    // check a -> b -> d
    assertThat(dynamicGraphService.path(graphWrapper, a, d).getVertexChain(), contains(a, c, b, d));
    assertThat(dynamicGraphService.distance(graphWrapper, a, d),
        is(weightAC + weightCB + weightBD));
    // check b -> d
    assertThat(dynamicGraphService.path(graphWrapper, b, d).getVertexChain(), contains(b, d));
    assertThat(dynamicGraphService.distance(graphWrapper, b, d), is(weightBD));
  }

  @Test
  public void testComplexGraph() {
    MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(10)
        .build();

    final String a = "a";
    final String b = "b";
    final String c = "c";
    final String d = "d";
    final String e = "e";

    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    graph.addNode(e);

    final int weightAE = 10;
    final int weightAD = 2;
    final int weightBA = 4;
    final int weightBE = 1;
    final int weightCE = 9;
    final int weightCB = 5;
    final int weightDC = 9;
    final int weightEA = 10;
    final int weightEB = 1;
    final int weightEC = 5;

    GraphWrapper<String> graphWrapper = new GraphWrapper<>("", graph);

    final Map<String, Integer> incomingOfA = new HashMap<>(2);
    incomingOfA.put(b, weightBA);
    incomingOfA.put(e, weightEA);
    final Map<String, Integer> outgoingOfA = new HashMap<>(2);
    outgoingOfA.put(e, weightAE);
    outgoingOfA.put(d, weightAD);
    dynamicGraphService.update(graphWrapper, new WeightUpdating<>(a, incomingOfA, outgoingOfA));
    // check b -> d
    assertThat(dynamicGraphService.path(graphWrapper, b, d).getVertexChain(), contains(b, a, d));
    assertThat(dynamicGraphService.distance(graphWrapper, b, d), is((long) (weightBA + weightAD)));

    final Map<String, Integer> incomingOfC = new HashMap<>(2);
    incomingOfC.put(d, weightDC);
    incomingOfC.put(e, weightEC);
    final Map<String, Integer> outgoingOfC = new HashMap<>(2);
    outgoingOfC.put(b, weightCB);
    outgoingOfC.put(e, weightCE);
    dynamicGraphService.update(graphWrapper, new WeightUpdating<>(c, incomingOfC, outgoingOfC));
    // check b -> d
    assertThat(dynamicGraphService.path(graphWrapper, b, d).getVertexChain(), contains(b, a, d));
    assertThat(dynamicGraphService.distance(graphWrapper, b, d), is((long) (weightBA + weightAD)));

    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(b, singletonMap(e, weightEB),
            singletonMap(e, weightBE)));
    System.out.println(dynamicGraphService.path(graphWrapper, b, d));
    System.out.println(GraphGenerator.directedStringToGraphVizRepresentation(graph));
    assertThat(dynamicGraphService.path(graphWrapper, b, d).getVertexChain(), contains(b, a, d));
    assertThat(dynamicGraphService.distance(graphWrapper, b, d), is((long) (weightBA + weightAD)));
  }

  @Test
  public void testThreeVertexCycle() {
    MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final String d = "d";
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);
    final long weightAB = 1;
    final long weightBC = 2;
    final long weightCA = 3;
    final long weightCD = 1;
    final long weightDA = 1;
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("", graph);
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(a, singletonMap(c, (int) weightCA),
            singletonMap(b, (int) weightAB)));
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(b, Collections.emptyMap(),
            singletonMap(c, (int) weightBC)));
    // check a -> b
    assertThat(dynamicGraphService.path(graphWrapper, a, b).getVertexChain(), contains(a, b));
    assertThat(dynamicGraphService.distance(graphWrapper, a, b), is(weightAB));
    // check b -> c
    assertThat(dynamicGraphService.path(graphWrapper, b, c).getVertexChain(), contains(b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, b, c), is(weightBC));
    // check c -> a
    assertThat(dynamicGraphService.path(graphWrapper, c, a).getVertexChain(), contains(c, a));
    assertThat(dynamicGraphService.distance(graphWrapper, c, a), is(weightCA));
    // check a -> b -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is(weightAB + weightBC));
    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(d, singletonMap(c, (int) weightCD),
            singletonMap(a, (int) weightDA)));
    // check c -> a
    assertThat(dynamicGraphService.path(graphWrapper, c, a).getVertexChain(), contains(c, d, a));
    assertThat(dynamicGraphService.distance(graphWrapper, c, a), is(weightCD + weightDA));
  }

  @Test
  public void testMultiPathOnThreeVertex() {
    MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    final int weightAB = 1;
    final int weightBC = 2;
    final int weightCA = 3;
    final int weightBA = 4;
    final int weightAC = 5;
    final int weightCB = 6;

    final Map<String, Integer> incomingOfA = new HashMap<>(2);
    incomingOfA.put(c, weightCA);
    incomingOfA.put(b, weightBA);
    final Map<String, Integer> outgoingOfA = new HashMap<>(2);
    outgoingOfA.put(b, weightAB);
    outgoingOfA.put(c, weightAC);
    dynamicGraphService.update(graphWrapper, new WeightUpdating<>(a, incomingOfA, outgoingOfA));

    dynamicGraphService.update(graphWrapper,
        new WeightUpdating<>(b, singletonMap(c, weightCB),
            singletonMap(c, weightBC)));
    // check a -> b -> c
    assertThat(dynamicGraphService.path(graphWrapper, a, c).getVertexChain(), contains(a, b, c));
    assertThat(dynamicGraphService.distance(graphWrapper, a, c), is((long) (weightAB + weightBC)));
  }

  @Test
  public void testBadGraphOnThreeVertexes() {
    MutableValueGraph<String, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(3)
        .build();
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("", graph);
    final String a = "a";
    final String b = "b";
    final String c = "c";
    final String d = "d";
    graph.addNode(a);
    graph.addNode(b);
    graph.addNode(c);
    graph.addNode(d);

    final int weightAB = 2;
    final int weightAD = 2;
    final int weightBA = 6;
    final int weightCA = 4;
    final int weightDA = 4;
    final int weightDC = 2;

    final Map<String, Integer> incomingOfA = new HashMap<>(3);
    incomingOfA.put(b, weightBA);
    incomingOfA.put(c, weightCA);
    incomingOfA.put(d, weightDA);
    final Map<String, Integer> outgoingOfA = new HashMap<>(2);
    outgoingOfA.put(b, weightAB);
    outgoingOfA.put(d, weightAD);
    dynamicGraphService.update(graphWrapper, new WeightUpdating<>(a, incomingOfA, outgoingOfA));

    dynamicGraphService
        .update(graphWrapper, new WeightUpdating<>(c, singletonMap(d, weightDC), emptyMap()));
    // check b -> a -> d
    assertThat(dynamicGraphService.path(graphWrapper, b, d).getVertexChain(), contains(b, a, d));
    assertThat(dynamicGraphService.distance(graphWrapper, b, d), is((long) (weightBA + weightAD)));
  }
}