package com.igoryan;

import com.google.inject.AbstractModule;
import com.igoryan.services.GraphService;
import com.igoryan.services.impl.GraphServiceImpl;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;

public class GraphModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(GraphService.class).to(GraphServiceImpl.class);
    bind(EmbeddedCacheManager.class).to(DefaultCacheManager.class);
  }
}
