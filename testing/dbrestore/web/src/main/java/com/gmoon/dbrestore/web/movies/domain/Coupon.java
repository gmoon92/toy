package com.gmoon.dbrestore.web.movies.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serializable;

@Entity
@Table(name = "tb_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(of = "id")
@ToString(of = "id")
public class Coupon implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "movie_id", referencedColumnName = "id")
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Movie movie;

	@Column(name = "used", nullable = false)
	private boolean used;

	@Builder
	public Coupon(Long id, Movie movie, boolean used) {
		this.id = id;
		this.movie = movie;
		this.used = used;
	}

	public void using() {
		used = true;
	}
}
