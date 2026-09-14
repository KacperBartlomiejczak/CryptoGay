package com.example.cryptogay.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_coins")
public class FavoriteCoin {

    @PrimaryKey
    @NonNull
    private String id;

    private String symbol;
    private String name;
    private String image;
    private Double currentPrice;
    private Double priceChangePercentage24h;
    private long addedAt;

    public FavoriteCoin(@NonNull String id, String symbol, String name, String image,
                        Double currentPrice, Double priceChangePercentage24h, long addedAt) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
        this.image = image;
        this.currentPrice = currentPrice;
        this.priceChangePercentage24h = priceChangePercentage24h;
        this.addedAt = addedAt;
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

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public com.example.cryptogay.data.model.Coin toCoin() {
        return new com.example.cryptogay.data.model.Coin(
                id,
                symbol,
                name,
                image,
                currentPrice,
                priceChangePercentage24h,
                null
        );
    }
}
