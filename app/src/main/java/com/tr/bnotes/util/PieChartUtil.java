package com.tr.bnotes.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

public class PieChartUtil {
    private PieChartUtil() {

    }

    public static void initChart(PieChartView pieChartView) {
        pieChartView.setChartRotationEnabled(true);
        pieChartView.setValueTouchEnabled(false);
        pieChartView.setHapticFeedbackEnabled(false);
        pieChartView.setCircleFillRatio(0.6f);
    }

    public static void setData(PieChartView pieChartView, Map<String, Long> items, int[] colors) {
        if (items == null || items.size() == 0) {
            throw new IllegalArgumentException("items == null || items.size() == 0");
        }

        List<SliceValue> values = toSliceValues(items, colors);
        PieChartData data = newPieChartData(values);
        pieChartView.setPieChartData(data);
    }

    private static PieChartData newPieChartData(List<SliceValue> values) {
        final PieChartData data;
        if (values == null) {
            data = new PieChartData();
        } else {
            data = new PieChartData(values);
        }
        data.setHasLabels(true);
        data.setHasLabelsOnlyForSelected(false);
        data.setHasLabelsOutside(true);
        data.setHasCenterCircle(true);
        data.setCenterCircleScale(0.3f);
        return data;
    }

    private static List<SliceValue> toSliceValues(Map<String, Long> items, int[] colors) {
        List<SliceValue> values = new ArrayList<>();
        int colorSize = colors.length;
        int colorIdx = 0;
        for (Map.Entry<String, Long> value : items.entrySet()) {
            SliceValue sliceValue = new SliceValue(value.getValue(), colors[colorIdx % colorSize]);
            sliceValue.setLabel(value.getKey() + " (" + CurrencyUtil.toUnsignedCurrencyString(value.getValue()) + ")");
            values.add(sliceValue);
            colorIdx++;
        }
        return values;
    }
}
