package com.zys.jmeter.protocol.hbase.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by zhuyongsheng on 2019/1/8.
 */
public class HbaseProperty {

    private Connection connection;

    /**
     * 初始化Hbase连接
     * 在windows环境下，如果没有配置hadoop环境变量，org.apache.hadoop.util.shell类
     * 会打印找不到winutils.exe文件的ERROR日志，实际并不会影响Hbase的使用，
     * 可在jmeter/bin目录下log4j2.xml文件中配置关闭org.apache.hadoop.util包的日志：
     * <logger name="org.apache.hadoop.util" level="off"/>
     *
     * @author zhuyongsheng
     * @date 2018/8/21
     */
    public HbaseProperty(String zkAddr) throws IOException {
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", zkAddr);
        this.connection = ConnectionFactory.createConnection(conf);
    }


    @SuppressWarnings("unchecked")
    public String read(String tableName, String rowKey, String family, String column) throws Exception {

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

    public String put(String tableName, String rowKey, String family, String column, String value) throws Exception {

        Put put = new Put(Bytes.toBytes(rowKey));
        put.addColumn(Bytes.toBytes(family), Bytes.toBytes(column), Bytes.toBytes(value));
        connection.getTable(TableName.valueOf(tableName)).put(put);
        return "put success.";

    }


    public String delete(String tableName, String rowKey, String family, String column) throws Exception {

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

    public void close(){
        try {
            this.connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
