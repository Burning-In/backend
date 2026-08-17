package com.momentum.infrastructure.query;

import java.time.LocalDate;

public record BaseRow(LocalDate startedAt, long supportPrice, long resistancePrice) {

}
