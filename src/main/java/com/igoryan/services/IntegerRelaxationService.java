package com.igoryan.services;

import com.igoryan.model.IntegerBaseNode;

public interface IntegerRelaxationService<N extends IntegerBaseNode> {

  default void relax(N u, N v, int weight) {
    if (v.getShortestPathEstimate() > (u.getShortestPathEstimate() + weight)) {
      v.setShortestPathEstimate(u.getShortestPathEstimate() + weight);
      v.setPredecessor(u);
    }
  }
}
