package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.*;
import kz.inessoft.sono.lib.fno.utils.rest.FormError;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils.*;

public class VXXFLKProcessor {
    private static Map<String, String> RU_ERRORS = new HashMap<>();
    private static Map<String, String> KZ_ERRORS = new HashMap<>();

    static {
        RU_ERRORS.put("msg20014905", "Реквизит отсутствует");
        KZ_ERRORS.put("msg20014905", "Реквизит көрсетілмеген");
    }

    //private PageXXXXXXX pageXXXX;

    private List<FormError> errors;

    public VXXFLKProcessor(Fno fno) {
        //pageXXXX = fno.getFormXXXXX().getSheetGroup().getPageXXXXXXX();
    }

    public List<FormError> doFlk() {
        errors = new ArrayList<>();

        //TODO FLK checks
//        doFlkFldNoticeNumber();

        return errors;
    }

    private boolean isDtNotice() {
        return isNotice(page0001);
    }

    private boolean isDtRegular() {
        return isRegular(page0001);
    }

    private boolean isDtAdditional() {
        return isAdditional(page0001);
    }

    private boolean isDtMain() {
        return isMain(page0001);
    }



    // page_101_04_01.notice_number
//    private void doFlkFldNoticeNumber() {
//        if (!isDtNotice() && pageXXXX.getNoticeNumber() != null)
//            addError("form_XXX_XX", "page_XXX_XX_XX", "notice_number", "msg20269577");
//
//        if (isDtNotice() && pageXXXX.getNoticeNumber() == null)
//            addError("form_XXX_XX", "page_XX_XX_XX", "notice_number", "msg20269068");
//    }

    private void addError(String formName, String sheetName, String fieldName, String msgCode, Integer rowIdx) {
        FormError e = new FormError();
        e.setFormName(formName);
        e.setSheetName(sheetName);
        e.setFieldName(fieldName);
        e.setRowIdx(rowIdx);
        e.setMsgRu(RU_ERRORS.get(msgCode));
        e.setMsgKz(KZ_ERRORS.get(msgCode));
        errors.add(e);
    }

    private void addError(String formName, String sheetName, String fieldName, String msgCode) {
        addError(formName, sheetName, fieldName, msgCode, null);
    }
}
