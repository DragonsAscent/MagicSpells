package com.nisovin.magicspells.variables.meta;

import com.nisovin.magicspells.variables.MetaVariable;

import java.time.Instant;

public class TimestampSecondsVariable extends MetaVariable {

    @Override
    public double getValue(String player) {
        Instant instant = Instant.now();
        return instant.getEpochSecond();
    }

}
