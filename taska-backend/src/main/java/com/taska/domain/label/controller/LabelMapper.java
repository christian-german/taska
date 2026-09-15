package com.taska.domain.label.controller;

import com.taska.domain.label.Label;
import com.taska.domain.label.service.LabelCreateParameters;
import com.taska.domain.label.service.LabelUpdateParameters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface LabelMapper {
  @Mapping(target = "order", source = "position")
  LabelDto toDto(Label label);

  @Mapping(target = "color", defaultValue = "charcoal")
  @Mapping(target = "position", source = "order", defaultValue = "0")
  @Mapping(target = "favorite", source = "isFavorite", defaultValue = "false")
  LabelCreateParameters toParameters(LabelCreateRequest labelCreateRequest);

  @Mapping(target = "position", source = "order")
  @Mapping(target = "favorite", source = "isFavorite")
  LabelUpdateParameters toParameters(LabelUpdateRequest labelUpdateRequest);
}
