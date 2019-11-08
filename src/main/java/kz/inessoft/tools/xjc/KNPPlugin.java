package kz.inessoft.tools.xjc;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.xsom.*;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.Map.Entry;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.model.CClassInfoParent.Package;


import static com.sun.codemodel.JMod.*;

public class KNPPlugin extends Plugin {

    private static final Logger logger = LogManager.getLogger(KNPPlugin.class);

    private List<JDefinedClass> interfacePageList = new ArrayList<>();

    private JDefinedClass restFnoClass;
    private List<JDefinedClass> restFormClassList = new ArrayList<>();
    private List<JDefinedClass> restPageClassList = new ArrayList<>();

    private JDefinedClass xmlFnoClass;
    private List<JDefinedClass> xmlFormClassList = new ArrayList<>();
    private List<JDefinedClass> xmlPageClassList = new ArrayList<>();


    private  JDefinedClass jBaseConverterClass = null;
    private  JDefinedClass jRestToXmlConverter = null;
    private  JDefinedClass jXmlToRestConverter = null;
    @Override
    public String getOptionName() {
        return "Xknp-generate";
    }

    @Override
    public int parseArgument(Options opt, String[] args, int i)
            throws BadCommandLineException, IOException {
        boolean quiet = false;
        for(String arg : args) {
            if(arg.equals("-onlydto")) {
                logger.debug("Generate only DTO");
            }
            if(arg.equals("-quiet")) {
                quiet = true;
            }
        }
        
        if(quiet) {
            Configurator.setRootLevel(Level.OFF);
        } else {
            Configurator.setRootLevel(Level.DEBUG);
        }
        return 1;
    }

    @Override
    public String getUsage() {
        return "  -Xknp-generate    :  knp xjc plugin for generate jaxb/json from xml";
    }

    @Override
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
            throws SAXException {


        for (ClassOutline classOutline : model.getClasses()) {

            if(classOutline.target.shortName.equals("Fno")) {
                //logger.debug( ToStringBuilder.reflectionToString(cPropertyInfo, ToStringStyle.SHORT_PREFIX_STYLE));
                CPropertyInfo cPropertyInfo = model.getModel().beans().get(classOutline.target).getProperty("code");
                logger.debug(this.getFixedValue(cPropertyInfo));
                CPropertyInfo cPropertyInfo2 = model.getModel().beans().get(classOutline.target).getProperty("version");
                logger.debug(this.getFixedValue(cPropertyInfo2));

                xmlFnoClass = classOutline.implClass;
            } else if(classOutline.target.shortName.startsWith("Form")) {
                xmlFormClassList.add(classOutline.implClass);
            } else  if (classOutline.target.shortName.startsWith("Page")) {
                xmlPageClassList.add(classOutline.implClass);
            }


            try {
                String shorname = classOutline.target.shortName;
                logger.debug("---------------- Short name:" + shorname);
                logger.debug("Type name:");
                if(classOutline.target.getTypeName() != null) {
                    logger.debug(classOutline.target.getTypeName().toString());
                }
                logger.debug("Element name:");
                if(classOutline.target.getElementName() != null) {
                    logger.debug(classOutline.target.getElementName().toString());
                }
                logger.debug("Parent: " + classOutline.target.parent().fullName());
                logger.debug("Parent: " + classOutline.target.parent().getClass().getSimpleName());
                logger.debug("Subclasses: " + classOutline.target.hasSubClasses());


                CClassInfo cClassInfo = classOutline.target;



                if(classOutline.target.shortName.equals("SheetGroup")) {
                    //logger.debug( ToStringBuilder.reflectionToString(classOutline.target.model, ToStringStyle.SHORT_PREFIX_STYLE));
                    logger.debug(classOutline.target.getBaseClass());

                    cClassInfo = (CClassInfo) classOutline.target.parent();
                }



                logger.debug("--2 Short name:" + cClassInfo.shortName);

                if(classOutline.implClass.fields().containsKey("sheetGroup")) {
                    continue;
                }


                JDefinedClass jRestDefinedClass = model.getCodeModel()._class("f421.rest." + cClassInfo.shortName);

                if(jRestDefinedClass.name().equals("Fno")) {
                    restFnoClass = jRestDefinedClass;
                } else if(jRestDefinedClass.name().startsWith("Form")) {
                    restFormClassList.add(jRestDefinedClass);
                } else if (jRestDefinedClass.name().startsWith("Page")) {
                    restPageClassList.add(jRestDefinedClass);
                }


                JDefinedClass jDefinedClassInterface = null;
                if(classOutline.target.shortName.contains("Page")) {
                    jDefinedClassInterface = model.getCodeModel()._class("f421.dto.I" + cClassInfo.shortName, ClassType.INTERFACE);
                    classOutline.implClass._implements(jDefinedClassInterface);

                    jRestDefinedClass._implements(jDefinedClassInterface);

                    interfacePageList.add(jDefinedClassInterface);
                }


                String packageName = "";
                if(classOutline.target.parent() instanceof Package) {
                    packageName = ((Package) classOutline.target.parent()) .fullName();
                } else {
                    if (classOutline.target.parent() instanceof CClassInfo) {
                        packageName = ( (Package)  ((CClassInfo) classOutline.target.parent()).parent()) .fullName();
                    }
                }

                for (Entry<String, JFieldVar> e : classOutline.implClass.fields().entrySet()) {

                    JType jTypeCustom = e.getValue().type();

                    logger.debug("TTTTP  " + jTypeCustom.fullName());
                    logger.debug("TTTTP  " + jTypeCustom.name());
                    logger.debug("TTTTB  " + (jTypeCustom.getClass()));
                    logger.debug("TTTTB  " + (jTypeCustom instanceof JDefinedClass));

                    if(jTypeCustom instanceof JDefinedClass) {
                        JDefinedClass jttt = (JDefinedClass) jTypeCustom;
                        if(jttt.getPackage().name().contains(packageName)) {
                            jTypeCustom = model.getCodeModel()._class( e.getValue().type().name().replace(packageName, "f421.rest."));
                            //jTypeCustom = model.getCodeModel().parseType( e.getValue().type().name().replace(packageName, "f421.rest.")); TODO решить
                        }
                    }


                    JFieldVar  jf = jRestDefinedClass.field(PRIVATE, jTypeCustom, e.getKey());

                    this.generateGetter(model.getCodeModel(), jRestDefinedClass, jTypeCustom, e.getKey(), packageName);
                    this.generateSetter(jRestDefinedClass, jTypeCustom, e.getKey());

                    if(jDefinedClassInterface != null) {
                        this.generateGetter(model.getCodeModel(), jDefinedClassInterface, jTypeCustom, e.getKey(), packageName);
                        this.generateSetter(jDefinedClassInterface, jTypeCustom, e.getKey());
                    }


                    try {
                        JAnnotationUse jAnnotationUse = e.getValue().annotations().iterator().next();

                        JAnnotationUse jAnnotationUse1= jf.annotate(JsonProperty.class);


                        for (Entry<String, JAnnotationValue> an1 : jAnnotationUse.getAnnotationMembers().entrySet()) {
                            String strVal = this.getStringValueOfAnnotation(an1.getValue());
                            if(an1.getKey().equals("required")) {
                                jAnnotationUse1.param(an1.getKey(), Boolean.parseBoolean(strVal));
                            } else {
                                jAnnotationUse1.param(an1.getKey(), strVal);
                            }
                        }

                    } catch (NoSuchElementException e1) {
                        e1.printStackTrace();
                    }

                }

            } catch (JClassAlreadyExistsException e) {
                logger.debug(classOutline.target.shortName);
                e.printStackTrace();
            }

        }


        this.jBaseConverterClass = this.generateBaseConverter(model.getCodeModel(), "f421.dto.");

        this.jRestToXmlConverter = this.generateRestToXmlConverter(model.getCodeModel(), "f421.dto.");

        this.jXmlToRestConverter = this.generateXmlToRestConverter(model.getCodeModel(), "f421.dto.");


        return true;
    }


    private JDefinedClass generateBaseConverter(JCodeModel jCodeModel, String packageName) {
        JDefinedClass jBaseConverterClass = null;
        try {

//            protected interface RowCreator<T, U> {
//                U createRow(T srcRow);
//            }

            jBaseConverterClass = jCodeModel._class(PUBLIC| ABSTRACT, packageName + "BaseV20Converter", ClassType.CLASS);
            JDefinedClass rowCreatorInterface = jBaseConverterClass._class(NONE, "RowCreator", ClassType.INTERFACE);

            rowCreatorInterface.generify("T");
            rowCreatorInterface.generify("U");

            JClass genericT = jCodeModel.ref("T");
            JClass genericU = jCodeModel.ref("U");

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

            JClass rawLLclazz = jCodeModel.ref(List.class);
            JClass fieldClazzT = rawLLclazz.narrow(genericT);
            jFillMethod.param(fieldClazzT, "srcCollection");

            JClass fieldClazzU = rawLLclazz.narrow(genericU);
            jFillMethod.param(fieldClazzU, "dstCollection");

            JClass fieldClazzTU = rowCreatorInterface.narrow(genericT).narrow(genericU);
            jFillMethod.param(fieldClazzTU, "rowCreator");


            //protected void copyTo(IPage1010401 src, IPage1010401 dst) {
            for (JDefinedClass interf: this.interfacePageList) {
                JMethod jCopyToMethod = jBaseConverterClass.method(PUBLIC, void.class, "copyTo");
                JVar src = jCopyToMethod.param(interf, "src");
                JVar dst = jCopyToMethod.param(interf, "dst");

                JBlock jBlock = jCopyToMethod.body();
                for (JMethod jInterfaceMethod: interf.methods()) {
                    if(jInterfaceMethod.name().startsWith("get")) {
                        continue;
                    }
                    jBlock.add(src.invoke(jInterfaceMethod).arg(dst.invoke(jInterfaceMethod.name().replace("set", "get"))));
                }
            }

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        }

        return jBaseConverterClass;
    }

    private JDefinedClass generateRestToXmlConverter(JCodeModel jCodeModel, String packageName) {
        JDefinedClass jRestToXmlConverter= null;
        try {

            jRestToXmlConverter = jCodeModel._class(packageName + "RestToXmlConverter");
            jRestToXmlConverter._extends(this.jBaseConverterClass);

            JFieldVar jFnoFieldVar = jRestToXmlConverter.field(PRIVATE, this.restFnoClass, "fno");

            // public RestToXmlConverter(
            JMethod jConstructor = jRestToXmlConverter.constructor(PUBLIC);
            JVar jFnoVar = jConstructor.param(this.restFnoClass, "fno");
            JBlock jConstructorBlock = jConstructor.body();
            jConstructorBlock.assign(jFnoFieldVar, jFnoVar);


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
            for (JDefinedClass xmlFormClass: this.xmlFormClassList) {
                JMethod jConvertFormMethod = jRestToXmlConverter.method(PUBLIC, xmlFormClass, "convert" + xmlFormClass.name());
                JType jRestType = jCodeModel.parseType( "f421.rest." + xmlFormClass.name());
                JVar jFormParam = jConvertFormMethod.param(jRestType, xmlFormClass.name().toLowerCase());


                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, xmlFormClass, "retVal", JExpr._new(xmlFormClass));

                JFieldVar jSheetGroupVar = xmlFormClass.fields().get("sheetGroup");
                JDefinedClass jSheetGroupClass = (JDefinedClass) jSheetGroupVar.type();

                JVar sheetGroup = jConvertBlock.decl(NONE, jSheetGroupClass, "sheetGroup", JExpr._new(jSheetGroupClass));
                jConvertBlock.add(retVal2.invoke("setSheetGroup").arg(sheetGroup));

                for (Entry<String, JFieldVar> eFieldVar: jSheetGroupClass.fields().entrySet()) {
                    JBlock jIfNullBlock= jConvertBlock._if(jFormParam.invoke("get" + WordUtils.capitalize( eFieldVar.getKey())).eq(JExpr._null()))._then();

                    JDefinedClass jPageClass = (JDefinedClass) eFieldVar.getValue().type();
                    JVar jSheetPageVar = jIfNullBlock.decl(NONE, jPageClass, eFieldVar.getKey(), JExpr._new(jPageClass));
                    jIfNullBlock.add(sheetGroup.invoke("set" + WordUtils.capitalize( eFieldVar.getKey())).arg(jSheetPageVar));

                    jIfNullBlock.invoke("copyTo").arg(jFormParam.invoke("get" +  WordUtils.capitalize( eFieldVar.getKey()))).arg(jSheetPageVar);

                    JFieldVar jPageRowVar = jPageClass.fields().get("row");
                    if(jPageRowVar != null) {

                        JLambda aLambda = new JLambda ();
                        JLambdaParam aParam = aLambda.addParam ("srcRow");
                        JBlock jLambdaBlock =  aLambda.body ();

                        String rowClassName = jPageRowVar.type().fullName().replace("java.util.List<", "").replace(">", "");

                        JVar lambdaRetVal = jLambdaBlock.decl(NONE, jCodeModel.parseType(rowClassName), "retVal1", JExpr._new(jCodeModel.parseType(rowClassName)));
                        jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);



                        jIfNullBlock.invoke("fillPageRows").arg(
                                jFormParam.invoke("get" +  WordUtils.capitalize( eFieldVar.getKey())).invoke("getRow"))
                                .arg(jSheetPageVar.invoke("getRow")).arg(aLambda);

//                                        JExpr.direct(
//                                "srcRow -> {\n" +
//                                "                        " + WordUtils.capitalize( eFieldVar.getKey()) + "Row retVal1 = new " + WordUtils.capitalize( eFieldVar.getKey()) + "Row();\n" +
//                                "                        copyTo(srcRow, retVal1);\n" +
//                                "                        return retVal1;\n" +
//                                "                    }"));


                    }

                }

                jConvertBlock._return(retVal2);
            }

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return jRestToXmlConverter;
    }

    private JDefinedClass generateXmlToRestConverter(JCodeModel jCodeModel, String packageName) {
        JDefinedClass jXmlToRestConverter= null;
        try {

            jXmlToRestConverter = jCodeModel._class(packageName + "XMLToRestConverter");
            jXmlToRestConverter._extends(this.jBaseConverterClass);

            JFieldVar jFnoFieldVar = jXmlToRestConverter.field(PRIVATE, this.xmlFnoClass, "fno");

            // public XMLToRestConverter(
            JMethod jConstructor = jXmlToRestConverter.constructor(PUBLIC);
            JVar jFnoVar = jConstructor.param(this.xmlFnoClass, "fno");
            JBlock jConstructorBlock = jConstructor.body();
            jConstructorBlock.assign(jFnoFieldVar, jFnoVar);


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
            for (JDefinedClass xmlFormClass: this.restFormClassList) {
                JMethod jConvertFormMethod = jXmlToRestConverter.method(PUBLIC, xmlFormClass, "convert" + xmlFormClass.name());
                JType jRestType = jCodeModel.parseType( "f421.rest." + xmlFormClass.name());
                JVar jFormParam = jConvertFormMethod.param(jRestType, xmlFormClass.name().toLowerCase());


                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, xmlFormClass, "retVal", JExpr._new(xmlFormClass));

//                JFieldVar jSheetGroupVar = xmlFormClass.fields().get("sheetGroup");
//                JDefinedClass jSheetGroupClass = (JDefinedClass) jSheetGroupVar.type();

//                JVar sheetGroup = jConvertBlock.decl(NONE, jSheetGroupClass, "sheetGroup", JExpr._new(jSheetGroupClass));
//                jConvertBlock.add(retVal2.invoke("setSheetGroup").arg(sheetGroup));

                for (Entry<String, JFieldVar> eFieldVar: xmlFormClass.fields().entrySet()) {
                    JBlock jIfNullBlock = jConvertBlock._if(jFormParam.invoke("get" + WordUtils.capitalize(eFieldVar.getKey())).eq(JExpr._null()))._then();

                    JType jPageClass = eFieldVar.getValue().type();
                    JVar jSheetPageVar = jIfNullBlock.decl(NONE, jPageClass, eFieldVar.getKey(), JExpr._new(jPageClass));
                    jIfNullBlock.add(retVal2.invoke("set" + WordUtils.capitalize(eFieldVar.getKey())).arg(jSheetPageVar));

                    jIfNullBlock.invoke("copyTo").arg(jFormParam.invoke("get" + WordUtils.capitalize(eFieldVar.getKey()))).arg(jSheetPageVar);

                    if (jPageClass instanceof JDefinedClass) {
                        JFieldVar jPageRowVar = ((JDefinedClass)jPageClass).fields().get("row");
                        if (jPageRowVar != null) {
                            JLambda aLambda = new JLambda();
                            JLambdaParam aParam = aLambda.addParam("srcRow");
                            JBlock jLambdaBlock = aLambda.body();

                            String rowClassName = jPageRowVar.type().fullName().replace("java.util.List<", "").replace(">", "");

                            JVar lambdaRetVal = jLambdaBlock.decl(NONE, jCodeModel.parseType(rowClassName), "retVal1", JExpr._new(jCodeModel.parseType(rowClassName)));
                            jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);


                            jIfNullBlock.invoke("fillPageRows").arg(
                                    jFormParam.invoke("get" + WordUtils.capitalize(eFieldVar.getKey())).invoke("getRow"))
                                    .arg(jSheetPageVar.invoke("getRow")).arg(aLambda);

//                                        JExpr.direct(
//                                "srcRow -> {\n" +
//                                "                        " + WordUtils.capitalize( eFieldVar.getKey()) + "Row retVal1 = new " + WordUtils.capitalize( eFieldVar.getKey()) + "Row();\n" +
//                                "                        copyTo(srcRow, retVal1);\n" +
//                                "                        return retVal1;\n" +
//                                "                    }"));


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

    private void generateGetter(JCodeModel jCodeModel, JDefinedClass jDefinedClass, JType jType, String name, String packageName) {
        JType jListType = jType;

        if(jType.fullName().contains("java.util.List")) {
            try {
                jListType = jCodeModel.parseType(jType.fullName().replace(packageName+".", ""));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        JMethod  jf = jDefinedClass.method(PUBLIC, jListType, "get" + WordUtils.capitalize(name));
        JBlock body = jf.body();

        if(jListType.fullName().contains("java.util.List")) {
            try {
                JType jArrayListType = jCodeModel.parseType("java.util." + jListType.name().replace("List", "ArrayList"));
                body._if(JExpr.ref(JExpr._this(), name).eq(JExpr._null()))
                        ._then().assign(JExpr.ref(JExpr._this(), name),  JExpr._new(jArrayListType));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        body._return(JExpr.ref(JExpr._this(), name));
    }

    private void generateSetter(JDefinedClass jDefinedClass, JType jType, String name) {
        if(jType.fullName().contains("java.util.List")) {
            return;
        }

        JMethod  jf = jDefinedClass.method(PUBLIC, jType, "set" + WordUtils.capitalize(name));

        JVar value = jf.param(jType, name);

        JBlock body = jf.body();

        body.assign(JExpr.ref(JExpr._this(), name), value);

    }


    private String getStringValueOfAnnotation(JAnnotationValue annotationValue) {
        StringWriter s = new StringWriter();
        JFormatter f = new JFormatter(new PrintWriter(s));
        annotationValue.generate(f);
        return s.toString().replace("\"", "");
    }

    private String getFixedValue(CPropertyInfo fieldInfo) {
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