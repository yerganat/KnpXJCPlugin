package kz.inessoft.tools.xjc.util;

import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XmlString;

public class Helper {

    public static String getFixedValue(CPropertyInfo fieldInfo) {
        if (!(fieldInfo.getSchemaComponent() instanceof XSAttributeUse)) {
            return "unknown";
        }
        XmlString fixedValue = ((XSAttributeUse) fieldInfo.getSchemaComponent()).getFixedValue();

        if(fixedValue == null) {
            return "unknown";
        }

        return fixedValue.value;
    }
}
