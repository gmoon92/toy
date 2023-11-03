# Spring JOOQ

## Environment

- spring-boot-starter:spring-boot:2.7.8
- mariadb:10.3
- org.jooq:jooq:3.14.16
- jdk 1.8

```sh
cd docker
docker compose -p batchinsert up -d
```

## [Generated source](https://www.jooq.org/doc/3.18/manual/getting-started/tutorials/jooq-in-7-steps/jooq-in-7-steps-step3/)

### [Java config](https://www.jooq.org/doc/latest/manual/code-generation/codegen-programmatic/)

java config 설정시 GenerationTool jooq version 3.11.0 이상 지원

- org.jooq:jooq-meta:${jooq.version}
- org.jooq:jooq-meta:${jooq.version}
- org.jooq:jooq-meta:${jooq.version}

```java
import org.jooq.meta.jaxb.*;

@Configuration
public class JooqConfig {

	@PostConstruct
	public void generateSource() throws Exception {
		Configuration configuration = new Configuration()
		  // jdbc config
		  .withJdbc(new Jdbc()
			.withDriver("org.h2.Driver")
			.withUrl("jdbc:h2:mem:test")
			.withUser("sa")
			.withPassword(""))
		  .withGenerator(new Generator()
			// jooq meta database config
			.withDatabase(new Database()
			  .withSchemaVersionProvider("SELECT 'V1' AS version") // todo make custom schema version provider.
			  .withName("org.jooq.meta.h2.H2Database") // org.jooq.meta.*.*Database
			  .withInputSchema("PUBLIC")
			)
			// generated source target dir.
			.withTarget(
			  new Target()
				.withDirectory("spring-jooq/src/test/java")
				.withPackageName("com.gmoon.springjooq.global.jooqschema")
			)
		  );

		GenerationTool.generate(configuration);
	}
}
```

### [Maven plugin]((https://www.jooq.org/doc/latest/manual/code-generation/codegen-maven/))

- jooq-codegen-maven

```xml

<plugin>
    <!-- https://www.jooq.org/doc/latest/manual/code-generation/codegen-maven/ -->
    <groupId>org.jooq</groupId>
    <artifactId>jooq-codegen-maven</artifactId>
    <version>${jooq.version}</version>
    <executions>
        <execution>
            <id>jooq-codegen</id>
            <phase>generate-sources</phase>
            <goals>
                <goal>generate</goal>
            </goals>
            <configuration>
                <jdbc>
                    <driver>${jdbc.driver-class-name}</driver>
                    <url>${jdbc.url}</url>
                    <user>${jdbc.username}</user>
                    <password>${jdbc.password}</password>
                </jdbc>
                <generator>
                    <database>
                        <inputSchema>${jdbc.schema}</inputSchema>
                    </database>
                    <generate>
                        <!-- Never generate deprecated code -->
                        <deprecated>false</deprecated>
                        <instanceFields>true</instanceFields>
                        <pojos>false</pojos>
                        <records>false</records>
                    </generate>
                    <target>
                        <!--<directory>target/generated-sources/</directory>-->
                        <directory>src/main/java</directory>
                        <packageName>com.gmoon.springjooq.global.jooqschema</packageName>
                    </target>
                </generator>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## Reference

- [www.jooq.org](https://www.jooq.org/)
- [h2database - Using H2 with jOOQ](https://www.h2database.com/html/tutorial.html#using_jooq)
