package com.igoryan.services;

import com.igoryan.model.BaseIntegerNode;

public interface IntegerRelaxationService<N extends BaseIntegerNode> {

  default void relax(N u, N v, int weight) {
    if (v.getShortestPathEstimate() > (u.getShortestPathEstimate() + weight)) {
      v.setShortestPathEstimate(u.getShortestPathEstimate() + weight);
      v.setPredecessor(u);
    }
  }
}
