package com.igoryan.model;

import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import lombok.Data;

@Data
public class VertexPair {

  private Node src;
  private Node dst;
}
