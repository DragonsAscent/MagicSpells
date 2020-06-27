package com.nisovin.magicspells.variables.meta;

import com.nisovin.magicspells.variables.MetaVariable;

import java.time.Instant;

public class TimestampMinutesVariable extends MetaVariable {

    @Override
    public double getValue(String player) {
        Instant instant = Instant.now();
        long mins = instant.getEpochSecond();
        return Math.floor(mins/60);
    }

}
