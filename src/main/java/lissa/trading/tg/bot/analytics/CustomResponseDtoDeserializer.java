package lissa.trading.tg.bot.analytics;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import lissa.trading.tg.bot.dto.tinkoffPulse.PulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo.BrandInfoPulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.StockIdeasPulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsPulseResponseDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CustomResponseDtoDeserializer extends JsonDeserializer<List<PulseResponseDto>> {

    @Override
    public List<PulseResponseDto> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        List<PulseResponseDto> result = new ArrayList<>();

        for (JsonNode element : node) {
            if (element.has("items")) {
                result.add(p.getCodec().treeToValue(element, StockNewsPulseResponseDto.class));
            } else if (element.has("ideas")) {
                result.add(p.getCodec().treeToValue(element, StockIdeasPulseResponseDto.class));
            } else if (element.has("brandInfo")) {
                result.add(p.getCodec().treeToValue(element, BrandInfoPulseResponseDto.class));
            }
        }

        return result;
    }
}
