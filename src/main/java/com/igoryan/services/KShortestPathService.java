package com.igoryan.services;

import com.google.common.graph.Network;
import com.igoryan.model.EdgeWithWeight;
import com.igoryan.model.Path;
import java.util.List;

public interface KShortestPathService<N> {

  List<Path<N>> calculate(Network<N, ? extends EdgeWithWeight> graph, N src, N dst, final int pathCount);
}
