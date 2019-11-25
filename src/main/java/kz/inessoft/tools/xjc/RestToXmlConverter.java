package kz.inessoft.tools.xjc;

import com.sun.codemodel.*;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.sun.codemodel.JMod.*;
import static com.sun.codemodel.JMod.NONE;
import static kz.inessoft.tools.xjc.Helper.getNameWithoutList;
import static kz.inessoft.tools.xjc.KNPPlugin.*;
import static kz.inessoft.tools.xjc.KNPPlugin.PKG_SERVICE_DTO_XML;


public class RestToXmlConverter {

    private static final Logger logger = LogManager.getLogger(RestToXmlConverter.class);


    static JDefinedClass generate() {
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
            JBlock jConvertMethodBody = jConvertMethod.body();
            jConvertMethodBody._if(jFnoFieldVar.eq(JExpr._null()))._then()._return(JExpr._null());
            JVar retVal = jConvertMethodBody.decl(NONE, xmlFnoClass, "retVal", JExpr._new(xmlFnoClass));

            jConvertMethodBody.add(retVal.invoke("setCode").arg(JExpr.direct(PKG_BASE_WITHOUT_VERSION + ".F" + FORM_CODE_VALUE + "Constants.FORM_CODE")));
            jConvertMethodBody.add(retVal.invoke("setVersion").arg(JExpr.direct("String.valueOf(" + PKG_SERVICE + "V" + FNO_VERSION + "Constants.VERSION)")));
            jConvertMethodBody.add(retVal.invoke("setFormatVersion").arg(JExpr.lit("1")));

            for (JFieldVar fnoField: xmlFnoClass.fields().values()) {
                if(!fnoField.name().contains("form") || fnoField.name().contains("formatVersion")) {
                    continue;
                }

                //retVal.setForm20000(convertForm20000(fno.getForm20000()));
                JInvocation setMethods = null;
                if(fnoField.type().name().contains("List")) {
                    setMethods = retVal.invoke("get" + StringUtils.capitalize(fnoField.name())).invoke("addAll");
                } else {
                    setMethods = retVal.invoke("set" + StringUtils.capitalize(fnoField.name()));
                }
                jConvertMethodBody.add(setMethods.arg(JExpr._this().invoke("convert" + StringUtils.capitalize(fnoField.name()))
                        .arg(jFnoFieldVar.invoke("get" + StringUtils.capitalize(fnoField.name())))));
            }

            jConvertMethodBody._return(retVal);

            //private Form10104 convertForm10104(kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.rest.Form10104 form10104) {
            String argName= "form";
            for (JFieldVar fnoField: xmlFnoClass.fields().values()) {
                JType xmlFormType = fnoField.type();
                if(!xmlFormType.name().contains("Form")) continue;

                boolean isListForm = xmlFormType.fullName().contains("java.util.List");

                String formTypeName = getNameWithoutList(xmlFormType.name());

                JType restFormType = J_MODEL.parseType(PKG_SERVICE_DTO_REST + formTypeName);

                JType returnFormTypeBase = J_MODEL.parseType(PKG_SERVICE_DTO_XML + formTypeName);
                JType returnFormType = returnFormTypeBase;
                JType returnFormTypeInstance = returnFormTypeBase;
                if(isListForm) {
                    restFormType = J_MODEL.ref(List.class).narrow(restFormType);
                    returnFormType = J_MODEL.ref(List.class).narrow(returnFormTypeBase);
                    returnFormTypeInstance = J_MODEL.ref(ArrayList.class).narrow(returnFormTypeBase);
                }

                JMethod jConvertFormMethod = jRestToXmlConverter.method(PUBLIC, returnFormType, "convert" + formTypeName);
                JVar jFormParam = jConvertFormMethod.param(restFormType, argName + (isListForm?"s":""));

                JBlock jConvertBlock = jConvertFormMethod.body();

                if(isListForm) {
                    jConvertBlock._if(jFormParam.eq(JExpr._null()).cor(jFormParam.invoke("isEmpty")))._then()._return(JExpr._null());
                } else {
                    jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                }

                JBlock mainCopyBlock = jConvertBlock;


                JFieldRef jFormParamRef = JExpr.ref(jFormParam.name());
                if(isListForm) {

                    //.map(form20003 -> {})

                    JLambda mapLambda = new JLambda();

                    JVar retValList   = jConvertBlock.decl(NONE, returnFormType, "retValList", jFormParam.invoke("stream")
                            .invoke("map").arg(mapLambda).invoke("collect").arg(JExpr.direct("java.util.stream.Collectors.toList()")));
                    jConvertBlock._return(retValList);

                    JLambdaParam mapLambdaParam = mapLambda.addParam(fnoField.name());
                    mainCopyBlock = mapLambda.body();


                    jFormParamRef = JExpr.ref(mapLambdaParam.name());


                }

                {
                    JVar xmlForm = mainCopyBlock.decl(NONE, returnFormTypeBase, "xmlForm", JExpr._new(returnFormTypeBase));

                    JType jSheetGroupClass = J_MODEL.parseType(returnFormTypeBase.fullName() + ".SheetGroup");
                    JVar sheetGroup = mainCopyBlock.decl(NONE, jSheetGroupClass, "sheetGroup", JExpr._new(jSheetGroupClass));

                    boolean isSheetGroupSet = false;

                    for (JFieldVar formField : xmlFormClassMap.get(formTypeName).fields().values()) {

                        if (!formField.type().name().contains("SheetGroup")) continue;

                        if(!isSheetGroupSet) {
                            if (formField.type().name().contains("List<SheetGroup>")) {
                                mainCopyBlock.add(xmlForm.invoke("getSheetGroup").invoke("add").arg(sheetGroup));
                            } else {
                                mainCopyBlock.add(xmlForm.invoke("setSheetGroup").arg(sheetGroup));
                            }
                            isSheetGroupSet = true;
                        }

                        String sheetGroupClassName = Helper.getNameWithoutList(formField.type().fullName());

                        //JDefinedClass jSheetClass = (JDefinedClass) formField.type();

                        JDefinedClass jSheetClass  = xmlSheetGroupClassMap.get(sheetGroupClassName);


                        for (JFieldVar sheetField : jSheetClass.fields().values()) {
                            JType jPageClass = sheetField.type();

                            logger.debug("  convert " + jPageClass.name());
                            if (!jPageClass.name().contains("Page")) continue;

                            JType pageType = J_MODEL.parseType(PKG_SERVICE_DTO_XML + jPageClass.name());
                            JVar pageVar = mainCopyBlock.decl(NONE, pageType, sheetField.name(), JExpr._new(pageType));
                            mainCopyBlock.add(sheetGroup.invoke("set" + StringUtils.capitalize(sheetField.name())).arg(pageVar));

                            JBlock jIfNullBlock = mainCopyBlock._if(jFormParamRef.invoke("get" + StringUtils.capitalize(sheetField.name())).ne(JExpr._null()))._then();
                            jIfNullBlock.invoke("copyTo").arg(jFormParamRef.invoke("get" + StringUtils.capitalize(sheetField.name()))).arg(pageVar);

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
                                            jFormParamRef.invoke("get" + StringUtils.capitalize(sheetField.name())).invoke("getRow"))
                                            .arg(pageVar.invoke("getRow")).arg(aLambda);

                                }

                            }
                        }
                    }


                    mainCopyBlock._return(xmlForm);
                }
            }

        } catch (JClassAlreadyExistsException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return jRestToXmlConverter;
    }
}
