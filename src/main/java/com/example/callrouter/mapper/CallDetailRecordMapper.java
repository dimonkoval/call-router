package com.example.callrouter.mapper;

import com.example.callrouter.dto.CallDetailRecordDTO;
import com.example.callrouter.model.CallDetailRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Mapper(componentModel = "spring") // Додаємо componentModel = "spring"
public interface CallDetailRecordMapper {
    CallDetailRecordMapper INSTANCE = Mappers.getMapper(CallDetailRecordMapper.class);

    @Mapping(source = "fromNumber", target = "from")
    @Mapping(source = "toNumber", target = "to")
    CallDetailRecordDTO toDTO(CallDetailRecord entity);

    default LocalDateTime map(Long millis) {
        if (millis == null || millis == 0) return null;
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneOffset.UTC)
                .toLocalDateTime();
    }
}