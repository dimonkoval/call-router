package com.example.callrouter.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.util.Map;

@Converter(autoApply = true)
public class JsonConverter implements AttributeConverter<Map<String, Object>, PGobject> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public PGobject convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null) return null;

        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(mapper.writeValueAsString(attribute));
            return pgObject;
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Помилка конвертації у JSONB", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(PGobject dbData) {
        if (dbData == null) return null;

        try {
            return mapper.readValue(dbData.getValue(),
                    new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Помилка конвертації з JSONB", e);
        }
    }
}