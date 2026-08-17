package com.momentum.infrastructure.query;

public record MovingAverageRow(long currentPrice, Long ma50, Long ma150, Long ma200) {

}
