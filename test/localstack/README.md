# LocalStack

```sh
cd docker
docker compose -p gmoon-testing-localstack up -d
```

## Dependency

```xml

<dependency>
    <groupId>com.amazonaws</groupId>
    <artifactId>aws-java-sdk-s3</artifactId>
    <version>1.12.295</version>
</dependency>

<dependency>
<groupId>cloud.localstack</groupId>
<artifactId>localstack-utils</artifactId>
<version>0.2.21</version>
<scope>test</scope>
</dependency>
```

- [com.amazonaws:aws-java-sdk-s3](https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3)
- [LocalStack Utilities](https://mvnrepository.com/artifact/cloud.localstack/localstack-utils)

## LocalStack Test

```java

@ExtendWith(LocalstackDockerExtension.class)
@LocalstackDockerProperties(
  services = ServiceName.S3,
  portEdge = "5001"
)
class LocalStackTest {

  @DisplayName("Amazon S3")
  @Nested
  class S3Test {

    final String BUCKET_NAME = "gmoon-local-bucket";
    AmazonS3 s3Client;

    @BeforeEach
    void setUp() {
      s3Client = TestUtils.getClientS3();
      s3Client.createBucket(BUCKET_NAME);
    }

    @DisplayName("S3는 우선 버킷을 생성해야 한다.")
    @Test
    void createBucket() {
      // when
      List<String> bucketNames = s3Client.listBuckets().stream()
        .map(Bucket::getName)
        .toList();

      // then
      assertThat(bucketNames)
        .contains(BUCKET_NAME);
    }

    @DisplayName("S3에 지정된 버킷으로 파일을 업로드한다.")
    @Test
    void uploadFile() {
      // given
      String fileName = "s3/github.txt";

      File file = FileUtils.getResourceFile(fileName);
      String key = UUID.randomUUID().toString();
      s3Client.putObject(BUCKET_NAME, key, file);

      // when
      S3ObjectInputStream is = s3Client.getObject(BUCKET_NAME, key)
        .getObjectContent();

      // then
      File actual = FileUtils.convertInputStreamToFile(is);
      assertThat(FileUtils.convertFileToString(actual))
        .contains("github");
    }
  }
}
```

## TestContainers

`Testcontainers`는 JUnit 테스트를 지원하는 Java 라이브러리로, Docker 컨테이너에서 실행할 수 있는 모든 것의 일회용 인스턴스를 제공한다.

- Data access layer integration tests
- Application integration tests
- UI/Acceptance tests

> 데이터베이스, Selenium 웹 브라우저 등등

## Dependency

```xml

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>localstack</artifactId>
    <version>${testcontainers.version}</version>
    <scope>test</scope>
</dependency>

<dependency>
<groupId>org.testcontainers</groupId>
<artifactId>junit-jupiter</artifactId>
<version>${testcontainers.version}</version>
<scope>test</scope>
</dependency>
```

- [org.testcontainers:localstack](https://mvnrepository.com/artifact/org.testcontainers/localstack)
- [org.testcontainers:junit-jupiter](https://mvnrepository.com/artifact/org.testcontainers/junit-jupiter)

## AmazonS3 With LocalStackTestContainer

```java

@Testcontainers
class LocalStackTestContainerTest {

  private static final LocalStackContainer.Service S3 = LocalStackContainer.Service.S3;

  @Container
  final LocalStackContainer localstack = new LocalStackContainer(
    DockerImageName.parse("localstack/localstack:0.14.3")
  ).withServices(S3);

  AmazonS3 s3Client = createAmazonS3Client();

  // create AmazonS3 instance.
  private AmazonS3 createAmazonS3Client() {
    String accessKey = localstack.getAccessKey();
    String secretKey = localstack.getSecretKey();

    return AmazonS3ClientBuilder.standard()
      .withCredentials(awsCredentialsProvider(accessKey, secretKey))
      .withEndpointConfiguration(getEndpointConfiguration())
      .build();
  }

  // AWS Client endpoint config 
  private AwsClientBuilder.EndpointConfiguration getEndpointConfiguration() {
    URI endpoint = localstack.getEndpointOverride(S3);
    return new AwsClientBuilder.EndpointConfiguration(
      endpoint.toString(),
      localstack.getRegion()
    );
  }

  // AWS Credentials config
  private AWSCredentialsProvider awsCredentialsProvider(String accessKey, String secretKey) {
    AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
    return new AWSStaticCredentialsProvider(credentials);
  }

  // AWS Test Case...
}
```

## Integration Testing With LocalStackContainers

```java

@TestConfiguration
public class LocalStackS3Config {

  @Bean
  public AmazonS3 amazonS3(LocalStackContainer localstack) {
    String accessKey = localstack.getAccessKey();
    String secretKey = localstack.getSecretKey();

    return AmazonS3ClientBuilder.standard()
      .withCredentials(awsCredentialsProvider(accessKey, secretKey))
      .withEndpointConfiguration(endpointConfigurations(localstack))
      .build();
  }

  private AWSCredentialsProvider awsCredentialsProvider(String accessKey, String secretKey) {
    return new AWSStaticCredentialsProvider(
      new BasicAWSCredentials(accessKey, secretKey)
    );
  }

  private AwsClientBuilder.EndpointConfiguration endpointConfigurations(LocalStackContainer localstack) {
    URI endpoint = localstack.getEndpointOverride(LocalStackContainer.Service.S3);
    return new AwsClientBuilder.EndpointConfiguration(
      endpoint.toString(),
      localstack.getRegion()
    );
  }
}

```

```properties
spring.main.allow-bean-definition-overriding:true
```

> Spring Boot 2.1 이상부터 Bean 오버라이딩이 비활성화 되어 있다.

```java
@SpringBootTest(classes = LocalStackS3Config.class)
class AmazonS3ServiceTest {
  
  @Autowired
  AmazonS3Service service;

  @DisplayName("MultipartFile S3 파일 업로드")
  @Test
  void upload() {
    // given
    File file = FileUtils.getResourceFile("s3/github.txt");

    S3RequestVO requestVO = S3RequestVO.builder()
      .bucketName("gmoon-local-bucket")
      .key("resources/public")
      .multipartFile(createMultipartFile(file))
      .build();

    // when then
    assertThat(service.upload(requestVO))
      .isInstanceOf(S3ResponseVO.class);
  }

  private MockMultipartFile createMultipartFile(File file) {
    try {
      return new MockMultipartFile(
        "file",
        file.getName(),
        MediaType.IMAGE_PNG_VALUE,
        FileUtils.convertFileToInputStream(file)
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
```

## Reference

- [localstack.cloud](https://localstack.cloud/)
- [www.testcontainers.org](https://www.testcontainers.org/)
- [testcontainers - junit5 quickstart](https://www.testcontainers.org/quickstart/junit_5_quickstart/)
- [github - Local Stack](https://github.com/localstack/localstack-java-utils)
- [github - S3 Mock](https://github.com/adobe/S3Mock)
- [docs.aws.amazon.com - Using_regions_availability_zones](https://docs.aws.amazon.com/ko_kr/AWSEC2/latest/UserGuide/using-regions-availability-zones.html)
- [docs.aws.amazon.com - AmazonS3 multipart upload](https://docs.aws.amazon.com/ko_kr/AmazonS3/latest/userguide/mpu-upload-object.html)
- [우아한 형제들 - LocalStack을 활용한 Integration Test 환경 만들기](https://techblog.woowahan.com/2638/)
