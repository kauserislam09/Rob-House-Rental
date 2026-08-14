package com.rob.houserental.commercial;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanConfig {

    public static final String PLAN_FREE = "FREE";
    public static final String PLAN_MONTHLY = "MONTHLY";
    public static final String PLAN_SIX_MONTHS = "SIX_MONTHS";
    public static final String PLAN_YEARLY = "YEARLY";

    private static final Map<String, SubscriptionPlan> PLANS = new HashMap<>();

    static {
        PLANS.put(PLAN_FREE, new SubscriptionPlan(PLAN_FREE, "Free Plan", 0, 0L, "BDT", true, 0));
        PLANS.put(PLAN_MONTHLY, new SubscriptionPlan(PLAN_MONTHLY, "Monthly Premium", 30, 19900L, "BDT", true, 1));
        PLANS.put(PLAN_SIX_MONTHS, new SubscriptionPlan(PLAN_SIX_MONTHS, "6 Months Premium", 180, 99900L, "BDT", true, 2));
        PLANS.put(PLAN_YEARLY, new SubscriptionPlan(PLAN_YEARLY, "Yearly Premium", 365, 149900L, "BDT", true, 3));
    }

    public static SubscriptionPlan getPlan(String planCode) {
        if (planCode == null) return PLANS.get(PLAN_FREE);
        SubscriptionPlan plan = PLANS.get(planCode.toUpperCase());
        return plan != null ? plan : PLANS.get(PLAN_FREE);
    }

    public static List<SubscriptionPlan> getAllPaidPlans() {
        List<SubscriptionPlan> list = new ArrayList<>();
        list.add(PLANS.get(PLAN_MONTHLY));
        list.add(PLANS.get(PLAN_SIX_MONTHS));
        list.add(PLANS.get(PLAN_YEARLY));
        Collections.sort(list, (p1, p2) -> Integer.compare(p1.getSortOrder(), p2.getSortOrder()));
        return list;
    }
}
