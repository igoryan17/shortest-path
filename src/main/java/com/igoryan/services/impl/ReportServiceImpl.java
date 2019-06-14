package com.igoryan.services.impl;

import com.igoryan.model.Algorithm;
import com.igoryan.services.ReportService;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportServiceImpl implements ReportService {

  private static final String REPORT_DYNAMIC = "report_dynamic.csv";
  private static final String REPORT_STATIC = "report_static.csv";
  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd:MM HH:mm:ss");

  @Override
  public void report(final int vertexCount, final double probability, final long time, final
  Algorithm algorithm)
      throws IOException {
    if (algorithm == Algorithm.DYNAMIC) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_DYNAMIC, true))) {
        writer.write(
            vertexCount + "," + probability + "," + time + ",\"" + DATE_FORMAT.format(new Date())
                + "\"");
        writer.newLine();
      }
    } else if (algorithm == Algorithm.STATIC) {
      try (BufferedWriter writer = new BufferedWriter(new FileWriter(REPORT_STATIC, true))) {
        writer.write(vertexCount + "," + probability + "," + time + ",\"" + DATE_FORMAT
            .format(new Date()) + "\"");
        writer.newLine();
      }
    }
  }
}
