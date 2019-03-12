package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.model.DejkstraNode;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import com.igoryan.services.IntegerRelaxationService;
import com.igoryan.services.impl.DejkstraAllPairsShortestPathServiceImpl;
import com.igoryan.services.impl.IntegerDynamicAlgorithmHelper;
import com.igoryan.services.impl.IntegerDynamicGraphServiceImpl;

public class GraphModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(new TypeLiteral<IntegerDynamicAlgorithmHelper<String>>() {})
        .toInstance(new IntegerDynamicAlgorithmHelper<>());
    bind(new TypeLiteral<IntegerRelaxationService<DejkstraNode>>() {})
        .toInstance(new IntegerRelaxationService<DejkstraNode>() {});
    bind(new TypeLiteral<IntegerDynamicGraphService<String>>() {})
        .to(new TypeLiteral<IntegerDynamicGraphServiceImpl<String>>() {});
    bind(new TypeLiteral<IntegerDejkstraAllPairsShortestPathService<DejkstraNode>>() {})
        .to(new TypeLiteral<DejkstraAllPairsShortestPathServiceImpl<DejkstraNode>>() {});
  }
}
