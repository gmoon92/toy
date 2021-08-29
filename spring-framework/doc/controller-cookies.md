# Controller에서 쿠키 제어하기

## 쿠키 생성 및 response set

```java
@Slf4j
@Controller
public class CookieController {

  @GetMapping("/create-cookie/{key}")
  public String createCookie(Model model, @PathVariable("key") String cookieKey, HttpServletResponse httpServletResponse) {
    // 1. 쿠키 생성
    Cookie newCookie = new Cookie(cookieKey, "data");
    
    // 2. 쿠키 유효기간 설정 (하루)
    int oneDayToMillionSeconds = 60 * 60 * 24;
    newCookie.setMaxAge(oneDayToMillionSeconds);
    
    // 3. HttpServletResponse 에 설정한 쿠키 set
    httpServletResponse.addCookie(newCookie);
    return "cookie-info";
  }
}
```


