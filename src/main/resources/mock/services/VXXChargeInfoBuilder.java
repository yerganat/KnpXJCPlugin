package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.*;
import kz.inessoft.sono.lib.charge.dtos.*;
import kz.inessoft.sono.lib.charge.dtos.request.Charge;
import kz.inessoft.sono.lib.charge.dtos.request.ChargeInfo;
import kz.inessoft.sono.lib.charge.dtos.request.TaxOrgCharges;
import kz.inessoft.sono.lib.dict.client.api.DictDaysOffService;
import kz.inessoft.sono.lib.dict.client.api.DictMSUService;
import kz.inessoft.sono.lib.dict.dtos.MSU;
import kz.inessoft.sono.lib.docs.registry.client.api.RelatedDocsService;
import kz.inessoft.sono.lib.docs.registry.dtos.LiquidInfoRequest;
import kz.inessoft.sono.lib.docs.registry.dtos.LiquidInfoResponce;
import kz.inessoft.sono.lib.services.commons.document.DocPeriod;
import kz.inessoft.sono.lib.services.commons.document.EDocumentStatus;
import kz.inessoft.sono.lib.services.commons.document.StatusInfo;
import kz.inessoft.sono.lib.tax.payers.dtos.BaseTaxPayer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static kz.inessoft.sono.app.fno.fXXX.FXXXConstants.FORM_CODE;
import static kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils.*;
import static kz.inessoft.sono.lib.fno.utils.FieldUtils.*;

public class VXXChargeInfoBuilder {

    public static final String KBKXXXXX = "XXXXXXX";


    private Fno fno;
    private BaseTaxPayer tp;
    private DocPeriod docPeriod;

    private String taxOrg;
    private Date paymentDate1;

    private DictDaysOffService dictDaysOffService;
    private RelatedDocsService relatedDocsService;

    //private PageX000001 pageX000001;

    private String msuCode;

    private boolean additionalOrNotice;


    public VXXChargeInfoBuilder(Fno fno, BaseTaxPayer tp, DocPeriod docPeriod, DictDaysOffService dictDaysOffService, RelatedDocsService relatedDocsService, DictMSUService msuService){
        this.fno = fno;
        this.tp = tp;
        this.docPeriod = docPeriod;

        this.dictDaysOffService = dictDaysOffService;
        this.relatedDocsService = relatedDocsService;

        //        TODO заполненеие страницы фно из xml !!!
//        pageX000001 = fno.getFormX0000().getSheetGroup().getPageX000001();

        additionalOrNotice = isAdditional(pageXXXXXXX) || isNotice(pageXXXXXXX);


        if(isFinal(pageXXXXXXX)) {
            paymentDate1 = this.getFinalPaymentDate();
        } else {
            paymentDate1 = this.getDate1();
        }

        taxOrg = pageXXXXXXX.getRatingAuthCode();
        String f045 = pageXXXXXXX.getField22000045();
        if (f045 != null && !f045.isEmpty()) {
            MSU byBin = msuService.getByBin(f045);
            msuCode = byBin.getCode();
        }

    }


    public ChargeInfo build() {
        List<Charge> chargesTaxOrg = new ArrayList<>();

        String KBK = KBKXXXXX;
        String TAX_ORG = taxOrg;

        if(StringUtils.isNotBlank(pageXXXXXXX.getField22000045()))
            TAX_ORG = msuCode;

        if(BooleanUtils.isTrue(pageXXXXXXX.isCbN1()))
            KBK = KBK101205;

        createAndAddCharge(chargesTaxOrg, pageXXXXXXX.getField22000044(), paymentDate1, "220.00.044", KBK);

        ChargeInfo retVal = createEmptyCharge();

        if (!chargesTaxOrg.isEmpty()) {
            TaxOrgCharges taxOrgCharges = new TaxOrgCharges();
            taxOrgCharges.setCurrencyCode(pageXXXXXXX.getCurrencyCode());
            taxOrgCharges.setTaxOrgCode(TAX_ORG);
            taxOrgCharges.setCharges(chargesTaxOrg);
            retVal.getTaxOrgCharges().add(taxOrgCharges);
        }

        return retVal.getTaxOrgCharges().isEmpty() ? null : retVal;
    }



    private void createAndAddCharge(List<Charge> charges, Long amount, Date paymentDate, String strNum, String kbk) {
        long sum = lv(amount);
        if (sum == 0 && additionalOrNotice)
            return;

        Charge charge = new Charge();
        charge.setPaymentSum(bdv(sum));
        charge.setPaymentDate(paymentDate);
        charge.setStringNum(strNum);
        charge.setKbk(kbk);
        charge.setChargeType(EChargeType.INCREASE_CHARGE);
        charges.add(charge);
    }

    private ChargeInfo createEmptyCharge() {
        ChargeInfo chargeInfo = new ChargeInfo();
        chargeInfo.setCreateDate(new Date());
        chargeInfo.setDocDate(new Date());

        if (isFinal(pageXXXXXXX))
            chargeInfo.setDocKind(EDocKind.INITIAL);
        else if (isMain(pageXXXXXXX))
            chargeInfo.setDocKind(EDocKind.INITIAL);
        else if (isAdditional(pageXXXXXXX))
            chargeInfo.setDocKind(EDocKind.ADDITIONAL);
        else if (isNotice(pageXXXXXXX))
            chargeInfo.setDocKind(EDocKind.BY_NOTICE);
        else if (isRegular(pageXXXXXXX))
            chargeInfo.setDocKind(EDocKind.REGULAR);

        chargeInfo.setDocVersion(VXXConstants.VERSION);
        chargeInfo.setRecieveMethod(ERecieveMethod.ECHANNELS_DELIVERY);
        TaxPayer taxPayer = new TaxPayer();
        chargeInfo.setTaxPayer(taxPayer);
        taxPayer.setXin(pageXXXXXXX.getIin());
        taxPayer.setRnn(tp.getRnn());
        TaxPeriod taxPeriod = new TaxPeriod();
        chargeInfo.setTaxPeriod(taxPeriod);
        StatusInfo statusOnSent = new StatusInfo();
        chargeInfo.setStatusOnSent(statusOnSent);
        statusOnSent.setStatus(EDocumentStatus.CULS_PROCESSING);
        taxPeriod.setYear(Integer.parseInt(pageXXXXXXX.getPeriodYear()));
        chargeInfo.setTaxOrgCharges(new ArrayList<>());
        return chargeInfo;
    }


    private Date getDate1(){
        //  Срок 1 – для всех видов декларации, кроме ликвидационной, не позднее десяти календарных дней после срока, установленного для сдачи декларации.
        // При предоставлении ликвидационной отчетности – не позднее 10 календарных дней со дня представления в орган государственных доходов ликвидационной налоговой отчетности.
        // Если срок уплаты налога, отраженного в налоговой отчетности, представленной перед ликвидационной налоговой отчетностью наступает после истечения 10 календарных дней
        // со дня представления ликвидационной отчетности, уплата должна быть произведена не позднее 10 календарных дней со дня представления в орган государственных доходов ликвидационной налоговой отчетности.
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.YEAR, Integer.parseInt(pageXXXXXXX.getPeriodYear()));
        cal.set(Calendar.MONTH, Calendar.MARCH);
        cal.set(Calendar.DAY_OF_MONTH, 31);
        cal.add(Calendar.YEAR, 1); //31 марта следующего кода

        cal.add(Calendar.DAY_OF_MONTH, 10);
        return dictDaysOffService.getWorkDate(checkByLiqDate(cal.getTime()), 0);
    }

    /**
     * Если у НП имеется действующая ликвидка то по идее сроки платежа по всем формам не могут превышать 10 дней после подачи ликвидки
     * этот метод проверяет нет ли у НП действующей ликвидки. И если ликвидка имеется и срок больше чем 10 дней, то срок
     * уплаты обрезается до нужного значения
     *
     * @param date срок уплаты на проверку
     * @return обновленный срок уплаты
     */
    private Date checkByLiqDate(Date date) {
        if (isAdditional(pageXXXXXXX) || isNotice(pageXXXXXXX)) {
            // еще не запрашивали данные по ликвидкам. Запрашиваем...
            LiquidInfoRequest liquidInfoRequest = new LiquidInfoRequest();
            liquidInfoRequest.setFormCodes(FORM_CODE);
            liquidInfoRequest.setPeriod(docPeriod);
            liquidInfoRequest.setRnn(tp.getRnn());
            liquidInfoRequest.setTaxOrgCode(taxOrg);
            LiquidInfoResponce liquidInfoResponce = relatedDocsService.getLiquidInfo(liquidInfoRequest);
            if (liquidInfoResponce != null) {  // подаваемый документ не является основным (дополнительный или по уведомлению)
                // Определяем дату уплаты по ней
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(liquidInfoResponce.getSubmitDate());
                calendar.add(Calendar.DATE, 10);
                Date liqDate = calendar.getTime();

                if (date.after(liqDate))
                    return liqDate;
            }
        }

        return date;
    }

    public Date getFinalPaymentDate() {
//        Срок уплаты при представлении ликвидационной отчетности не позднее 10 календарных дней со дня представления в орган государственных доходов ликвидационной налоговой отчетности.
//        Если срок уплаты налога, отраженного в налоговой отчетности, представленной перед ликвидационной отчетностью,
//        наступает после 10 календарных со дня представления ликвидационной налоговой отчетности, то уплата должна быть произведена не позднее 10 календарных дней
//        со дня представления в орган государственных доходов ликвидационной налоговой отчетности.
        Calendar cal = Calendar.getInstance();
        Date date = date(pageXXXXXXX.getSubmitDate());
        cal.setTime(date);
        cal.add(Calendar.DATE, 10);
        return  dictDaysOffService.getWorkDate(cal.getTime(), 0);
    }
}