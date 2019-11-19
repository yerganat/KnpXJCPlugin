package kz.inessoft.tools.xjc.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XmlString;
import org.apache.commons.lang3.text.WordUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.sun.codemodel.JMod.PUBLIC;
import static kz.inessoft.tools.xjc.KNPPluginNew.*;

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

    public static void generateGetter(JDefinedClass restClass, JType fieldType, String fieldName, boolean isInterface) {
        if(isInterface) {
            restClass.method(PUBLIC,
                    (fieldName.equals("row") ? (J_MODEL.ref(List.class).narrow(J_MODEL.ref("T"))): fieldType),
                    getMethodName(fieldName, fieldType));
            return;
        }

        JMethod jf = restClass.method(PUBLIC, fieldType, getMethodName(fieldName, fieldType));

        JBlock body = jf.body();

        if(fieldType.fullName().contains("java.util.List")) {
            try {
                JType jArrayListType = J_MODEL.parseType("java.util." + fieldType.name().replace("List", "ArrayList"));
                body._if(JExpr.ref(JExpr._this(), fieldName).eq(JExpr._null()))
                        ._then().assign(JExpr.ref(JExpr._this(), fieldName),  JExpr._new(jArrayListType));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        body._return(JExpr.ref(JExpr._this(), fieldName));
    }

    public static void generateSetter(JDefinedClass restClass, JType fieldType, String name, boolean isInterface) {
        if(fieldType.fullName().contains("java.util.List")) {
            return;
        }

        JMethod  jf = restClass.method(PUBLIC, void.class, "set" + WordUtils.capitalize(name));

        JVar value = jf.param(fieldType, name);

        if(isInterface) return;
        JBlock body = jf.body();

        body.assign(JExpr.ref(JExpr._this(), name), value);

    }


    private static String getMethodName(String fieldName, JType fieldType) {
        String getMethodName = "get";
        if(fieldType.name().equals("Boolean")) {
            getMethodName = "is";
        }

        return getMethodName + WordUtils.capitalize(fieldName);
    }

    public static JType getRestFieldType(JType fieldType, String xmlPackageName) throws ClassNotFoundException {
        JType restFieldType = fieldType;

        if (fieldType instanceof JDefinedClass) {
            JDefinedClass generatedByXmlClassType = (JDefinedClass) fieldType;
            if (generatedByXmlClassType.getPackage().name().contains(xmlPackageName)) { //Меняем пакет в наших кастомных генерируемых классах
                restFieldType = J_MODEL.parseType(fieldType.name().replace(xmlPackageName, PKG_REST));
            }
        }

        if (fieldType.fullName().contains("java.util.List<")) {
            String rowFieldClassName = fieldType.name().replace("List<", "").replace(">", "");
            JClass rawLLclazz = J_MODEL.ref(List.class);
            restFieldType = rawLLclazz.narrow(J_MODEL.parseType(rowFieldClassName));
        }
        return restFieldType;

    }

    public static void annotateWithJsonProperty(JFieldVar restField, String fieldName, Collection<JAnnotationUse> annotations) throws ClassNotFoundException {
        if(fieldName.equals("row")) {
            JAnnotationUse jAnnotationForRow = restField.annotate(JsonProperty.class);
            jAnnotationForRow.param("value", "rows");
            //logger.debug("          annotate  JsonProperty(\"rows\")");
        } else {
            /**
             * Генерирует аннотацию JsonProperty по XmlElement
             */
            for (Iterator<JAnnotationUse> jAnnotationUseIterator = annotations.iterator(); jAnnotationUseIterator.hasNext(); ) {
                JAnnotationUse jAnnotationUse = jAnnotationUseIterator.next();

                if (!jAnnotationUse.getAnnotationClass().name().equals("XmlElement")) {
                    //logger.debug("      skip " + jAnnotationUse.getAnnotationClass().name());
                    continue;
                }

                JAnnotationUse jAnnotationForField = restField.annotate(JsonProperty.class);
                JAnnotationValue jAnnotationValue = jAnnotationUse.getAnnotationMembers().get("name");

                if (jAnnotationValue != null) {
                    String annotateValueStr = getStringValueOfAnnotation(jAnnotationValue);
                    //logger.debug("          annotate  JsonProperty(\"" + annotateValueStr + "\")");

                    jAnnotationForField.param("value", annotateValueStr);
                }
            }
        }

    }


    private static String getStringValueOfAnnotation(JAnnotationValue annotationValue) {
        StringWriter s = new StringWriter();
        JFormatter f = new JFormatter(new PrintWriter(s));
        annotationValue.generate(f);
        return s.toString().replace("\"", "");
    }

    public static JDefinedClass implementInterface(JDefinedClass currentClass, JDefinedClass restClass, String classShortName) throws JClassAlreadyExistsException {
        JDefinedClass commonInterface = J_MODEL._class(PKG_SERVICE_DTO + "I" + classShortName, ClassType.INTERFACE);
        //public interface IPage1010400101<T extends IPage1010400101Row> { TODO сделать T extend для Rows


        currentClass._implements(commonInterface);

        if(currentClass.fields().get("row") == null) {
            restClass._implements(commonInterface);
        } else {
            restClass._implements(commonInterface.narrow(J_MODEL._class(PKG_SERVICE_DTO + "I" + restClass.name() + "Row")));
        }

        return commonInterface;


    }

}
