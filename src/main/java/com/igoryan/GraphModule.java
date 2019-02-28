package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerRelaxationService;
import com.igoryan.services.impl.DejkstraAllPairsShortestPathServiceImpl;

public class GraphModule<N extends IntegerBaseNode> extends AbstractModule {

  @Override
  protected void configure() {
    bind(new TypeLiteral<IntegerRelaxationService<N>>() {})
        .toInstance(new IntegerRelaxationService<N>() {});
    bind(new TypeLiteral<IntegerDejkstraAllPairsShortestPathService<N>>() {})
        .to(new TypeLiteral<DejkstraAllPairsShortestPathServiceImpl<N>>() {});
  }
}
