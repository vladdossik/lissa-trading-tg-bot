package lissa.trading.tg.bot.mapper;

import lissa.trading.tg.bot.analytics.dto.TgNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgPulseBrandInfoResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgPulseIdeaResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgPulseNewsResponseDto;
import lissa.trading.tg.bot.dto.news.NewsDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo.BrandInfoDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.StockIdeaDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.TickerIdeaDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.NewsTickerDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Mapper(componentModel = "spring")
public interface AnalyticsMapper {

    @Mapping(target = "tickers", source = "content.instruments")
    @Mapping(target = "nickname", source = "owner.nickname")
    @Mapping(target = "text", source = "content.text")
    @Mapping(target = "inserted", qualifiedByName = "formatDate")
    TgPulseNewsResponseDto toTgPulseNewsResponseDto(StockNewsDto stockNewsDto);

    TgNewsResponseDto toTgNewsResponseDto(NewsDto newsDto);

    @Mapping(target = "name", source = "broker.name")
    @Mapping(target = "accuracy", source = "broker.accuracy")
    @Mapping(target = "dateStart", qualifiedByName = "formatDate")
    @Mapping(target = "dateEnd", qualifiedByName = "formatDate")
    TgPulseIdeaResponseDto toTgPulseIdeaResponseDto(StockIdeaDto stockIdeaDto);

    @Mapping(target = "main", source = "externalLinks.main")
    TgPulseBrandInfoResponseDto toTgPulseBrandInfoResponseDto(BrandInfoDto brandInfoDto);

    default List<String> mapNewsTickers(List<NewsTickerDto> tickers) {
        if (tickers == null) {
            return null;
        }

        return tickers.stream()
                .map(NewsTickerDto::getTicker)
                .toList();
    }

    default List<String> mapIdeasTickers(List<TickerIdeaDto> tickers) {
        if (tickers == null) {
            return null;
        }

        return tickers.stream()
                .map(TickerIdeaDto::getTicker)
                .toList();
    }

    @Named("formatDate")
    default String formatDate(String inserted) {
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
        );
        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime time = LocalDateTime.parse(inserted, formatter);
                return time.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm"));
            } catch (DateTimeParseException ignored) {

            }
        }
        throw new RuntimeException("Could not parse date " + inserted);
    }
}
