package com.igoryan.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class BaseIntegerNode implements Comparable<BaseIntegerNode> {

  protected long shortestPathEstimate;
  protected BaseIntegerNode predecessor;

  @Override
  public int compareTo(@NonNull final BaseIntegerNode o) {
    return Math.toIntExact(shortestPathEstimate - o.shortestPathEstimate);
  }
}