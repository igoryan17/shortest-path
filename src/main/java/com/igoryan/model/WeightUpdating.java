package com.igoryan.model;

import java.util.Map;
import lombok.Data;

@Data
public class WeightUpdating<N extends IntegerBaseNode> {

  private final N node;
  private final Map<N, Integer> incomingNodeToNewWeight;
  private final Map<N, Integer> outgoingNodeToNewWeight;
}
