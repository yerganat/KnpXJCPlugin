package kz.inessoft.sono.app.fno.fXXX.vXX.services.flk;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno;
import kz.inessoft.sono.lib.fno.utils.rest.FormError;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static kz.inessoft.sono.lib.fno.utils.FieldUtils.*;

public class FXXXFormXXVXXFlk extends ABaseFXXXVXXFlk {
    private static final String FORM_NAME = "form_X00_00";
    private static final String PAGE_1 = "page_X00_00_01";

    public FXXXFormXXVXXFlk(Fno fno, List<FormError> errors) {
        super(fno, errors);
    }

    @Override
    public void doFlk() {
        //TODO FLK checks
//        doFlkFldNoticeNumber();
    }

    // page_101_04_01.notice_number
//    private void doFlkFldNoticeNumber() {
//        if (!isDtNotice() && pageXXXX.getNoticeNumber() != null)
//            addError(FORM_NAME, PAGE_1, "notice_number", "msg20269577");
//
//        if (isDtNotice() && pageXXXX.getNoticeNumber() == null)
//            addError(FORM_NAME, PAGE_1, "notice_number", "msg20269068");
//    }


}
