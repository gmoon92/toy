package com.gmoon.javacore.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DateUtilsTest {

	private final ZoneId korea = ZoneId.of("Asia/Seoul"); // UTC +9
	private final ZoneId america = ZoneId.of("America/Anchorage"); // UTC -8

	@BeforeEach
	void setUp() {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		assertThat(ZoneId.systemDefault()).isEqualTo(ZoneId.of("UTC"));
	}

	@AfterEach
	void tearDown() {
		assertThat(ZoneId.systemDefault()).isEqualTo(ZoneId.of("UTC"));
	}

	@Test
	void truncateAndAdjust() {
		String input = "2025-06-04 00:00:00";
		verify(input, dt -> DateUtils.truncateAndAdjust(dt, korea), "2025-06-03 15:00:00");
		verify(input, dt -> DateUtils.truncateAndAdjust(dt, america), "2025-06-04 08:00:00");

		String input2 = "2025-06-04 00:00:01";
		verify(input2, dt -> DateUtils.truncateAndAdjust(dt, korea), "2025-06-03 15:00:00");
		verify(input2, dt -> DateUtils.truncateAndAdjust(dt, america), "2025-06-04 08:00:00");

		String input3 = "2025-06-04 23:59:59";
		verify(input3, dt -> DateUtils.truncateAndAdjust(dt, korea), "2025-06-03 15:00:00");
		verify(input3, dt -> DateUtils.truncateAndAdjust(dt, america), "2025-06-04 08:00:00");

		String input4 = "2025-06-05 00:00:00";
		verify(input4, dt -> DateUtils.truncateAndAdjust(dt, korea), "2025-06-04 15:00:00");
		verify(input4, dt -> DateUtils.truncateAndAdjust(dt, america), "2025-06-05 08:00:00");
	}

	private void verify(String inputStr, Function<Date, Date> function, String expected) {
		final String pattern = "yyyy-MM-dd HH:mm:ss";

		var input = DateUtils.toDate(pattern, inputStr);
		var result = function.apply(input);
		log.info("input: {} truncate: {}, result: {}", inputStr, toString(result), result);
		assertThat(result.toInstant()).isEqualTo(DateUtils.toDate(pattern, expected).toInstant());
	}

	@Test
	void truncateAndAdjustEndDt() {
		String input = "2025-06-04 00:00:00";
		verify(input, dt -> DateUtils.truncateAndAdjustEndDt(dt, korea), "2025-06-04 14:59:59");
		verify(input, dt -> DateUtils.truncateAndAdjustEndDt(dt, america), "2025-06-05 07:59:59");

		String input2 = "2025-06-04 00:00:01";
		verify(input2, dt -> DateUtils.truncateAndAdjustEndDt(dt, korea), "2025-06-04 14:59:59");
		verify(input2, dt -> DateUtils.truncateAndAdjustEndDt(dt, america), "2025-06-05 07:59:59");

		String input3 = "2025-06-04 23:59:59";
		verify(input3, dt -> DateUtils.truncateAndAdjustEndDt(dt, korea), "2025-06-04 14:59:59");
		verify(input3, dt -> DateUtils.truncateAndAdjustEndDt(dt, america), "2025-06-05 07:59:59");

		String input4 = "2025-06-05 00:00:00";
		verify(input4, dt -> DateUtils.truncateAndAdjustEndDt(dt, korea), "2025-06-05 14:59:59");
		verify(input4, dt -> DateUtils.truncateAndAdjustEndDt(dt, america), "2025-06-06 07:59:59");
	}

	/**
	 * 벽시계(wall-clock) 시간 + 수동 offset 보정 방식 -> UTC 산출
	 *
	 * <pre>
	 * ## 상황
	 * - 프론트에서 "2025-06-04 00:00:00" (로컬 현지 자정/벽시계시간) 문자만 서버로 전달
	 * - 서버에서는 타임존 지정 없이 UTC 기준 Date 생성
	 *
	 * ## 이슈
	 * - UTC+권역(한국/일본 등)은 "내가 입력한 6/4 자정"이 거의 그대로 맞음.
	 * - 하지만 UTC-권역(미국/서유럽 등)은 "내가 입력한 벽시계자정"과 실제 현지 날짜가 어긋나서 하루(혹은 더) 차이 발생!
	 *
	 * <b>핵심: UTC로 저장된 Date를 그냥 오프셋 계산하면 현지 날짜 계산이 틀림.</b>
	 *
	 * ## 수동 offset 방식 코드 시나리오
	 * 입력: "2025-06-04 00:00:00" (벽시계/타임존 없음)
	 *
	 * Case 1. Asia/Seoul (UTC+9)
	 * <code>
	 * toLocalDateTime(date) -> 2025-06-04T00:00:00
	 *   .plusHours(9)       -> 2025-06-04T09:00:00
	 *   .truncatedTo        -> 2025-06-04T00:00:00
	 *   .minusHours(9)      -> 2025-06-03T15:00:00 (OK)
	 * </code>
	 *
	 * Case 2. America/Anchorage (UTC-8)
	 * <code>
	 * toLocalDateTime(date) -> 2025-06-04T00:00:00
	 *   .plusHours(-8)      -> 2025-06-03T16:00:00
	 *   .truncatedTo        -> 2025-06-03T00:00:00
	 *   .minusHours(-8)     -> 2025-06-03T08:00:00
	 *  틀림! 실제  06-<b>04</b> 08:00:00 UTC 2025
	 * </code>
	 *
	 * ## 정리/권고
	 * - 이 패턴은 UTC+권역엔 (한국/일본/동남아)엔 대체로 맞으나, UTC-권역, DST 등 환경에서는 의도한 현지 날짜와 어긋남
	 * - 글로벌 서비스라면 반드시 LocalDateTime.atZone(ZoneId).toInstant() (ZonedDateTime 활용) 패턴을 사용할 것!
	 *
	 *
	 * ## 관련 용어
	 * - wall time(벽시계 시간): 타임존 없는 현지 시간 (LocalDateTime)
	 *    -> 실제 방에서 보는 그 나라/도시의 시계 시간(타임존 정보 없음)
	 * - Date/Instant: 전세계 UTC 기준 절대 시점
	 * - ZonedDateTime: wall time + region(ZoneId) 현지 자정의 정확한 UTC를 보장
	 * - DST(Daylight Saving Time, 썸머타임)
	 *    여름철(3~11월 등)에 낮 시간을 더 길게 쓰기 위해 시계를 1시간 앞당기는 제도.
	 *    미국·유럽·호주·일본 일부 등에서 실제 운영하며,
	 *    날짜/타임존 계산에는 항상 DST 변수도 함께 반영해야 함!
	 *    ex) 미국 뉴욕은 3~11월 시계가 1시간 빠름 (DST off시 다시 표준시로 복귀, 즉 윈터타임)
	 * </pre>
	 */
	private Date truncateAndAdjust(Date date, ZoneId zoneId) {
		long offset = getOffset(zoneId).toHours();
		LocalDateTime startOfDay = DateUtils.toLocalDateTime(date)
			 .plusHours(offset)
			 .truncatedTo(ChronoUnit.DAYS)
			 .minusHours(offset);
		return DateUtils.toDate(startOfDay);
	}

	public Duration getOffset(ZoneId target) {
		Instant instant = new Date().toInstant();
		LocalDateTime systemTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		LocalDateTime targetTime = LocalDateTime.ofInstant(instant, target);
		return Duration.between(systemTime, targetTime);
	}

	private LocalDateTime toString(Date dt1) {
		return DateUtils.toLocalDateTime(dt1);
	}
}
