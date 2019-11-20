package kz.inessoft.tools.xjc;

import java.io.*;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.*;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.xml.xsom.*;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.tools.xjc.model.CClassInfoParent.Package;


import javax.xml.bind.annotation.XmlRootElement;

import static com.sun.codemodel.JMod.*;

public class KNPPlugin extends Plugin {

    private static Boolean GENERATE_ONLY_DTO = false;

    private static String PKG_BASE = "kz.inessoft.sono.app.fno.";
    private static String PKG_FNO = "f";
    private static String PKG_FNO_WTIH_VERSION = "";
    private static String PKG_FNO1 = "";
    private static String PKG_FNO2= "";
    private static String PKG_SERVICE = "kz.inessoft.sono.app.fno.f421.v19.services.";
    //private static String PKG = "kz.";

    private File targetDir;

    private static final Logger logger = LogManager.getLogger(KNPPlugin.class);

    private JCodeModel J_MODEL;

    private String FORM_CODE;

    private String FNO_VERSION;

    private List<JDefinedClass> interfacePageList = new ArrayList<>();

    private JDefinedClass restFnoClass;
    private List<JDefinedClass> restFormClassList = new ArrayList<>();
    private List<JDefinedClass> restPageClassList = new ArrayList<>();

    private JDefinedClass xmlFnoClass;
    private List<JDefinedClass> xmlFormClassList = new ArrayList<>();
    Map<String, JDefinedClass> xmlFormClassMap = new HashMap<String, JDefinedClass>();
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
                GENERATE_ONLY_DTO = true;
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

        if(true) return false;
        this.targetDir = opt.targetDir;
        //logger.debug( ToStringBuilder.reflectionToString(opt, ToStringStyle.SHORT_PREFIX_STYLE));
        this.J_MODEL = model.getCodeModel();

        for (ClassOutline classOutline : model.getClasses()) {
            if(classOutline.target.shortName.equals("Fno")) {
                CPropertyInfo cPropertyInfo = model.getModel().beans().get(classOutline.target).getProperty("code");
                if(cPropertyInfo != null) {
                    this.FORM_CODE = this.getFixedValue(cPropertyInfo);
                }
                logger.debug(this.FORM_CODE);

                CPropertyInfo cPropertyInfo2 = model.getModel().beans().get(classOutline.target).getProperty("version");
                if(cPropertyInfo2 != null) {
                    this.FNO_VERSION = this.getFixedValue(cPropertyInfo2);
                }
                logger.debug(this.FNO_VERSION);
            }
        }


        {
            if(this.FORM_CODE != null) {

                String[] codeParts = this.FORM_CODE.split("\\.");
                if(codeParts.length > 1) {
                    PKG_FNO1 = codeParts[0];
                    PKG_FNO = PKG_FNO +  PKG_FNO1;

                    if(!codeParts[1].equals("00")) {
                        PKG_FNO2 = codeParts[2];
                        PKG_FNO = PKG_FNO + ".app" + PKG_FNO2;
                    }
                } else {
                    PKG_FNO = PKG_FNO + this.FORM_CODE;
                }

                //logger.debug( ToStringBuilder.reflectionToString(codeParts, ToStringStyle.SHORT_PREFIX_STYLE));

                if(this.FNO_VERSION != null) {
                    PKG_FNO_WTIH_VERSION = PKG_FNO + ".v" + this.FNO_VERSION;
                } else {
                    PKG_FNO_WTIH_VERSION = PKG_FNO;
                }

                PKG_SERVICE = PKG_BASE + PKG_FNO_WTIH_VERSION + ".services.";


            }

            System.out.println(PKG_SERVICE);

        }


        this.generateMocks();

        //System.exit(1);

        for (ClassOutline classOutline : model.getClasses()) {

            if(classOutline.target.shortName.equals("Fno")) {
                //logger.debug( ToStringBuilder.reflectionToString(cPropertyInfo, ToStringStyle.SHORT_PREFIX_STYLE));
                xmlFnoClass = classOutline.implClass;
                JAnnotationUse jAnnotationForRow = classOutline.implClass.annotate(XmlRootElement.class);
                jAnnotationForRow.param("name", "fno");
            } else if(classOutline.target.shortName.startsWith("Form")) {
                xmlFormClassList.add(classOutline.implClass);
                xmlFormClassMap.put(classOutline.implClass.name(), classOutline.implClass);
            } else  if (classOutline.target.shortName.startsWith("Page")) {
                xmlPageClassList.add(classOutline.implClass);
            }


            try {
                String shorname = classOutline.target.shortName;
                logger.debug("Class name " + shorname);
                if(classOutline.target.getTypeName() != null) {
                    logger.debug("  attr name in parent " + classOutline.target.getTypeName().toString());
                }

                CClassInfo cClassInfo = classOutline.target;

                if(classOutline.implClass.fields().containsKey("sheetGroup")) { //Если есть параметр sheetGroup то пропускаем, так как Json атрибуты сгенерирована в классе SheetGroup
                    logger.debug("  skip... ");
                    continue;
                }

                if(classOutline.target.shortName.equals("SheetGroup")) { //Если класс SheetGroup то генерируем на его Parent класс Json атрибуты
                    //logger.debug( ToStringBuilder.reflectionToString(classOutline.target.model, ToStringStyle.SHORT_PREFIX_STYLE));
                    cClassInfo = (CClassInfo) classOutline.target.parent();
                }

                logger.debug("  to be generated fields for " + cClassInfo.shortName);


                JDefinedClass restClass = this.J_MODEL._class(PKG_SERVICE + "dto.rest." + cClassInfo.shortName);

                if(restClass.name().equals("Fno")) {
                    restFnoClass = restClass;
                } else if(restClass.name().startsWith("Form")) {
                    restFormClassList.add(restClass);
                } else if (restClass.name().startsWith("Page")) {
                    restPageClassList.add(restClass);
                }


                JDefinedClass commonInterface = null;
                if(classOutline.target.shortName.contains("Page")) {
                    commonInterface = this.J_MODEL._class(PKG_SERVICE + "dto.I" + cClassInfo.shortName, ClassType.INTERFACE);
                    //public interface IPage1010400101<T extends IPage1010400101Row> { TODO сделать extend для Rows


                    classOutline.implClass._implements(commonInterface);

                    if(classOutline.implClass.fields().get("row") == null) {
                        restClass._implements(commonInterface);
                    } else {
                        restClass._implements(commonInterface.narrow(this.J_MODEL._class(PKG_SERVICE + "dto." + "I" + restClass.name() + "Row")));
                    }

                    interfacePageList.add(commonInterface);
                }


                String xmlPackageName = "";
                if(classOutline.target.parent() instanceof Package) {
                    xmlPackageName = ((Package) classOutline.target.parent()) .fullName();
                } else { //Получение имя пакета из SheetGroup
                    if (classOutline.target.parent() instanceof CClassInfo) {
                        xmlPackageName = ( (Package)  ((CClassInfo) classOutline.target.parent()).parent()) .fullName();
                    }
                }

                for (Entry<String, JFieldVar> fieldVarEntry : classOutline.implClass.fields().entrySet()) {

                    String fieldName = fieldVarEntry.getKey();
                    JType fieldType = fieldVarEntry.getValue().type();

                    if(fieldName.equals("code") || fieldName.equals("version") || fieldName.equals("formatVersion")) {
                        continue;
                    }
//                    logger.debug("      xml field " + fieldType.fullName() + " " + fieldName);

//                    logger.debug("for extra debug  " + fieldType.name());
//                    logger.debug("for extra debug   " + (fieldType.getClass()));

                    if (fieldType instanceof JDefinedClass) {
                        JDefinedClass generatedByXmlClassType = (JDefinedClass) fieldType;
                        if (generatedByXmlClassType.getPackage().name().contains(xmlPackageName)) { //Меняем пакет в наших кастомных генерируемых классах
                            fieldType = this.J_MODEL.parseType(fieldType.name().replace(xmlPackageName, PKG_SERVICE + "dto.rest."));
                        }
                    }

                    if(fieldType.fullName().contains("java.util.List<")) {
                        String rowFieldClassName = fieldType.name().replace("List<", "").replace(">", "");
                        JClass rawLLclazz = this.J_MODEL.ref(List.class);
                        fieldType = rawLLclazz.narrow(this.J_MODEL.parseType(rowFieldClassName));
                    }


                    JFieldVar jf = restClass.field(PRIVATE, fieldType, fieldName);
                    logger.debug("      gen field " + jf.type().fullName() + " " + jf.name());

                    this.generateGetter(restClass, fieldType, fieldName, false);
                    this.generateSetter(restClass, fieldType, fieldName, false);

                    if (commonInterface != null) {
                        this.generateGetter(commonInterface, fieldType, fieldName, true);
                        this.generateSetter(commonInterface, fieldType, fieldName, true);

                        if(fieldName.equals("row")) {
                            commonInterface.generify("T", this.J_MODEL._class(PKG_SERVICE + "dto." + commonInterface.name() + "Row"));
                        }
                    }



                    if(fieldName.equals("row")) {
                        JAnnotationUse jAnnotationForRow = jf.annotate(JsonProperty.class);
                        jAnnotationForRow.param("value", "rows");
                        logger.debug("          annotate  JsonProperty(\"rows\")");
                    } else {
                        /**
                         * Генерирует аннотацию JsonProperty по XmlElement
                         */
                        for (Iterator<JAnnotationUse> jAnnotationUseIterator = fieldVarEntry.getValue().annotations().iterator(); jAnnotationUseIterator.hasNext(); ) {
                            JAnnotationUse jAnnotationUse = jAnnotationUseIterator.next();

                            if (!jAnnotationUse.getAnnotationClass().name().equals("XmlElement")) {
                                //logger.debug("      skip " + jAnnotationUse.getAnnotationClass().name());
                                continue;
                            }

                            JAnnotationUse jAnnotationForField = jf.annotate(JsonProperty.class);
                            JAnnotationValue jAnnotationValue = jAnnotationUse.getAnnotationMembers().get("name");

                            if (jAnnotationValue != null) {
                                String annotateValueStr = this.getStringValueOfAnnotation(jAnnotationValue);
                                logger.debug("          annotate  JsonProperty(\"" + annotateValueStr + "\")");

                                jAnnotationForField.param("value", annotateValueStr);
                            }
                        }
                    }



                }

            } catch (JClassAlreadyExistsException e) {
                logger.debug(classOutline.target.shortName);
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }


        this.jBaseConverterClass = this.generateBaseConverter(PKG_SERVICE + "dto.");

        this.jRestToXmlConverter = this.generateRestToXmlConverter(PKG_SERVICE + "dto.");

        this.jXmlToRestConverter = this.generateXmlToRestConverter(PKG_SERVICE + "dto.");


        return true;
    }


    private JDefinedClass generateBaseConverter(String packageName) {
        JDefinedClass jBaseConverterClass = null;
        try {

//            protected interface RowCreator<T, U> {
//                U createRow(T srcRow);
//            }

            jBaseConverterClass = this.J_MODEL._class(PUBLIC| ABSTRACT, packageName + "BaseV20Converter", ClassType.CLASS);
            JDefinedClass rowCreatorInterface = jBaseConverterClass._class(NONE, "RowCreator", ClassType.INTERFACE);

            rowCreatorInterface.generify("T");
            rowCreatorInterface.generify("U");

            JClass genericT = this.J_MODEL.ref("T");
            JClass genericU = this.J_MODEL.ref("U");

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

            JClass rawLLclazz = this.J_MODEL.ref(List.class);
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
                    if(jInterfaceMethod.name().startsWith("get") || jInterfaceMethod.name().startsWith("is")) {
                        continue;
                    }

                    String fieldName = jInterfaceMethod.name().replace("set", "");
                    jBlock.add(src.invoke(jInterfaceMethod).arg(dst.invoke(this.getMethodName(fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1), jInterfaceMethod.params().get(0).type()))));
                }
            }

        } catch (JClassAlreadyExistsException e) {
            e.printStackTrace();
        }

        return jBaseConverterClass;
    }

    private JDefinedClass generateRestToXmlConverter(String packageName) {
        JDefinedClass jRestToXmlConverter= null;
        try {

            jRestToXmlConverter = this.J_MODEL._class(packageName + "RestToXmlConverter");
            jRestToXmlConverter._extends(this.jBaseConverterClass);

            JFieldVar jFnoFieldVar = jRestToXmlConverter.field(PRIVATE, this.restFnoClass, "fno");

            // public RestToXmlConverter(
            JMethod jConstructor = jRestToXmlConverter.constructor(PUBLIC);
            JVar jFnoVar = jConstructor.param(this.restFnoClass, "fno");
            JBlock jConstructorBlock = jConstructor.body();
            jConstructorBlock.assign(JExpr.ref(JExpr._this(), jFnoFieldVar.name()), jFnoVar);


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
                JType jRestType = this.J_MODEL.parseType( PKG_SERVICE + "dto.rest." + xmlFormClass.name());
                JVar jFormParam = jConvertFormMethod.param(jRestType, xmlFormClass.name().toLowerCase());


                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, xmlFormClass, "retVal", JExpr._new(xmlFormClass));

                JFieldVar jSheetGroupVar = xmlFormClass.fields().get("sheetGroup");
                JDefinedClass jSheetGroupClass = (JDefinedClass) jSheetGroupVar.type();

                JVar sheetGroup = jConvertBlock.decl(NONE, jSheetGroupClass, "sheetGroup", JExpr._new(jSheetGroupClass));
                jConvertBlock.add(retVal2.invoke("setSheetGroup").arg(sheetGroup));

                for (Entry<String, JFieldVar> eFieldVar: jSheetGroupClass.fields().entrySet()) {

                    JDefinedClass jPageClass = (JDefinedClass) eFieldVar.getValue().type();
                    JVar jSheetPageVar = jConvertBlock.decl(NONE, jPageClass, eFieldVar.getKey(), JExpr._new(jPageClass));
                    jConvertBlock.add(sheetGroup.invoke("set" + WordUtils.capitalize( eFieldVar.getKey())).arg(jSheetPageVar));

                    JBlock jIfNullBlock= jConvertBlock._if(jFormParam.invoke("get" + WordUtils.capitalize( eFieldVar.getKey())).ne(JExpr._null()))._then();
                    jIfNullBlock.invoke("copyTo").arg(jFormParam.invoke("get" +  WordUtils.capitalize( eFieldVar.getKey()))).arg(jSheetPageVar);

                    JFieldVar jPageRowVar = jPageClass.fields().get("row");
                    if(jPageRowVar != null) {

                        JLambda aLambda = new JLambda ();
                        JLambdaParam aParam = aLambda.addParam ("srcRow");
                        JBlock jLambdaBlock =  aLambda.body ();

                        String rowClassName = jPageRowVar.type().fullName().replace("java.util.List<", "").replace(">", "");

                        JVar lambdaRetVal = jLambdaBlock.decl(NONE, this.J_MODEL.parseType(rowClassName), "retVal1", JExpr._new(this.J_MODEL.parseType(rowClassName)));
                        jLambdaBlock.add(JExpr._this().invoke("copyTo").arg(JExpr.ref(aParam.name())).arg(lambdaRetVal))._return(lambdaRetVal);


                        jIfNullBlock.invoke("fillPageRows").arg(
                                jFormParam.invoke("get" +  WordUtils.capitalize( eFieldVar.getKey())).invoke("getRow"))
                                .arg(jSheetPageVar.invoke("getRow")).arg(aLambda);
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

    private JDefinedClass generateXmlToRestConverter(String packageName) {
        JDefinedClass jXmlToRestConverter= null;
        try {

            jXmlToRestConverter = this.J_MODEL._class(packageName + "XMLToRestConverter");
            jXmlToRestConverter._extends(this.jBaseConverterClass);

            JFieldVar jFnoFieldVar = jXmlToRestConverter.field(PRIVATE, this.xmlFnoClass, "fno");

            // public XMLToRestConverter(
            JMethod jConstructor = jXmlToRestConverter.constructor(PUBLIC);
            JVar jFnoVar = jConstructor.param(this.xmlFnoClass, "fno");
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
            for (Entry<String, JFieldVar> xmlFnoFieldVar: xmlFnoClass.fields().entrySet()) {
                JType xmlFormClass = xmlFnoFieldVar.getValue().type();
                JMethod jConvertFormMethod = jXmlToRestConverter.method(PUBLIC, xmlFormClass, "convert" + xmlFormClass.name());
                JType jRestType = this.J_MODEL.parseType( PKG_SERVICE + "dto.xml." + xmlFormClass.name());
                JVar jFormParam = jConvertFormMethod.param(jRestType, xmlFormClass.name().toLowerCase());


                JBlock jConvertBlock = jConvertFormMethod.body();
                jConvertBlock._if(jFormParam.eq(JExpr._null()))._then()._return(JExpr._null());
                JVar retVal2 = jConvertBlock.decl(NONE, xmlFormClass, "retVal", JExpr._new(xmlFormClass));


                //logger.debug( ToStringBuilder.reflectionToString(xmlFormClassMap, ToStringStyle.SHORT_PREFIX_STYLE));
                System.out.println(xmlFnoFieldVar.getValue().type().name());

                if(!xmlFnoFieldVar.getValue().type().name().contains("Form")) continue;

                for (Entry<String, JFieldVar> eFieldVar: xmlFormClassMap.get(xmlFnoFieldVar.getValue().type().name().replace("List<", "").replace(">", "")).fields().entrySet()) {

                    JType jPageClass = eFieldVar.getValue().type();

                    JType jInitPage  = this.J_MODEL.parseType( PKG_SERVICE + "dto.rest." + jPageClass.name());
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

                            JVar lambdaRetVal = jLambdaBlock.decl(NONE, this.J_MODEL.parseType(rowClassName), "retVal1", JExpr._new(this.J_MODEL.parseType(rowClassName)));
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


    private String getMethodName(String fieldName, JType fieldType) {
        String getMethodName = "get";
        if(fieldType.name().equals("Boolean")) {
            getMethodName = "is";
        }

        return getMethodName + WordUtils.capitalize(fieldName);
    }
    private void generateGetter(JDefinedClass restClass, JType fieldType, String fieldName, boolean isInterface) {
        if(isInterface) {
            restClass.method(PUBLIC,
                    (fieldName.equals("row") ? (this.J_MODEL.ref(List.class).narrow(this.J_MODEL.ref("T"))): fieldType),
                    this.getMethodName(fieldName, fieldType));
            return;
        }

        JMethod  jf = restClass.method(PUBLIC, fieldType, this.getMethodName(fieldName, fieldType));

        JBlock body = jf.body();

        if(fieldType.fullName().contains("java.util.List")) {
            try {
                JType jArrayListType = this.J_MODEL.parseType("java.util." + fieldType.name().replace("List", "ArrayList"));
                body._if(JExpr.ref(JExpr._this(), fieldName).eq(JExpr._null()))
                        ._then().assign(JExpr.ref(JExpr._this(), fieldName),  JExpr._new(jArrayListType));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        body._return(JExpr.ref(JExpr._this(), fieldName));
    }

    private void generateSetter(JDefinedClass restClass, JType fieldType, String name, boolean isInterface) {
        if(fieldType.fullName().contains("java.util.List")) {
            return;
        }

        JMethod  jf = restClass.method(PUBLIC, void.class, "set" + WordUtils.capitalize(name));

        JVar value = jf.param(fieldType, name);

        if(isInterface) return;
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


    private void generateMocks() {
        if(!GENERATE_ONLY_DTO) {
            this.genMockResourceContent(PKG_FNO_WTIH_VERSION + ".rest.", "VXXRestController.java", "rest/");

            this.genMockResourceContent(PKG_FNO_WTIH_VERSION + ".services.", "VXXChargeInfoBuilder.java", "services/");
            this.genMockResourceContent(PKG_FNO_WTIH_VERSION + ".services.", "VXXConstants.java", "services/");
            this.genMockResourceContent(PKG_FNO_WTIH_VERSION + ".services.", "VXXFLKProcessor.java", "services/");
            this.genMockResourceContent(PKG_FNO_WTIH_VERSION + ".services.", "VXXService.java", "services/");
            this.genMockResourceContent(PKG_FNO_WTIH_VERSION + ".services.", "VXXUtils.java", "services/");

            this.genMockResourceContent(PKG_FNO + ".", "FXXXApplication.java", "");
            this.genMockResourceContent(PKG_FNO + ".", "FXXXChargeCallback.java", "");
            this.genMockResourceContent(PKG_FNO + ".", "FXXXConfiguration.java", "");
            this.genMockResourceContent(PKG_FNO + ".", "FXXXConstants.java", "");
        }
    }


    private  void genMockResourceContent(String addPkg, String resName, String subDir) {
        try {
            String restDir = this.targetDir.getAbsolutePath() + File.separator + (PKG_BASE+ addPkg).replace('.', File.separatorChar);
            new File(restDir).mkdirs();
            String restPath = restDir + resName.replace("VXX", "V" + this.FNO_VERSION ).replace("FXXX", "F" + this.PKG_FNO1 );

            logger.debug("Gen file " + restPath);


            InputStream inputStream = getClass().getResourceAsStream("/mock/" + subDir + resName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String restController = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            restController = restController
                    .replace("fXXX", "f" +PKG_FNO1)
                    .replace("FXXX", "F" +PKG_FNO1)
                    .replace("vXX", "v"+FNO_VERSION)
                    .replace("VXX", "V"+FNO_VERSION)
                    .replace("x_form_path", FORM_CODE)
                    .replace("x_fno_version", "v" + FNO_VERSION)
                    .replace("x_only_fno_version", FNO_VERSION);
            Files.write( Paths.get(restPath), restController.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}