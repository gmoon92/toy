package com.gmoon.springwebconverter.api;

import java.util.Date;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.gmoon.springwebconverter.config.annotation.LocalStartDate;
import com.gmoon.springwebconverter.model.BoardRequest;

@Controller
@RequestMapping("/web")
public class WebController {

	@GetMapping("/converter/param")
	@ResponseBody
	public ResponseEntity<Date> genericConverterParam(@LocalStartDate Date wallTime) {
		return ResponseEntity.ok(wallTime);
	}

	@PostMapping("/converter/body")
	@ResponseBody
	public ResponseEntity<BoardRequest> genericConverterBody(@RequestBody BoardRequest request) {
		return ResponseEntity.ok(request);
	}

	@PostMapping("/converter/modelAndView")
	@ResponseBody
	public ResponseEntity<BoardRequest> genericConverterModelAndView(@ModelAttribute BoardRequest request) {
		return ResponseEntity.ok(request);
	}
}
