package com.taska.config;

import org.mapstruct.MapperConfig;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Shared configuration for every API boundary mapper.
 *
 * <p>{@link ReportingPolicy#ERROR} on unmapped targets is the reason these mappers are generated: a
 * property added to a DTO or to a service parameter record breaks the build instead of being
 * silently left null. A target deliberately left out is declared with {@code @Mapping(target =
 * "...", ignore = true)}.
 */
@MapperConfig(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface ApiMapperConfig {}
