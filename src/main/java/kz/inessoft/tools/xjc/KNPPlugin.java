package kz.inessoft.tools.xjc;

import com.sun.codemodel.*;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

import static com.sun.codemodel.JMod.*;
import static kz.inessoft.tools.xjc.Helper.getInterfacePageRow;

public class KNPPlugin extends Plugin {

    static Boolean GENERATE_ONLY_DTO = false;

    static String FORM_CODE = ""; //710.00
    static String FNO_VERSION = ""; //22

    static String FORM_CODE_VALUE = ""; //710
    static String FORM_APP_VALUE = ""; //00

    static String PKG_BASE_WITHOUT_VERSION = "kz.inessoft.sono.app.fno"; //kz.inessoft.sono.app.fno.f710.
    static String PKG_BASE = "kz.inessoft.sono.app.fno"; //kz.inessoft.sono.app.fno.f710.v22
    static String PKG_REST = ""; //kz.inessoft.sono.app.fno.f710.v22.rest.
    static String PKG_SERVICE = ""; //kz.inessoft.sono.app.fno.f710.v22.services.
    static String PKG_SERVICE_DTO = ""; //kz.inessoft.sono.app.fno.f710.v22.services.dto.
    static String PKG_SERVICE_DTO_REST = ""; //kz.inessoft.sono.app.fno.f710.v22.services.dto.rest.
    static String PKG_SERVICE_DTO_XML = ""; //kz.inessoft.sono.app.fno.f710.v22.services.dto.xml.
    static String PKG_SERVICE_FLK = ""; //kz.inessoft.sono.app.fno.f710.v22.services.flk.

    static File targetDir;

    public static final Logger logger = LogManager.getLogger(KNPPlugin.class);

    static  JCodeModel J_MODEL;

    static JDefinedClass xmlFnoClass;
    static Map<String, JDefinedClass> xmlFormClassMap = new HashMap<String, JDefinedClass>();
    static Map<String, JDefinedClass> xmlPageClassMap = new HashMap<>();
    static Map<String, JDefinedClass> xmlSheetGroupClassMap = new HashMap<>();
    static Map<String, JDefinedClass> interfacePageMap = new HashMap<>();

//    static JDefinedClass restFnoClass;
//    static Map<String, JDefinedClass> restFormClassMap = new HashMap<String, JDefinedClass>();
//    static Map<String, JDefinedClass> restPageClassMap = new HashMap<>();

    static JDefinedClass jBaseConverterClass = null;

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
        this.targetDir = opt.targetDir;
        //logger.debug( ToStringBuilder.reflectionToString(opt, ToStringStyle.SHORT_PREFIX_STYLE));
        J_MODEL = model.getCodeModel();

        for (ClassOutline classOutline : model.getClasses()) {
            if(classOutline.target.shortName.equals("Fno")) {
                CPropertyInfo cPropertyInfo = model.getModel().beans().get(classOutline.target).getProperty("code");
                if(cPropertyInfo != null) {
                    FORM_CODE = Helper.getFixedValue(cPropertyInfo);
                }
                logger.debug(FORM_CODE);

                CPropertyInfo cPropertyInfo2 = model.getModel().beans().get(classOutline.target).getProperty("version");
                if(cPropertyInfo2 != null) {
                    FNO_VERSION = Helper.getFixedValue(cPropertyInfo2);
                }
                logger.debug(FNO_VERSION);
            }
        }


        {
            if(StringUtils.isNotBlank(FORM_CODE)) {

                String[] codeParts = FORM_CODE.split("\\.");
                if(codeParts.length > 1) {
                    FORM_CODE_VALUE = codeParts[0];

                    if(!codeParts[1].equals("00")) {
                        FORM_APP_VALUE = codeParts[1];
                    }
                }


                //logger.debug( ToStringBuilder.reflectionToString(codeParts, ToStringStyle.SHORT_PREFIX_STYLE));


                PKG_BASE =  PKG_BASE + ".f" + FORM_CODE_VALUE;

                if(StringUtils.isNotBlank(FORM_APP_VALUE)) {
                    PKG_BASE =  PKG_BASE + ".app" + FORM_APP_VALUE;
                }

                PKG_BASE_WITHOUT_VERSION = PKG_BASE;
                if(StringUtils.isNotBlank(FNO_VERSION)) {
                    PKG_BASE =  PKG_BASE + ".v" + FNO_VERSION;
                }
            }

            PKG_REST = PKG_BASE + ".rest.";
            PKG_SERVICE = PKG_BASE + ".services.";
            PKG_SERVICE_DTO = PKG_SERVICE + "dto.";
            PKG_SERVICE_DTO_REST = PKG_SERVICE_DTO + "rest.";
            PKG_SERVICE_DTO_XML = PKG_SERVICE_DTO + "xml.";
            PKG_SERVICE_FLK = PKG_SERVICE + "flk.";

            logger.debug(PKG_BASE);
        }


        for (ClassOutline classOutline : model.getClasses()) {
            JDefinedClass currentClass = classOutline.implClass;
            CClassInfo cClassInfo = classOutline.target;

            logger.debug("Class name " + cClassInfo.shortName);

            if(cClassInfo.shortName.equals("Fno")) {
                xmlFnoClass = currentClass;
                JAnnotationUse jAnnotationForRow = currentClass.annotate(XmlRootElement.class);
                jAnnotationForRow.param("name", "fno");
            } else if(cClassInfo.shortName.startsWith("Form")) {
                xmlFormClassMap.put(currentClass.name(), currentClass);
            } else  if (cClassInfo.shortName.startsWith("Page")) {
                xmlPageClassMap.put(currentClass.name(), currentClass);
            } else  if (cClassInfo.shortName.startsWith("SheetGroup")) {
                logger.debug("Class name " + currentClass.fullName());
                xmlSheetGroupClassMap.put(currentClass.fullName(), currentClass);
            }

            if(currentClass.fields().containsKey("sheetGroup")) { //Если есть параметр sheetGroup то пропускаем(Это Form класс), так как Json атрибуты сгенерирована в классе SheetGroup
                logger.debug("  skip... ");
                continue;
            }

            if(cClassInfo.shortName.equals("SheetGroup")) { //Если класс SheetGroup то генерируем на его Parent класс Json атрибуты
                cClassInfo = (CClassInfo) cClassInfo.parent();
            }

            try {
                JDefinedClass restClass = J_MODEL._class(PKG_SERVICE_DTO_REST + cClassInfo.shortName);


//                if(restClass.name().equals("Fno")) {
//                    restFnoClass = restClass;
//                } else if(restClass.name().startsWith("Form")) {
//                    restFormClassMap.put(cClassInfo.shortName, restClass);
//                } else if (restClass.name().startsWith("Page")) {
//                    restPageClassMap.put(cClassInfo.shortName, restClass);
//                }


                JDefinedClass commonInterface = null;
                if(cClassInfo.shortName.contains("Page")) {
                    commonInterface = Helper.implementInterface(interfacePageMap, currentClass, restClass, cClassInfo.shortName);
                }


                String xmlPackageName = "";
                if(cClassInfo.parent() instanceof Package) {
                    xmlPackageName = ((Package) cClassInfo.parent()) .fullName();
                } else { //Получение имя пакета из SheetGroup
                    if (cClassInfo.parent() instanceof CClassInfo) {
                        xmlPackageName = ( (Package)  ((CClassInfo) cClassInfo.parent()).parent()) .fullName();
                    }
                }


                logger.debug("  to be generated fields for " + cClassInfo.shortName);


                for (Entry<String, JFieldVar> fieldVarEntry : currentClass.fields().entrySet()) {

                    String fieldName = fieldVarEntry.getKey();
                    JType fieldType = fieldVarEntry.getValue().type();

                    if (fieldName.equals("code") || fieldName.equals("version") || fieldName.equals("formatVersion")) {
                        continue;
                    }

                    JType restFieldType = Helper.getRestFieldType(fieldType, xmlPackageName);
                    JFieldVar restField = restClass.field(PRIVATE, restFieldType, fieldName);
                    logger.debug("      gen field " + restField.type().fullName() + " " + restField.name());

                    Helper.generateGetter(restClass, restFieldType, fieldName, false);
                    Helper.generateSetter(restClass, restFieldType, fieldName, false);

                    if (commonInterface != null) {
                        Helper.generateGetter(commonInterface, restFieldType, fieldName, true);
                        Helper.generateSetter(commonInterface, restFieldType, fieldName, true);

                        if(fieldName.equals("row")) {
                            commonInterface.generify("T", getInterfacePageRow(interfacePageMap, commonInterface.name() + "Row"));
                        }
                    }

                    Helper.annotateWithJsonProperty(restField, fieldName, fieldVarEntry.getValue().annotations());

                }

            }  catch (JClassAlreadyExistsException | ClassNotFoundException e) {
                e.printStackTrace();
            }

        }


        jBaseConverterClass = Helper.generateBaseConverter(interfacePageMap);
        XmlToRestConverter.generate();
        RestToXmlConverter.generate();
        new MockGenerator().generateMocks();

        return true;
    }

}