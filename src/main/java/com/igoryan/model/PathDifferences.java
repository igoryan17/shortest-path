package com.igoryan.model;

import java.util.List;
import lombok.Value;

@Value
public class PathDifferences {

  private final List<String> dynamicPath;
  private final long dynamicWeight;
  private final List<String> dejkstraPath;
  private final long dejkstraWeight;

  @Override
  public String toString() {
    return "dynamic path: " + dynamicPath + " with weight:" + dynamicWeight + " vs dejkstra path:"
        + dejkstraPath + " with weight:" + dejkstraWeight;
  }
}
