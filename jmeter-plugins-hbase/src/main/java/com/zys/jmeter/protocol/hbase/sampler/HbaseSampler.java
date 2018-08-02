package com.zys.jmeter.protocol.hbase.sampler;

/**
 * Created by zhuyongsheng on 2018/1/3.
 */

import com.zys.jmeter.protocol.hbase.util.HbaseUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.exceptions.DeserializationException;
import org.apache.hadoop.hbase.filter.FuzzyRowFilter;
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
    private int opr;
    private String value;

    public SampleResult sample(Entry entry) {
        SampleResult res = new SampleResult();
        StringBuffer sb = new StringBuffer(OPRS.values()[opr]+ "'" + tableName + "','" + rowKey + "'");
        if (StringUtils.isNotEmpty(family)) {
            sb.append(",'" + family).append(StringUtils.isEmpty(column) ? "'" : ":" + column + "'");
        }
        if (StringUtils.isNotEmpty(value)) {
            sb.append(" with value of " + value);
        }
        res.setSamplerData(sb.toString());
        res.setSampleLabel(getName());
        try {
            res.sampleStart();
            res.setResponseData(run(), "UTF-8");
            res.setResponseCode("0");
            res.setSuccessful(true);
        } catch (Exception e) {
            res.setResponseMessage(e.toString());
            res.setResponseData(e.getMessage(), "UTF-8");
            res.setResponseCode("500");
            res.setSuccessful(false);
        } finally {
            res.sampleEnd();
            return res;
        }
    }
    public String run() throws Exception {
        String result;
        Connection connection = (Connection) getProperty(hbase).getObjectValue();
        switch (OPRS.values()[opr]){
            case CREATE:
                result = HbaseUtils.create(connection, tableName, rowKey, family, column, value);
                break;
            case UPDATE:
                result = HbaseUtils.update(connection, tableName, rowKey, family, column, value);
                break;
            case READ:
                result = HbaseUtils.read(connection, tableName, rowKey, family, column);
                break;
            case DELETE:
                result = HbaseUtils.delete(connection, tableName, rowKey, family, column);
                break;
            default:
                throw new Exception("unknown operation.");
        }
        return result;
    }

    public String getHbase() {
        return hbase;
    }

    public void setHbase(String hbase) {
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

    public int getOpr() {
        return opr;
    }

    public void setOpr(int opr) {
        this.opr = opr;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public enum OPRS {
        CREATE,
        READ,
        UPDATE,
        DELETE
    }

}
