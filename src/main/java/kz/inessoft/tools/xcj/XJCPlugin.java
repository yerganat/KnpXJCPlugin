package kz.inessoft.tools.xcj;

import java.io.IOException;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import com.sun.tools.xjc.BadCommandLineException;
import com.sun.tools.xjc.Options;
import com.sun.tools.xjc.Plugin;
import com.sun.tools.xjc.outline.ClassOutline;
import com.sun.tools.xjc.outline.Outline;

public class XJCPlugin extends Plugin {

    public final static String ID = "id";
    public final static JType LONG_TYPE = new JCodeModel().LONG;
    public final static String ID_GETTER = "getId";
    public final static JType VOID_TYPE = new JCodeModel().VOID;
    public final static String ID_SETTER = "setId";

    @Override
    public String getOptionName() {
        return "Xknp-pattern";
    }

    @Override
    public int parseArgument(Options opt, String[] args, int i)
            throws BadCommandLineException, IOException {
        return 1;
    }

    @Override
    public String getUsage() {
        return "  -Xknp-pattern    :  knp xjc plugin for generate jaxb/json from xml";
    }

    @Override
    public boolean run(Outline model, Options opt, ErrorHandler errorHandler)
            throws SAXException {

        for (ClassOutline classOutline : model.getClasses()) {
            JFieldVar globalId = classOutline.implClass.field(JMod.PRIVATE,
                    LONG_TYPE, ID);

            JMethod idGetterMethod = classOutline.implClass.method(JMod.PUBLIC,
                    LONG_TYPE, ID_GETTER);
            JBlock idGetterBlock = idGetterMethod.body();
            idGetterBlock._return(globalId);

            JMethod idSetterMethod = classOutline.implClass.method(JMod.PUBLIC,
                    VOID_TYPE, ID_SETTER);
            JVar localId = idSetterMethod.param(LONG_TYPE, "_" + ID);
            JBlock idSetterBlock = idSetterMethod.body();
            idSetterBlock.assign(globalId, localId);
        }
        return true;
    }

}