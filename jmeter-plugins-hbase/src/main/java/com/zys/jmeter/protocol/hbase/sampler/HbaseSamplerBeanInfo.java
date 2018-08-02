package com.zys.jmeter.protocol.hbase.sampler;

/**
 * Created by zhuyongsheng on 2018/1/4.
 */
import java.beans.PropertyDescriptor;
import org.apache.jmeter.testbeans.BeanInfoSupport;

public class HbaseSamplerBeanInfo extends BeanInfoSupport {
    public HbaseSamplerBeanInfo()
    {
        super(HbaseSampler.class);
        createPropertyGroup("操作信息", new String[] {"hbase", "tableName", "opr", "rowKey", "family", "column", "value"});
        PropertyDescriptor p = property("hbase");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("tableName");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("rowKey");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("family");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("column");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
        p = property("opr", HbaseSampler.OPRS.class);
        p.setValue(DEFAULT, HbaseSampler.OPRS.READ.ordinal());
        p.setValue(NOT_UNDEFINED, Boolean.TRUE);
        p = property("value");
        p.setValue("notUndefined", Boolean.TRUE);
        p.setValue("default", "");
    }
}