package com.igoryan.util;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.DejkstraNode;
import com.igoryan.model.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class GraphGenerator {

  private GraphGenerator() {
  }

  public static MutableValueGraph<DejkstraNode, Integer> generate(int vertexCount, int edgeCount) {
    final Random random = new Random();
    final MutableValueGraph<DejkstraNode, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(vertexCount)
        .build();
    final List<DejkstraNode> nodes = new ArrayList<>();
    for (int i = 0; i < vertexCount; i++) {
      final DejkstraNode node = new DejkstraNode(Integer.toString(i));
      nodes.add(node);
      graph.addNode(node);
    }
    int currentEdgeCount = 0;
    while (currentEdgeCount < edgeCount) {
      final int indexU = random.nextInt(vertexCount);
      final int indexV = random.nextInt(vertexCount);
      final DejkstraNode u = nodes.get(indexU);
      final DejkstraNode v = nodes.get(indexV);
      if (!u.equals(v) && !graph.hasEdgeConnecting(u, v)) {
        currentEdgeCount++;
        graph.putEdgeValue(u, v, random.nextInt(edgeCount) + 1);
      }
    }
    return graph;
  }

  public static MutableValueGraph<String, Integer> fromGraph(
      ValueGraph<DejkstraNode, Integer> graph) {
    final MutableValueGraph<String, Integer> copy = ValueGraphBuilder.directed()
        .expectedNodeCount(graph.nodes().size())
        .build();
    graph.nodes().stream()
        .map(DejkstraNode::getName)
        .forEach(copy::addNode);
    graph.nodes().forEach(u -> {
      graph.nodes().forEach(v -> {
        if (!u.equals(v) && graph.hasEdgeConnecting(u, v)) {
          copy.putEdgeValue(u.getName(), v.getName(),
              graph.edgeValueOrDefault(u, v, Integer.MAX_VALUE));
        }
      });
    });
    return copy;
  }

  public static String directedStringToGraphVizRepresentation(ValueGraph<String, Integer> graph) {
    StringBuilder result = new StringBuilder("digraph {\n");
    graph.edges().forEach(edge -> {
      int weight = graph.edgeValueOrDefault(edge.source(), edge.target(), Integer.MAX_VALUE);
      result.append('\t')
          .append(edge.source())
          .append(" -> ")
          .append(edge.target())
          .append("[label=\"")
          .append(weight)
          .append('"')
          .append(", weight=\"")
          .append(weight)
          .append("\"];\n");
    });
    result.append("}");
    return result.toString();
  }

  public static String directedDejkstraToGraphVizRepresentation(
      ValueGraph<DejkstraNode, Integer> graph) {
    return directedStringToGraphVizRepresentation(fromGraph(graph));
  }

  public static String directedStringToGraphVizRepresentation(ValueGraph<String, Integer> graph,
      Path<String> path) {
    StringBuilder result = new StringBuilder("digraph {\n");
    graph.edges().forEach(edge -> {
      int weight = graph.edgeValueOrDefault(edge.source(), edge.target(), Integer.MAX_VALUE);
      result.append('\t')
          .append(edge.source())
          .append(" -> ")
          .append(edge.target())
          .append("[label=\"")
          .append(weight)
          .append('"')
          .append(", weight=\"")
          .append(weight)
          .append('"');
      if (path.contains(edge)) {
        result.append(", color=red");
      }
      result.append("];\n");
    });
    result.append("}");
    return result.toString();
  }
}
