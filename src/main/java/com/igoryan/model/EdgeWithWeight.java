package com.igoryan.model;

import lombok.Value;

@Value
public class EdgeWithWeight<N> {

  private final N data;
  private final long weight;
}
