package com.taska.domain.comment.controller;

import com.taska.domain.comment.Comment;
import com.taska.domain.comment.service.CommentCreateParameters;
import com.taska.domain.comment.service.CommentUpdateParameters;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentMapper {
  CommentDto toDto(Comment comment);

  CommentCreateParameters toParameters(CommentCreateRequest commentCreateRequest);

  CommentUpdateParameters toParameters(CommentUpdateRequest commentUpdateRequest);
}
