package com.gmoon.springsecurity.login;

import com.gmoon.springsecurity.form.SampleService;
import com.gmoon.springsecurity.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Callable;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SampleController {

  private final SampleService sampleService;

  @GetMapping("/async-handler")
  @ResponseBody
  public Callable<String> asyncHandler() {
    SecurityUtils.logging("MVC");

    return () -> {
      SecurityUtils.logging("Callable");
      return "Async Handler";
    };
  }

  @GetMapping("/async-service")
  @ResponseBody
  public String asyncService() {
    SecurityUtils.logging("MVC before async service");
    sampleService.async();
    SecurityUtils.logging("MVC after async service");
    return "Async Service";
  }
}
