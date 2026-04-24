package com.skillsops.market.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillsops.common.api.dto.PageResponse;
import com.skillsops.market.dto.MarketSkillDetailDTO;
import com.skillsops.market.dto.MarketSkillSummaryDTO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@Service
public class MarketCacheService {

    private static final Duration LIST_TTL = Duration.ofSeconds(45);
    private static final Duration DETAIL_TTL = Duration.ofSeconds(30);
    private static final String LIST_PREFIX = "skillsops:market:list:";
    private static final String DETAIL_PREFIX = "skillsops:market:detail:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MarketCacheService(ObjectProvider<StringRedisTemplate> redisTemplateProvider, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
        this.objectMapper = objectMapper;
    }

    public Optional<PageResponse<MarketSkillSummaryDTO>> getPublishedList(String cacheKey) {
        if (redisTemplate == null) {
            return Optional.empty();
        }
        String raw = redisTemplate.opsForValue().get(LIST_PREFIX + cacheKey);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            var value = objectMapper.readValue(raw, new TypeReference<PageResponse<MarketSkillSummaryDTO>>() {
            });
            return Optional.of(value);
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public void cachePublishedList(String cacheKey, PageResponse<MarketSkillSummaryDTO> value) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(LIST_PREFIX + cacheKey, objectMapper.writeValueAsString(value), LIST_TTL);
        } catch (Exception ignored) {
            // ignore cache write failures, keep query path available
        }
    }

    public Optional<MarketSkillDetailDTO> getPublishedDetail(Long skillId) {
        if (redisTemplate == null) {
            return Optional.empty();
        }
        String raw = redisTemplate.opsForValue().get(DETAIL_PREFIX + skillId);
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(raw, MarketSkillDetailDTO.class));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    public void cachePublishedDetail(Long skillId, MarketSkillDetailDTO value) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(DETAIL_PREFIX + skillId, objectMapper.writeValueAsString(value), DETAIL_TTL);
        } catch (Exception ignored) {
            // ignore cache write failures
        }
    }

    public void evictListCache() {
        if (redisTemplate == null) {
            return;
        }
        Set<String> keys = redisTemplate.keys(LIST_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void evictSkillDetail(Long skillId) {
        if (redisTemplate == null) {
            return;
        }
        redisTemplate.delete(DETAIL_PREFIX + skillId);
    }
}
