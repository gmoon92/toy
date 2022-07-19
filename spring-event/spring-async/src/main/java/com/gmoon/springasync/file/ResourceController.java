package com.gmoon.springasync.file;

import java.io.File;
import java.io.FileInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user-resource")
public class ResourceController {

	private static final MultiValueMap<String, String> IMAGE_CONTENT_TYPE;

	static {
		IMAGE_CONTENT_TYPE = new HttpHeaders();
		IMAGE_CONTENT_TYPE.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_GIF_VALUE);
		IMAGE_CONTENT_TYPE.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE);
		IMAGE_CONTENT_TYPE.add(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_PNG_VALUE);
	}

	@ResponseBody
	@GetMapping("/image")
	public ResponseEntity<byte[]> getImageFile() throws Exception {
		File resourceFile = FileUtils.getFileFromResource("public/images/cat.png");
		FileInputStream fis = new FileInputStream(resourceFile);
		return new ResponseEntity<>(IOUtils.toByteArray(fis), IMAGE_CONTENT_TYPE, HttpStatus.CREATED);
	}
}
