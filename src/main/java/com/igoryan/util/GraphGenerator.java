package com.igoryan.util;

import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.igoryan.model.DejkstraNode;
import com.igoryan.model.Path;
import com.igoryan.model.Topology;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  public static MutableValueGraph<DejkstraNode, Integer> generate(int vertexCount,
      double probability, int maxWeight) {
    final Random random = new Random();
    final MutableValueGraph<DejkstraNode, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(vertexCount)
        .build();
    final List<DejkstraNode> nodes = new ArrayList<>(vertexCount);
    for (int i = 0; i < vertexCount; i++) {
      final DejkstraNode node = new DejkstraNode(Integer.toString(i));
      nodes.add(node);
      graph.addNode(node);
    }
    for (int i = 0; i < vertexCount; i++) {
      for (int j = 0; j < vertexCount; j++) {
        if (i == j) {
          continue;
        }
        if (random.nextDouble() < probability) {
          graph.putEdgeValue(nodes.get(i), nodes.get(j), random.nextInt(maxWeight));
        }
      }
    }
    return graph;
  }

  public static MutableValueGraph<DejkstraNode, Integer> generate(Topology topology,
      int vertexCount) {
    if (topology == Topology.STAR) {
      return generateStar(vertexCount);
    }
    return null;
  }

  public static MutableValueGraph<DejkstraNode, Integer> generateStar(int vertexCount) {
    final MutableValueGraph<DejkstraNode, Integer> graph = ValueGraphBuilder.directed()
        .expectedNodeCount(vertexCount + 2)
        .build();
    final DejkstraNode gw1 = new DejkstraNode("gw1");
    final DejkstraNode gw2 = new DejkstraNode("gw2");
    graph.addNode(gw1);
    graph.addNode(gw2);
    graph.putEdgeValue(gw1, gw2, 1);
    graph.putEdgeValue(gw2, gw1, 1);
    for (int i = 0; i < vertexCount; i++) {
      final DejkstraNode cpe = new DejkstraNode("cpe" + i);
      graph.addNode(cpe);
      graph.putEdgeValue(gw1, cpe, i + 1);
      graph.putEdgeValue(cpe, gw1, i + 2);
      graph.putEdgeValue(gw2, cpe, i + 3);
      graph.putEdgeValue(cpe, gw2, i + 4);
    }
    return graph;
  }

  public static MutableValueGraph<DejkstraNode, Integer> generate(String fileName, int coefficient)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      final int vertex = Integer.valueOf(reader.readLine());
      final int edges = Integer.valueOf(reader.readLine());
      System.out.println("vertex=" + vertex + " edges=" + edges);
      final Map<String, DejkstraNode> nodeNumberToNode = new HashMap<>();
      final MutableValueGraph<DejkstraNode, Integer> graph = ValueGraphBuilder.directed()
          .expectedNodeCount(vertex)
          .build();
      reader.lines().forEach(line -> {
        final String[] parameters = line.split(" ");
        DejkstraNode src = nodeNumberToNode.computeIfAbsent(parameters[0], DejkstraNode::new);
        DejkstraNode dst = nodeNumberToNode.computeIfAbsent(parameters[1], DejkstraNode::new);
        Integer weight = Math.round(Float.parseFloat(parameters[2]) * coefficient);
        graph.putEdgeValue(src, dst, weight);
      });
      return graph;
    }
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
