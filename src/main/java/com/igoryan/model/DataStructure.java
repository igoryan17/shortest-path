package com.igoryan.model;

import static com.google.common.collect.Sets.newHashSet;

import com.google.common.graph.EndpointPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "key")
public class DataStructure<N extends IntegerBaseNode> {

  private final EndpointPair<N> key;
  private Set<List<N>> locallyShortestPath = newHashSet();
  private List<N> shortestPath;
  private Map<EndpointPair<N>, List<N>> leftExtensionOfLocallyShortestPaths = new HashMap<>();
  private Map<EndpointPair<N>, List<N>> leftExtensionOfShortestPaths = new HashMap<>();
  private Map<EndpointPair<N>, List<N>> rightExtensionOfLocallyShortestPaths = new HashMap<>();
  private Map<EndpointPair<N>, List<N>> rightExtensionOfShortestPaths = new HashMap<>();
}
