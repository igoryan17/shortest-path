package com.igoryan.services;

import static java.lang.Integer.toUnsignedLong;

import com.igoryan.model.BaseIntegerNode;

public interface IntegerRelaxationService<N extends BaseIntegerNode> {

  default void relax(N u, N v, int weight) {
    if (v.getShortestPathEstimate() > (u.getShortestPathEstimate() + weight)) {
      v.setShortestPathEstimate(u.getShortestPathEstimate() + weight);
      v.setPredecessor(u);
    }
  }
}
