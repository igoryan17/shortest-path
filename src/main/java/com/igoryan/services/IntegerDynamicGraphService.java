package com.igoryan.services;

import com.igoryan.model.GraphWrapper;
import com.igoryan.model.Path;
import com.igoryan.model.WeightUpdating;
import lombok.NonNull;

public interface IntegerDynamicGraphService<N> {

  void update(GraphWrapper<N> graphWrapper, @NonNull WeightUpdating<N> weightUpdating);

  long distance(GraphWrapper<N> graphWrapper, @NonNull N src, @NonNull N dst);

  Path<N> path(GraphWrapper<N> graphWrapper, @NonNull N src, @NonNull N dst);

  void init(GraphWrapper<N> graphWrapper);
}
