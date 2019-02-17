package com.igoryan.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class BaseIntegerNode implements Comparable<BaseIntegerNode> {

  private int shortestPathEstimate;
  private BaseIntegerNode predecessor;

  @Override
  public int compareTo(@NonNull final BaseIntegerNode o) {
    return shortestPathEstimate - o.shortestPathEstimate;
  }
}
