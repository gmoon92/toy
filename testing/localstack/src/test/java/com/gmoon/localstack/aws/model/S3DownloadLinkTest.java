package com.gmoon.localstack.aws.model;

import static org.assertj.core.api.Assertions.assertThat;
import com.gmoon.javacore.util.StringUtils;
import com.gmoon.localstack.aws.constants.AmazonRegion;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@Slf4j
class S3DownloadLinkTest {

	@DisplayName("URL 경로에 더블 슬래시를 포함하지 않는다.")
	@Test
	void downloadLink() {
		// given
		S3DownloadLink link = S3DownloadLink.builder()
			.bucketName("gmoon-bucket")
			.region(AmazonRegion.AF_SOUTH_1)
			.userResourceUri("//resources/public/")
			.fileName("/moon.jpg")
			.build();

		// when then
		String downloadLink = link.getValue();
		log.info("downloadLink: {}", downloadLink);
		assertThat(StringUtils.replace(downloadLink, "https://", ""))
			.doesNotContain("//");
	}
}
