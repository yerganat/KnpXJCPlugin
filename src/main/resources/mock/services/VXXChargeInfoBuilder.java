package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.*;
import kz.inessoft.sono.lib.charge.dtos.*;
import kz.inessoft.sono.lib.charge.dtos.request.Charge;
import kz.inessoft.sono.lib.charge.dtos.request.ChargeInfo;
import kz.inessoft.sono.lib.charge.dtos.request.TaxOrgCharges;
import kz.inessoft.sono.lib.dict.client.api.DictDaysOffService;
import kz.inessoft.sono.lib.dict.client.api.DictRawMaterialSupplierService;
import kz.inessoft.sono.lib.tax.payers.dtos.BaseTaxPayer;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils.*;
import static kz.inessoft.sono.lib.fno.utils.FieldUtils.bdv;
import static kz.inessoft.sono.lib.fno.utils.FieldUtils.lv;

public class VXXChargeInfoBuilder {

    public static final String KBKXXXXX = "XXXXXX";


    private Fno fno;
    private BaseTaxPayer tp;

    private String taxOrg;
    private Date paymentDate1;

    //private PageXXXXXXX pageXXXXXXX;

    private boolean additionalOrNotice;


    public VXXChargeInfoBuilder(Fno fno, BaseTaxPayer tp, DictDaysOffService dictDaysOffService) throws ParseException {
        this.fno = fno;
        this.tp = tp;

        paymentDate1 = this.getDate1();

        paymentDate1 = dictDaysOffService.getWorkDate(paymentDate1, 0);
    }


    public ChargeInfo build() {
        List<Charge> chargesTaxOrg = new ArrayList<>();

        //TODO Дополнить реализацией расзноски!!!
        //createAndAddCharge(chargesTaxOrg, pageXXXXXXX.getFieldXXXXXX(), paymentDate1, "101.04.002 I", KBK);

        ChargeInfo retVal = createEmptyCharge();

        if (!chargesTaxOrg.isEmpty()) {
            TaxOrgCharges taxOrgCharges = new TaxOrgCharges();

            taxOrgCharges.setCurrencyCode("KZT");
            taxOrgCharges.setTaxOrgCode(taxOrg);
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
        //TODO set params
        return chargeInfo;
    }


    private Date getDate1(){
        //Срок 1 - см Документацию по разноске
        Calendar cal = Calendar.getInstance();
        //TODO дата
        return  cal.getTime();
    }
}
