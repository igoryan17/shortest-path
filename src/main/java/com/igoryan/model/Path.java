package com.igoryan.model;

import com.google.common.graph.EndpointPair;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Path<N extends IntegerBaseNode> {

  private final EndpointPair<N> fistAndLast;
  private final List<N> vertexChain;
  private long weight;
}
