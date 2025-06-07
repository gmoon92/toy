package com.gmoon.springwebconverter.model;

import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.gmoon.springwebconverter.config.annotation.LocalEndDate;
import com.gmoon.springwebconverter.config.annotation.LocalStartDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@ToString
public class BoardRequest {

	private Search search = new Search();

	@NoArgsConstructor(access = AccessLevel.PROTECTED)
	@AllArgsConstructor
	@Getter
	@ToString
	public static class Search {
		private String keyword;

		@LocalStartDate
		private Date startDate;

		@LocalEndDate
		private Date endDate;
	}
}
