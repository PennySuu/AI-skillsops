package com.skillsops.common.api.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.skillsops.common.api.error.ErrorCode;
import org.junit.jupiter.api.Test;

class ApiResponseTest {

    @Test
    void should_returnSuccessEnvelope_when_okWithPayload() {
        ApiResponse<String> r = ApiResponse.ok("skillsops");
        assertTrue(r.success());
        assertEquals(ErrorCode.OK.getCode(), r.code());
        assertEquals("skillsops", r.data());
    }

    @Test
    void should_returnFailureEnvelope_when_failWithCode() {
        ApiResponse<Void> r = ApiResponse.fail(ErrorCode.VALIDATION_FAILED, "bad");
        assertFalse(r.success());
        assertEquals(ErrorCode.VALIDATION_FAILED.getCode(), r.code());
        assertEquals("bad", r.message());
        assertNull(r.data());
    }
}
