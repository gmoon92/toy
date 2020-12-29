package com.gmoon.spreadsheet.utils;

import com.gmoon.spreadsheet.config.GoogleSpreadSheetProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(classes = { GoogleSpreadSheetToResources.class, GoogleSpreadSheetProperties.class })
class GoogleSpreadSheetToResourcesTest {

    @Test
    void run() {
        GoogleSpreadSheetToResources.generate("src/main/resources/message");
    }
}