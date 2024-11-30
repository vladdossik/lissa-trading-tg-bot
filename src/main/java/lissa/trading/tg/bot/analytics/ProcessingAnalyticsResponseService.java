package lissa.trading.tg.bot.analytics;

import lissa.trading.tg.bot.analytics.dto.AnalyticsBrandInfoResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsIdeasPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsNewsResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsPulseResponseDto;
import lissa.trading.tg.bot.analytics.dto.AnalyticsRequestDto;
import lissa.trading.tg.bot.dto.news.NewsDto;
import lissa.trading.tg.bot.dto.news.NewsSourceResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo.BrandInfoDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.brandInfo.BrandInfoPulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.BrokerIdeaDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.StockIdeaDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.StockIdeasPulseResponseDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.ideas.TickerIdeaDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.NewsTickerDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsDto;
import lissa.trading.tg.bot.dto.tinkoffPulse.news.StockNewsPulseResponseDto;
import lissa.trading.tg.bot.utils.MessageConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProcessingAnalyticsResponseService {

    private final AnalyticsSender analyticsSender;

    public List<String> processPulseResponse(AnalyticsPulseResponseDto responseDto) {
        if (responseDto instanceof AnalyticsNewsPulseResponseDto news) {
            return processPulseNews(news);
        } else if (responseDto instanceof AnalyticsIdeasPulseResponseDto ideas) {
            return processPulseIdeas(ideas);
        } else if (responseDto instanceof AnalyticsBrandInfoResponseDto brandIfo) {
            return processPulseBrandInfo(brandIfo);
        } else {
            log.error("Unknown type of response");
            throw new RuntimeException("Unknown type of response");
        }
    }

    public List<String> processNewsResponse(AnalyticsNewsResponseDto newsResponseDto) {
        List<String> newsForMessage = new ArrayList<>();
        List<NewsSourceResponseDto> data = newsResponseDto.getData();
        for (NewsSourceResponseDto newsSource : data) {
            for (NewsDto item : newsSource.getNews().getItems()) {
                String pubDate = item.getPubDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm"));

                String result = String.format(MessageConstants.NEWS_MESSAGE,
                        newsSource.getSource(), item.getTitle(), item.getDescription(), pubDate, item.getUrl());

                newsForMessage.add(result);
            }
        }
        return newsForMessage;
    }

    public void createRequest(AnalyticsRequestDto requestDto) {
        analyticsSender.sendRequest(requestDto);
    }

    private List<String> processPulseNews(AnalyticsNewsPulseResponseDto pulseNews) {
        List<String> newsForMessage = new ArrayList<>();
        List<StockNewsPulseResponseDto> data = pulseNews.getData();
        for (StockNewsPulseResponseDto news : data) {
            for (StockNewsDto stockNewsDto : news.getItems()) {
                String tickers = stockNewsDto.getContent().getInstruments().stream()
                        .map(NewsTickerDto::getTicker)
                        .collect(Collectors.joining(", "));

                LocalDateTime time = LocalDateTime.parse(stockNewsDto.getInserted(),
                        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"));
                String createdAt = time.format(DateTimeFormatter.ofPattern("d MMMM yyyy, HH:mm"));

                String result = String.format(MessageConstants.PULSE_NEWS_MESSAGE,
                        tickers, stockNewsDto.getOwner().getNickname(), stockNewsDto.getContent().getText(),
                        createdAt, stockNewsDto.getUrl());

                newsForMessage.add(result);
            }
        }
        return newsForMessage;
    }

    private List<String> processPulseIdeas(AnalyticsIdeasPulseResponseDto ideasDto) {
        List<String> ideasForMessage = new ArrayList<>();
        List<StockIdeasPulseResponseDto> data = ideasDto.getData();
        for (StockIdeasPulseResponseDto ideas : data) {
            for (StockIdeaDto idea : ideas.getIdeas()) {
                BrokerIdeaDto broker = idea.getBroker();
                String tickers = idea.getTickers().stream()
                        .map(TickerIdeaDto::getTicker)
                        .collect(Collectors.joining(", "));

                String result = String.format(MessageConstants.IDEAS_MESSAGE,
                        idea.getTitle(), broker.getName(), broker.getAccuracy(), tickers, idea.getPriceStart(),
                        idea.getActualPrice(), idea.getYield() + "%", idea.getTargetYield() + "%",
                        idea.getDateStart(), idea.getDateEnd(), idea.getUrl());

                ideasForMessage.add(result);
            }
        }
        return ideasForMessage;
    }

    private List<String> processPulseBrandInfo(AnalyticsBrandInfoResponseDto brandInfoResponseDto) {
        List<String> brandsForMessage = new ArrayList<>();
        List<BrandInfoPulseResponseDto> data = brandInfoResponseDto.getData();
        for (BrandInfoPulseResponseDto brandInfo : data) {
            BrandInfoDto fullInfo = brandInfo.getBrandInfo();
            String tickers = String.join(",", fullInfo.getTickers());

            String result = String.format(MessageConstants.BRAND_INFO_MESSAGE,
                    fullInfo.getName(), tickers, fullInfo.getBrandInfo(), fullInfo.getSector(),
                    fullInfo.getCountry(), fullInfo.getExternalLinks().getMain());

            brandsForMessage.add(result);
        }
        return brandsForMessage;
    }
}
