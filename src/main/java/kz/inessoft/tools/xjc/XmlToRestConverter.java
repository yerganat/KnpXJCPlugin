package kz.inessoft.tools.xjc;

import com.sun.codemodel.*;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.sun.codemodel.JMod.*;
import static com.sun.codemodel.JMod.NONE;
import static kz.inessoft.tools.xjc.Helper.getNameWithoutList;
import static kz.inessoft.tools.xjc.KNPPluginNew.*;
import static kz.inessoft.tools.xjc.KNPPluginNew.PKG_SERVICE_DTO_REST;

public class XmlToRestConverter {
    static JDefinedClass generate() {
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
                jBlock.add(setMethods.arg(JExpr._this().invoke("convert" + StringUtils.capitalize(fnoField.name()))
                        .arg(jFnoFieldVar.invoke("get" + StringUtils.capitalize(fnoField.name())))));
            }
            jBlock._return(retVal);


            //private Form10104 convertForm10104(kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.xml.Form10104 form[s]) {
            String argName= "form";
            for (JFieldVar fnoField: xmlFnoClass.fields().values()) {
                JType xmlFormType = fnoField.type();
                if(!xmlFormType.name().contains("Form")) continue;

                boolean isListForm = xmlFormType.fullName().contains("java.util.List");

                String formTypeName = getNameWithoutList(xmlFormType.name());
                JType returnFormTypeBase = J_MODEL.parseType(PKG_SERVICE_DTO_REST + formTypeName);
                JType returnFormType = returnFormTypeBase;
                JType returnFormTypeInstance = returnFormTypeBase;

                if(isListForm) {
                    returnFormType = J_MODEL.ref(List.class).narrow(returnFormTypeBase);
                    returnFormTypeInstance = J_MODEL.ref(ArrayList.class).narrow(returnFormTypeBase);
                }

                JMethod jConvertFormMethod = jXmlToRestConverter.method(PUBLIC, returnFormType, "convert" + formTypeName);
                JVar jFormParam = jConvertFormMethod.param(xmlFormType, argName + (isListForm?"s":""));

                JBlock jConvertBlock = jConvertFormMethod.body();

                if(isListForm) {
                    jConvertBlock._if(jFormParam.eq(JExpr._null()).cor(jFormParam.invoke("isEmpty")))._then()._return(JExpr._null());
                } else {
                    jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                }

                JBlock mainCopyBlock = jConvertBlock;


                JFieldRef jFormParamRef = JExpr.ref(jFormParam.name());
                if(isListForm) {

                    //                .filter(form20003 -> form20003.getSheetGroup() != null &&
                    //                        (form20003.getSheetGroup().getPage2000301() != null || form20003.getSheetGroup().getPage2000302() != null))

                    //String filterLambdaParamName = StringUtils.lowerCase(formTypeName);
                    JLambda filterLambda = new JLambda();
                    JLambdaParam filterLambdaParam = filterLambda.addParam(fnoField.name());
                    JBlock filterLambdaBlock = filterLambda.body();

//                    JVar lambdaRetVal = filterLambdaBlock.decl(NONE, J_MODEL.BOOLEAN, "retVal1",
//                            JExpr.ref(filterLambdaParam.name()).invoke("getSheetGroup").ne(JExpr._null())
//                                    .cand(JExpr.ref(filterLambdaParam.name()).invoke("getSheetGroup").invoke("getPageXXX1").ne(JExpr._null()))
//                                    .cor(JExpr.ref(filterLambdaParam.name()).invoke("getSheetGroup").invoke("getPageXXX1").ne(JExpr._null())));

                    filterLambdaBlock._return(JExpr.ref(filterLambdaParam.name()).invoke("getSheetGroup").ne(JExpr._null()));

                    //.map(form20003 -> {})

                    JLambda mapLambda = new JLambda();

                    JVar retValList   = jConvertBlock.decl(NONE, returnFormType, "retValList", jFormParam.invoke("stream").invoke("filter")
                            .arg(filterLambda).invoke("map").arg(mapLambda).invoke("collect").arg(JExpr.direct("java.util.stream.Collectors.toList()")));
                    jConvertBlock._return(retValList);

                    JLambdaParam mapLambdaParam = mapLambda.addParam(fnoField.name());
                    mainCopyBlock = mapLambda.body();


                    jFormParamRef = JExpr.ref(mapLambdaParam.name());


                }


                {
                    JVar restForm = mainCopyBlock.decl(NONE, returnFormTypeBase, "restForm", JExpr._new(returnFormTypeBase));

                    for (JFieldVar formField : xmlFormClassMap.get(formTypeName).fields().values()) {

                        String sheetGroupClassName = Helper.getNameWithoutList(formField.type().fullName());

                        JVar varSheetGroup = null;


                        if( formField.type().name().contains("List<SheetGroup>")) {
                            jConvertBlock._if(jFormParam.invoke("getSheetGroup").eq(JExpr._null()).cor(jFormParam.invoke("getSheetGroup").invoke("isEmpty")))._then()._return(JExpr._null());
                            varSheetGroup = mainCopyBlock.decl(NONE, J_MODEL.parseType(sheetGroupClassName), "sheetGroup", jFormParamRef.invoke("getSheetGroup").invoke("get").arg(JExpr.lit(0)));
                        } else {
                            varSheetGroup = mainCopyBlock.decl(NONE, J_MODEL.parseType(sheetGroupClassName), "sheetGroup", jFormParamRef.invoke("getSheetGroup"));
                        }

                        if (!formField.type().name().contains("SheetGroup")) continue;


                        //JDefinedClass jSheetClass = (JDefinedClass) formField.type();

                        JDefinedClass jSheetClass  = xmlSheetGroupClassMap.get(sheetGroupClassName);



                        for (JFieldVar sheetField : jSheetClass.fields().values()) {
                            JType jPageClass = sheetField.type();

                            logger.debug("+++++++ " + jPageClass.name());
                            if (!jPageClass.name().contains("Page")) continue;

                            JType pageType = J_MODEL.parseType(PKG_SERVICE_DTO_REST + jPageClass.name());
                            JVar pageVar = mainCopyBlock.decl(NONE, pageType, sheetField.name(), JExpr._new(pageType));
                            mainCopyBlock.add(restForm.invoke("set" + StringUtils.capitalize(sheetField.name())).arg(pageVar));

                            JBlock jIfNullBlock = mainCopyBlock._if(varSheetGroup.invoke("get" + StringUtils.capitalize(sheetField.name())).ne(JExpr._null()))._then();
                            jIfNullBlock.invoke("copyTo").arg(varSheetGroup.invoke("get" + StringUtils.capitalize(sheetField.name()))).arg(pageVar);

                            if (jPageClass instanceof JDefinedClass) {
                                JFieldVar jPageRowVar = ((JDefinedClass) jPageClass).fields().get("row");
                                if (jPageRowVar != null) {
                                    JLambda aLambda = new JLambda();
                                    JLambdaParam aParam = aLambda.addParam("srcRow");
                                    JBlock jLambdaBlock = aLambda.body();

                                    JType pageRowType = J_MODEL.parseType(PKG_SERVICE_DTO_REST + pageVar.type().name() + "Row");
                                    JVar lambdaRetVal = jLambdaBlock.decl(NONE, pageRowType, "retVal1", JExpr._new(pageRowType));
                                    jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);

                                    jIfNullBlock.invoke("fillPageRows").arg(
                                            varSheetGroup.invoke("get" + StringUtils.capitalize(sheetField.name())).invoke("getRow"))
                                            .arg(pageVar.invoke("getRow")).arg(aLambda);

                                }

                            }
                        }
                    }


                    mainCopyBlock._return(restForm);
                }
            }

        } catch (JClassAlreadyExistsException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return jXmlToRestConverter;
    }
}
