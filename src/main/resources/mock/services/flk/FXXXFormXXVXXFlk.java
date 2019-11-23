package kz.inessoft.sono.app.fno.f200.v29.services.flk;

import kz.inessoft.sono.app.fno.f200.v29.services.dto.xml.Fno;
import kz.inessoft.sono.lib.fno.utils.rest.FormError;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import static kz.inessoft.sono.lib.fno.utils.FieldUtils.*;

public class FXXXFormXXVXXFlk extends ABaseF200V29Flk {
    private static final String FORM_NAME = "form_200_00"; //TODO вынеси в параметр
    private static final String PAGE_1 = "page_200_00_01"; //TODO Вынести в параметр
    private static final String PAGE_2 = "page_200_00_02";
    private static final String PAGE_3 = "page_200_00_03";

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
//            addError("form_XXX_XX", "page_XXX_XX_XX", "notice_number", "msg20269577");
//
//        if (isDtNotice() && pageXXXX.getNoticeNumber() == null)
//            addError("form_XXX_XX", "page_XX_XX_XX", "notice_number", "msg20269068");
//    }


}
