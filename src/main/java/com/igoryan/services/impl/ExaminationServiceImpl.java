package com.igoryan.services.impl;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import com.google.common.base.Stopwatch;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.inject.Inject;
import com.igoryan.model.Algorithm;
import com.igoryan.model.DejkstraNode;
import com.igoryan.model.GraphWrapper;
import com.igoryan.model.Topology;
import com.igoryan.model.WeightUpdating;
import com.igoryan.services.ExaminationService;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import com.igoryan.services.ReportService;
import com.igoryan.util.GraphGenerator;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ExaminationServiceImpl implements ExaminationService {

  private static final Random RANDOM = new Random();
  private final IntegerDynamicGraphService<String> dynamicGraphService;
  private final IntegerDejkstraAllPairsShortestPathService<DejkstraNode>
      allPairsShortestPathService;
  private final ReportService reportService;

  @Inject
  public ExaminationServiceImpl(
      final IntegerDynamicGraphService<String> dynamicGraphService,
      final IntegerDejkstraAllPairsShortestPathService<DejkstraNode> allPairsShortestPathService,
      final ReportService reportService) {
    this.dynamicGraphService = dynamicGraphService;
    this.allPairsShortestPathService = allPairsShortestPathService;
    this.reportService = reportService;
  }

  @Override
  public void examine(final int vertexCount, final int edgeCount) {

  }

  @Override
  public void examine(final Topology topology, final int edgeCount) {

  }

  @Override
  public void examine(final int vertexCount, final double probability, Algorithm algorithm,
      final int attemptingCount)
      throws IOException {
    final MutableValueGraph<DejkstraNode, Integer> dejkstraGraph =
        GraphGenerator.generate(vertexCount, probability, (int) (vertexCount * probability + 1));
    if (algorithm == Algorithm.DYNAMIC) {
      final MutableValueGraph<String, Integer> graph = GraphGenerator.fromGraph(dejkstraGraph);
      final GraphWrapper<String> graphWrapper = new GraphWrapper<>("", graph);
      dynamicGraphService.init(graphWrapper);
      for (int i = 0; i < attemptingCount; i++) {
        final Set<EndpointPair<String>> edges = graph.edges();
        final int edgeNumber = RANDOM.nextInt(edges.size());
        final Iterator<EndpointPair<String>> edgeIterator = edges.iterator();
        for (int edgeNum = 0; edgeNum < edgeNumber; edgeNum++) {
          edgeIterator.next();
        }
        final EndpointPair<String> edge = edgeIterator.next();
        int newWeight = RANDOM.nextInt(edges.size());
        graph.putEdgeValue(edge, newWeight);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        dynamicGraphService.update(graphWrapper,
            new WeightUpdating<>(edge.target(), singletonMap(edge.source(), newWeight),
                emptyMap()));
        stopwatch.stop();
        if (i > 2) {
          reportService
              .report(vertexCount, probability, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                  algorithm);
        }
      }
    } else if (algorithm == Algorithm.STATIC) {
      for (int i = 0; i < attemptingCount; i++) {
        final Set<EndpointPair<DejkstraNode>> edges = dejkstraGraph.edges();
        final int edgeNumber = RANDOM.nextInt(edges.size());
        final Iterator<EndpointPair<DejkstraNode>> edgeIterator = edges.iterator();
        for (int edgeNum = 0; edgeNum < edgeNumber; edgeNum++) {
          edgeIterator.next();
        }
        final EndpointPair<DejkstraNode> edge = edgeIterator.next();
        final int newWeight = RANDOM.nextInt(edges.size());
        dejkstraGraph.putEdgeValue(edge, newWeight);
        final Stopwatch stopwatch = Stopwatch.createStarted();
        allPairsShortestPathService.calculate(dejkstraGraph);
        stopwatch.stop();
        if (i > 2) {
          reportService
              .report(vertexCount, probability, stopwatch.elapsed(TimeUnit.MILLISECONDS),
                  algorithm);
        }
      }
    }
  }
}
