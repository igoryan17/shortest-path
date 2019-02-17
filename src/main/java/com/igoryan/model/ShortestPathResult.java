package com.igoryan.model;

import java.util.List;
import lombok.Data;

@Data
public class ShortestPathResult<N> {

  private final List<N> shortestPath;
  private final int weight;
}
