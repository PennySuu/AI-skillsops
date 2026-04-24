package com.skillsops.review.mapper;

import com.skillsops.review.domain.ReviewRecord;
import com.skillsops.review.dto.PendingReviewItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ReviewRecordMapper {

    int insertPending(@Param("skillId") Long skillId, @Param("submittedBy") Long submittedBy);

    ReviewRecord findById(@Param("id") Long id);

    List<PendingReviewItem> listPending();

    int updateStatus(@Param("id") Long id, @Param("status") String status, @Param("reason") String reason, @Param("reviewedBy") Long reviewedBy);
}
