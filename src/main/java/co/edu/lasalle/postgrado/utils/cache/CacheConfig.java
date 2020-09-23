package co.edu.lasalle.postgrado.utils.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@EnableConfigurationProperties(CacheProperties.class)
//@EnableCaching
public class CacheConfig extends CachingConfigurerSupport {
	
	@Autowired
	private CacheProperties properties;
	
	private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);
	
	private static RedisCacheConfiguration createCacheConfiguration(long timeoutInSeconds) {
		return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(timeoutInSeconds));
	}
	
	@Bean
	public LettuceConnectionFactory redisConnectionFactory() {
		log.info("Redis (/Lettuce) configuration enabled. With cache timeout " + properties.getTimeoutSeconds() + " seconds.");
		RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
		redisStandaloneConfiguration.setHostName(properties.getRedisHost());
		log.debug("[REDIS] host: " + properties.getRedisHost());
		redisStandaloneConfiguration.setPort(properties.getRedisPort());
		log.debug("[REDIS] puerto: " + properties.getRedisPort());
		return new LettuceConnectionFactory(redisStandaloneConfiguration);
	}
	
	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
		RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
		redisTemplate.setConnectionFactory(cf);
		return redisTemplate;
	}

	@Bean
	public RedisCacheConfiguration cacheConfiguration() {
		return createCacheConfiguration(properties.getTimeoutSeconds());
	}
	
	@Bean
	public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, CacheProperties properties) {
		Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
		for (Entry<String, Long> cacheNameAndTimeout : properties.getCacheExpirations().entrySet()) {
			cacheConfigurations.put(cacheNameAndTimeout.getKey(), createCacheConfiguration(cacheNameAndTimeout.getValue()));
		}
		return RedisCacheManager
				.builder(redisConnectionFactory)
				.cacheDefaults(cacheConfiguration())
				.withInitialCacheConfigurations(cacheConfigurations).build();
	}
	
}
