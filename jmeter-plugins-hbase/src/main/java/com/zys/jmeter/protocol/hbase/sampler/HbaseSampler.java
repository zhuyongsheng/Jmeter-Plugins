package com.zys.jmeter.protocol.hbase.sampler;

/**
 * Created by 01369755 on 2018/1/3.
 */

import com.zys.jmeter.protocol.hbase.config.HbaseConfig;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.hbase.util.Pair;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testbeans.TestBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class HbaseSampler extends AbstractSampler implements TestBean {
    private static final Logger log = LoggerFactory.getLogger(HbaseSampler.class);


    private String hbase;
    private String tableName;
    private String rowKey;
    private String family;
    private String column;


    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        StringBuffer sp = new StringBuffer("scan '" + tableName + "','" + rowKey + "'");
        if (!"".equals(family)) {
            sp.append(",'" + family).append("".equals(column) ? "'" : ":" + column + "'" );
        }
        res.setSamplerData(sp.toString());
        res.setSampleLabel(getName());
        try {
            res.sampleStart();
            res.setResponseData(scan(),"UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);
        } catch (Exception e) {
            res.setResponseMessage(e.toString());
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
            return res;
        }
    }

    public String scan() throws IOException, DeserializationException {

        Connection connection = HbaseConfig.getConnection(hbase);

        Table table = connection.getTable(TableName.valueOf(tableName));

        Scan scan = new Scan();

        if (!"".equals(rowKey)) {

            char[] ch = rowKey.toCharArray();

            byte[] mask = new byte[rowKey.length()];

            for (int i = 0; i < rowKey.length(); i++){
                mask[i] =  '?' == ch[i] ? (byte)1 : 0;
            }

            List<Pair<byte[], byte[]>> fuzzyKeysData = new ArrayList<>();

            fuzzyKeysData.add(new Pair(Bytes.toBytes(rowKey), mask));

            FuzzyRowFilter fuzzy = new FuzzyRowFilter(fuzzyKeysData);

            scan.setFilter(fuzzy);

        }

        if (!"".equals(family)){
            scan.addFamily(Bytes.toBytes(family));
            if (!"".equals(column)){
                scan.addColumn(Bytes.toBytes(family),Bytes.toBytes(column));
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

    public String getHbase() {
        return hbase;
    }

    public void setHbase(String hbaseName) {
        this.hbase = hbase;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setRowKey(String rowKey) {
        this.rowKey = rowKey;
    }

    public String getTableName() {
        return tableName;
    }

    public String getRowKey() {
        return rowKey;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getColumn() {
        return column;
    }

    public void setColumn(String column) {
        this.column = column;
    }

}
