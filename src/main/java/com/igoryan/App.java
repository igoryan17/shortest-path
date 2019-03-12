package com.igoryan;

import com.google.common.base.Stopwatch;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.igoryan.model.DejkstraNode;
import com.igoryan.model.GraphWrapper;
import com.igoryan.model.Path;
import com.igoryan.model.WeightUpdating;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import com.igoryan.util.GraphGenerator;
import java.util.Collections;

public class App {
  public static void main(String[] args) {
    final Injector injector = Guice.createInjector(new GraphModule());
    final IntegerDynamicGraphService<String> dynamicGraphService = injector
        .getInstance(Key.get(new TypeLiteral<IntegerDynamicGraphService<String>>() {
        }));
    final IntegerDejkstraAllPairsShortestPathService<DejkstraNode> allPairsShortestPathService =
        injector.getInstance(Key.get(
            new TypeLiteral<IntegerDejkstraAllPairsShortestPathService<DejkstraNode>>() {
            }));
    final MutableValueGraph<DejkstraNode, Integer> dejkstraGraph = GraphGenerator.generate(5, 10);
    final MutableValueGraph<String, Integer> graph = GraphGenerator.fromGraph(dejkstraGraph);
    final GraphWrapper<String> graphWrapper = new GraphWrapper<>("", graph);
    final Stopwatch stopwatchDynamicGraph = Stopwatch.createStarted();
    dynamicGraphService.init(graphWrapper);
    stopwatchDynamicGraph.stop();
    final Stopwatch stopwatchDejkstra = Stopwatch.createStarted();
    allPairsShortestPathService.calculate(dejkstraGraph);
    stopwatchDejkstra.stop();
    System.out.println("init time: " + stopwatchDynamicGraph);
    System.out.println("init time: " + stopwatchDejkstra);
    EndpointPair<DejkstraNode> edge = dejkstraGraph.edges().iterator().next();
    dejkstraGraph.putEdgeValue(edge.source(), edge.target(), 2);
    graph.putEdgeValue(edge.source().getName(), edge.target().getName(), 2);
    final Stopwatch updateDynamic = Stopwatch.createStarted();
    dynamicGraphService.update(graphWrapper, new WeightUpdating<>(edge.target().getName(),
        Collections.singletonMap(edge.source().getName(), 2), Collections.emptyMap()));
    updateDynamic.stop();
    final Stopwatch updateAll = Stopwatch.createStarted();
    allPairsShortestPathService.calculate(dejkstraGraph);
    updateAll.stop();
    System.out.println("update time: " + updateDynamic);
    System.out.println("update time: " + updateAll);
    Path<String> shortestPath = dynamicGraphService.path(graphWrapper, "1", "3");
    System.out.println(shortestPath);
    System.out.println(GraphGenerator.directedStringToGraphVizRepresentation(graph, shortestPath));
  }
}
