package com.example.cryptogay.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class Coin {

    @SerializedName("id")
    private final String id;

    @SerializedName("symbol")
    private final String symbol;

    @SerializedName("name")
    private final String name;

    @SerializedName("image")
    private final String image;

    @SerializedName("current_price")
    private final Double currentPrice;

    @SerializedName("price_change_percentage_24h")
    private final Double priceChangePercentage24h;

    @SerializedName("price_change_24h")
    private final Double priceChange24h;

    @SerializedName("market_cap_rank")
    private final Integer marketCapRank;

    @SerializedName("market_cap")
    private final Double marketCap;

    @SerializedName("total_volume")
    private final Double totalVolume;

    @SerializedName("high_24h")
    private final Double high24h;

    @SerializedName("low_24h")
    private final Double low24h;

    @SerializedName("ath")
    private final Double ath;

    @SerializedName("ath_change_percentage")
    private final Double athChangePercentage;

    @SerializedName("circulating_supply")
    private final Double circulatingSupply;

    @SerializedName("total_supply")
    private final Double totalSupply;

    public Coin(String id, String symbol, String name, String image,
                Double currentPrice, Double priceChangePercentage24h, Integer marketCapRank) {
        this(id, symbol, name, image, currentPrice, priceChangePercentage24h, null,
                marketCapRank, null, null, null, null, null, null, null, null);
    }

    public Coin(String id, String symbol, String name, String image,
                Double currentPrice, Double priceChangePercentage24h, Double priceChange24h,
                Integer marketCapRank, Double marketCap, Double totalVolume,
                Double high24h, Double low24h, Double ath, Double athChangePercentage,
                Double circulatingSupply, Double totalSupply) {
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
    }

    public String getId() {
        return id;
    }

    public String getSymbol() {
        return symbol != null ? symbol.toUpperCase() : "";
    }

    public String getName() {
        return name != null ? name : "";
    }

    public String getImage() {
        return image;
    }

    public Double getCurrentPrice() {
        return currentPrice != null ? currentPrice : 0.0;
    }

    public Double getPriceChangePercentage24h() {
        return priceChangePercentage24h != null ? priceChangePercentage24h : 0.0;
    }

    public Double getPriceChange24h() {
        return priceChange24h != null ? priceChange24h : 0.0;
    }

    public Integer getMarketCapRank() {
        return marketCapRank != null ? marketCapRank : 0;
    }

    public Double getMarketCap() {
        return marketCap != null ? marketCap : 0.0;
    }

    public Double getTotalVolume() {
        return totalVolume != null ? totalVolume : 0.0;
    }

    public Double getHigh24h() {
        return high24h != null ? high24h : 0.0;
    }

    public Double getLow24h() {
        return low24h != null ? low24h : 0.0;
    }

    public Double getAth() {
        return ath != null ? ath : 0.0;
    }

    public Double getAthChangePercentage() {
        return athChangePercentage != null ? athChangePercentage : 0.0;
    }

    public Double getCirculatingSupply() {
        return circulatingSupply != null ? circulatingSupply : 0.0;
    }

    public Double getTotalSupply() {
        return totalSupply != null ? totalSupply : 0.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Coin coin = (Coin) o;
        return Objects.equals(id, coin.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
