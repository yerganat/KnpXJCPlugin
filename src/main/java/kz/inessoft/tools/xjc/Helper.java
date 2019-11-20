package kz.inessoft.tools.xjc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XmlString;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import org.apache.commons.lang3.text.WordUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.sun.codemodel.JMod.*;
import static com.sun.codemodel.JMod.NONE;
import static kz.inessoft.tools.xjc.KNPPluginNew.*;

class Helper {

    static String getFixedValue(CPropertyInfo fieldInfo) {
        if (!(fieldInfo.getSchemaComponent() instanceof XSAttributeUse)) {
            return "unknown";
        }
        XmlString fixedValue = ((XSAttributeUse) fieldInfo.getSchemaComponent()).getFixedValue();

        if(fixedValue == null) {
            return "unknown";
        }

        return fixedValue.value;
    }

    static void generateGetter(JDefinedClass restClass, JType fieldType, String fieldName, boolean isInterface) {
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

    static void generateSetter(JDefinedClass restClass, JType fieldType, String name, boolean isInterface) {
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

    static JType getRestFieldType(JType fieldType, String xmlPackageName) throws ClassNotFoundException {
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

    static void annotateWithJsonProperty(JFieldVar restField, String fieldName, Collection<JAnnotationUse> annotations) throws ClassNotFoundException {
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

    static JDefinedClass implementInterface(Map<String, JDefinedClass> interfacePageMap, JDefinedClass currentClass, JDefinedClass restClass, String classShortName) throws JClassAlreadyExistsException {
        logger.debug("---->interface " + PKG_SERVICE_DTO + "I" + classShortName, ClassType.INTERFACE);

        JDefinedClass commonInterface = getInterfacePageRow(interfacePageMap, "I" + classShortName);
        interfacePageMap.put("I" + classShortName, commonInterface);

        currentClass._implements(commonInterface);

        boolean tt = currentClass.fields().get("row") == null;
        logger.debug("---->interface " + tt);

        restClass._implements(commonInterface);

        return commonInterface;
    }

    static JDefinedClass getInterfacePageRow(Map<String, JDefinedClass> interfacePageMap, String pageRowInterfaceName) throws JClassAlreadyExistsException {
        if(interfacePageMap.containsKey(pageRowInterfaceName))
            return interfacePageMap.get(pageRowInterfaceName);

        JDefinedClass commonInterface = J_MODEL._class(PKG_SERVICE_DTO + pageRowInterfaceName, ClassType.INTERFACE);
        interfacePageMap.put(pageRowInterfaceName, commonInterface);
        return commonInterface;
    }

    static JDefinedClass generateBaseConverter(Map<String, JDefinedClass> interfacePageMap) {
        JDefinedClass jBaseConverterClass = null;
        try {

//            protected interface RowCreator<T, U> {
//                U createRow(T srcRow);
//            }

            jBaseConverterClass = J_MODEL._class(PUBLIC| ABSTRACT, PKG_SERVICE_DTO + "BaseV20Converter", ClassType.CLASS);
            JDefinedClass rowCreatorInterface = jBaseConverterClass._class(NONE, "RowCreator", ClassType.INTERFACE);

            rowCreatorInterface.generify("T");
            rowCreatorInterface.generify("U");

            JClass genericT = J_MODEL.ref("T");
            JClass genericU = J_MODEL.ref("U");

            JMethod jMethod = rowCreatorInterface.method(NONE, genericU, "createRow");
            jMethod.param(genericT, "srcRow");


//            protected <T, U> void fillPageRows(List<T> srcCollection, List<U> dstCollection, RowCreator<T, U> rowCreator) {
//                for (int j =0; j < srcCollection.size(); j++) {
//                    U row = rowCreator.createRow(srcCollection.get(j));
//                    dstCollection.add(row);
//                }
//            }


            JMethod jFillMethod = jBaseConverterClass.method(JMod.PUBLIC, void.class,  "fillPageRows");

            jFillMethod.generify("T");
            jFillMethod.generify("U");

            JClass rawLLclazz = J_MODEL.ref(List.class);
            JClass fieldClazzT = rawLLclazz.narrow(genericT);
            jFillMethod.param(fieldClazzT, "srcCollection");

            JClass fieldClazzU = rawLLclazz.narrow(genericU);
            jFillMethod.param(fieldClazzU, "dstCollection");

            JClass fieldClazzTU = rowCreatorInterface.narrow(genericT).narrow(genericU);
            jFillMethod.param(fieldClazzTU, "rowCreator");


            //protected void copyTo(IPage1010401 src, IPage1010401 dst) {
            for (Map.Entry<String, JDefinedClass> interf: interfacePageMap.entrySet()) {
                logger.debug("====== " + interf.getKey());
                JMethod jCopyToMethod = jBaseConverterClass.method(PUBLIC, void.class, "copyTo");
                JVar src = jCopyToMethod.param(interf.getValue(), "src");
                JVar dst = jCopyToMethod.param(interf.getValue(), "dst");

                JBlock jBlock = jCopyToMethod.body();
                for (JMethod jInterfaceMethod: interf.getValue().methods()) {
                    if(jInterfaceMethod.name().startsWith("get") || jInterfaceMethod.name().startsWith("is")) {
                        continue;
                    }

                    String fieldName = jInterfaceMethod.name().replace("set", "");
                    jBlock.add(dst.invoke(jInterfaceMethod).arg(src.invoke(getMethodName(fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1), jInterfaceMethod.params().get(0).type()))));
                }
            }

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        }

        return jBaseConverterClass;
    }


    static JDefinedClass generateXmlToRestConverter() {
        JDefinedClass jXmlToRestConverter= null;
        try {

            jXmlToRestConverter = J_MODEL._class(PKG_SERVICE_DTO + "XMLToRestConverter");
            jXmlToRestConverter._extends(jBaseConverterClass);

            JFieldVar jFnoFieldVar = jXmlToRestConverter.field(PRIVATE, xmlFnoClass, "fno");

            // public XMLToRestConverter(
            JMethod jConstructor = jXmlToRestConverter.constructor(PUBLIC);
            JVar jFnoVar = jConstructor.param(xmlFnoClass, "fno");
            JBlock jConstructorBlock = jConstructor.body();
            jConstructorBlock.assign(JExpr.ref(JExpr._this(), jFnoFieldVar.name()), jFnoVar);


            //public Fno convert() {
            JMethod jConvertMethod = jXmlToRestConverter.method(PUBLIC, restFnoClass, "convert");
            JBlock jBlock = jConvertMethod.body();
            jBlock._if(jFnoFieldVar.eq(JExpr._null()))._then()._return(JExpr._null());
            JVar retVal = jBlock.decl(NONE, restFnoClass, "retVal", JExpr._new(restFnoClass));

            for (JMethod jXmlFnoMethod: restFnoClass.methods()) {
                if(jXmlFnoMethod.name().startsWith("get") || !jXmlFnoMethod.name().contains("Form")|| jXmlFnoMethod.name().contains("FormatVersion")) {
                    continue;
                }

                //JType jFromType = jXmlFnoMethod.type();
                jBlock.add(retVal.invoke(jXmlFnoMethod).arg(JExpr._this().invoke("convert" + jXmlFnoMethod.name().replace("set", "")) .arg(jFnoFieldVar.ref(jXmlFnoMethod.name().replace("set", "get") + "()"))));
            }
            jBlock._return(retVal);


            //private Form10104 convertForm10104(kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.xml.Form10104 form10104) {
            for (Map.Entry<String, JFieldVar> xmlFnoFieldVar: xmlFnoClass.fields().entrySet()) {
                JType xmlFormClass = xmlFnoFieldVar.getValue().type();
                JMethod jConvertFormMethod = jXmlToRestConverter.method(PUBLIC, xmlFormClass, "convert" + xmlFormClass.name());
                JType jRestType = J_MODEL.parseType( PKG_SERVICE + "dto.xml." + xmlFormClass.name());
                JVar jFormParam = jConvertFormMethod.param(jRestType, xmlFormClass.name().toLowerCase());


                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, xmlFormClass, "retVal", JExpr._new(xmlFormClass));


                //logger.debug( ToStringBuilder.reflectionToString(xmlFormClassMap, ToStringStyle.SHORT_PREFIX_STYLE));
                System.out.println(xmlFnoFieldVar.getValue().type().name());

                if(!xmlFnoFieldVar.getValue().type().name().contains("Form")) continue;

                for (Map.Entry<String, JFieldVar> eFieldVar: xmlFormClassMap.get(xmlFnoFieldVar.getValue().type().name().replace("List<", "").replace(">", "")).fields().entrySet()) {

                    JType jPageClass = eFieldVar.getValue().type();

                    JType jInitPage  = J_MODEL.parseType( PKG_SERVICE + "dto.rest." + jPageClass.name());
                    JVar jSheetPageVar = jConvertBlock.decl(NONE, jInitPage, eFieldVar.getKey(), JExpr._new(jInitPage));
                    jConvertBlock.add(retVal2.invoke("set" + WordUtils.capitalize(eFieldVar.getKey())).arg(jSheetPageVar));

                    JBlock jIfNullBlock = jConvertBlock._if(jFormParam.invoke("getSheetGroup").invoke("get" + WordUtils.capitalize(eFieldVar.getKey())).ne(JExpr._null()))._then();
                    jIfNullBlock.invoke("copyTo").arg(jFormParam.invoke("getSheetGroup").invoke("get" + WordUtils.capitalize(eFieldVar.getKey()))).arg(jSheetPageVar);

                    if (jPageClass instanceof JDefinedClass) {
                        JFieldVar jPageRowVar = ((JDefinedClass)jPageClass).fields().get("row");
                        if (jPageRowVar != null) {
                            JLambda aLambda = new JLambda();
                            JLambdaParam aParam = aLambda.addParam("srcRow");
                            JBlock jLambdaBlock = aLambda.body();

                            String rowClassName = jPageRowVar.type().fullName().replace("java.util.List<", "").replace(">", "");

                            JVar lambdaRetVal = jLambdaBlock.decl(NONE, J_MODEL.parseType(rowClassName), "retVal1", JExpr._new(J_MODEL.parseType(rowClassName)));
                            jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);


                            jIfNullBlock.invoke("fillPageRows").arg(
                                    jFormParam.invoke("get" + WordUtils.capitalize(eFieldVar.getKey())).invoke("getRow"))
                                    .arg(jSheetPageVar.invoke("getRow")).arg(aLambda);

                        }

                    }
                }

                jConvertBlock._return(retVal2);
            }

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return jXmlToRestConverter;
    }



}
