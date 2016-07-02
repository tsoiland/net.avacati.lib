package net.avacati.lib.sqlmapper;

import java.util.List;
import java.util.UUID;

public class TestDbo {
    public UUID uuidColumn;
    public String data;
    public SubTestDbo subTestDbo;
    public List<ListItemTestDbo> subTestDboList;

    public static class SubTestDbo {
        public UUID primaryKeyColumn;
        public SubSubTestDbo subSubTestDbo;
        public List<SubListItemTestDbo> subListItemTestDboList;
        public String subData;
    }

    public static class SubSubTestDbo {
        public UUID primaryKeyColumn;
        public String a;

    }

    public static class SubListItemTestDbo {
        public UUID primaryKeyColumn;
        public String listData;
    }

    public static class ListItemTestDbo {
        public UUID primaryKeyColumn;
        public String listData;

        public ListItemTestDbo(String listData) {
            this.primaryKeyColumn = UUID.randomUUID();
            this.listData = listData;
        }

        public ListItemTestDbo() {
        }
    }
}
