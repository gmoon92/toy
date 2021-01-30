package com.gmoon.spreadsheet.utils;

import com.gmoon.spreadsheet.config.GoogleSpreadSheetProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

@Slf4j
@SpringBootTest(classes = { GoogleSpreadSheetToResources.class
        , GoogleSpreadSheetToXmlDataSet.class
        , GoogleSpreadSheetProperties.class })
class GoogleSpreadSheetToResourcesTest {

  @Test
  void generateToResources() {
    GoogleSpreadSheetToResources.generate("src/main/resources/message");
  }

  @Test
  void generateToXmlDataSet() throws Exception {
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    GoogleSpreadSheetToXmlDataSet.generate("sample-data", "default-data");
    stopWatch.stop();
    StopWatch.TaskInfo task = stopWatch.getLastTaskInfo();
    log.debug("{}", task.getTimeSeconds());
  }
}