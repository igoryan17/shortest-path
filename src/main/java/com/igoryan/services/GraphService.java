package com.igoryan.services;

import it.uniroma1.dis.wsngroup.gexf4j.core.Edge;
import it.uniroma1.dis.wsngroup.gexf4j.core.Graph;
import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import java.util.List;

public interface GraphService {

  void add(Graph graph);

  void update(Edge edge, int weight);

  int distance(Node x, Node y);

  List<Node> path(Node x, Node y);
}
