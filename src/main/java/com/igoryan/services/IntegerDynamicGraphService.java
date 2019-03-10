package com.igoryan.services;

import com.igoryan.model.GraphWrapper;
import com.igoryan.model.IntegerBaseNode;
import com.igoryan.model.WeightUpdating;
import java.util.List;
import lombok.NonNull;

public interface IntegerDynamicGraphService<N extends IntegerBaseNode> {

  void update(GraphWrapper<N> graphWrapper, @NonNull WeightUpdating<N> weightUpdating);

  long distance(GraphWrapper<N> graphWrapper, @NonNull N src, @NonNull N dst);

  List<N> path(GraphWrapper<N> graphWrapper, @NonNull N src, @NonNull N dst);
}
