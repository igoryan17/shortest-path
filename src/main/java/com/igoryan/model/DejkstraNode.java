package com.igoryan.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(of = "name")
public class DejkstraNode extends IntegerBaseNode {

  private final String name;
}
