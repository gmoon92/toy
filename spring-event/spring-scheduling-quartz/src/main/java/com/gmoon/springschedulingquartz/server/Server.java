package com.gmoon.springschedulingquartz.server;

import com.querydsl.core.annotations.QueryDelegate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static lombok.AccessLevel.PRIVATE;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Immutable
@Table(name = "nt_server",
        indexes = { @Index(name = "idx_type_host", columnList = "name, host, type") },
        uniqueConstraints = { @UniqueConstraint(name = "u_name_type_host", columnNames = { "name", "type", "host" }) })
@NoArgsConstructor(access = PROTECTED)
@ToString
@EqualsAndHashCode(of = { "name", "type", "host" })
public class Server {
  @Id
  @GeneratedValue
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ServerType type;

  @ColumnDefault("'server_gmoon'")
  @Column(nullable = false)
  private String name;

  @Column(name = "host", nullable = false)
  private String publicHost;

  @Column(nullable = false)
  private String privateHost;

  @ColumnDefault("443")
  @Column(nullable = false)
  private Integer port1;

  @ColumnDefault("80")
  @Column(nullable = false)
  private Integer port2;

  private Integer port3;

  @ColumnDefault("0")
  @Column(nullable = false)
  private boolean enabled;

  @Builder(access = PRIVATE)
  private Server(ServerType type, String name, String publicHost, String privateHost, Integer port1, Integer port2, Integer port3, boolean enabled) {
    this.type = type;
    this.name = name;
    this.publicHost = publicHost;
    this.privateHost = privateHost;
    this.port1 = port1;
    this.port2 = port2;
    this.port3 = port3;
    this.enabled = enabled;
  }

  public static Server createWebServer(String name) {
    return Server.builder()
            .name(name)
            .type(ServerType.WEB)
            .enabled(true)
            .build();
  }

  public void setPublicUrl(String publicHost, int port1) {
    this.publicHost = publicHost;
    this.port1 = port1;
  }

  public void setPrivateHost(String privateHost, int port2) {
    this.privateHost = privateHost;
    this.port2 = port2;
  }

  @QueryDelegate(Server.class)
  public static BooleanExpression isEnabled(QServer server) {
    return server.enabled.isTrue();
  }
}
