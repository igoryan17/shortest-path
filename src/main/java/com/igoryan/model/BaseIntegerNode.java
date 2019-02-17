package com.igoryan.model;

import lombok.Data;
import lombok.NonNull;

@Data
public class BaseIntegerNode implements Comparable<BaseIntegerNode> {

  protected int shortestPathEstimate;
  protected BaseIntegerNode predecessor;

  @Override
  public int compareTo(@NonNull final BaseIntegerNode o) {
    return shortestPathEstimate - o.shortestPathEstimate;
  }
}