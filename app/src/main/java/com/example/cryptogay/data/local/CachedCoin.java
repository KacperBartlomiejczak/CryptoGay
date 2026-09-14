package com.example.cryptogay.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.cryptogay.data.model.Coin;

@Entity(tableName = "cached_coins")
public class CachedCoin {

    @PrimaryKey
    @NonNull
    private String id;

    private String symbol;
    private String name;
    private String image;
    private Double currentPrice;
    private Double priceChangePercentage24h;
    private Double priceChange24h;
    private Integer marketCapRank;
    private Double marketCap;
    private Double totalVolume;
    private Double high24h;
    private Double low24h;
    private Double ath;
    private Double athChangePercentage;
    private Double circulatingSupply;
    private Double totalSupply;
    private long cachedAt;

    public CachedCoin(@NonNull String id, String symbol, String name, String image,
                      Double currentPrice, Double priceChangePercentage24h, Double priceChange24h,
                      Integer marketCapRank, Double marketCap, Double totalVolume,
                      Double high24h, Double low24h, Double ath, Double athChangePercentage,
                      Double circulatingSupply, Double totalSupply, long cachedAt) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.priceChange24h = priceChange24h;
        this.marketCapRank = marketCapRank;
        this.marketCap = marketCap;
        this.totalVolume = totalVolume;
        this.high24h = high24h;
        this.low24h = low24h;
        this.ath = ath;
        this.athChangePercentage = athChangePercentage;
        this.circulatingSupply = circulatingSupply;
        this.totalSupply = totalSupply;
        this.cachedAt = cachedAt;
    }

    public static CachedCoin fromCoin(Coin coin, long timestamp) {
        if (coin == null || coin.getId() == null) return null;
        return new CachedCoin(
                coin.getId(),
                coin.getSymbol(),
                coin.getName(),
                coin.getImage(),
                coin.getCurrentPrice(),
                coin.getPriceChangePercentage24h(),
                coin.getPriceChange24h(),
                coin.getMarketCapRank(),
                coin.getMarketCap(),
                coin.getTotalVolume(),
                coin.getHigh24h(),
                coin.getLow24h(),
                coin.getAth(),
                coin.getAthChangePercentage(),
                coin.getCirculatingSupply(),
                coin.getTotalSupply(),
                timestamp
        );
    }

    public Coin toCoin() {
        return new Coin(
                id,
                symbol,
                name,
                image,
                currentPrice,
                priceChangePercentage24h,
                priceChange24h,
                marketCapRank,
                marketCap,
                totalVolume,
                high24h,
                low24h,
                ath,
                athChangePercentage,
                circulatingSupply,
                totalSupply
        );
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h;
    }

    public void setPriceChangePercentage24h(Double priceChangePercentage24h) {
        this.priceChangePercentage24h = priceChangePercentage24h;
    }

    public Double getPriceChange24h() {
        return priceChange24h;
    }

    public void setPriceChange24h(Double priceChange24h) {
        this.priceChange24h = priceChange24h;
    }

    public Integer getMarketCapRank() {
        return marketCapRank;
    }

    public void setMarketCapRank(Integer marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    public Double getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(Double marketCap) {
        this.marketCap = marketCap;
    }

    public Double getTotalVolume() {
        return totalVolume;
    }

    public void setTotalVolume(Double totalVolume) {
        this.totalVolume = totalVolume;
    }

    public Double getHigh24h() {
        return high24h;
    }

    public void setHigh24h(Double high24h) {
        this.high24h = high24h;
    }

    public Double getLow24h() {
        return low24h;
    }

    public void setLow24h(Double low24h) {
        this.low24h = low24h;
    }

    public Double getAth() {
        return ath;
    }

    public void setAth(Double ath) {
        this.ath = ath;
    }

    public Double getAthChangePercentage() {
        return athChangePercentage;
    }

    public void setAthChangePercentage(Double athChangePercentage) {
        this.athChangePercentage = athChangePercentage;
    }

    public Double getCirculatingSupply() {
        return circulatingSupply;
    }

    public void setCirculatingSupply(Double circulatingSupply) {
        this.circulatingSupply = circulatingSupply;
    }

    public Double getTotalSupply() {
        return totalSupply;
    }

    public void setTotalSupply(Double totalSupply) {
        this.totalSupply = totalSupply;
    }

    public long getCachedAt() {
        return cachedAt;
    }

    public void setCachedAt(long cachedAt) {
        this.cachedAt = cachedAt;
    }
}
