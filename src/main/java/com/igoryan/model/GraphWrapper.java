package com.igoryan.model;

import com.google.common.graph.MutableValueGraph;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "id")
public class GraphWrapper<N extends IntegerBaseNode> {

  private final String id;
  private final MutableValueGraph<N, Integer> graph;
}
