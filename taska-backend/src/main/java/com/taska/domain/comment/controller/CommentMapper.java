package com.taska.domain.comment.controller;

import com.taska.config.ApiMapperConfig;
import com.taska.domain.comment.repository.Comment;
import com.taska.domain.comment.service.CommentCreateParameters;
import com.taska.domain.comment.service.CommentUpdateParameters;
import org.mapstruct.Mapper;

@Mapper(config = ApiMapperConfig.class)
public interface CommentMapper {
  CommentDto toDto(Comment comment);

  CommentCreateParameters toParameters(CommentCreateRequest commentCreateRequest);

  CommentUpdateParameters toParameters(CommentUpdateRequest commentUpdateRequest);
}
