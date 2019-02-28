package com.igoryan.model;

import com.google.common.graph.EndpointPair;
import java.util.List;
import lombok.Data;

@Data
public class Path<N extends IntegerBaseNode> {

  private final EndpointPair<N> fistAndLast;
  private final List<N> vertexChain;
}
