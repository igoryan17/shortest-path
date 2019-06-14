package com.igoryan.services;

import com.igoryan.model.Algorithm;
import com.igoryan.model.Topology;
import java.io.IOException;

public interface ExaminationService {

  void examine(int vertexCount, int edgeCount);

  void examine(Topology topology, int edgeCount);

  void examine(int vertexCount, double probability, Algorithm algorithm, final int attemptingCount)
      throws IOException;
}
