package com.gmoon.spreadsheet.utils;

import com.gmoon.spreadsheet.config.GoogleSpreadSheetProperties;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import java.io.FileOutputStream;

@Slf4j
@SpringBootTest(classes = { GoogleSpreadSheetToResources.class
        , GoogleSpreadSheetToXmlDataSet.class
        , GoogleSpreadSheetProperties.class })
class GoogleApiServiceTest {

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


  @Test
  void export() throws Exception{
    String fileId = "1JU6YH1OfbZqT06uZVRlXECD3E-Rs95Pls-cKWSt5ysg";
    Drive.Files files = GoogleApiService.getDriveService().files();
    File file = files.get(fileId).execute();
    String fileName = file.getName();
    log.debug("file name : {}", fileName);

    files.export(fileId, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .executeMediaAndDownloadTo(new FileOutputStream(String.format("src/test/resources/google/%s.xlsx", fileName)));

  }
}