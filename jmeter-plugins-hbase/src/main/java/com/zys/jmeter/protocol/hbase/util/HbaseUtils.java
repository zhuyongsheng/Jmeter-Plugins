package com.zys.jmeter.protocol.hbase.util;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 01369755 on 2018/8/2.
 */
public class HbaseUtils {
    public static String read(Connection connection, String tableName, String rowKey, String family, String column) throws Exception {

        Table table = connection.getTable(TableName.valueOf(tableName));
        Scan scan = new Scan();

        if (StringUtils.isNotEmpty(rowKey)) {
            char[] ch = rowKey.toCharArray();
            byte[] mask = new byte[rowKey.length()];
            for (int i = 0; i < rowKey.length(); i++) {
                mask[i] = '?' == ch[i] ? (byte) 1 : 0;
            }
            List<Pair<byte[], byte[]>> fuzzyKeysData = new ArrayList<>();
            fuzzyKeysData.add(new Pair(Bytes.toBytes(rowKey), mask));
            scan.setFilter(new FuzzyRowFilter(fuzzyKeysData));
        }

        if (StringUtils.isNotEmpty(family)) {
            scan.addFamily(Bytes.toBytes(family));
            if (StringUtils.isNotEmpty(column)) {
                scan.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }

        ResultScanner scanner = table.getScanner(scan);
        StringBuffer sb = new StringBuffer();

        for (Result res : scanner) {

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

        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(family),Bytes.toBytes(column),Bytes.toBytes(value));

        table.put(put);
        return "put success.";

    }


    public static String delete(Connection connection, String tableName, String rowKey, String family, String column) throws IOException {

        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        if (StringUtils.isNotEmpty(family)) {
            delete.addFamily(Bytes.toBytes(family));
            if (StringUtils.isNotEmpty(column)) {
                delete.addColumn(Bytes.toBytes(family), Bytes.toBytes(column));
            }
        }
        table.delete(delete);
        return "delete success.";
    }
}
