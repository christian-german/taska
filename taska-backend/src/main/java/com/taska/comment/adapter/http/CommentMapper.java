package com.taska.comment.adapter.http;

import com.taska.comment.model.Comment;
import com.taska.comment.model.CommentCreateParameters;
import com.taska.comment.model.CommentUpdateParameters;
import com.taska.platform.config.ApiMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface CommentMapper {
    CommentDto toDto(Comment comment);

    CommentCreateParameters toParameters(CommentCreateRequest commentCreateRequest);

    CommentUpdateParameters toParameters(CommentUpdateRequest commentUpdateRequest);
}
