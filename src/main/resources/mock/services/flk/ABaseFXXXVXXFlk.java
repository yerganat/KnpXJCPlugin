package kz.inessoft.sono.app.fno.f200.v29.services.flk;

import kz.inessoft.sono.app.fno.f200.v29.services.VXXUtils;
import kz.inessoft.sono.app.fno.f200.v29.services.dto.xml.*;
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

    }

    private List<FormError> errors;
//    protected Page2000001 page2000001;
//    protected Page2000002 page2000002;
//    protected Page2000003 page2000003;
//
//    protected Page2000101 page2000101;
//    protected Page2000102 page2000102;
//
//    protected List<Page2000201> page2000201 = new ArrayList<>();
//    protected List<Page2000202> page2000202 = new ArrayList<>();
//    protected List<Page2000203> page2000203 = new ArrayList<>();
//    protected List<Page2000204> page2000204 = new ArrayList<>();
//    protected List<Page2000205> page2000205 = new ArrayList<>();
//    protected List<Page2000206> page2000206 = new ArrayList<>();
//    protected List<Page2000207> page2000207 = new ArrayList<>();
//    protected List<Page2000208> page2000208 = new ArrayList<>();
//
//    protected List<Page2000301> page2000301 = new ArrayList<>();
//    protected List<Page2000302> page2000302 = new ArrayList<>();
//
//    protected List<Page2000401> page2000401 = new ArrayList<>();
//
//    protected List<Page2000501> page2000501 = new ArrayList<>();
//    protected List<Page2000502> page2000502 = new ArrayList<>();

    public ABaseFXXXVXXFlk(Fno fno, List<FormError> errors) {
        this.errors = errors;
        Form20000 form20000 = fno.getForm20000();
        Form20000.SheetGroup sheetGroup00 = form20000.getSheetGroup();
        page2000001 = sheetGroup00.getPage2000001();
        page2000002 = sheetGroup00.getPage2000002();
        page2000003 = sheetGroup00.getPage2000003();

        Form20001 form20001 = fno.getForm20001();
        if (form20001 != null) {
            Form20001.SheetGroup sheetGroup = form20001.getSheetGroup();
            if (sheetGroup != null) {
                page2000101 = sheetGroup.getPage2000101();
                page2000102 = sheetGroup.getPage2000102();
            }
        }


        Form20002 form20002 = fno.getForm20002();
        if (form20002 != null) {
            form20002.getSheetGroup().forEach(sg -> {
                page2000201.add(sg.getPage2000201());
                page2000202.add(sg.getPage2000202());
                page2000203.add(sg.getPage2000203());
                page2000204.add(sg.getPage2000204());
                page2000205.add(sg.getPage2000205());
                page2000206.add(sg.getPage2000206());
                page2000207.add(sg.getPage2000207());
                page2000208.add(sg.getPage2000208());
            });
        }

        fno.getForm20003().forEach(form -> {
            Form20003.SheetGroup sheetGroup = form.getSheetGroup();
            if (sheetGroup != null) {
                page2000301.add(sheetGroup.getPage2000301());
                page2000302.add(sheetGroup.getPage2000302());
            }
        });

        fno.getForm20004().forEach(form -> {
            Form20004.SheetGroup sheetGroup = form.getSheetGroup();
            if (sheetGroup != null)
                page2000401.add(sheetGroup.getPage2000401());
        });

        Form20005 form20005 = fno.getForm20005();
        if (form20005 != null) {
            form20005.getSheetGroup().forEach(sg -> {
                if (sg != null) {
                    page2000501.add(sg.getPage2000501());
                    page2000502.add(sg.getPage2000502());
                }
            });
        }
    }

    public abstract void doFlk();

    protected boolean isMain() {
        return VXXUtils.isMain(page2000001);
    }

    protected boolean isRegular() {
        return VXXUtils.isRegular(page2000001);
    }

    protected boolean isAdditional() {
        return VXXUtils.isAdditional(page2000001);
    }

    protected boolean isNotice() {
        return VXXUtils.isNotice(page2000001);
    }

    protected boolean isFinal() {
        return VXXUtils.isFinal(page2000001);
    }

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



    protected boolean anyRowFieldNotEmpty(Page7100101 page7100101, Page7100102 page7100102) { //TODO реализовать проверку всех полей
        return page7100101!= null && (page7100101.getField71001001A1() != null || page7100101.getField71001001B1() != null || page7100101.getField71001001C1() != null
                || page7100101.getField71001001A2() != null || page7100101.getField71001001B2() != null || page7100101.getField71001001C2() != null
                || page7100101.getField71001001A3() != null || page7100101.getField71001001B3() != null || page7100101.getField71001001C3() != null);
    }

}
