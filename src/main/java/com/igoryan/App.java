package com.igoryan;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.igoryan.model.Algorithm;
import com.igoryan.services.ExaminationService;
import java.io.IOException;

public class App {

  public static void main(String[] args) throws IOException {
    int vertexCount = Integer.valueOf(args[0]);
    int repeatCount = Integer.valueOf(args[1]);
    double probability = Double.valueOf(args[2]);
    Algorithm algorithm = Algorithm.valueOf(args[3]);
    final Injector injector = Guice.createInjector(new GraphModule());
    final ExaminationService examinationService = injector.getInstance(ExaminationService.class);
    examinationService.examine(vertexCount, probability, algorithm, repeatCount);
  }
}
