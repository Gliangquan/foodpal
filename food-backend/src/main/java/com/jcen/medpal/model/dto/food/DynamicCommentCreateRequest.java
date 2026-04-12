package com.jcen.medpal.model.dto.food;

import lombok.Data;

@Data
public class DynamicCommentCreateRequest {

    private Long dynamicId;

    private String content;

    private Long parentId;
}
