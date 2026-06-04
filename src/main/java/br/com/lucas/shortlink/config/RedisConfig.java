package br.com.lucas.shortlink.config; // Ajuste para o seu pacote

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        // 1. Criamos um ObjectMapper customizado
        ObjectMapper objectMapper = new ObjectMapper();

        // 2. Registramos o módulo que ensina o Jackson a ler/escrever LocalDateTime
        objectMapper.registerModule(new JavaTimeModule());

        // 3. Dizemos para ele salvar a data no formato ISO-8601 (String legível) e não como Timestamp numérico
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // 4. Injetamos nosso ObjectMapper no serializador do Redis
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 5. Retornamos a configuração padrão acoplando o nosso serializador corrigido
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(60)) // Opcional: Tempo de vida do cache
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer));
    }
}