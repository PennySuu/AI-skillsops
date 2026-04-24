package com.skillsops.skill.service;

import com.skillsops.common.api.error.ErrorCode;
import com.skillsops.common.exception.BusinessException;
import com.skillsops.skill.domain.Skill;
import com.skillsops.skill.domain.SkillStatus;
import com.skillsops.skill.domain.SkillVersion;
import com.skillsops.skill.dto.InstallCommandResponse;
import com.skillsops.skill.mapper.InstallRecordMapper;
import com.skillsops.skill.mapper.SkillMapper;
import com.skillsops.skill.mapper.SkillVersionMapper;
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
    private final SkillVersionMapper skillVersionMapper;
    private final InstallRecordMapper installRecordMapper;
    private final CurrentUserService currentUserService;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, LocalCacheValue> localIdempotencyCache = new ConcurrentHashMap<>();
    private final Map<String, TokenValue> localTokenCache = new ConcurrentHashMap<>();

    public InstallCommandService(
            SkillMapper skillMapper,
            SkillVersionMapper skillVersionMapper,
            InstallRecordMapper installRecordMapper,
            CurrentUserService currentUserService,
            ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.skillMapper = skillMapper;
        this.skillVersionMapper = skillVersionMapper;
        this.installRecordMapper = installRecordMapper;
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
        IdempotencyValue existing = readIdempotency(idemStorageKey);
        if (existing != null) {
            if (!existing.skillId().equals(skillId)) {
                throw new BusinessException(ErrorCode.IDEMPOTENCY_CONFLICT, "同一幂等键已用于其他 Skill");
            }
            return new InstallCommandResponse(existing.command());
        }

        String token = UUID.randomUUID().toString().replace("-", "");
        String signedUrl = "https://skillsops.local/install/" + token;
        String command = "npx skills add " + signedUrl;
        writeToken(token, skillId, userId);
        writeIdempotency(idemStorageKey, new IdempotencyValue(skillId, command));
        return new InstallCommandResponse(command);
    }

    public void consumeInstallToken(String token, Long userId) {
        TokenValue tokenValue = readToken(token);
        if (tokenValue == null) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "安装命令已过期或不存在");
        }
        if (tokenValue.consumed()) {
            throw new BusinessException(ErrorCode.OPERATION_FAILED, "安装命令已被消费");
        }
        if (!tokenValue.userId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED, "安装命令不属于当前用户");
        }

        SkillVersion latest = skillVersionMapper.findLatestBySkillId(tokenValue.skillId());
        String installedVersion = latest == null ? null : latest.version();
        installRecordMapper.upsert(userId, tokenValue.skillId(), installedVersion);
        markTokenConsumed(token, tokenValue);
    }

    private IdempotencyValue readIdempotency(String key) {
        if (redisTemplate != null) {
            String raw = redisTemplate.opsForValue().get(key);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String[] values = raw.split("\\|", 2);
            if (values.length != 2) {
                return null;
            }
            try {
                return new IdempotencyValue(Long.parseLong(values[0]), values[1]);
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        LocalCacheValue local = localIdempotencyCache.get(key);
        if (local == null || local.expireAt().isBefore(Instant.now())) {
            localIdempotencyCache.remove(key);
            return null;
        }
        return local.value();
    }

    private void writeIdempotency(String key, IdempotencyValue value) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue().set(key, value.skillId() + "|" + value.command(), COMMAND_TTL);
            return;
        }
        localIdempotencyCache.put(key, new LocalCacheValue(value, Instant.now().plus(COMMAND_TTL)));
    }

    private void writeToken(String token, Long skillId, Long userId) {
        if (redisTemplate == null) {
            localTokenCache.put(token, new TokenValue(skillId, userId, false, Instant.now().plus(COMMAND_TTL)));
            return;
        }
        String payload = skillId + "|" + userId + "|0";
        redisTemplate.opsForValue().set(TOKEN_PREFIX + token, payload, COMMAND_TTL);
    }

    private TokenValue readToken(String token) {
        if (redisTemplate != null) {
            String raw = redisTemplate.opsForValue().get(TOKEN_PREFIX + token);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String[] values = raw.split("\\|");
            if (values.length != 3) {
                return null;
            }
            try {
                return new TokenValue(
                        Long.parseLong(values[0]),
                        Long.parseLong(values[1]),
                        "1".equals(values[2]),
                        Instant.now().plus(COMMAND_TTL));
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        TokenValue value = localTokenCache.get(token);
        if (value == null || value.expireAt().isBefore(Instant.now())) {
            localTokenCache.remove(token);
            return null;
        }
        return value;
    }

    private void markTokenConsumed(String token, TokenValue tokenValue) {
        if (redisTemplate != null) {
            String payload = tokenValue.skillId() + "|" + tokenValue.userId() + "|1";
            redisTemplate.opsForValue().set(TOKEN_PREFIX + token, payload, COMMAND_TTL);
            return;
        }
        localTokenCache.put(token, new TokenValue(tokenValue.skillId(), tokenValue.userId(), true, Instant.now().plus(COMMAND_TTL)));
    }

    private record LocalCacheValue(IdempotencyValue value, Instant expireAt) {
    }

    private record IdempotencyValue(Long skillId, String command) {
    }

    private record TokenValue(Long skillId, Long userId, boolean consumed, Instant expireAt) {
    }
}
