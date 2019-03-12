package com.igoryan.model;

import com.google.common.graph.EndpointPair;
import java.util.Iterator;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class Path<N> {

  private final EndpointPair<N> fistAndLast;
  private final List<N> vertexChain;
  private long weight;

  public boolean contains(EndpointPair<N> edge) {
    final Iterator<N> vertexIterator = vertexChain.iterator();
    while (vertexIterator.hasNext()) {
      if (vertexIterator.next().equals(edge.source()) && (vertexIterator.hasNext() && vertexIterator
          .next().equals(edge.target()))) {
        return true;
      }
    }
    return false;
  }
}
