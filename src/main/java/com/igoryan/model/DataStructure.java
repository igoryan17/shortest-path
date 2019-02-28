package com.igoryan.model;

import com.google.common.graph.EndpointPair;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataStructure<N extends IntegerBaseNode> {

  private Map<EndpointPair<N>, Set<Path<N>>> locallyShortestPath = new HashMap<>();
  private Map<EndpointPair<N>, Path<N>> shortestPath = new HashMap<>(); // unique shortest paths
  private Map<Path<N>, Set<Path<N>>> leftExtensionOfLocallyShortestPaths = new HashMap<>();
  private Map<Path<N>, Set<Path<N>>> leftExtensionOfShortestPaths = new HashMap<>();
  private Map<Path<N>, Set<Path<N>>> rightExtensionOfLocallyShortestPaths = new HashMap<>();
  private Map<Path<N>, Set<Path<N>>> rightExtensionOfShortestPaths = new HashMap<>();
}
