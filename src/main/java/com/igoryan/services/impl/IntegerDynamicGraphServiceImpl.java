package com.igoryan.services.impl;

import static java.lang.Integer.MAX_VALUE;

import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.DataStructure;
import com.igoryan.model.GraphWrapper;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.Path;
import com.igoryan.model.WeightUpdating;
import com.igoryan.services.IntegerDynamicGraphService;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class IntegerDynamicGraphServiceImpl<N extends IntegerBaseNode>
    implements IntegerDynamicGraphService<N> {

  private final Map<GraphWrapper<N>, DataStructure<N>>
      graphToDataStructure = new HashMap<>();
  private final IntegerDynamicAlgorithmHelper<N> dynamicAlgorithmHelper;

  public IntegerDynamicGraphServiceImpl(
      final IntegerDynamicAlgorithmHelper<N> dynamicAlgorithmHelper) {
    this.dynamicAlgorithmHelper = dynamicAlgorithmHelper;
  }

  @Override
  public void update(final GraphWrapper<N> graphWrapper,
      final WeightUpdating<N> weightUpdating) {
    final DataStructure<N> dataStructure =
        graphToDataStructure.computeIfAbsent(graphWrapper, k -> new DataStructure<>());
    MutableValueGraph<N, Integer> graph = graphWrapper.getGraph();
    cleanUp(graph, dataStructure, weightUpdating.getNode());
    fixUp(graph, dataStructure, weightUpdating);
  }

  @Override
  public long distance(final GraphWrapper<N> graphWrapper, @NonNull final N src,
      @NonNull final N dst) {
    final MutableValueGraph<N, Integer> graph = graphWrapper.getGraph();
    final DataStructure<N> dataStructure = graphToDataStructure.get(graphWrapper);
    final Optional<Path<N>> path =
        dataStructure.getShortestPath()
            .get(dynamicAlgorithmHelper.vertexPair(src, dst, graph)).stream()
            .min(Comparator.comparing(Path::getWeight));
    return path.map(Path::getWeight).orElse(Long.MAX_VALUE);
  }

  @Override
  public List<N> path(final GraphWrapper<N> graphWrapper, @NonNull final N src,
      @NonNull final N dst) {
    return graphToDataStructure.get(graphWrapper).getShortestPath()
        .getOrDefault(dynamicAlgorithmHelper.vertexPair(src, dst, graphWrapper.getGraph()),
            Collections.emptySet())
        .stream()
        .min(Comparator.comparing(Path::getWeight))
        .map(Path::getVertexChain)
        .orElse(null);
  }

  private void cleanUp(@NonNull ValueGraph<N, Integer> graph,
      @NonNull DataStructure<N> dataStructure, @NonNull N node) {
    log.info("start cleanup; node: {}", node);
    final Queue<Path<N>> queue = new LinkedList<>();
    queue.add(new Path<>(dynamicAlgorithmHelper.vertexPair(node, node, graph),
        Lists.newArrayList(node)));
    while (!queue.isEmpty()) {
      final Path<N> path = queue.poll();
      Stream.concat(dataStructure.getLeftExtensionOfLocallyShortestPaths()
              .getOrDefault(path, Collections.emptySet()).stream(),
          dataStructure.getRightExtensionOfLocallyShortestPaths()
              .getOrDefault(path, Collections.emptySet()).stream())
          .forEach(extendedPath -> {
            queue.add(extendedPath);
            dataStructure.getLocallyShortestPath()
                .getOrDefault(extendedPath.getFistAndLast(), Collections.emptySet())
                .removeIf(local -> local.equals(extendedPath));
            final Path<N> rightSubPath = dynamicAlgorithmHelper.rightSubPath(graph, extendedPath);
            dataStructure.getLeftExtensionOfLocallyShortestPaths()
                .getOrDefault(rightSubPath, Collections.emptySet())
                .removeIf(left -> left.equals(extendedPath));
            final Path<N> leftSubPath = dynamicAlgorithmHelper.leftSubPath(graph, extendedPath);
            dataStructure.getRightExtensionOfLocallyShortestPaths()
                .getOrDefault(leftSubPath, Collections.emptySet())
                .removeIf(right -> right.equals(extendedPath));
            if (extendedPath
                .equals(dataStructure.getShortestPath().get(extendedPath.getFistAndLast()))) {
              dataStructure.getShortestPath().remove(extendedPath.getFistAndLast());
              dataStructure.getLeftExtensionOfShortestPaths()
                  .getOrDefault(rightSubPath, Collections.emptySet())
                  .removeIf(left -> left.equals(extendedPath));
              dataStructure.getRightExtensionOfShortestPaths()
                  .getOrDefault(leftSubPath, Collections.emptySet())
                  .removeIf(right -> right.equals(extendedPath));
            }
          });
    }
  }

  private void fixUp(@NonNull MutableValueGraph<N, Integer> graph,
      final DataStructure<N> dataStructure,
      @NonNull WeightUpdating<N> weightUpdating) {
    final Comparator<Path<N>> pathComparator = (path, other) ->
        (int) (dynamicAlgorithmHelper.weight(graph, path)
            - dynamicAlgorithmHelper.weight(graph, other));
    phase1(graph, weightUpdating, dataStructure);
    final PriorityQueue<Path<N>> queue =
        new PriorityQueue<>(graph.nodes().size() * graph.nodes().size(), pathComparator);
    final Map<EndpointPair<N>, Boolean> firstExtracted = new HashMap<>();
    phase2(graph, dataStructure, pathComparator, queue, firstExtracted);
    phase3(graph, dataStructure, queue, firstExtracted);
  }

  private void phase3(final @NonNull MutableValueGraph<N, Integer> graph,
      final DataStructure<N> dataStructure, final PriorityQueue<Path<N>> queue,
      final Map<EndpointPair<N>, Boolean> firstExtracted) {
    while (!queue.isEmpty()) {
      final Path<N> path = queue.poll();
      if (firstExtracted.getOrDefault(path.getFistAndLast(), Boolean.TRUE)) {
        firstExtracted.put(path.getFistAndLast(), Boolean.FALSE);
        final Path<N> rightSubPath = dynamicAlgorithmHelper.rightSubPath(graph, path);
        final Path<N> leftSubPath = dynamicAlgorithmHelper.leftSubPath(graph, path);
        if (!dataStructure.getShortestPath()
            .getOrDefault(path.getFistAndLast(), Collections.emptySet()).contains(path)) {
          dataStructure.getShortestPath()
              .computeIfAbsent(path.getFistAndLast(), k -> new HashSet<>())
              .add(path);
          dataStructure.getLeftExtensionOfShortestPaths()
              .computeIfAbsent(rightSubPath, k -> new HashSet<>())
              .add(path);
          dataStructure.getRightExtensionOfShortestPaths()
              .computeIfAbsent(leftSubPath, k -> new HashSet<>())
              .add(path);
        }
        dataStructure.getLeftExtensionOfShortestPaths()
            .getOrDefault(leftSubPath, Collections.emptySet())
            .forEach(extension -> {
              log.debug("left extension shortest path: {}", extension);
              Path<N> leftAdded = dynamicAlgorithmHelper
                  .addAsFirst(graph, extension.getFistAndLast().source(), path);
              log.debug("extend to left path; toLeftExtended: {}", leftAdded);
              long recalculatedWeightOfLeftAdded = graph
                  .edgeValueOrDefault(leftAdded.getFistAndLast().source(),
                      path.getFistAndLast().source(), MAX_VALUE) + path.getWeight();
              log.debug("relaxation; weight: {}, path: {}", recalculatedWeightOfLeftAdded, path);
              graph.putEdgeValue(leftAdded.getFistAndLast().source(),
                  extension.getFistAndLast().target(), (int) recalculatedWeightOfLeftAdded);
              leftAdded.setWeight(recalculatedWeightOfLeftAdded);
              Path<N> leftSubPathOfLeftAdded =
                  dynamicAlgorithmHelper.leftSubPath(graph, leftAdded);
              Path<N> rightSubPathOfLeftAdded =
                  dynamicAlgorithmHelper.rightSubPath(graph, leftAdded);
              dataStructure.getLocallyShortestPath()
                  .computeIfAbsent(leftAdded.getFistAndLast(), k -> new HashSet<>())
                  .add(leftAdded);
              dataStructure.getLeftExtensionOfLocallyShortestPaths()
                  .computeIfAbsent(rightSubPathOfLeftAdded, k -> new HashSet<>())
                  .add(leftAdded);
              dataStructure.getRightExtensionOfLocallyShortestPaths()
                  .computeIfAbsent(leftSubPathOfLeftAdded, k -> new HashSet<>())
                  .add(leftAdded);
              queue.add(leftAdded);
            });
        dataStructure.getRightExtensionOfShortestPaths()
            .getOrDefault(rightSubPath, Collections.emptySet())
            .forEach(extension -> {
              log.debug("right extension of shortest path: {}", extension);
              Path<N> rightAdded = dynamicAlgorithmHelper
                  .addAsLast(graph, extension.getFistAndLast().target(), path);
              log.debug("extend to right path; toRightExtended: {}", rightAdded);
              long recalculatedWeightOfRightAdded = path.getWeight() + graph
                  .edgeValueOrDefault(path.getFistAndLast().target(),
                      extension.getFistAndLast().target(), MAX_VALUE);
              log.debug("relaxation; weight: {}, path: {}", recalculatedWeightOfRightAdded,
                  rightAdded);
              rightAdded.setWeight(recalculatedWeightOfRightAdded);
              graph.putEdgeValue(rightAdded.getFistAndLast().source(),
                  rightAdded.getFistAndLast().target(), (int) recalculatedWeightOfRightAdded);
              Path<N> leftSubPathOfRightAdded =
                  dynamicAlgorithmHelper.leftSubPath(graph, rightAdded);
              Path<N> rightSubPathOfRightAdded =
                  dynamicAlgorithmHelper.rightSubPath(graph, rightAdded);
              dataStructure.getLocallyShortestPath()
                  .computeIfAbsent(rightAdded.getFistAndLast(), k -> new HashSet<>())
                  .add(rightAdded);
              dataStructure.getLeftExtensionOfLocallyShortestPaths()
                  .computeIfAbsent(rightSubPathOfRightAdded, k -> new HashSet<>())
                  .add(rightAdded);
              dataStructure.getRightExtensionOfLocallyShortestPaths()
                  .computeIfAbsent(leftSubPathOfRightAdded, k -> new HashSet<>())
                  .add(rightAdded);
              queue.add(rightAdded);
            });
      }
    }
  }

  private void phase2(final @NonNull MutableValueGraph<N, Integer> graph,
      final DataStructure<N> dataStructure, final Comparator<Path<N>> pathComparator,
      final PriorityQueue<Path<N>> queue, final Map<EndpointPair<N>, Boolean> firstExtracted) {
    for (N x : graph.nodes()) {
      for (N y : graph.nodes()) {
        if (x.equals(y)) {
          continue;
        }
        final EndpointPair<N> xy = dynamicAlgorithmHelper.vertexPair(x, y, graph);
        dataStructure.getLocallyShortestPath()
            .getOrDefault(xy, Collections.emptySet())
            .stream()
            .min(pathComparator)
            .ifPresent(path -> {
              queue.add(path);
              firstExtracted.put(path.getFistAndLast(), Boolean.TRUE);
            });
        if (graph.isDirected()) {
          final EndpointPair<N> yx = dynamicAlgorithmHelper.vertexPair(y, x, graph);
          dataStructure.getLocallyShortestPath()
              .getOrDefault(yx, Collections.emptySet())
              .stream()
              .min(pathComparator)
              .ifPresent(path -> {
                queue.add(path);
                firstExtracted.put(path.getFistAndLast(), Boolean.TRUE);
              });
        }
      }
    }
  }

  private void phase1(final @NonNull MutableValueGraph<N, Integer> graph,
      final @NonNull WeightUpdating<N> weightUpdating, final DataStructure<N> dataStructure) {
    final N v = weightUpdating.getNode();
    weightUpdating.getIncomingNodeToNewWeight().forEach((u, weight) -> {
      graph.putEdgeValue(u, v, weight);
    });
    weightUpdating.getOutgoingNodeToNewWeight().forEach((u, weight) -> {
      graph.putEdgeValue(v, u, weight);
    });
    final EndpointPair<N> vv = dynamicAlgorithmHelper.vertexPair(v, v, graph);
    Path<N> pathVV = new Path<>(vv, Collections.singletonList(v));
    graph.predecessors(v).forEach(u -> {
      final Integer weightUV = weightUpdating.getIncomingNodeToNewWeight().get(u);
      if (graph.edgeValueOrDefault(u, v, MAX_VALUE) < MAX_VALUE) {
        final EndpointPair<N> uv = dynamicAlgorithmHelper.vertexPair(u, v, graph);
        Path<N> pathUV = new Path<>(uv, Arrays.asList(u, v), weightUV);
        dataStructure.getLocallyShortestPath()
            .computeIfAbsent(uv, k -> new HashSet<>())
            .add(pathUV);
        dataStructure.getLeftExtensionOfShortestPaths()
            .computeIfAbsent(pathVV, k -> new HashSet<>())
            .add(pathUV);
        final EndpointPair<N> uu = dynamicAlgorithmHelper.vertexPair(u, u, graph);
        Path<N> pathUU = new Path<>(uu, Collections.singletonList(u), 0);
        dataStructure.getRightExtensionOfLocallyShortestPaths()
            .computeIfAbsent(pathUU, k -> new HashSet<>())
            .add(pathUV);
      }
    });
    graph.successors(v).forEach(u -> {
      final Integer weightVU = weightUpdating.getOutgoingNodeToNewWeight().get(u);
      if (graph.edgeValueOrDefault(v, u, MAX_VALUE) < MAX_VALUE) {
        final EndpointPair<N> vu = dynamicAlgorithmHelper.vertexPair(v, u, graph);
        Path<N> pathVU = new Path<>(vu, Arrays.asList(v, u), weightVU);
        dataStructure.getLocallyShortestPath()
            .computeIfAbsent(vu, k -> new HashSet<>())
            .add(pathVU);
        final EndpointPair<N> uu = dynamicAlgorithmHelper.vertexPair(u, u, graph);
        Path<N> pathUU = new Path<>(uu, Collections.singletonList(u));
        dataStructure.getLeftExtensionOfShortestPaths()
            .computeIfAbsent(pathUU, k -> new HashSet<>())
            .add(pathVU);
        dataStructure.getRightExtensionOfLocallyShortestPaths()
            .computeIfAbsent(pathVV, k -> new HashSet<>())
            .add(pathVU);
      }
    });
  }
}
