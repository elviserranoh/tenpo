package org.tenpo.challenge.infrastructure.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tenpo.challenge.domain.exception.JsonConversionException;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
public final class JsonUtil {

    private final ObjectMapper mapper;

    public JsonUtil(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public String objectToJson(Object object) {
        try {
            log.info("Convirtiendo objeto a JSON: {}", object);
            String jsonResult = mapper.writeValueAsString(object);
            log.info("Resultado JSON de objeto: {}", jsonResult); // Usar trace para payloads potencialmente grandes
            return jsonResult;
        } catch (JsonProcessingException e) {
            log.error("Error al convertir objeto de tipo {} a JSON: {}", object.getClass().getSimpleName(), object, e);
            throw new RuntimeException("Error al convertir objeto a JSON: " + e.getMessage(), e);
        }
    }


    public <T> T jsonToObject(String jsonString, Class<T> clazz) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            // Dependiendo del caso de uso, podrías retornar null o lanzar excepción
            log.warn("Intento de convertir una cadena JSON nula o vacía al tipo {}", clazz.getSimpleName());
            return null;
        }
        try {
            log.debug("Convirtiendo JSON a objeto. Tipo: {}, JSON: {}", clazz.getSimpleName(), jsonString);
            T resultObject = mapper.readValue(jsonString, clazz);
            log.debug("Objeto resultado de la conversión: {}", resultObject);
            return resultObject;
        } catch (IOException e) { // readValue puede lanzar subtypes de IOException como JsonProcessingException
            log.error("Error al convertir JSON a objeto de tipo {}. JSON: {}", clazz.getSimpleName(), jsonString, e);
            throw new JsonConversionException("Error al convertir JSON a " + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> jsonToMap(String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            log.warn("Intento de convertir una cadena JSON nula o vacía a Map.");
            return null;
        }
        try {
            log.debug("Convirtiendo JSON a Map. JSON: {}", jsonString);
            Map<String, Object> resultMap = mapper.readValue(jsonString, Map.class);
            log.debug("Map resultado de la conversión: {}", resultMap);
            return resultMap;
        } catch (IOException e) {
            log.error("Error al convertir JSON a Map. JSON: {}", jsonString, e);
            throw new JsonConversionException("Error al convertir JSON a Map: " + e.getMessage(), e);
        }
    }


}
