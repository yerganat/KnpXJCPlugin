package kz.inessoft.tools.xjc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.codemodel.*;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.model.CClassInfo;
import com.sun.tools.xjc.model.CClassInfoParent.Package;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XmlString;
import kz.inessoft.tools.xjc.ext.JLambda;
import kz.inessoft.tools.xjc.ext.JLambdaParam;
import kz.inessoft.tools.xjc.util.Helper;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.sun.codemodel.JMod.*;

public class KNPPluginNew extends Plugin {

    private static Boolean GENERATE_ONLY_DTO = false;

    private static String FORM_CODE = "";
    private static String FNO_VERSION = "";

    private static String FORM_CODE_VALUE = "";
    private static String FNO_VERSION_VALUE = "";

    private static String PKG_BASE = "kz.inessoft.sono.app.fno."; //kz.inessoft.sono.app.fno.f710.v22
    private static String PKG_REST = ""; //kz.inessoft.sono.app.fno.f710.v22.rest
    private static String PKG_SERVICE = ""; //kz.inessoft.sono.app.fno.f710.v22.services
    private static String PKG_SERVICE_DTO = ""; //kz.inessoft.sono.app.fno.f710.v22.services.dto
    private static String PKG_SERVICE_DTO_REST = ""; //kz.inessoft.sono.app.fno.f710.v22.services.dto.rest
    private static String PKG_SERVICE_DTO_XML = ""; //kz.inessoft.sono.app.fno.f710.v22.services.dto.xml
    private static String PKG_SERVICE_FLK = ""; //kz.inessoft.sono.app.fno.f710.v22.services.flk

    private File targetDir;

    private static final Logger logger = LogManager.getLogger(KNPPluginNew.class);

    private JCodeModel J_MODEL;

    private JDefinedClass xmlFnoClass;
    private Map<String, JDefinedClass> xmlFormClassMap = new HashMap<String, JDefinedClass>();
    private Map<String, JDefinedClass> xmlPageClassMap = new HashMap<>();


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
        this.J_MODEL = model.getCodeModel();

        for (ClassOutline classOutline : model.getClasses()) {
            if(classOutline.target.shortName.equals("Fno")) {
                CPropertyInfo cPropertyInfo = model.getModel().beans().get(classOutline.target).getProperty("code");
                if(cPropertyInfo != null) {
                    this.FORM_CODE = Helper.getFixedValue(cPropertyInfo);
                }
                logger.debug(this.FORM_CODE);

                CPropertyInfo cPropertyInfo2 = model.getModel().beans().get(classOutline.target).getProperty("version");
                if(cPropertyInfo2 != null) {
                    this.FNO_VERSION = Helper.getFixedValue(cPropertyInfo2);
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


        for (ClassOutline classOutline : model.getClasses()) {
            if(classOutline.target.shortName.equals("Fno")) {
                xmlFnoClass = classOutline.implClass;
            } else if(classOutline.target.shortName.startsWith("Form")) {
                xmlFormClassMap.put(classOutline.implClass.name(), classOutline.implClass);
            } else  if (classOutline.target.shortName.startsWith("Page")) {
                xmlPageClassMap.put(classOutline.implClass.name(), classOutline.implClass);
            }
        }

        return true;
    }
}