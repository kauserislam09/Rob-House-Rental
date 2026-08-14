package com.rob.houserental.financial;

import android.content.Context;

import com.rob.houserental.data.AppDatabase;
import com.rob.houserental.data.ExpenseDao;
import com.rob.houserental.data.PaymentDao;
import com.rob.houserental.data.PropertyDao;
import com.rob.houserental.data.RentDao;
import com.rob.houserental.data.UnitDao;
import com.rob.houserental.data.UtilityBillDao;
import com.rob.houserental.model.ExpenseDisplayItem;
import com.rob.houserental.model.Property;
import com.rob.houserental.model.RentRecordDisplayItem;
import com.rob.houserental.model.Unit;
import com.rob.houserental.model.UtilityBillDisplayItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import com.rob.houserental.repository.DatabaseCallback;
import com.rob.houserental.utils.AppExecutors;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public class FinancialRepository {

    private final RentDao rentDao;
    private final ExpenseDao expenseDao;
    private final UtilityBillDao utilityBillDao;
    private final PaymentDao paymentDao;
    private final PropertyDao propertyDao;
    private final UnitDao unitDao;

    private final ExecutorService executor = AppExecutors.getInstance().getDatabaseExecutor();

    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public interface Callback<T> extends DatabaseCallback<T> {}

    public FinancialRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        this.rentDao = db.rentDao();
        this.expenseDao = db.expenseDao();
        this.utilityBillDao = db.utilityBillDao();
        this.paymentDao = db.paymentDao();
        this.propertyDao = db.propertyDao();
        this.unitDao = db.unitDao();
    }

    /**
     * Net Income Accounting Formula:
     * Net Income = Rent Collected - Active Expenses (isArchived = 0) - Utility Bills Paid.
     * Expenses and Utility Bills are separate database entities, so deducting both prevents double-counting
     * while accurately reflecting net operational revenue.
     */
    public void getFinancialSummary(FinancialFilterPeriod period, com.rob.houserental.repository.DatabaseCallback<FinancialSummary> callback) {
        executor.execute(() -> {
            try {
                String currentMonth = monthFormat.format(Calendar.getInstance().getTime());

                double expectedRent = 0.0;
                double collectedRent = 0.0;
                double activeExpenses = 0.0;
                double utilityBillsPaid = 0.0;

                if (period != null && period.getStartMonth() != null && period.getEndMonth() != null) {
                    expectedRent = rentDao.getTotalExpectedRentByMonthRange(period.getStartMonth(), period.getEndMonth());
                    collectedRent = paymentDao.getCollectedRentByPaymentDate(period.getStartDate(), period.getEndDate());
                    activeExpenses = expenseDao.getExpenseTotalByDateRange(period.getStartDate(), period.getEndDate());
                    utilityBillsPaid = utilityBillDao.getTotalCollectedBillsByMonthRange(period.getStartMonth(), period.getEndMonth());
                } else {
                    expectedRent = rentDao.getTotalExpectedRentAllTime();
                    collectedRent = rentDao.getTotalCollectedRentAllTime();
                    activeExpenses = expenseDao.getAllTimeExpenseTotal();
                    utilityBillsPaid = utilityBillDao.getTotalCollectedBillsAllTime();
                }

                double outstandingRent = rentDao.getTotalCumulativeOutstandingRent(currentMonth);
                double overdueRent = rentDao.getTotalOverdueRentAllTime();

                double netIncome = collectedRent - activeExpenses - utilityBillsPaid;

                List<Property> properties = propertyDao.getAllProperties();
                int totalUnits = 0;
                int occupiedUnits = 0;

                for (Property prop : properties) {
                    int propCount = unitDao.getUnitCount(prop.getId());
                    int propOccupied = unitDao.getUnitCountByStatus(prop.getId(), "OCCUPIED");
                    totalUnits += propCount;
                    occupiedUnits += propOccupied;
                }

                int vacantUnits = Math.max(0, totalUnits - occupiedUnits);
                double occupancyRate = totalUnits > 0 ? ((double) occupiedUnits / totalUnits) * 100.0 : 0.0;
                double collectionRate = expectedRent > 0 ? (collectedRent / expectedRent) * 100.0 : 0.0;

                FinancialSummary summary = new FinancialSummary();
                summary.setExpectedRent(expectedRent);
                summary.setCollectedRent(collectedRent);
                summary.setOutstandingRent(outstandingRent);
                summary.setOverdueRent(overdueRent);
                summary.setActiveExpenses(activeExpenses);
                summary.setUtilityBillsPaid(utilityBillsPaid);
                summary.setNetIncome(netIncome);
                summary.setTotalUnits(totalUnits);
                summary.setOccupiedUnits(occupiedUnits);
                summary.setVacantUnits(vacantUnits);
                summary.setOccupancyRate(occupancyRate);
                summary.setCollectionRate(collectionRate);

                if (callback != null) {
                    callback.onSuccess(summary);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getMonthlyTrends(int monthsBack, com.rob.houserental.repository.DatabaseCallback<List<MonthlyFinancialTrend>> callback) {
        executor.execute(() -> {
            try {
                List<MonthlyFinancialTrend> trends = new ArrayList<>();
                Calendar cal = Calendar.getInstance();

                for (int i = monthsBack - 1; i >= 0; i--) {
                    Calendar itemCal = (Calendar) cal.clone();
                    itemCal.add(Calendar.MONTH, -i);
                    String m = monthFormat.format(itemCal.getTime());
                    String mStart = m + "-01";
                    String mEnd = m + "-31";

                    double expected = rentDao.getTotalExpectedRentByMonth(m);
                    double collected = paymentDao.getCollectedRentByPaymentDate(mStart, mEnd);
                    double expenses = expenseDao.getMonthlyExpenseTotal(m);
                    double bills = utilityBillDao.getTotalCollectedBillsByMonth(m);
                    double net = collected - expenses - bills;

                    trends.add(new MonthlyFinancialTrend(m, expected, collected, expenses + bills, net));
                }

                if (callback != null) {
                    callback.onSuccess(trends);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    public void getPropertySummaries(FinancialFilterPeriod period, com.rob.houserental.repository.DatabaseCallback<List<PropertyFinancialSummary>> callback) {
        executor.execute(() -> {
            try {
                List<PropertyFinancialSummary> summaries = new ArrayList<>();
                List<Property> properties = propertyDao.getAllProperties();
                String currentMonth = monthFormat.format(Calendar.getInstance().getTime());

                for (Property prop : properties) {
                    long pid = prop.getId();
                    int total = unitDao.getUnitCount(pid);
                    int occupied = unitDao.getUnitCountByStatus(pid, "OCCUPIED");
                    int vacant = Math.max(0, total - occupied);

                    double expected = 0.0;
                    double collected = 0.0;
                    double expenses = 0.0;
                    double bills = 0.0;

                    if (period != null && period.getStartMonth() != null && period.getEndMonth() != null) {
                        expected = rentDao.getPropertyExpectedRentByMonthRange(pid, period.getStartMonth(), period.getEndMonth());
                        collected = paymentDao.getPropertyCollectedRentByPaymentDate(pid, period.getStartDate(), period.getEndDate());
                        expenses = expenseDao.getPropertyExpenseTotalByDateRange(pid, period.getStartDate(), period.getEndDate());
                        bills = utilityBillDao.getPropertyCollectedBillsByMonthRange(pid, period.getStartMonth(), period.getEndMonth());
                    } else {
                        expected = rentDao.getPropertyExpectedRentAllTime(pid);
                        collected = paymentDao.getPropertyCollectedRentAllTime(pid);
                        expenses = expenseDao.getPropertyExpenseTotal(pid);
                        bills = utilityBillDao.getPropertyCollectedBillsAllTime(pid);
                    }

                    double outstanding = rentDao.getPropertyCumulativeOutstandingRent(pid, currentMonth);
                    double net = collected - expenses - bills;
                    double colRate = expected > 0 ? (collected / expected) * 100.0 : 0.0;
                    double occRate = total > 0 ? ((double) occupied / total) * 100.0 : 0.0;

                    PropertyFinancialSummary pws = new PropertyFinancialSummary();
                    pws.setPropertyId(pid);
                    pws.setPropertyName(prop.getName());
                    pws.setTotalUnits(total);
                    pws.setOccupiedUnits(occupied);
                    pws.setVacantUnits(vacant);
                    pws.setExpectedRent(expected);
                    pws.setCollectedRent(collected);
                    pws.setOutstandingRent(outstanding);
                    pws.setExpenses(expenses);
                    pws.setUtilityBillsPaid(bills);
                    pws.setNetIncome(net);
                    pws.setCollectionRate(colRate);
                    pws.setOccupancyRate(occRate);

                    summaries.add(pws);
                }

                if (callback != null) {
                    callback.onSuccess(summaries);
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e);
                }
            }
        });
    }

    private String getFilterMonth(FinancialFilterPeriod period) {
        if (period == null || period.getType() == null) return null;
        Calendar cal = Calendar.getInstance();
        switch (period.getType()) {
            case THIS_MONTH:
                return monthFormat.format(cal.getTime());
            case LAST_MONTH:
                cal.add(Calendar.MONTH, -1);
                return monthFormat.format(cal.getTime());
            case CUSTOM:
                return period.getStartMonth();
            default:
                return null;
        }
    }
}
