package com.gmoon.javacore.util;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

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

	/**
	 * DST(Daylight Saving Time) 전환 시점의 LocalDateTime vs Instant 계산 차이
	 *
	 * <pre>
	 * ## 상황
	 * 2025-11-02 일요일 새벽 2시: EDT(UTC-4) → EST(UTC-5) 전환
	 * - 미국 뉴욕은 이 시점에 시계를 1시간 뒤로 되돌림
	 * - 오전 2:00 → 오전 1:00으로 회귀
	 * - 결과: 오전 1:37:57이 두 번 발생!
	 *
	 * ## 문제
	 * source: 2025-11-02 01:37:57 EDT (GMT-04:00) = GMT 05:37:57
	 * target: 2025-11-02 01:37:57 EST (GMT-05:00) = GMT 06:37:57
	 * -> 벽시계로는 같은 시간이지만, 실제로는 1시간(60분) 차이!
	 *
	 * ## LocalDateTime 계산의 문제점
	 * LocalDateTime.ofInstant()는 타임존 오프셋을 적용한 후 "벽시계 시간"만 비교
	 * - source → 2025-11-02T01:37:57
	 * - target → 2025-11-02T01:37:57
	 * - 결과: 0분 차이 (잘못된 계산!)
	 *
	 * ## Instant 계산의 정확성
	 * Instant는 UTC 절대 시간으로 계산
	 * - source → 2025-11-02T05:37:57Z
	 * - target → 2025-11-02T06:37:57Z
	 * - 결과: 60분 차이 (정확한 계산!)
	 *
	 * ## 실무 영향
	 * LocalDateTime 기반 시간 계산 사용 시:
	 * - 사용자 활동 로그 시간 중복/누락
	 * - 배치 작업 스케줄링 오류
	 * - 세션 시간, 경과 시간 계산 부정확
	 *
	 * ## 권고사항
	 * 타임존이 중요한 시스템에서는 반드시 Instant 또는 ZonedDateTime 사용!
	 * </pre>
	 */
	@Test
	void dstTransition_LocalDateTimeVsInstant_CalculationDifference() {
		// Given: DST 전환 시점 (EDT → EST)
		TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
		ZoneId newYork = ZoneId.systemDefault();

		// 2025-11-02 01:37:57 EDT (GMT-04:00)
		// GMT: 2025-11-02 05:37:57
		Date sourceEDT = new Date(1762061877000L);

		// 2025-11-02 01:37:57 EST (GMT-05:00) - 1시간 후, 하지만 벽시계는 같은 시간!
		// GMT: 2025-11-02 06:37:57
		Date targetEST = new Date(1762065477000L);

		// When: LocalDateTime 기반 시간 차이 계산 (벽시계 시간)
		long diffByLocalDateTime = ChronoUnit.MINUTES.between(
			 LocalDateTime.ofInstant(targetEST.toInstant(), newYork),
			 LocalDateTime.ofInstant(sourceEDT.toInstant(), newYork)
		);

		// When: Instant 기반 시간 차이 계산 (UTC 절대 시간)
		long diffByInstant = ChronoUnit.MINUTES.between(
			 targetEST.toInstant(),
			 sourceEDT.toInstant()
		);

		// Then: LocalDateTime은 DST를 고려하지 않아 0분 차이로 계산 (잘못됨)
		assertThat(diffByLocalDateTime)
			 .as("LocalDateTime은 벽시계 시간만 비교하여 DST 전환을 무시함")
			 .isZero();

		// Then: Instant는 UTC 기준으로 정확히 60분 차이 계산 (정확함)
		assertThat(diffByInstant)
			 .as("Instant는 UTC 절대 시간으로 계산하여 DST 전환을 정확히 반영함")
			 .isEqualTo(-60L);

		log.info("DST 전환 시점 시간 계산:");
		log.info("  source(EDT): {} = {}", sourceEDT, LocalDateTime.ofInstant(sourceEDT.toInstant(), newYork));
		log.info("  target(EST): {} = {}", targetEST, LocalDateTime.ofInstant(targetEST.toInstant(), newYork));
		log.info("  LocalDateTime 차이: {}분 (잘못된 계산)", diffByLocalDateTime);
		log.info("  Instant 차이: {}분 (정확한 계산)", diffByInstant);

		// Cleanup: 테스트 종료 후 UTC로 복원
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
}
