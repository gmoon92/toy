package com.gmoon.spreadsheet.utils;

import com.gmoon.spreadsheet.config.GoogleSpreadSheetProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestConstructor;

@Slf4j
@SpringBootTest(classes = { GoogleSpreadSheetToResources.class
        , GoogleSpreadSheetProperties.class })
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@RequiredArgsConstructor
class GoogleSpreadSheetToResourcesTest {

    @Test
    void run() {
        GoogleSpreadSheetToResources.generate("src/main/resources/message");
    }
}