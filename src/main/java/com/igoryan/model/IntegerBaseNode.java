package com.igoryan.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class IntegerBaseNode implements Comparable<IntegerBaseNode> {

  protected long shortestPathEstimate;
  protected IntegerBaseNode predecessor;

  @Override
  public int compareTo(@NonNull final IntegerBaseNode o) {
    return Math.toIntExact(shortestPathEstimate - o.shortestPathEstimate);
  }
}