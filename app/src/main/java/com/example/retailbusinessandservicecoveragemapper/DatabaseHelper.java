package com.example.retailbusinessandservicecoveragemapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper class written in Java to manage Users and Branches.
 * Performs real-time SQLite database operations for account registration,
 * user authentication, and branch management.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "NasugbuCoverageMapper.db";
    private static final int DATABASE_VERSION = 3;

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_USERNAME = "username";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_ROLE = "role";

    // Branches Table
    public static final String TABLE_BRANCHES = "branches";
    public static final String COLUMN_BRANCH_ID = "id";
    public static final String COLUMN_BRANCH_NAME = "name";
    public static final String COLUMN_BRANCH_ADDRESS = "address";
    public static final String COLUMN_BRANCH_CONTACT = "contact";
    public static final String COLUMN_BRANCH_CATEGORY = "category";
    public static final String COLUMN_BRANCH_LATITUDE = "latitude";
    public static final String COLUMN_BRANCH_LONGITUDE = "longitude";
    public static final String COLUMN_BRANCH_RADIUS = "coverage_radius_km";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT NOT NULL, " +
                COLUMN_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                COLUMN_USER_PASSWORD + " TEXT NOT NULL, " +
                COLUMN_USER_ROLE + " TEXT NOT NULL DEFAULT 'ADMIN'" +
                ");";
        db.execSQL(createUsersTable);

        String createBranchesTable = "CREATE TABLE " + TABLE_BRANCHES + " (" +
                COLUMN_BRANCH_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_BRANCH_NAME + " TEXT NOT NULL, " +
                COLUMN_BRANCH_ADDRESS + " TEXT NOT NULL, " +
                COLUMN_BRANCH_CONTACT + " TEXT, " +
                COLUMN_BRANCH_CATEGORY + " TEXT NOT NULL, " +
                COLUMN_BRANCH_LATITUDE + " REAL NOT NULL, " +
                COLUMN_BRANCH_LONGITUDE + " REAL NOT NULL, " +
                COLUMN_BRANCH_RADIUS + " REAL NOT NULL" +
                ");";
        db.execSQL(createBranchesTable);

        ContentValues adminValues = new ContentValues();
        adminValues.put(COLUMN_USER_NAME, "System Administrator");
        adminValues.put(COLUMN_USER_USERNAME, "admin");
        adminValues.put(COLUMN_USER_PASSWORD, "admin123");
        adminValues.put(COLUMN_USER_ROLE, "ADMIN");
        db.insert(TABLE_USERS, null, adminValues);

        seedInitialBranches(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANCHES);
        onCreate(db);
    }

    // ==================== USER ACCOUNT OPERATIONS ====================

    public boolean registerUser(User user) {
        if (isUsernameTaken(user.getUsername())) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_USERNAME, user.getUsername());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());
        values.put(COLUMN_USER_ROLE, user.getRole() != null ? user.getRole() : "ADMIN");

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public User authenticateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                null,
                COLUMN_USER_USERNAME + " = ? AND " + COLUMN_USER_PASSWORD + " = ?",
                new String[]{username.trim(), password},
                null, null, null
        );

        User authenticatedUser = null;
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_NAME));
            String user = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME));
            String pass = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_PASSWORD));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_ROLE));
            authenticatedUser = new User(id, name, user, pass, role);
            cursor.close();
        }
        return authenticatedUser;
    }

    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USER_USERNAME + " = ?",
                new String[]{username.trim()},
                null, null, null
        );

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        return exists;
    }

    public int getUserCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ==================== BRANCH OPERATIONS ====================

    public List<Branch> getAllBranches() {
        SQLiteDatabase dbWrite = this.getWritableDatabase();
        seedInitialBranches(dbWrite);

        List<Branch> branches = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_BRANCHES, null, null, null, null, null, COLUMN_BRANCH_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_ID));
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_NAME));
                String address = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_ADDRESS));
                String contact = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_CONTACT));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_CATEGORY));
                double lat = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_LATITUDE));
                double lng = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_LONGITUDE));
                double radius = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BRANCH_RADIUS));

                branches.add(new Branch(id, name, address, contact, category, lat, lng, radius));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return branches;
    }

    public boolean addBranch(Branch branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRANCH_ID, branch.getId());
        values.put(COLUMN_BRANCH_NAME, branch.getName());
        values.put(COLUMN_BRANCH_ADDRESS, branch.getAddress());
        values.put(COLUMN_BRANCH_CONTACT, branch.getContact());
        values.put(COLUMN_BRANCH_CATEGORY, branch.getCategory());
        values.put(COLUMN_BRANCH_LATITUDE, branch.getLatitude());
        values.put(COLUMN_BRANCH_LONGITUDE, branch.getLongitude());
        values.put(COLUMN_BRANCH_RADIUS, branch.getCoverageRadiusKm());

        long result = db.insertWithOnConflict(TABLE_BRANCHES, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return result != -1;
    }

    public boolean updateBranch(Branch branch) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRANCH_NAME, branch.getName());
        values.put(COLUMN_BRANCH_ADDRESS, branch.getAddress());
        values.put(COLUMN_BRANCH_CONTACT, branch.getContact());
        values.put(COLUMN_BRANCH_CATEGORY, branch.getCategory());
        values.put(COLUMN_BRANCH_LATITUDE, branch.getLatitude());
        values.put(COLUMN_BRANCH_LONGITUDE, branch.getLongitude());
        values.put(COLUMN_BRANCH_RADIUS, branch.getCoverageRadiusKm());

        int rows = db.update(TABLE_BRANCHES, values, COLUMN_BRANCH_ID + " = ?", new String[]{String.valueOf(branch.getId())});
        return rows > 0;
    }

    public boolean deleteBranch(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_BRANCHES, COLUMN_BRANCH_ID + " = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public int getBranchCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_BRANCHES, null);
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    // ==================== DATABASE FILE EXPORT OPERATIONS ====================

    public static String getDatabasePathString(Context context) {
        return context.getDatabasePath(DATABASE_NAME).getAbsolutePath();
    }

    public static String exportDatabaseToDownloads(Context context) {
        try {
            File currentDb = context.getDatabasePath(DATABASE_NAME);
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs();
            }
            File backupDb = new File(downloadsDir, "NasugbuCoverageMapper.sqlite");

            InputStream in = new FileInputStream(currentDb);
            OutputStream out = new FileOutputStream(backupDb);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
            out.flush();
            out.close();
            in.close();
            return backupDb.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void seedInitialBranches(SQLiteDatabase db) {
        insertSeedBranch(db, new Branch(1L, "Nasugbu Central Retail Hub", "Poblacion Ward 1, Nasugbu, Batangas", "+63 917 123 4567", "General Retail", 14.07326, 120.63265, 3.5));
        insertSeedBranch(db, new Branch(2L, "Wawa Supermarket & Supply", "Barangay Wawa, Nasugbu, Batangas", "+63 918 234 5678", "Grocery", 14.07850, 120.62620, 2.5));
        insertSeedBranch(db, new Branch(3L, "Natipuan Retail & Service Point", "Barangay Natipuan, Nasugbu, Batangas", "+63 919 345 6789", "Service Center", 14.12000, 120.59000, 4.0));
        insertSeedBranch(db, new Branch(5L, "Kaylaway Express Pharmacy & Mart", "Barangay Kaylaway, Nasugbu, Batangas", "+63 921 567 8901", "Pharmacy", 14.06200, 120.78000, 3.0));

        // Hardware & Construction Supply Branches
        insertSeedBranch(db, new Branch(101L, "Golden Jc8 const supplies Trading", "F. Castro, cor Escaler, Nasugbu, 4231 Batangas", "+63 917 101 2001", "Hardware", 14.07410, 120.63350, 3.0));
        insertSeedBranch(db, new Branch(102L, "Maquiling Builders Depot - Nasugbu Branch", "Barangay, along J. P. Laurel St, Lumbangan, Nasugbu, 4231 Batangas", "+63 918 202 3002", "Hardware", 14.06820, 120.64180, 4.0));
        insertSeedBranch(db, new Branch(103L, "Kiby's Enterprises", "Tagaytay - Nasugbu Hwy, Nasugbu, Batangas", "+63 919 303 4003", "Hardware", 14.08150, 120.65200, 3.5));
        insertSeedBranch(db, new Branch(104L, "Lucky 3 Hardware & Const Supply", "3JCM+43Q, Mulingbayan St, Nasugbu, Batangas", "+63 920 404 5004", "Hardware", 14.07350, 120.63220, 3.0));
        insertSeedBranch(db, new Branch(105L, "Nasugbu Bolts and Powertools Trading", "Block 6 Lot 31 Landing Shophouse Roxaco, Pantalan, Nasugbu, 4231 Batangas", "+63 921 505 6005", "Hardware", 14.07680, 120.62850, 3.0));
        insertSeedBranch(db, new Branch(106L, "XYJ HARDWARE AND CONSTRUCTION SUPPLY", "3J6P+653, Nasugbu, Batangas", "+63 922 606 7006", "Hardware", 14.06050, 120.63600, 3.0));
        insertSeedBranch(db, new Branch(107L, "SBS HARDWARE AND CONSTRUCTION SUPPLIES CORP.", "SITIO EL PASO SBS HARDWARE AND CONSTRUCTION SUPPLIES CORP, Nasugbu, 4231 Batangas", "+63 923 707 8007", "Hardware", 14.06900, 120.63800, 3.5));
        insertSeedBranch(db, new Branch(108L, "John & Carl Builders Depot", "3J2X+JVX Sagbat, J. P. Laurel St, Nasugbu, 4231 Batangas", "+63 924 808 9008", "Hardware", 14.05150, 120.64900, 4.0));
        insertSeedBranch(db, new Branch(109L, "EB Hardware & Construction Supplies", "Sitio Pandan Brgy Cogunan, Nasugbu, 4231 Batangas", "+63 925 909 0009", "Hardware", 14.08900, 120.67200, 3.0));
        insertSeedBranch(db, new Branch(110L, "G. Rome Hardware & Construction Supply", "3RGQ+PQR, Tagaytay - Nasugbu Hwy, Nasugbu, Batangas", "+63 926 010 1110", "Hardware", 14.11400, 120.73800, 3.5));

        // Grocery Stores & Supermarkets & Convenience (Alfamart)
        insertSeedBranch(db, new Branch(201L, "SUPERMAC MART- NASUGBU BRANCH", "F Castro St, BRGY. 3, Nasugbu, 4231 Batangas", "+63 917 201 0001", "Grocery", 14.07250, 120.63200, 2.5));
        insertSeedBranch(db, new Branch(202L, "Savemore Market Nasugbu", "J. P. Laurel St, Poblacion, Nasugbu, 4231 Batangas", "+63 917 202 0002", "Grocery", 14.07350, 120.63280, 3.0));
        insertSeedBranch(db, new Branch(203L, "Baryo Mart", "Looc Rd, Nasugbu, Batangas", "+63 917 203 0003", "Grocery", 14.07500, 120.62800, 2.5));
        insertSeedBranch(db, new Branch(204L, "Shoppers Nest Grocery", "3 J. P. Laurel St, Nasugbu, Batangas", "+63 917 204 0004", "Grocery", 14.07150, 120.63400, 2.5));
        insertSeedBranch(db, new Branch(205L, "Dali Nasugbu Centro", "C Alvarez, Barangay 4, Nasugbu, Batangas", "+63 917 205 0005", "Grocery", 14.07280, 120.63320, 2.5));
        insertSeedBranch(db, new Branch(206L, "Dali Nasugbu Pob 2", "J. P. Laurel St, Brgy. 2, Nasugbu, Batangas", "+63 917 206 0006", "Grocery", 14.07180, 120.63500, 2.5));
        insertSeedBranch(db, new Branch(207L, "Dali Nasugbu Aga", "Aga, Tagaytay - Nasugbu Hwy, Nasugbu, Batangas", "+63 917 207 0007", "Grocery", 14.09300, 120.73800, 3.0));
        insertSeedBranch(db, new Branch(208L, "Dali Nasugbu Looc", "Ternate - Nasugbu Rd, Looc, Nasugbu, Batangas", "+63 917 208 0008", "Grocery", 14.16100, 120.63400, 3.0));
        insertSeedBranch(db, new Branch(209L, "Dali Nasugbu Pob 1", "P. Rinoza St, cor Escalera St, Brgy. 1, Nasugbu, Batangas", "+63 917 209 0009", "Grocery", 14.07050, 120.63150, 2.5));
        insertSeedBranch(db, new Branch(210L, "Dali Nasugbu Pob 11", "J. P. Laurel St, Nasugbu, 4231 Batangas", "+63 917 210 0010", "Grocery", 14.06150, 120.63400, 2.5));

        // Alfamart Branches
        insertSeedBranch(db, new Branch(301L, "Alfamart Poblacion 2", "Barangay 2, Nasugbu, Batangas", "+63 918 301 0001", "Grocery", 14.07220, 120.63580, 2.5));
        insertSeedBranch(db, new Branch(302L, "Alfamart Poblacion 2 (Store 2)", "St. Francis Xavier St, Nasugbu Batangas", "+63 918 302 0002", "Grocery", 14.07110, 120.63560, 2.5));
        insertSeedBranch(db, new Branch(303L, "Alfamart Poblacion 5", "P. Hugo St, Poblacion 5, Nasugbu Batangas", "+63 918 303 0003", "Grocery", 14.07130, 120.63390, 2.5));
        insertSeedBranch(db, new Branch(304L, "Alfamart Poblacion 7", "100 Margarita St, Nasugbu, Batangas", "+63 918 304 0004", "Grocery", 14.06740, 120.63550, 2.5));
        insertSeedBranch(db, new Branch(305L, "Alfamart F Alix Margarita (Pob 7)", "F. Alix cor Margarita St, Poblacion 7, Nasugbu, Batangas", "+63 918 305 0005", "Grocery", 14.06640, 120.63410, 2.5));
        insertSeedBranch(db, new Branch(306L, "Alfamart Poblacion XI", "Nasugbu, Batangas", "+63 918 306 0006", "Grocery", 14.06170, 120.63430, 2.5));
        insertSeedBranch(db, new Branch(307L, "Alfamart JP Laurel", "J.P. Laurel St, Nasugbu, Batangas", "+63 918 307 0007", "Grocery", 14.06320, 120.63450, 2.5));
        insertSeedBranch(db, new Branch(308L, "Alfamart Brgy. Wawa", "Ternate - Nasugbu Rd, Brgy. Wawa Nasugbu, Batangas", "+63 918 308 0008", "Grocery", 14.07290, 120.63090, 2.5));
        insertSeedBranch(db, new Branch(309L, "Alfamart Wawa 2", "Apacible Blvd, Brgy. Wawa, Nasugbu, Batangas", "+63 918 309 0009", "Grocery", 14.07060, 120.63110, 2.5));
        insertSeedBranch(db, new Branch(310L, "Alfamart Wawa 3", "Brgy. Wawa, Nasugbu, Batangas", "+63 918 310 0010", "Grocery", 14.07410, 120.62950, 2.5));
        insertSeedBranch(db, new Branch(311L, "Alfamart Bucana", "R. Martinez St, Brgy. Bucana, Nasugbu, Batangas", "+63 918 311 0011", "Grocery", 14.06550, 120.63150, 2.5));
        insertSeedBranch(db, new Branch(312L, "Alfamart Looc", "Ternate - Nasugbu Rd, Brgy. Looc, Nasugbu, Batangas", "+63 918 312 0012", "Grocery", 14.16110, 120.63450, 3.0));
        insertSeedBranch(db, new Branch(313L, "Alfamart Calayo", "Brgy. Calayo, Nasugbu, Batangas", "+63 918 313 0013", "Grocery", 14.15170, 120.62720, 3.0));
        insertSeedBranch(db, new Branch(314L, "Alfamart Aga 2", "Sitio Kaysuyo, Brgy. Aga, Nasugbu Batangas", "+63 918 314 0014", "Grocery", 14.09340, 120.73850, 3.5));
        insertSeedBranch(db, new Branch(315L, "Alfamart Kaylaway", "Tagaytay - Nasugbu Hwy, Brgy Kaylaway, Nasugbu, Batangas", "+63 918 315 0015", "Grocery", 14.11320, 120.78120, 3.5));

        // Pharmacies
        insertSeedBranch(db, new Branch(401L, "Sure Health Pharmacy", "J. P. Laurel St, Nasugbu, Batangas", "+63 919 401 0001", "Pharmacy", 14.07300, 120.63250, 2.5));
        insertSeedBranch(db, new Branch(402L, "Family Healthline PHARMACY", "222 J. P. Laurel St, Poblacion, Nasugbu, 4231 Batangas", "+63 919 402 0002", "Pharmacy", 14.07400, 120.63220, 2.5));
        insertSeedBranch(db, new Branch(403L, "Generika Drugstore", "Immaculate Space Rental, F. Castro St., Brgy. 10, Nasugbu, 4231, Batangas", "+63 919 403 0003", "Pharmacy", 14.07000, 120.63100, 2.5));
        insertSeedBranch(db, new Branch(404L, "TGP The Generics Pharmacy (F.Castro Street)", "F Castro St, Poblacion, Nasugbu, 4231 Batangas", "+63 919 404 0004", "Pharmacy", 14.07120, 120.63180, 2.5));
        insertSeedBranch(db, new Branch(405L, "Mercury Drug", "J. P. Laurel St, Nasugbu, Batangas", "+63 919 405 0005", "Pharmacy", 14.07200, 120.63300, 3.0));
        insertSeedBranch(db, new Branch(406L, "BOTIKA-ESQUINA DRUGSTORE", "Sitio katorse Barangay, Bilaran, Nasugbu, 4231 Batangas", "+63 919 406 0006", "Pharmacy", 14.07100, 120.66500, 3.0));
        insertSeedBranch(db, new Branch(407L, "Ehmzz Pharmacy", "Sitio Centro, Nasugbu, Batangas", "+63 919 407 0007", "Pharmacy", 14.07300, 120.63200, 2.5));
        insertSeedBranch(db, new Branch(408L, "REINE PHARMACY", "Consuelo St, Nasugbu, Batangas", "+63 919 408 0008", "Pharmacy", 14.07150, 120.63350, 2.5));
        insertSeedBranch(db, new Branch(409L, "PENALES PHARMACY", "F Castro St, Nasugbu, Batangas", "+63 919 409 0009", "Pharmacy", 14.07220, 120.63150, 2.5));
        insertSeedBranch(db, new Branch(410L, "TGP The Generics Pharmacy (Sitio Batulao)", "Sitio Batulao, Nasugbu, 4231 Batangas", "+63 919 410 0010", "Pharmacy", 14.10000, 120.75000, 3.5));

        // Stationery Retails
        insertSeedBranch(db, new Branch(501L, "Formosa", "228 J.P. Laurel St, Nasugbu, Batangas", "+63 920 501 0001", "Stationery", 14.07410, 120.63240, 2.5));
        insertSeedBranch(db, new Branch(502L, "Kitleya Office and School Supplies Trading", "Nasugbu, Batangas", "+63 920 502 0002", "Stationery", 14.06785, 120.62697, 2.5));
        insertSeedBranch(db, new Branch(503L, "Rico & Tess Enterprises", "F. Alix Street / F. Castro Street, Public Market Area, Nasugbu, Batangas", "+63 920 503 0003", "Stationery", 14.07121, 120.63139, 2.5));

        // Printing Shops / Stores
        insertSeedBranch(db, new Branch(601L, "CitiCorner Graphics & Computer Services", "188 J. P. Laurel St., Poblacion (Brgy. 9), Nasugbu, Batangas", "+63 921 601 0001", "Printing Shop", 14.07098, 120.63238, 2.5));
        insertSeedBranch(db, new Branch(602L, "Jprintz Enterprises", "Concepcion Street, Barangay 4, Nasugbu, Batangas", "+63 921 602 0002", "Printing Shop", 14.07226, 120.63356, 2.5));
        insertSeedBranch(db, new Branch(603L, "Chloe Angela Enterprises / Printing Services", "C. Alvarez St, near Plaza De Roxas, Nasugbu, Batangas", "+63 921 603 0003", "Printing Shop", 14.07266, 120.63305, 2.5));
        insertSeedBranch(db, new Branch(604L, "IC Printing Services (Nasugbu Branch)", "Campo Avejar, Brgy. Lumbangan, Nasugbu, Batangas", "+63 921 604 0004", "Printing Shop", 14.05520, 120.64590, 3.0));
        insertSeedBranch(db, new Branch(605L, "Mister Ink Computer Sales and Services", "C. Alvarez Street, Brgy. 4, Nasugbu, Batangas", "+63 921 605 0005", "Printing Shop", 14.07350, 120.63430, 2.5));
        insertSeedBranch(db, new Branch(606L, "3A Enterprises", "C. Alvarez Street, Nasugbu, Batangas", "+63 921 606 0006", "Printing Shop", 14.07270, 120.63330, 2.5));
        insertSeedBranch(db, new Branch(607L, "Ezranet Computer Shop", "C. Alvarez Street in Barangay 3, Nasugbu, Batangas", "+63 921 607 0007", "Printing Shop", 14.07244, 120.63181, 2.5));
    }

    private void insertSeedBranch(SQLiteDatabase db, Branch branch) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRANCH_ID, branch.getId());
        values.put(COLUMN_BRANCH_NAME, branch.getName());
        values.put(COLUMN_BRANCH_ADDRESS, branch.getAddress());
        values.put(COLUMN_BRANCH_CONTACT, branch.getContact());
        values.put(COLUMN_BRANCH_CATEGORY, branch.getCategory());
        values.put(COLUMN_BRANCH_LATITUDE, branch.getLatitude());
        values.put(COLUMN_BRANCH_LONGITUDE, branch.getLongitude());
        values.put(COLUMN_BRANCH_RADIUS, branch.getCoverageRadiusKm());
        db.insertWithOnConflict(TABLE_BRANCHES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }
}
