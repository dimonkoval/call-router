package com.example.callrouter.mapper;

import com.example.callrouter.dto.CallDetailRecordDTO;
import com.example.callrouter.model.CallDetailRecord;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CallDetailRecordMapper {
    CallDetailRecordMapper INSTANCE = Mappers.getMapper(CallDetailRecordMapper.class);

    CallDetailRecordDTO toDTO(CallDetailRecord entity);
}
