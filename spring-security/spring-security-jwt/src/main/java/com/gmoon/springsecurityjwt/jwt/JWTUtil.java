package com.gmoon.springsecurityjwt.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.gmoon.springsecurityjwt.jwt.exception.InvalidAuthTokenException;
import com.gmoon.springsecurityjwt.jwt.exception.NotFoundAuthenticationSchemaException;
import com.gmoon.springsecurityjwt.user.Role;
import com.gmoon.springsecurityjwt.user.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class JWTUtil {
  private static final Pattern PATTERN_OF_TOKEN = Pattern.compile("^(\\w+)(\\s)(\\w+)");
  private static final int DAY_OF_EXPIRATION = 1;
  private static final int SECONDS_OF_TOLERANCE_RANGE = 30;

  private final String apiVersion;
  private final Algorithm algorithm;
  private final JWTVerifier verifier;

  public JWTUtil(@Value("${jwt.secret}") String secret,
                 @Value("${api.version:v1}") String apiVersion) {
    this.apiVersion = apiVersion;
    this.algorithm = Algorithm.HMAC256(secret);
    this.verifier = JWT.require(algorithm)
            .withIssuer(apiVersion)
            .acceptLeeway(SECONDS_OF_TOLERANCE_RANGE)
            .acceptExpiresAt(Duration.ofDays(DAY_OF_EXPIRATION).getSeconds())
            .build();
  }

  public String generate(User user) {
    try {
      ZonedDateTime today = ZonedDateTime.now();
      String token = JWT.create()
              .withIssuer(apiVersion)
              .withClaim("username", user.getUsername())
              .withClaim("role", user.getRole().name())
              .withIssuedAt(Date.from(today.toInstant()))
              .withExpiresAt(Date.from(today.plusDays(DAY_OF_EXPIRATION).toInstant()))
              .sign(algorithm);
      return String.format("%s %s", AuthenticationSchema.BEARER.getName(), token);
    } catch (JWTCreationException e) {
      throw new JWTCreationException("Invalid Signing configuration or Couldn't convert Claims.", e);
    }
  }

  public User decode(String tokenOfIncludeSchema) {
    String token = obtainTokenWithoutSchema(tokenOfIncludeSchema);

    verify(token);

    DecodedJWT jwt = JWT.decode(token);
    String username = jwt.getClaim("username").asString();
    Role role = Role.valueOf(jwt.getClaim("role").asString());
    return User.create(username, "", role);
  }

  private String obtainTokenWithoutSchema(String tokenOfIncludeSchema) {
    Matcher matcher = PATTERN_OF_TOKEN.matcher(tokenOfIncludeSchema);
    if (matcher.find()) {
      String schema = matcher.group(1);
      AuthenticationSchema.checkValidSchema(schema);
      return StringUtils.trim(StringUtils.removeStart(tokenOfIncludeSchema, schema));
    }
    throw new NotFoundAuthenticationSchemaException(tokenOfIncludeSchema);
  }

  private void verify(String token) {
    try {
      verifier.verify(token);
    } catch (JWTVerificationException e) {
      throw new InvalidAuthTokenException(token, e);
    }
  }
}
