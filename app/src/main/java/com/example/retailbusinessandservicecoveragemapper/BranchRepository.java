package com.example.retailbusinessandservicecoveragemapper;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Data repository for managing Retail Branches using SQLite Database via DatabaseHelper.
 * Demonstrates Java + Kotlin interoperability with real SQLite persistence.
 */
public class BranchRepository {

    public static List<Branch> getAllBranches(Context context) {
        return DatabaseHelper.getInstance(context).getAllBranches();
    }

    public static void addBranch(Context context, Branch branch) {
        DatabaseHelper.getInstance(context).addBranch(branch);
    }

    public static void updateBranch(Context context, Branch updatedBranch) {
        DatabaseHelper.getInstance(context).updateBranch(updatedBranch);
    }

    public static void deleteBranch(Context context, long id) {
        DatabaseHelper.getInstance(context).deleteBranch(id);
    }

    public static List<Branch> searchBranches(Context context, String query, String category) {
        List<Branch> all = getAllBranches(context);
        if ((query == null || query.trim().isEmpty()) && (category == null || category.equalsIgnoreCase("All"))) {
            return all;
        }

        String lowerQuery = query != null ? query.toLowerCase().trim() : "";
        List<Branch> filtered = new ArrayList<>();

        for (Branch b : all) {
            boolean matchesQuery = lowerQuery.isEmpty() ||
                    b.getName().toLowerCase().contains(lowerQuery) ||
                    b.getAddress().toLowerCase().contains(lowerQuery) ||
                    b.getContact().toLowerCase().contains(lowerQuery) ||
                    b.getCategory().toLowerCase().contains(lowerQuery);

            boolean matchesCategory = category == null || category.equalsIgnoreCase("All") ||
                    b.getCategory().equalsIgnoreCase(category);

            if (matchesQuery && matchesCategory) {
                filtered.add(b);
            }
        }
        return filtered;
    }
}
