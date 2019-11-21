package kz.inessoft.tools.xjc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XmlString;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

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


            JMethod fillPageRowsMethod = jBaseConverterClass.method(JMod.PUBLIC, void.class,  "fillPageRows");

            JTypeVar T  = fillPageRowsMethod.generify("T");
            JTypeVar U  = fillPageRowsMethod.generify("U");

            JClass rawLLclazz = J_MODEL.ref(List.class);
            JClass fieldClazzT = rawLLclazz.narrow(genericT);
            JVar srcCollection = fillPageRowsMethod.param(fieldClazzT, "srcCollection");

            JClass fieldClazzU = rawLLclazz.narrow(genericU);
            JVar dstCollection = fillPageRowsMethod.param(fieldClazzU, "dstCollection");

            JClass fieldClazzTU = rowCreatorInterface.narrow(genericT).narrow(genericU);
            JVar rowCreator = fillPageRowsMethod.param(fieldClazzTU, "rowCreator");

            JBlock fillPageRowsMethodBlock = fillPageRowsMethod.body();

            JForLoop jForLoop = fillPageRowsMethodBlock._for();
            JVar jForLoopArg = jForLoop.init(J_MODEL.INT, "j", JExpr.lit(0));
            jForLoop.test(jForLoopArg.lt(srcCollection.invoke("size")));
            jForLoop.update(jForLoopArg.incr());

            JBlock jForLoopBody = jForLoop.body();
            JVar row = jForLoopBody.decl(U, "row");
            jForLoopBody.assign(row, rowCreator.invoke("createRow").arg(srcCollection.invoke("get").arg(jForLoopArg)));
            jForLoopBody.add(dstCollection.invoke("add").arg(row));



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


            JType restFnoClass = J_MODEL.parseType(PKG_SERVICE_DTO_REST + StringUtils.capitalize(jFnoVar.name()));
            //public Fno convert() {
            JMethod jConvertMethod = jXmlToRestConverter.method(PUBLIC, restFnoClass, "convert");
            JBlock jBlock = jConvertMethod.body();
            jBlock._if(jFnoFieldVar.eq(JExpr._null()))._then()._return(JExpr._null());
            JVar retVal = jBlock.decl(NONE, restFnoClass, "retVal", JExpr._new(restFnoClass));

            for (JMethod jXmlFnoMethod: xmlFnoClass.methods()) {
                if(jXmlFnoMethod.name().startsWith("get") || !jXmlFnoMethod.name().contains("Form")|| jXmlFnoMethod.name().contains("FormatVersion")) {
                    continue;
                }

                //JType jFromType = jXmlFnoMethod.type();
                jBlock.add(retVal.invoke(jXmlFnoMethod).arg(JExpr._this().invoke("convert" + jXmlFnoMethod.name().replace("set", "")) .arg(jFnoFieldVar.ref(jXmlFnoMethod.name().replace("set", "get") + "()"))));
            }
            jBlock._return(retVal);


            //private Form10104 convertForm10104(kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.xml.Form10104 form10104) {
            for (JFieldVar fnoField: xmlFnoClass.fields().values()) {
                JType xmlFormType = fnoField.type();
                if(!xmlFormType.name().contains("Form")) continue;

                String formTypeName = getNameWithoutList(xmlFormType.name());
                JType returnFormTypeBase = J_MODEL.parseType(PKG_SERVICE_DTO_REST + formTypeName);
                JType returnFormType = returnFormTypeBase;
                JType returnFormTypeInstance = returnFormTypeBase;
                if(xmlFormType.fullName().contains("java.util.List")) {
                    returnFormType = J_MODEL.ref(List.class).narrow(returnFormTypeBase);
                    returnFormTypeInstance = J_MODEL.ref(ArrayList.class).narrow(returnFormTypeBase);
                }

                JMethod jConvertFormMethod = jXmlToRestConverter.method(PUBLIC, returnFormType, "convert" + formTypeName);
                JVar jFormParam = jConvertFormMethod.param(xmlFormType, fnoField.name());

                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, returnFormType, "retVal", JExpr._new(returnFormTypeInstance));

                for (JFieldVar formField: xmlFormClassMap.get(formTypeName).fields().values()) {

                    if(!formField.name().equals("sheetGroup")) continue;

                    JDefinedClass jSheetClass = (JDefinedClass)  formField.type();

                    for (JFieldVar sheetField: jSheetClass.fields().values()) {
                        JType jPageClass = sheetField.type();

                        logger.debug("+++++++ " + jPageClass.name());
                        if (!jPageClass.name().contains("Page")) continue;

                        JType pageType = J_MODEL.parseType(PKG_SERVICE_DTO_REST + jPageClass.name());
                        JVar pageVar = jConvertBlock.decl(NONE, pageType, sheetField.name(), JExpr._new(pageType));
                        jConvertBlock.add(retVal2.invoke("set" + StringUtils.capitalize(sheetField.name())).arg(pageVar));

                        jConvertBlock.invoke("copyTo").arg(jFormParam.invoke("getSheetGroup").invoke("get" + StringUtils.capitalize(sheetField.name()))).arg(pageVar);

                        if (jPageClass instanceof JDefinedClass) {
                            JFieldVar jPageRowVar = ((JDefinedClass) jPageClass).fields().get("row");
                            if (jPageRowVar != null) {
                                JLambda aLambda = new JLambda();
                                JLambdaParam aParam = aLambda.addParam("srcRow");
                                JBlock jLambdaBlock = aLambda.body();

                                JType pageRowType = J_MODEL.parseType(PKG_SERVICE_DTO_REST + pageVar.type().name() + "Row");
                                JVar lambdaRetVal = jLambdaBlock.decl(NONE, pageRowType, "retVal1", JExpr._new(pageRowType));
                                jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);


                                jConvertBlock.invoke("fillPageRows").arg(
                                        jFormParam.invoke("getSheetGroup").invoke("get" + StringUtils.capitalize(sheetField.name())).invoke("getRow"))
                                        .arg(pageVar.invoke("getRow")).arg(aLambda);

                            }

                        }
                    }
                }


                jConvertBlock._return(retVal2);
            }

        } catch (JClassAlreadyExistsException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return jXmlToRestConverter;
    }

    static JDefinedClass generateRestToXmlConverter() {
        JDefinedClass jRestToXmlConverter= null;
        try {

            jRestToXmlConverter = J_MODEL._class(PKG_SERVICE_DTO + "RestToXmlConverter");
            jRestToXmlConverter._extends(jBaseConverterClass);

            JType restFnoClass = J_MODEL.parseType(PKG_SERVICE_DTO_REST + StringUtils.capitalize(xmlFnoClass.name()));

            // public XMLToRestConverter(
            JMethod jConstructor = jRestToXmlConverter.constructor(PUBLIC);
            JVar jFormVar = jConstructor.param(restFnoClass, "form");
            JBlock jConstructorBlock = jConstructor.body();

            JFieldVar jFnoFieldVar = jRestToXmlConverter.field(PRIVATE, restFnoClass, "fno");

            jConstructorBlock.assign(JExpr.ref(JExpr._this(), jFnoFieldVar.name()), jFormVar);


            //public Fno convert() {
            JMethod jConvertMethod = jRestToXmlConverter.method(PUBLIC, xmlFnoClass, "convert");
            JBlock jBlock = jConvertMethod.body();
            jBlock._if(jFnoFieldVar.eq(JExpr._null()))._then()._return(JExpr._null());
            JVar retVal = jBlock.decl(NONE, xmlFnoClass, "retVal", JExpr._new(xmlFnoClass));

            for (JMethod jXmlFnoMethod: xmlFnoClass.methods()) {
                if(jXmlFnoMethod.name().startsWith("get") || !jXmlFnoMethod.name().contains("Form")|| jXmlFnoMethod.name().contains("FormatVersion")) {
                    continue;
                }

                //JType jFromType = jXmlFnoMethod.type();
                jBlock.add(retVal.invoke(jXmlFnoMethod).arg(JExpr._this().invoke("convert" + jXmlFnoMethod.name().replace("set", "")) .arg(jFnoFieldVar.ref(jXmlFnoMethod.name().replace("set", "get") + "()"))));
            }
            jBlock._return(retVal);

            //private Form10104 convertForm10104(kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.rest.Form10104 form10104) {
            for (JFieldVar fnoField: xmlFnoClass.fields().values()) {
                JType xmlFormType = fnoField.type();
                if(!xmlFormType.name().contains("Form")) continue;

                JType retFormType = J_MODEL.parseType(PKG_SERVICE_DTO_REST + xmlFormType.name());
                String formTypeName = getNameWithoutList(xmlFormType.name());
                JType returnFormTypeBase = J_MODEL.parseType(PKG_SERVICE_DTO_XML + formTypeName);
                JType returnFormType = returnFormTypeBase;
                JType returnFormTypeInstance = returnFormTypeBase;
                if(xmlFormType.fullName().contains("java.util.List")) {
                    returnFormType = J_MODEL.ref(List.class).narrow(returnFormTypeBase);
                    returnFormTypeInstance = J_MODEL.ref(ArrayList.class).narrow(returnFormTypeBase);
                }

                JMethod jConvertFormMethod = jRestToXmlConverter.method(PUBLIC, returnFormType, "convert" + formTypeName);
                JVar jFormParam = jConvertFormMethod.param(retFormType, fnoField.name());

                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, returnFormType, "retVal", JExpr._new(returnFormTypeInstance));

                JType jSheetGroupClass = J_MODEL.parseType(returnFormType.fullName() + ".SheetGroup");

                JVar sheetGroup = jConvertBlock.decl(NONE, jSheetGroupClass, "sheetGroup", JExpr._new(jSheetGroupClass));
                retVal2.invoke("setSheetGroup").arg(sheetGroup);

                for (JFieldVar formField: xmlFormClassMap.get(formTypeName).fields().values()) {

                    if(!formField.name().equals("sheetGroup")) continue;

                    JDefinedClass jSheetClass = (JDefinedClass)  formField.type();

                    for (JFieldVar sheetField: jSheetClass.fields().values()) {
                        JType jPageClass = sheetField.type();

                        logger.debug("+++++++ " + jPageClass.name());
                        if (!jPageClass.name().contains("Page")) continue;

                        JType pageType = J_MODEL.parseType(PKG_SERVICE_DTO_XML + jPageClass.name());
                        JVar pageVar = jConvertBlock.decl(NONE, pageType, sheetField.name(), JExpr._new(pageType));
                        jConvertBlock.add(sheetGroup.invoke("set" + StringUtils.capitalize(sheetField.name())).arg(pageVar));

                        JBlock jIfNullBlock= jConvertBlock._if(jFormParam.invoke("get" + StringUtils.capitalize( sheetField.name())).ne(JExpr._null()))._then();
                        jIfNullBlock.invoke("copyTo").arg(jFormParam.invoke("get" + StringUtils.capitalize(sheetField.name()))).arg(pageVar);

                        if (jPageClass instanceof JDefinedClass) {
                            JFieldVar jPageRowVar = ((JDefinedClass) jPageClass).fields().get("row");
                            if (jPageRowVar != null) {
                                JLambda aLambda = new JLambda();
                                JLambdaParam aParam = aLambda.addParam("srcRow");
                                JBlock jLambdaBlock = aLambda.body();

                                JType pageRowType = J_MODEL.parseType(PKG_SERVICE_DTO_XML + pageVar.type().name() + "Row");
                                JVar lambdaRetVal = jLambdaBlock.decl(NONE, pageRowType, "retVal1", JExpr._new(pageRowType));
                                jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);


                                jIfNullBlock.invoke("fillPageRows").arg(
                                        jFormParam.invoke("get" + StringUtils.capitalize(sheetField.name())).invoke("getRow"))
                                        .arg(pageVar.invoke("getRow")).arg(aLambda);

                            }

                        }
                    }
                }


                jConvertBlock._return(retVal2);
            }

        } catch (JClassAlreadyExistsException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return jRestToXmlConverter;
    }


    static String getNameWithoutList(String typeName) {
        return typeName.replace("List<", "").replace(">", "");
    }



}
