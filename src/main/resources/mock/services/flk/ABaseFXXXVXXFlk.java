package kz.inessoft.sono.app.fno.fXXX.vXX.services.flk;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils;
import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.*;
import kz.inessoft.sono.lib.fno.utils.rest.FormError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ABaseFXXXVXXFlk {
    private static Map<String, String> RU_ERRORS = new HashMap<>();
    private static Map<String, String> KZ_ERRORS = new HashMap<>();

    static {
        RU_ERRORS.put("msgEmpty", "Реквизит отсутствует");
        KZ_ERRORS.put("msgEmpty", "Реквизит көрсетілмеген");

        RU_ERRORS.put("msgCalcError", "Расчет некорректен");
        KZ_ERRORS.put("msgCalcError", "Есептің мәні сәйкес емес");

        RU_ERRORS.put("msgPeriod", "Налоговый период не определен");
        KZ_ERRORS.put("msgPeriod", "Налоговый период не определен");

        RU_ERRORS.put("msgYear", "Налоговый период, за который представляется налоговая отчетность не указан");
        KZ_ERRORS.put("msgYear", "Налоговый период, за который представляется налоговая отчетность не указан");

        RU_ERRORS.put("msgIINCheck", "Введенное значение не является (ИИН)БИН");
        KZ_ERRORS.put("msgIINCheck", "Енгізілген шама (ЖСН)БСН  құрылымына сәйкес емес");

        RU_ERRORS.put("msgError", "Поле заполненно некорректно");
        KZ_ERRORS.put("msgError", "Өріс қате толтырылған");

        RU_ERRORS.put("msgIfNoticeRequired", "Если выбран вид декларации 'дополнительная по уведомлению', то поле должно быть заполнено.");
        KZ_ERRORS.put("msgIfNoticeRequired", "Егер Декларацияның 'Хабарлама бойынша қосымша' деген түрі көрсетілсе, онда жол толтырылуы тиіс.");

        RU_ERRORS.put("msgIfNoticeMustEmpty", "Если не выбран вид декларации 'дополнительная по уведомлению', то поле не должно быть заполнено");
        KZ_ERRORS.put("msgIfNoticeMustEmpty", "Егер Декларацияның 'Хабарлама бойынша қосымша' деген түрі көрсетілмесе, онда жол толтырылмауы тиіс");

        RU_ERRORS.put("msgDateGreater", "Указана дата больше текущей даты");
        KZ_ERRORS.put("msgDateGreater", "Ағымдағы күннен үлкен күн көрсетілген");

        RU_ERRORS.put("msgDeclarationEmpty", "Не указан вид декларации");
        KZ_ERRORS.put("msgDeclarationEmpty", "Нысанның түрі көрсетілуі тиіс");

        RU_ERRORS.put("msgPrilNotChecked", "Не отмечено представленное приложение");
        KZ_ERRORS.put("msgPrilNotChecked", "Берілген қосымша көрсетілмеген");

        RU_ERRORS.put("msgPril1", "Отсутствует приложение 1");
        KZ_ERRORS.put("msgPril1", "Қосымша 1 толтырылмаған");

    }

    private List<FormError> errors;
//    protected PageX000001 pageX000001;
//    protected PageX000002 pageX000002;


    public ABaseFXXXVXXFlk(Fno fno, List<FormError> errors) {
        this.errors = errors;
//        TODO заполненеие страницы фно из xml !!!
//        pageX000001 = fno.getFormX0000().getSheetGroup().getPageX000001();
    }

    public abstract void doFlk();

    protected void addError(String formName, Integer formIdx, String sheetName, Integer rowIdx, String fieldName, String msgCode) {
        FormError e = new FormError();
        e.setFormName(formName);
        e.setFormIdx(formIdx);
        e.setSheetName(sheetName);
        e.setRowIdx(rowIdx);
        e.setFieldName(fieldName);
        e.setMsgRu(RU_ERRORS.get(msgCode));
        e.setMsgKz(KZ_ERRORS.get(msgCode));
        errors.add(e);
    }


//
//    protected boolean anyRowFieldNotEmpty(PageX000001 pageX000001, PageX000002 pageX000002) {
//        return page7100101!= null && (pageX000001.getFieldX1001001A1() != null || pageX000001.getFieldX1001001B1() != null || pageX000002.getFieldX1001001C1() != null);
//    }
//

}
