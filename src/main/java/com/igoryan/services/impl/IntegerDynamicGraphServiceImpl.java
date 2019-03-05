package com.igoryan.services.impl;

import static java.lang.Integer.MAX_VALUE;

import com.google.common.collect.Lists;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.igoryan.model.DataStructure;
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
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Stream;
import lombok.NonNull;

public class IntegerDynamicGraphServiceImpl<N extends IntegerBaseNode>
    implements IntegerDynamicGraphService<N> {

  private final Map<ValueGraph<N, Integer>, DataStructure<N>>
      graphToDataStructure = new HashMap<>();
  private final IntegerDynamicAlgorithmHelper<N> dynamicAlgorithmHelper;

  public IntegerDynamicGraphServiceImpl(
      final IntegerDynamicAlgorithmHelper<N> dynamicAlgorithmHelper) {
    this.dynamicAlgorithmHelper = dynamicAlgorithmHelper;
  }

  @Override
  public void update(final MutableValueGraph<N, Integer> graph,
      final WeightUpdating<N> weightUpdating) {
    cleanUp(graph, weightUpdating.getNode());
    fixUp(graph, weightUpdating);
  }

  @Override
  public long distance(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N src,
      @NonNull final N dst) {
    final DataStructure<N> dataStructure = graphToDataStructure.get(graph);
    final Path<N> path =
        dataStructure.getShortestPath().get(dynamicAlgorithmHelper.vertexPair(src, dst, graph));
    if (path == null) {
      return Long.MAX_VALUE;
    } else {
      return dynamicAlgorithmHelper.weight(graph, path);
    }
  }

  @Override
  public List<N> path(@NonNull final ValueGraph<N, Integer> graph, @NonNull final N src,
      @NonNull final N dst) {
    return null;
  }

  private void cleanUp(@NonNull ValueGraph<N, Integer> graph, @NonNull N node) {
    final DataStructure<N> dataStructure = graphToDataStructure.get(graph);
    final Queue<Path<N>> queue = new LinkedList<>();
    queue.add(new Path<>(dynamicAlgorithmHelper.vertexPair(node, node, graph),
        Lists.newArrayList(node)));
    while (!queue.isEmpty()) {
      final Path<N> path = queue.poll();
      Stream.concat(dataStructure.getLeftExtensionOfLocallyShortestPaths().get(path).stream(),
          dataStructure.getRightExtensionOfLocallyShortestPaths().get(path).stream())
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
      @NonNull WeightUpdating<N> weightUpdating) {
    final DataStructure<N> dataStructure = graphToDataStructure.get(graph);
    final Comparator<Path<N>> pathComparator = (path, other) ->
        (int) (dynamicAlgorithmHelper.weight(graph, path)
            - dynamicAlgorithmHelper.weight(graph, other));
    phase1(graph, weightUpdating, dataStructure);
    final PriorityQueue<Path<N>> queue =
        new PriorityQueue<>(graph.nodes().size() * graph.nodes().size(), pathComparator);
    final Map<Path<N>, Boolean> firstExtracted = new HashMap<>();
    phase2(graph, dataStructure, pathComparator, queue, firstExtracted);
    phase3(graph, dataStructure, queue, firstExtracted);
  }

  private void phase3(final @NonNull MutableValueGraph<N, Integer> graph,
      final DataStructure<N> dataStructure, final PriorityQueue<Path<N>> queue,
      final Map<Path<N>, Boolean> firstExtracted) {
    while (!queue.isEmpty()) {
      final Path<N> path = queue.poll();
      if (firstExtracted.get(path)) {
        firstExtracted.put(path, Boolean.FALSE);
        if (dataStructure.getShortestPath()
            .getOrDefault(path.getFistAndLast(), Collections.emptySet()).contains(path)) {
          dataStructure.getShortestPath()
              .computeIfAbsent(path.getFistAndLast(), k -> new HashSet<>())
              .add(path);
          Path<N> rightSubPath = dynamicAlgorithmHelper.rightSubPath(graph, path);
          dataStructure.getLeftExtensionOfShortestPaths()
              .computeIfAbsent(rightSubPath, k -> new HashSet<>())
              .add(path);
          Path<N> leftSubPath = dynamicAlgorithmHelper.leftSubPath(graph, path);
          dataStructure.getRightExtensionOfShortestPaths()
              .computeIfAbsent(leftSubPath, k -> new HashSet<>())
              .add(path);
          dataStructure.getLeftExtensionOfShortestPaths()
              .getOrDefault(leftSubPath, Collections.emptySet())
              .forEach(extension -> {
                Path<N> leftAdded = dynamicAlgorithmHelper
                    .addAsFirst(graph, extension.getFistAndLast().source(), path);
                int recalculatedWeightOfLeftAdded = graph
                    .edgeValueOrDefault(leftAdded.getFistAndLast().source(),
                        extension.getFistAndLast().source(), MAX_VALUE) + path.getWeight();
                graph.putEdgeValue(leftAdded.getFistAndLast().source(),
                    extension.getFistAndLast().target(), recalculatedWeightOfLeftAdded);
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
                queue.offer(leftAdded);
              });
          dataStructure.getRightExtensionOfShortestPaths()
              .getOrDefault(rightSubPath, Collections.emptySet())
              .forEach(extension -> {
                Path<N> rightAdded = dynamicAlgorithmHelper
                    .addAsLast(graph, extension.getFistAndLast().target(), path);
                int recalculatedWeightOfRightAdded = path.getWeight() + graph
                    .edgeValueOrDefault(path.getFistAndLast().target(),
                        extension.getFistAndLast().target(), MAX_VALUE);
                rightAdded.setWeight(recalculatedWeightOfRightAdded);
                graph.putEdgeValue(rightAdded.getFistAndLast().source(),
                    rightAdded.getFistAndLast().target(), recalculatedWeightOfRightAdded);
                Path<N> leftSubPathOfRightAdded =
                    dynamicAlgorithmHelper.leftSubPath(graph, rightAdded);
                Path<N> rightSubPathOfRightAdded =
                    dynamicAlgorithmHelper.rightSubPath(graph, rightAdded);
                dataStructure.getLocallyShortestPath()
                    .computeIfAbsent(rightAdded.getFistAndLast(), k -> new HashSet<>())
                    .add(rightAdded);
                dataStructure.getLeftExtensionOfLocallyShortestPaths()
                    .computeIfAbsent(rightSubPath, k -> new HashSet<>())
                    .add(rightAdded);
                dataStructure.getRightExtensionOfLocallyShortestPaths()
                    .computeIfAbsent(leftSubPath, k -> new HashSet<>())
                    .add(rightAdded);
                queue.offer(rightAdded);
              });
        }
      }
    }
  }

  private void phase2(final @NonNull MutableValueGraph<N, Integer> graph,
      final DataStructure<N> dataStructure, final Comparator<Path<N>> pathComparator,
      final PriorityQueue<Path<N>> queue, final Map<Path<N>, Boolean> firstExtracted) {
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
              firstExtracted.put(path, Boolean.TRUE);
            });
        if (graph.isDirected()) {
          final EndpointPair<N> yx = dynamicAlgorithmHelper.vertexPair(y, x, graph);
          dataStructure.getLocallyShortestPath()
              .getOrDefault(yx, Collections.emptySet())
              .stream()
              .min(pathComparator)
              .ifPresent(path -> {
                queue.add(path);
                firstExtracted.put(path, Boolean.TRUE);
              });
        }
      }
    }
  }

  private void phase1(final @NonNull MutableValueGraph<N, Integer> graph,
      final @NonNull WeightUpdating<N> weightUpdating, final DataStructure<N> dataStructure) {
    final N v = weightUpdating.getNode();
    final EndpointPair<N> vv = dynamicAlgorithmHelper.vertexPair(v, v, graph);
    Path<N> pathVV = new Path<>(vv, Collections.singletonList(v));
    graph.predecessors(v).forEach(u -> {
      final Integer weightUV = weightUpdating.getIncomingNodeToNewWeight().get(u);
      graph.putEdgeValue(u, v, weightUV);
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
        Path<N> pathUU = new Path<>(uu, Collections.singletonList(u));
        dataStructure.getRightExtensionOfLocallyShortestPaths()
            .computeIfAbsent(pathUU, k -> new HashSet<>())
            .add(pathUV);
      }
    });
    graph.successors(v).forEach(u -> {
      final Integer weightVU = weightUpdating.getOutgoingNodeToNewWeight().get(u);
      graph.putEdgeValue(v, u, weightVU);
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
