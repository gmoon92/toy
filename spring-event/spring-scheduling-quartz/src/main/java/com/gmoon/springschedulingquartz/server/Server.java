package com.gmoon.springschedulingquartz.server;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "lt_server",
        indexes = { @Index(name = "idx_type_host", columnList = "host, type") },
        uniqueConstraints = { @UniqueConstraint(name = "uc_type_host", columnNames = { "type", "host" }) })
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

  @ColumnDefault("80")
  @Column(nullable = false)
  private int port1;

  @ColumnDefault("443")
  @Column(nullable = false)
  private int port2;

  @ColumnDefault("8443")
  @Column(nullable = false)
  private int port3;

  @ColumnDefault("0")
  @Column(nullable = false)
  private boolean enabled;

}
