package com.tr.bnotes;

import java.util.Collections;
import java.util.LinkedHashMap;
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

    private ConsolidatedStatement(Map<String, Long> expenses, Map<String, Long> incomes,
                                  long totalExpense, long totalIncome) {
        mExpenses = Collections.unmodifiableMap(expenses);
        mIncomes = Collections.unmodifiableMap(incomes);

        mTotalExpense = totalExpense;
        mTotalIncome = totalIncome;
    }

    public static final class Builder {
        private final Map<String, Long> mExpenses = new LinkedHashMap<>();
        private final Map<String, Long> mIncomes = new LinkedHashMap<>();

        private long mTotalExpense;
        private long mTotalIncome;

        public Builder addExpense(String type, long val) {
            mTotalExpense += val;
            add(mExpenses, type, val);
            return this;
        }

        public Builder addIncome(String type, long val) {
            mTotalIncome += val;
            add(mIncomes, type, val);
            return this;
        }

        public ConsolidatedStatement build() {
            return new ConsolidatedStatement(mExpenses, mIncomes, mTotalExpense, mTotalIncome);
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
}