package com.zys.jmeter.protocol.hbase.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import java.util.Collections;

/**
 * Created by zhuyongsheng on 2018/8/2.
 */
public class HbaseUtils {

    private HbaseUtils() {
    }

    @SuppressWarnings("unchecked")
    public static String read(Connection connection, String tableName, String rowKey, String family, String column) throws Exception {

        Scan scan = new Scan();
        if (StringUtils.isNotEmpty(rowKey)) {
            int i = 0;
            byte[] mask = new byte[rowKey.length()];
            for (char ch : rowKey.toCharArray()) {
                mask[i++] = '?' == ch ? (byte) 1 : 0;
            }
            scan.setFilter(new FuzzyRowFilter(Collections.singletonList(new Pair(Bytes.toBytes(rowKey), mask))));
        }
        if (StringUtils.isNotEmpty(family)) {
            scan.addFamily(Bytes.toBytes(family));
            if (StringUtils.isNotEmpty(column)) {
                scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Result res : connection.getTable(TableName.valueOf(tableName)).getScanner(scan)) {
            for (Cell c : res.rawCells()) {
                sb.append(Bytes.toString(CellUtil.cloneRow(c)));
                sb.append(" ==> ");
                sb.append(Bytes.toString(CellUtil.cloneFamily(c)));
                sb.append(" {");
                sb.append(Bytes.toString(CellUtil.cloneQualifier(c)));
                sb.append(":");
                sb.append(Bytes.toString(CellUtil.cloneValue(c)));
                sb.append("}\n");
            }
        }
        return sb.toString();
    }

    public static String put(Connection connection, String tableName, String rowKey, String family, String column, String value) throws Exception {

        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
        connection.getTable(TableName.valueOf(tableName)).put(put);
        return "put success.";

    }


    public static String delete(Connection connection, String tableName, String rowKey, String family, String column) throws Exception {

        Delete delete = new Delete(Bytes.toBytes(rowKey));
        if (StringUtils.isNotEmpty(family)) {
            delete.addFamily(Bytes.toBytes(family));
            if (StringUtils.isNotEmpty(column)) {
                delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }
        connection.getTable(TableName.valueOf(tableName)).delete(delete);
        return "delete success.";
    }
}
