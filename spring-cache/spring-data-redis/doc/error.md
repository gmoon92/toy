### Can't start redis server

```text
Caused by: java.lang.RuntimeException: Can't start redis server. Check logs for details. Redis process log: 
	at redis.embedded.AbstractRedisInstance.awaitRedisServerReady(AbstractRedisInstance.java:70)
	at redis.embedded.AbstractRedisInstance.start(AbstractRedisInstance.java:42)
	at redis.embedded.RedisServer.start(RedisServer.java:9)
	at com.gmoon.springdataredis.test.EmbeddedRedisConfig.afterPropertiesSet(EmbeddedRedisConfig.java:50)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1863)
	at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1800)
	... 75 more
```

### [can not start for mac, #127](https://github.com/kstyrc/embedded-redis/issues/127#issuecomment-1049823658)

https://download.redis.io/releases/

> `wget https://download.redis.io/releases/redis-7.2.4.tar.gz`

1. Get redis binary file (arm)
    - $ wget https://download.redis.io/releases/redis-6.2.6.tar.gz
    - $ tar xzf redis-6.2.6.tar.gz
    - $ cd redis-6.2.6
    - $ make
    - Copy redis-6.2.6/src/redis-server file to your project.
2. Running Redis server with redis binary file
    - in my case, the file path is src/test/resources/binary/redis-server

```java

@TestConfiguration
public class TestRedisConfig {
	private RedisServer redisServer;

	@PostConstruct
	public void setUp() throws IOException {
		final int redisDefaultPort = 6379;
		this.redisServer = getRedisServer(redisDefaultPort);
		redisServer.start();
	}

	@PreDestroy
	public void stopRedis() {
		if (Objects.nonNull(redisServer) && redisServer.isActive()) {
			redisServer.stop();
		}
	}

	private RedisServer getRedisServer(int port) throws IOException {
		if (isMacM1()) {
			return new RedisServer(
				RedisExecProvider.defaultProvider()
					.override(OS.MAC_OS_X, Architecture.x86_64, "binary/redis-server"),
				redisPort);
		} else {
			return new RedisServer(redisPort);
		}
	}

	private boolean isMacM1() {
		if (!System.getProperty("os.name").equals("Mac OS X")) {
			return false;
		}

		return System.getProperty("os.arch").equals("aarch64") || System.getProperty("os.arch").equals("x86_64");
	}
}
```
