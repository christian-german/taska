package com.taska.label.adapter.http;

import com.taska.label.model.Label;
import com.taska.label.model.LabelCreateParameters;
import com.taska.label.model.LabelUpdateParameters;
import com.taska.platform.config.ApiMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = ApiMapperConfig.class)
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
