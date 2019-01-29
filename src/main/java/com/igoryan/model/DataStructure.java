package com.igoryan.model;

import it.uniroma1.dis.wsngroup.gexf4j.core.Node;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "vertexes")
public class DataStructure {

  private VertexPair vertexes;
  private List<Node> leftLocalShortestPathExtension;
  private List<Node> leftShortestPathExtension;
  private List<Node> rightLocalShortestPathExtension;
  private List<Node> rightShortestPathExtension;
}
