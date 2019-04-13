package com.example.criminalintent.database;

public class CrimeDbSchema {
    /**
     * CrimeLab内部类唯一的用途是定义描述数据表元素的String常量。
     * 它的首个定义是数据库表名（CrimeTable.Name）
     */
    public static final class CrimeTable {
        public static final String NAME = "crimes";
//定义表中的字段
        public static final class Cols {
            public static final String UUID = "uuid";
            public static final String TITLE = "title";
            public static final String DATE = "date";
            public static final String SOLVED = "solved";
            public static final String SUSPECT = "suspect";
        }
    }
}