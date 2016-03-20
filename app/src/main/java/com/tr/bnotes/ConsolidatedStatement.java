package com.tr.bnotes;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents consolidated statement of incomes / expenses.
 *
 * This is analogical to bank account statement, but with the same income / expense items summed
 * together.
 */
public final class ConsolidatedStatement {
    private final Map<String, Long> mExpenses;
    private final Map<String, Long> mIncomes;

    private final long mTotalExpense;
    private final long mTotalIncome;

    private ConsolidatedStatement(Map<String, Long> expenses, Map<String, Long> incomes) {
        mExpenses = Collections.unmodifiableMap(expenses);
        mIncomes = Collections.unmodifiableMap(incomes);

        long totalExpense = 0;
        for (long expense: expenses.values()) {
            totalExpense += expense;
        }

        long totalIncome = 0;
        for (long income: incomes.values()) {
            totalIncome += income;
        }

        mTotalExpense = totalExpense;
        mTotalIncome = totalIncome;
    }

    public static ConsolidatedStatement from(List<Item> items) {
        Map<String, Long> expenses = new LinkedHashMap<>();
        Map<String, Long> incomes = new LinkedHashMap<>();
        for (Item item: items) {
            String subType = item.getSubType();
            long amount = item.getAmount();
            if (item.getType() == Item.TYPE_EXPENSE) {
                add(expenses, subType, amount);
            } else {
                add(incomes, subType, amount);
            }
        }

        return new ConsolidatedStatement(expenses, incomes);
    }

    public Map<String, Long> getExpenses() {
        return mExpenses;
    }

    public Map<String, Long> getIncomes() {
        return mIncomes;
    }

    public long getTotalExpense() {
        return mTotalExpense;
    }

    public long getTotalIncome() {
        return mTotalIncome;
    }

    public long getMargin() {
        return mTotalIncome - mTotalExpense;
    }

    private static void add(Map<String, Long> map, String key, long val) {
        Long currentVal = map.get(key);
        if (currentVal == null) {
            map.put(key, val);
        } else {
            map.put(key, currentVal + val);
        }
    }
}