package com.igoryan;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.igoryan.model.DejkstraNode;
import com.igoryan.services.ExaminationService;
import com.igoryan.services.IntegerDejkstraAllPairsShortestPathService;
import com.igoryan.services.IntegerDynamicGraphService;
import com.igoryan.services.ReportService;
import com.igoryan.services.impl.DejkstraAllPairsShortestPathServiceImpl;
import com.igoryan.services.impl.ExaminationServiceImpl;
import com.igoryan.services.impl.IntegerDynamicAlgorithmHelper;
import com.igoryan.services.impl.IntegerDynamicGraphServiceImpl;
import com.igoryan.services.impl.ReportServiceImpl;

public class GraphModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(new TypeLiteral<IntegerDynamicAlgorithmHelper<String>>() {
    })
        .toInstance(new IntegerDynamicAlgorithmHelper<>());
    bind(new TypeLiteral<IntegerDynamicGraphService<String>>() {
    })
        .to(new TypeLiteral<IntegerDynamicGraphServiceImpl<String>>() {
        });
    bind(new TypeLiteral<IntegerDejkstraAllPairsShortestPathService<DejkstraNode>>() {
    })
        .to(new TypeLiteral<DejkstraAllPairsShortestPathServiceImpl<DejkstraNode>>() {
        });
    bind(ExaminationService.class).to(ExaminationServiceImpl.class);
    bind(ReportService.class).to(ReportServiceImpl.class);
  }
}
