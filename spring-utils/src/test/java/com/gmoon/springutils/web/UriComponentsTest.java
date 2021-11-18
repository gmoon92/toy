package com.gmoon.springutils.web;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UriComponentsTest {
  private static final Charset UTF_8 = StandardCharsets.UTF_8;

  @Test
  @DisplayName("https://www.baeldung.com/spring-uricomponentsbuilder")
  void uriComponentsBuilder() {
    // given
    String uri = "https://gmoon92.github.com";
    String token = UUID.randomUUID().toString();
    String locale = "ko";

    // when
    UriComponents uriComponents = UriComponentsBuilder.fromUriString(uri)
            .encode(UTF_8)
            .queryParam("locale", locale)
            .queryParam("_csrf", token)
            .build();

    // then
    assertThat(uriComponents.toString())
            .contains(uri, token, locale);
  }

  /**
   * @see UriComponentsBuilder#QUERY_PARAM_PATTERN
   * @see UriComponentsBuilder#queryParams
   * @see UriComponentsBuilder#query(String)
   * @see UriComponentsBuilder#queryParam(String, Object...)
   * @see UriComponentsBuilder#toUriString()
   * */
  @Test
  @DisplayName("UriComponents 는 내부적으로 Query String 을 ?, & 로 파싱한다." +
          "만약 Query String parameter 값에 또 다른 파라미터 값이 있을 경우 인코딩 하여 보낸다." +
          "결과적으로 decode 를 두번 해야 복호화가 된다..")
  void toUriString() throws Exception {
    // get
    String cloudGatewayUrl = "https://gateway.gmoon.com/route?" +
            "dispflg=0" +
            "&rout=";
    String routParam = "http://wwwkr1.gmoon.com/login?error=886&locale=ko";
    String logoutUrl = cloudGatewayUrl + encode(routParam);

    // when
    UriComponents uriComponents = getUriComponents(logoutUrl);
    String result = uriComponents.toString();

    // then
    assertThat(result).contains(encode(encode(routParam)));
    assertThat(decode(decode(result))).isEqualTo(cloudGatewayUrl + routParam);
  }

  private String encode(String str) throws Exception{
    return URLEncoder.encode(str, UTF_8.toString());
  }

  private String decode(String str) throws Exception{
    return URLDecoder.decode(str, UTF_8.toString());
  }

  private UriComponents getUriComponents(String uri) {
    return UriComponentsBuilder.fromUriString(uri)
            .encode(UTF_8)
            .build();
  }
}