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
}
