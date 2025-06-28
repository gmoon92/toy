package com.gmoon.test.fixturemonkey.test;

import static org.assertj.core.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestInstance;

import net.jqwik.api.Arbitrary;

import com.gmoon.test.fixturemonkey.orders.Delivery;
import com.gmoon.test.fixturemonkey.orders.Order;
import com.gmoon.test.fixturemonkey.products.Product;
import com.navercorp.fixturemonkey.ArbitraryBuilder;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

/**
 * Fixture Monkey 사용법 예제입니다.
 * <p>
 * 다양한 방식으로 랜덤 테스트 데이터를 생성할 수 있으며,
 * 다음 기능을 테스트합니다:
 * <ul>
 *     <li>단일 객체 생성 (giveMeOne)</li>
 *     <li>복수 객체 생성 (giveMe)</li>
 *     <li>필드 지정 및 커스터마이징 (giveMeBuilder)</li>
 *     <li>Arbitrary 직접 사용 (giveMeArbitrary)</li>
 * </ul>
 *
 * @see <a href="https://naver.github.io/fixture-monkey/v1-1-0/docs/generating-objects/fixture-monkey/">Fixture Monkey 공식 문서</a>
 * @see <a href="https://naver.github.io/fixture-monkey/v1-1-0/docs/generating-objects/introspector/#choosing-the-right-introspector-for-your-classes">Choosing the Right Introspector for Your Classes</a>
 */
@Slf4j
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FixtureMonkeyTest {

	private static FixtureMonkey fixtureMonkey;

	@BeforeAll
	static void beforeAll() {
		fixtureMonkey = FixtureMonkey.builder()
			 .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
			 // .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
			 // .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
			 // .objectIntrospector(BuilderArbitraryIntrospector.INSTANCE)
			 .build();
	}

	/**
	 * <pre>
	 * 단일 객체 또는 제네릭 타입 객체(List 등)를 랜덤 생성합니다.
	 *
	 * - `giveMeOne(Class)` : 단일 객체 반환
	 * - `giveMeOne(TypeReference)` : 제네릭 타입 객체 반환
	 *
	 * 이 테스트는 객체가 null이 아닌지, 구조가 잘 생성됐는지를 확인합니다.
	 * </pre>
	 */
	@DisplayName("단일 객체 생성: giveMeOne() 사용")
	@RepeatedTest(10)
	void giveMeOne() {
		Order actual = fixtureMonkey.giveMeOne(Order.class);
		List<Order> list = fixtureMonkey.giveMeOne(new TypeReference<>() {
		});

		log.info("{}", actual);
		log.info("{}", list);
		assertThat(actual).isNotNull();
		assertThat(list).isNotNull();
	}

	/**
	 * <pre>
	 * 여러 개의 랜덤 객체를 생성합니다.
	 *
	 * - Stream 또는 List 형태로 받을 수 있습니다.
	 * - 제네릭 타입도 지원됩니다.
	 * </pre>
	 */
	@DisplayName("복수 객체 생성: giveMe(), giveMe(size)")
	@RepeatedTest(10)
	void giveMe() {
		Stream<Product> productStream = fixtureMonkey.giveMe(Product.class);
		Stream<List<Product>> strListStream = fixtureMonkey.giveMe(new TypeReference<>() {
		});

		List<Product> products = fixtureMonkey.giveMe(Product.class, 3);
		List<List<String>> list = fixtureMonkey.giveMe(
			 new TypeReference<>() {
			 },
			 3
		);

		log.info("{}", products);
		log.info("{}", list);

		assertThat(productStream).isNotNull();
		assertThat(strListStream).isNotNull();
		assertThat(products).hasSize(3);
		assertThat(list).hasSize(3);
	}

	/**
	 * <pre>
	 * 특정 필드 값을 지정해 커스터마이징된 객체를 생성합니다.
	 *
	 * - `.set("field", value)`로 특정 필드 값을 설정
	 * - `.size("listField", n)`으로 리스트 크기 지정
	 * - `.set("list[index]")`로 리스트 내부 필드까지 제어 가능
	 *
	 * 이 예제에서는 Order 객체의 id와 리스트 필드 deliveries를 제어합니다.
	 * </pre>
	 */
	@DisplayName("필드 값을 직접 지정해 객체 생성: giveMeBuilder() 사용")
	@RepeatedTest(10)
	void giveMeBuilder() {
		ArbitraryBuilder<Order> orderBuilder = fixtureMonkey.giveMeBuilder(Order.class)
			 .set("id", "order01")
			 .size("deliveries", 3)
			 .set("deliveries[0]", fixtureMonkey.giveMeBuilder(Delivery.class)
				  .set("id", "delivery01")
				  .sample()
			 );

		Order actual = orderBuilder.build()
			 .sample();

		log.info("actual: {}", actual);
		assertThat(actual).isNotNull();
		assertThat(actual.getId()).isEqualTo("order01");
		assertThat(actual.getDeliveries()).hasSize(3);

		ArbitraryBuilder<List<String>> builder = fixtureMonkey.giveMeBuilder(new TypeReference<>() {
		});
		assertThat(builder).isNotNull();
	}

	/**
	 * <pre>
	 * jqwik의 Arbitrary 객체를 직접 얻어 sample()로 값 추출이 가능합니다.
	 *
	 * - Fixture Monkey는 jqwik 기반이므로 Arbitrary 타입을 직접 얻을 수 있습니다.
	 * - 테스트 목적이나 jqwik 통합 시 유용합니다.
	 * </pre>
	 */
	@DisplayName("Arbitrary 타입으로 객체를 생성할 수 있다: giveMeArbitrary() 사용")
	@RepeatedTest(10)
	void giveMeArbitrary() {
		Arbitrary<Product> productArbitrary = fixtureMonkey.giveMeArbitrary(Product.class);

		Arbitrary<List<String>> strListArbitrary = fixtureMonkey.giveMeArbitrary(
			 new TypeReference<>() {
			 });

		assertThat(productArbitrary.sample()).isNotNull();
		assertThat(strListArbitrary.sample()).isNotNull();
	}
}
