package lissa.trading.tg.bot.analytics.service;

import lissa.trading.tg.bot.analytics.dto.AnalyticsBrandInfoResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsIdeasPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgPulseBrandInfoResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgPulseIdeaResponseDto;
import lissa.trading.tg.bot.analytics.dto.TgPulseNewsResponseDto;
import lissa.trading.tg.bot.bot.TelegramBot;
import lissa.trading.tg.bot.dto.news.NewsDto;
import lissa.trading.tg.bot.dto.news.NewsSourceResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo.BrandInfoPulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.StockIdeaDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.StockIdeasPulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsPulseResponseDto;
import lissa.trading.tg.bot.mapper.AnalyticsMapper;
import lissa.trading.tg.bot.utils.MessageConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsResponseServiceImpl implements AnalyticsResponseService {

    private final AnalyticsMapper analyticsMapper;
    private final TelegramBot telegramBot;

    @Override
    public void processPulseResponse(AnalyticsPulseResponseDto responseDto) {
        List<String> messages;
        if (responseDto instanceof AnalyticsNewsPulseResponseDto news) {
            messages = processPulseNews(news);
        } else if (responseDto instanceof AnalyticsIdeasPulseResponseDto ideas) {
            messages = processPulseIdeas(ideas);
        } else if (responseDto instanceof AnalyticsBrandInfoResponseDto brandIfo) {
            messages = processPulseBrandInfo(brandIfo);
        } else {
            log.error("Unknown type of response");
            throw new RuntimeException("Unknown type of response");
        }

        telegramBot.printPulseResponse(messages, responseDto.getChatId());
    }

    @Override
    public void processNewsResponse(AnalyticsNewsResponseDto newsResponseDto) {
        List<String> newsForMessage = new ArrayList<>();
        List<NewsSourceResponseDto> data = newsResponseDto.getData();
        for (NewsSourceResponseDto newsSource : data) {
            for (NewsDto item : newsSource.getNews().getItems()) {
                TgNewsResponseDto response = analyticsMapper.toTgNewsResponseDto(item);

                newsForMessage.add(createFormatResponse(MessageConstants.NEWS_MESSAGE,
                        response.getTitle(),response.getPubDate(), response.getUrl()));
            }
        }
        telegramBot.printNewsResponse(newsForMessage, newsResponseDto.getChatId());
    }

    private List<String> processPulseNews(AnalyticsNewsPulseResponseDto pulseNews) {
        List<String> newsForMessage = new ArrayList<>();
        List<StockNewsPulseResponseDto> data = pulseNews.getData();
        for (StockNewsPulseResponseDto news : data) {
            for (StockNewsDto stockNewsDto : news.getItems()) {
                TgPulseNewsResponseDto response = analyticsMapper.toTgPulseNewsResponseDto(stockNewsDto);

                newsForMessage.add(createFormatResponse(MessageConstants.PULSE_NEWS_MESSAGE,
                        response.getTickers(), response.getNickname(), response.getText(),
                        response.getInserted(), response.getUrl()));
            }
        }
        return newsForMessage;
    }

    private List<String> processPulseIdeas(AnalyticsIdeasPulseResponseDto ideasDto) {
        List<String> ideasForMessage = new ArrayList<>();
        List<StockIdeasPulseResponseDto> data = ideasDto.getData();
        for (StockIdeasPulseResponseDto ideas : data) {
            for (StockIdeaDto idea : ideas.getIdeas()) {
                TgPulseIdeaResponseDto response = analyticsMapper.toTgPulseIdeaResponseDto(idea);

                ideasForMessage.add(createFormatResponse(MessageConstants.IDEAS_MESSAGE,
                        response.getTitle(), response.getName(), response.getAccuracy() + "%",
                        response.getTickers(), response.getPriceStart(), response.getActualPrice(),
                        response.getYield() + "%", response.getTargetYield() + "%", response.getDateStart(),
                        response.getDateEnd(), response.getUrl()));
            }
        }
        return ideasForMessage;
    }

    private List<String> processPulseBrandInfo(AnalyticsBrandInfoResponseDto brandInfoResponseDto) {
        List<String> brandsForMessage = new ArrayList<>();
        List<BrandInfoPulseResponseDto> data = brandInfoResponseDto.getData();
        for (BrandInfoPulseResponseDto brandInfo : data) {
            TgPulseBrandInfoResponseDto response = analyticsMapper.toTgPulseBrandInfoResponseDto(brandInfo.getBrandInfo());

            brandsForMessage.add(createFormatResponse(MessageConstants.BRAND_INFO_MESSAGE,
                    response.getName(), response.getTickers(), response.getBrandInfo(), response.getSector(),
                    response.getCountry(), response.getMain()));
        }
        return brandsForMessage;
    }

    private String createFormatResponse(String message, Object... data) {
        return String.format(message, data);
    }
}
