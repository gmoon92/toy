package com.gmoon.querydslprojections.movies.movie.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FilmRatings {
	G("General Audience"),
	PG("Parental Guidance suggested"),
	PG_13("Parents strongly cautioned"),
	R("Restricted"),
	NC_17("No one 17 and Under Admitted");

	private final String description;
}
