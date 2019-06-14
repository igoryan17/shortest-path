package com.igoryan.services;

import com.igoryan.model.Algorithm;
import java.io.IOException;

public interface ReportService {

  void report(int vertexCount, double probability, long time, Algorithm algorithm)
      throws IOException;
}
