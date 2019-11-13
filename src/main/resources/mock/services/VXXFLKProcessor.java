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

    private Page4210001 page0001;
    private Page4210002 page0002;

    private Page4210101 page0101;

    private Page4210201 page0201;
    private Page4210202 page0202;

    private Page4210301 page0301;

    private Page4210401 page0401;
    private Page4210402 page0402;
    private Page4210403 page0403;




    private List<String> rowNumsOf1Month;
    private List<String> rowNumsOf2Month;
    private List<String> rowNumsOf3Month;

    private List<FormError> errors;

    public VXXFLKProcessor(Fno fno) {
        page0001 = fno.getForm42100().getSheetGroup().getPage4210001();
        page0002 = fno.getForm42100().getSheetGroup().getPage4210002();

        page0101 = fno.getForm42101().getSheetGroup().getPage4210101();


        page0201 = fno.getForm42102().getSheetGroup().getPage4210201();
        page0202 = fno.getForm42102().getSheetGroup().getPage4210202();

        page0301 = fno.getForm42103().getSheetGroup().getPage4210301();

        page0401 = fno.getForm42104().getSheetGroup().getPage4210401();
        page0402 = fno.getForm42104().getSheetGroup().getPage4210402();
        page0403 = fno.getForm42104().getSheetGroup().getPage4210403();

    }

    public List<FormError> doFlk() {
        errors = new ArrayList<>();
        doFlkFldNoticeNumber();
        doFlkNoticeDate();

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
    private void doFlkFldNoticeNumber() {
        if (!isDtNotice() && page0001.getNoticeNumber() != null)
            addError("form_101_04", "page_101_04_01", "notice_number", "msg20269577");

        if (isDtNotice() && page0001.getNoticeNumber() == null)
            addError("form_101_04", "page_101_04_01", "notice_number", "msg20269068");
    }

    // page_101_04_01.notice_date
    private void doFlkNoticeDate() {
        if (page0001.getNoticeDate() != null && !page0001.getNoticeDate().isEmpty() &&
                !isDtNotice())
            addError("form_101_04", "page_101_04_01", "notice_date", "msg20269577");

        try {
            if (page0001.getNoticeDate() != null && !page0001.getNoticeDate().isEmpty() &&
                    new SimpleDateFormat("dd.MM.yyyy").parse(page0001.getNoticeDate()).after(new Date()))
                addError("form_101_04", "page_101_04_01", "notice_date", "msg20270002");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (isDtNotice() && (page0001.getNoticeDate() == null || page0001.getNoticeDate().isEmpty()))
            addError("form_101_04", "page_101_04_01", "notice_date", "msg20269068");
    }


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
