package com.igoryan.services.impl;

import com.google.inject.Inject;
import com.igoryan.model.DataStructure;
import com.igoryan.model.VertexPair;
import com.igoryan.services.GraphService;
import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import java.util.List;
import java.util.Map;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;

public class GraphServiceImpl implements GraphService {

  private final Cache<Graph, Map<VertexPair, DataStructure>> cache;

  @Inject
  public GraphServiceImpl(EmbeddedCacheManager cacheManager) {
    this.cache = cacheManager.getCache();
  }

  public void update(final Edge edge, final int weight) {

  }

  public int distance(final Node x, final Node y) {
    return 0;
  }

  public List<Node> path(final Node x, final Node y) {
    return null;
  }
}
