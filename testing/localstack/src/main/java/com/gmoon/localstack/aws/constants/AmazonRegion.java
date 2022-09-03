package com.gmoon.localstack.aws.constants;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AmazonRegion {

	CA_CENTRAL_1("ca-central-1", "Canada (Central)"),
	EU_CENTRAL_1("eu-central-1", "Europe (Frankfurt)"),

	SA_EAST_1("sa-east-1", "남아메리카(상파울루)"),
	ME_SOUTH_1("me-south-1", "Middle East (Bahrain"),

	US_EAST_1("us-east-1", "미국 동부(버지니아"),
	US_EAST_2("us-east-2", "US East (Ohio"),
	US_WEST_1("us-west-1", "미국 서부(캘리포니아"),
	US_WEST_2("us-west-2", "미국 서부(오리건"),

	AF_SOUTH_1("af-south-1", "Africa (Cape Town"),

	AP_EAST_1("ap-east-1", "Asia Pacific (Hong"),
	AP_SOUTHEAST_3("ap-southeast-3", "아시아 태평양(자카르타"),
	AP_SOUTH_1("ap-south-1", "Asia Pacific (Mumbai"),
	AP_SOUTHEAST_1("ap-southeast-1", "아시아 태평양(싱가포르"),
	AP_SOUTHEAST_2("ap-southeast-2", "아시아 태평양(시드니"),
	AP_NORTHEAST_1("ap-northeast-1", "아시아 태평양(도쿄"),
	AP_NORTHEAST_2("ap-northeast-2", "Asia Pacific (Seoul"),
	AP_NORTHEAST_3("ap-northeast-3", "Asia Pacific (Osaka"),

	EU_WEST_1("eu-west-1", "유럽(아일랜드)"),
	EU_WEST_2("eu-west-2", "Europe (London)"),
	EU_WEST_3("eu-west-3", "Europe (Paris)"),
	EU_SOUTH_1("eu-south-1", "Europe (Milan)"),
	EU_NORTH_1("eu-north-1", "Europe (Stockholm)");

	public static final Map<String, AmazonRegion> ALL = Stream.of(AmazonRegion.values())
		.collect(collectingAndThen(
			toMap(AmazonRegion::getValue, Function.identity()),
			Collections::unmodifiableMap
		));

	private final String value;
	private final String description;
}
