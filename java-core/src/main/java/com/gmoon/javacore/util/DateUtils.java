package com.gmoon.javacore.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

	/**
	 * 벽시계(wall-clock) 시간(Date/LocalDateTime)과 타임존을 조합,
	 * 현지 자정(0시)을 UTC로 변환하는 안전 패턴.
	 *
	 * @param wallTimeDate 타임존 없는 입력(예: "2025-06-04 00:00:00"를 UTC Date로 저장한 것. 항상 벽시계)
	 * @param userZoneId   실제 처리할 타임존(예: "Asia/Seoul", "America/Anchorage" 등)
	 * @return 해당 타임존 현지에서의 0시(자정)가 UTC 시각일 때의 Date 인스턴스
	 * @apiNote <ul>
	 * <li>프론트 "벽시계시간" 문자열 입력이 Date(UTC)로 변환되어 서버로 들어오는 구조에 적합.</li>
	 * <li>입력케이스:
	 * <pre>
	 *          "2025-06-04 00:00:00" + Asia/Seoul      → 2025-06-03T15:00:00Z
	 *          "2025-06-04 00:00:00" + Anchorage(US)   → 2025-06-04T08:00:00Z
	 *          </pre>
	 * </li>
	 * <li>LocalDateTime.atZone(userZone).toInstant() 패턴이 DST·글로벌 환경에서도 100% 안전</li>
	 * </ul>
	 * @implNote 이 메소드들은 wallTimeDate 파싱시 서버 JVM timezone이 UTC로 고정되어 있다는 전제하에 동작함.
	 */
	public static Date truncateAndAdjust(Date wallTimeDate, ZoneId userZoneId) {
		Instant instant = wallTimeDate.toInstant();
		LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		ZonedDateTime zonedDateTime = truncateAndAdjust(localDateTime, userZoneId);
		return Date.from(zonedDateTime.toInstant());
	}

	private static ZonedDateTime truncateAndAdjust(LocalDateTime ldt, ZoneId zoneId) {
		return ldt.atZone(zoneId)
			 .truncatedTo(ChronoUnit.DAYS);
	}

	public static Date truncateAndAdjustEndDt(Date localDate, ZoneId zoneId) {
		Date adjust = truncateAndAdjust(localDate, zoneId);
		Date date = plus(adjust, ChronoUnit.DAYS, 1);
		return plus(date, ChronoUnit.SECONDS, -1);
	}

	public static Date plus(Date date, ChronoUnit unit, int amount) {
		if (date == null) {
			return null;
		}
		LocalDateTime ldt = toLocalDateTime(date)
			 .plus(amount, unit);
		return toDate(ldt);
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	public static Date toDate(LocalDateTime ldt) {
		ZonedDateTime zonedDateTime = ldt.atZone(ZoneId.systemDefault());
		return Date.from(zonedDateTime.toInstant());
	}

	public static Date toDate(String pattern, String dateString) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(pattern);
			return sdf.parse(dateString);
		} catch (Exception ex) {
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

}
