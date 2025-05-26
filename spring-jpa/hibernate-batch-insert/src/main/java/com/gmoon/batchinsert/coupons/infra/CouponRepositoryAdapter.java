package com.gmoon.batchinsert.coupons.infra;

import com.gmoon.batchinsert.coupons.domain.Coupon;
import com.gmoon.batchinsert.coupons.domain.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {

	private final JpaCouponRepository jpaRepository;
	private final JdbcTemplate jdbcTemplate;

	@Override
	public void multiInsert(Collection<Coupon> coupons) {
		bathUpdate(coupons); // 1s
	}

	/**
	 * [중요!] MySQL/MariaDB JDBC URL에서
	 * rewriteBatchedStatements=true 옵션이 반드시 필요!
	 * (없으면 N건을 N번의 insert로 보냄 → 느림)
	 * <p>
	 * 예시:
	 * jdbc:mysql://localhost:3306/db?rewriteBatchedStatements=true
	 * <p>
	 * chunk size와 관련된 한계
	 * - DB 패킷 크기/SQL 사이즈(max_allowed_packet) 이상은 내부적으로 자동 분리됨
	 * - 실무에서는 chunk size: 50~1000에서 test 후 결정 추천
	 * <p>
	 * batchUpdate는 한 번에 N건의 insert를 db에 패키징해서 보냄
	 * log에서 VALUES (...)가 여러 줄 보이면 최적화 정상 동작
	 */
	private void bathUpdate(Collection<Coupon> coupons) {
		if (coupons.isEmpty()) {
			return;
		}

		String sql = """
			 INSERT INTO tb_coupon
			     (no, coupon_group_id, start_at, end_at, created_at)
			 VALUES (?, ?, ?, ?, ?)
			 """;

		jdbcTemplate.batchUpdate(sql, coupons, coupons.size(), (ps, coupon) -> {
			ps.setString(1, coupon.getNo());
			ps.setString(2, coupon.getCouponGroup().getId());
			ps.setTimestamp(3, Timestamp.from(coupon.getStartAt()));
			ps.setTimestamp(4, Timestamp.from(coupon.getEndAt()));
			ps.setTimestamp(5, Timestamp.from(coupon.getCreatedAt()));
		});
	}
}
