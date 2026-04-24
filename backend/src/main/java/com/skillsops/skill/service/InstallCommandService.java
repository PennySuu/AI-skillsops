package com.skillsops.skill.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.dto.InstallCommandResponse;
import com.skillsops.skill.mapper.SkillMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InstallCommandService {

    private static final Duration COMMAND_TTL = Duration.ofMinutes(2);
    private static final String IDEMPOTENCY_PREFIX = "skillsops:install:idem:";
    private static final String TOKEN_PREFIX = "skillsops:install:token:";

    private final SkillMapper skillMapper;
    private final CurrentUserService currentUserService;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, LocalCacheValue> localIdempotencyCache = new ConcurrentHashMap<>();

    public InstallCommandService(
            SkillMapper skillMapper,
            CurrentUserService currentUserService,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.skillMapper = skillMapper;
        this.currentUserService = currentUserService;
        this.redisTemplate = redisTemplateProvider.getIfAvailable();
    }

    public InstallCommandResponse issueInstallCommand(Long skillId, String idempotencyKey, jakarta.servlet.http.HttpServletRequest request) {
        Long userId = currentUserService.requireUserId(request);
        Skill skill = skillMapper.findById(skillId);
        if (skill == null) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "Skill 不存在");
        }
        if (skill.status() == SkillStatus.offline) {
            throw new BusinessException(ErrorCode.SKILL_OFFLINE_NOT_INSTALLABLE, "该 Skill 已下架，暂不可安装");
        }
        if (skill.status() != SkillStatus.published) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "当前 Skill 未上架，暂不可安装");
        }

        String idemStorageKey = IDEMPOTENCY_PREFIX + userId + ":" + idempotencyKey;
        String existing = readIdempotency(idemStorageKey);
        if (existing != null) {
            return new InstallCommandResponse(existing);
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String signedUrl = "https://skillsops.local/install/" + token;
        String command = "npx skills add " + signedUrl;
        writeToken(token, skillId, userId);
        writeIdempotency(idemStorageKey, command);
        return new InstallCommandResponse(command);
    }

    private String readIdempotency(String key) {
        if (redisTemplate != null) {
            return redisTemplate.opsForValue().get(key);
        }
        LocalCacheValue local = localIdempotencyCache.get(key);
        if (local == null || local.expireAt().isBefore(Instant.now())) {
            localIdempotencyCache.remove(key);
            return null;
        }
        return local.value();
    }

    private void writeIdempotency(String key, String value) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value, COMMAND_TTL);
            return;
        }
        localIdempotencyCache.put(key, new LocalCacheValue(value, Instant.now().plus(COMMAND_TTL)));
    }

    private void writeToken(String token, Long skillId, Long userId) {
        if (redisTemplate == null) {
            return;
        }
        String payload = "{\"skillId\":" + skillId + ",\"userId\":" + userId + ",\"consumed\":false}";
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, payload, COMMAND_TTL);
    }

    private record LocalCacheValue(String value, Instant expireAt) {
    }
}
