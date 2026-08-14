package com.rob.houserental.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.rob.houserental.model.Expense;
import com.rob.houserental.model.ExpenseDisplayItem;

import java.util.List;

@Dao
public interface ExpenseDao {

    @Insert
    long insert(Expense expense);

    @Update
    void update(Expense expense);

    @Delete
    void delete(Expense expense);

    @Query("SELECT * FROM expenses WHERE id = :id LIMIT 1")
    Expense getExpenseById(long id);

    @Query("SELECT * FROM expenses WHERE isArchived = 0 ORDER BY id DESC")
    List<Expense> getAllExpenses();

    @Query("SELECT * FROM expenses WHERE propertyId = :propertyId AND isArchived = 0 ORDER BY id DESC")
    List<Expense> getExpensesByProperty(long propertyId);

    @Query("SELECT * FROM expenses WHERE unitId = :unitId AND isArchived = 0 ORDER BY id DESC")
    List<Expense> getExpensesByUnit(long unitId);

    @Query("SELECT * FROM expenses WHERE expenseMonth = :expenseMonth AND isArchived = 0 ORDER BY id DESC")
    List<Expense> getExpensesByMonth(String expenseMonth);

    @Query("SELECT * FROM expenses WHERE category = :category AND isArchived = 0 ORDER BY id DESC")
    List<Expense> getExpensesByCategory(String category);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE expenseMonth = :expenseMonth AND isArchived = 0")
    double getMonthlyExpenseTotal(String expenseMonth);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE expenseDate >= :startDate AND expenseDate <= :endDate AND isArchived = 0")
    double getExpenseTotalByDateRange(String startDate, String endDate);

    @Query("SELECT COUNT(*) FROM expenses WHERE expenseMonth = :expenseMonth AND isArchived = 0")
    int getMonthlyExpenseCount(String expenseMonth);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE propertyId = :propertyId AND isArchived = 0")
    double getPropertyExpenseTotal(long propertyId);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE propertyId = :propertyId AND expenseMonth = :expenseMonth AND isArchived = 0")
    double getPropertyExpenseTotalByMonth(long propertyId, String expenseMonth);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE propertyId = :propertyId AND expenseDate >= :startDate AND expenseDate <= :endDate AND isArchived = 0")
    double getPropertyExpenseTotalByDateRange(long propertyId, String startDate, String endDate);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE isArchived = 0")
    double getAllTimeExpenseTotal();

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE e.isArchived = 0 " +
            "ORDER BY e.id DESC")
    List<ExpenseDisplayItem> getAllExpenseDisplayItems();

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE e.isArchived = 1 " +
            "ORDER BY e.id DESC")
    List<ExpenseDisplayItem> getArchivedExpenseDisplayItems();

    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE isArchived = 1")
    double getArchivedExpenseTotal();

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE e.expenseMonth = :expenseMonth AND e.isArchived = 0 " +
            "ORDER BY e.id DESC")
    List<ExpenseDisplayItem> getExpenseDisplayItemsByMonth(String expenseMonth);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE e.id = :id LIMIT 1")
    ExpenseDisplayItem getExpenseDisplayItemById(long id);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE e.propertyId = :propertyId AND e.isArchived = 0 " +
            "ORDER BY e.id DESC")
    List<ExpenseDisplayItem> getExpenseDisplayItemsByProperty(long propertyId);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE (p.name LIKE '%' || :query || '%' OR u.unitNumber LIKE '%' || :query || '%' OR e.category LIKE '%' || :query || '%' OR e.description LIKE '%' || :query || '%' OR e.notes LIKE '%' || :query || '%') AND e.isArchived = 0 " +
            "ORDER BY e.id DESC")
    List<ExpenseDisplayItem> searchExpenses(String query);

    @Query("SELECT " +
            "e.id AS id, " +
            "e.propertyId AS propertyId, " +
            "e.unitId AS unitId, " +
            "e.category AS category, " +
            "e.amount AS amount, " +
            "e.expenseDate AS expenseDate, " +
            "e.expenseMonth AS expenseMonth, " +
            "e.description AS description, " +
            "e.receiptPath AS receiptPath, " +
            "e.receiptName AS receiptName, " +
            "e.receiptMimeType AS receiptMimeType, " +
            "e.notes AS notes, " +
            "e.isArchived AS isArchived, " +
            "e.createdAt AS createdAt, " +
            "e.updatedAt AS updatedAt, " +
            "p.name AS propertyName, " +
            "u.unitNumber AS unitNumber, " +
            "u.floor AS floor " +
            "FROM expenses e " +
            "JOIN properties p ON e.propertyId = p.id " +
            "LEFT JOIN units u ON e.unitId = u.id " +
            "WHERE e.expenseDate >= :startDate AND e.expenseDate <= :endDate AND e.isArchived = 0 " +
            "ORDER BY e.expenseDate DESC, e.id DESC")
    List<ExpenseDisplayItem> getExpenseDisplayItemsByDateRange(String startDate, String endDate);
}
