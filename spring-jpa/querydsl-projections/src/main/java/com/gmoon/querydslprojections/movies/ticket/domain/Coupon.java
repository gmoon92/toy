package com.gmoon.querydslprojections.movies.ticket.domain;

import java.io.Serializable;
import java.time.Instant;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Coupon implements Serializable {

	@Id
	@UuidGenerator
	@Column(length = 50)
	private String id;

	private Instant expireAt;
}
