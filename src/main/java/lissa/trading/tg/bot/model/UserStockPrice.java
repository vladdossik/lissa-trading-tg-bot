package lissa.trading.tg.bot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lissa.trading.tg.bot.dto.tinkoff.Currency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Entity
@Table(name = "user_stock_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class UserStockPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @NotNull
    @Column(name = "figi", nullable = false)
    private String figi;

    @NotNull
    @Column(name = "last_price", nullable = false)
    private Double lastPrice;

    @NotNull
    @Column(name = "price_timestamp", nullable = false)
    private OffsetDateTime priceTimestamp;

    @Column(name = "ticker")
    private String ticker;

    @Column(name = "name")
    private String name;

    @Column(name = "instrument_type")
    private String instrumentType;

    @Column(name = "last_notified_price")
    private Double lastNotifiedPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false)
    private Currency currency;
}
