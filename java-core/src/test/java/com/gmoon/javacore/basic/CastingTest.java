package com.gmoon.javacore.basic;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class CastingTest {

	@DisplayName("멤버가 변한다.")
	@Test
	void up_casting() {
		Ahri ahri = new Ahri();

		Champion champion = ahri; // 참조 다형성

		// 멤버가 변한다.
		assertThat(champion.name).isEqualTo("Ahri");

		// 런타임시 오버라이딩된 자식 메서드 실행
		assertThat(champion.attack()).isEqualTo("MID-Ahri-Ahri is undeveloped");
	}

	@Nested
	class DownCastingTest {

		@DisplayName("복구한다. 업캐스팅한 객체를 되돌리는데 목적")
		@Test
		void down_casting() {
			Ahri ahri = new Ahri();

			Champion champion = ahri; // up-casting

			Ahri downCasting = (Ahri)champion; // down-casting

			assertThat(downCasting.name).isEqualTo("Ahri");
			assertThat(downCasting.position).isEqualTo("MID");
			assertThat(downCasting.attack()).isEqualTo("MID-Ahri-Ahri is undeveloped");
		}

		@DisplayName("업캐스팅하지 않은 객체 다운캐스팅시 에러 발생")
		@Test
		void error() {
			assertThatExceptionOfType(ClassCastException.class)
				.isThrownBy(() -> {
					Champion champion = new Champion();

					// 참조 다형성 위배
					Ahri ahri = (Ahri)champion; // error
				});
		}

		@DisplayName("다운캐스팅시 형변환 에러를 방지하기 위해 instanceof 연산자 사용")
		@Test
		void down_casting_instanceOf() {
			Champion champion = new Champion();

			if (champion instanceof Ahri) {
				Ahri ahri = (Ahri)champion;
			} else {
				// error!
			}

			assertThat(champion instanceof Ahri).isFalse();
		}
	}

	@DisplayName("형제 클래스간 캐스팅 및 참조 형변환 불가")
	@Test
	void noCasting() {
		assertThatExceptionOfType(ClassCastException.class)
			.isThrownBy(() -> {
				Champion ahri = new Ahri();
				// 참조 다형성 위배
				AurelionSol sol = (AurelionSol)ahri;
			});
	}

	static class Champion {

		protected String name;

		public Champion() {
			this.name = "unknown";
		}

		public String attack() {
			return name + " is undeveloped";
		}
	}

	static class Ahri extends Champion {

		protected String position;

		public Ahri() {
			this.name = "Ahri";
			this.position = "MID";
		}

		@Override
		public String attack() {
			return String.format("%s-%s-%s", position, name, super.attack());
		}
	}

	static class AurelionSol extends Champion {

		protected String position;

		public AurelionSol() {
			this.name = "Aurelion Sol";
			this.position = "MID";
		}

		@Override
		public String attack() {
			return String.format("%s-%s-%s", position, name, super.attack());
		}
	}
}
