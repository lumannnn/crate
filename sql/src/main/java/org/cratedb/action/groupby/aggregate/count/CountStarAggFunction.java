package org.cratedb.action.groupby.aggregate.count;

import org.cratedb.DataType;
import org.cratedb.action.groupby.aggregate.AggFunction;

import java.util.Set;

public class CountStarAggFunction extends AggFunction<CountAggState> {

    public static String NAME = "COUNT(*)";

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public void iterate(CountAggState state, Object columnValue) {
        state.value++;
    }

    @Override
    public Set<DataType> supportedColumnTypes() {
        return DataType.ALL_TYPES;
    }
}