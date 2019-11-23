package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.IPage4210001;
import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.RestToXmlConverter;
import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.XMLToRestConverter;
import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.rest.*;
import kz.inessoft.sono.lib.audit.client.api.AuditService;
import kz.inessoft.sono.lib.charge.dtos.request.ChargeInfo;
import kz.inessoft.sono.lib.dict.client.api.DictDaysOffService;
import kz.inessoft.sono.lib.dict.client.api.DictRawMaterialSupplierService;
import kz.inessoft.sono.lib.dict.client.api.DictTaxOrgService;
import kz.inessoft.sono.lib.docs.registry.client.api.DocsRegistryService;
import kz.inessoft.sono.lib.docs.registry.client.api.DraftsService;
import kz.inessoft.sono.lib.docs.registry.client.api.ErrorMsg;
import kz.inessoft.sono.lib.docs.registry.client.api.RelatedDocsService;
import kz.inessoft.sono.lib.docs.registry.dtos.PayerInfo;
import kz.inessoft.sono.lib.docs.registry.dtos.RegisterDocRequest;
import kz.inessoft.sono.lib.docs.registry.dtos.RegisterDocResponce;
import kz.inessoft.sono.lib.docs.registry.dtos.SaveDraftRequest;
import kz.inessoft.sono.lib.dto.audit.AuditRequest;
import kz.inessoft.sono.lib.fno.notifications.client.api.ReceptionNotificationService;
import kz.inessoft.sono.lib.fno.notifications.dtos.NewStatusInfo;
import kz.inessoft.sono.lib.fno.utils.rest.AcceptResult;
import kz.inessoft.sono.lib.fno.utils.rest.FormError;
import kz.inessoft.sono.lib.fno.utils.rest.SaveDraftResponse;
import kz.inessoft.sono.lib.fno.utils.xml.XSDChecker;
import kz.inessoft.sono.lib.fno.utils.xml.XSLTTransformer;
import kz.inessoft.sono.lib.services.commons.document.*;
import kz.inessoft.sono.lib.sign.check.client.api.CheckSignService;
import kz.inessoft.sono.lib.sign.check.dtos.EStatus;
import kz.inessoft.sono.lib.sign.check.dtos.TaxPayerCheckEDSResult;
import kz.inessoft.sono.lib.sso.EUinType;
import kz.inessoft.sono.lib.sso.UserInfo;
import kz.inessoft.sono.lib.tax.payers.client.api.TaxPayersService;
import kz.inessoft.sono.lib.tax.payers.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.TransformerException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.*;

import static kz.inessoft.sono.app.fno.fXXX.FXXXConstants.FORM_CODE;
import static kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils.*;
import static kz.inessoft.sono.lib.fno.utils.FieldUtils.date;

@Service
public class VXXService {
    private static JAXBContext jaxbContext;

    private TaxPayersService taxPayersService;
    private DocsRegistryService docsRegistryService;
    private RelatedDocsService relatedDocsService;
    private CheckSignService checkSignService;
    private DraftsService draftsService;
    private DictTaxOrgService dictTaxOrgService;
    private DictDaysOffService daysOffService;
    private ReceptionNotificationService notificationService;
    private AuditService auditService;
    private int minMonth;
    private int minYear;
    private int maxMonth;
    private int maxYear;


    @Autowired
    public VXXService(@Value("${vXX.min.month}") int minMonth, @Value("${vXX.min.year}") int minYear,
                      @Value("${vXX.max.month}") int maxMonth, @Value("${vXX.max.year}") int maxYear,
                      TaxPayersService taxPayersService, DocsRegistryService docsRegistryService,
                      RelatedDocsService relatedDocsService, CheckSignService checkSignService,
                      DraftsService draftsService, DictTaxOrgService dictTaxOrgService, DictDaysOffService daysOffService, ReceptionNotificationService notificationService,
                      AuditService auditService) {
        this.minMonth = minMonth;
        this.minYear = minYear;
        this.maxMonth = maxMonth;
        this.maxYear = maxYear;
        this.taxPayersService = taxPayersService;
        this.docsRegistryService = docsRegistryService;
        this.relatedDocsService = relatedDocsService;
        this.checkSignService = checkSignService;
        this.draftsService = draftsService;
        this.dictTaxOrgService = dictTaxOrgService;
        this.daysOffService = daysOffService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public Fno getPrefilledForm(UserInfo userInfo, int year) {
        switch (userInfo.getUinType()) {
            case RNN:
                return generateByRNN(userInfo.getUin(), year);
            case IIN_BIN:
                return generateByXIN(userInfo.getUin(), year);
            default:
                return null;
        }
    }

    private Fno generateByRNN(String rnn, int year) {
        return generateDefaultForm(taxPayersService.findByRnn(rnn), year);
    }

    private Fno generateByXIN(String xin, int year) {
        return generateDefaultForm(taxPayersService.findByXin(xin), year);
    }

    private Fno generateDefaultForm(BaseTaxPayer taxPayer, int year) {
        Fno retVal = new Fno();
//        TODO надо зполнить функцию, можно сгенерировать
//        Form42100 form42100 = new Form42100();
//        retVal.setForm42100(form42100);
//
//        Page4210001 page4210001 = new Page4210001();
//        form42100.setPage4210001(page4210001);
//        page4210001.setRnn(taxPayer.getRnn());
//        page4210001.setIin(taxPayer.getXin());
//        page4210001.setPeriodYear(String.valueOf(year));
//
//
//        Page4210002 page4210002 = new Page4210002();
//        form42100.setPage4210002(page4210002);
//
//        String strNow = date(new Date());
//        page4210002.setSubmitDate(strNow);
//        page4210002.setAcceptDate(strNow);
//        page4210002.setIin(taxPayer.getXin());
//        page4210002.setPeriodYear(String.valueOf(year));
//
//
//
//        Form42101 form42101 = new Form42101();
//        retVal.setForm42101(form42101);
//
//        Page4210101 page4210101 = new Page4210101();
//        form42101.setPage4210101(page4210101);
//        page4210101.setIin(taxPayer.getXin());
//        page4210101.setPeriodYear(String.valueOf(year));
//
//
//
//        Form42102 form42102 = new Form42102();
//        retVal.setForm42102(form42102);
//
//        Page4210201 page4210201 = new Page4210201();
//        form42102.setPage4210201(page4210201);
//        page4210201.setIin(taxPayer.getXin());
//        page4210201.setPeriodYear(String.valueOf(year));
//
//        Page4210202 page4210202 = new Page4210202();
//        form42102.setPage4210202(page4210202);
//        page4210202.setIin(taxPayer.getXin());
//        page4210202.setPeriodYear(String.valueOf(year));
//
//
//
//        Form42103 form42103 = new Form42103();
//        retVal.setForm42103(form42103);
//
//        Page4210301 page4210301 = new Page4210301();
//        form42103.setPage4210301(page4210301);
//        page4210301.setIin(taxPayer.getXin());
//        page4210301.setPeriodYear(String.valueOf(year));
//
//
//
//        Form42104 form42104 = new Form42104();
//        retVal.setForm42104(form42104);
//
//        Page4210401 page4210401 = new Page4210401();
//        form42104.setPage4210401(page4210401);
//        page4210401.setIin(taxPayer.getXin());
//        page4210401.setPeriodYear(String.valueOf(year));
//
//        Page4210402 page4210402 = new Page4210402();
//        form42104.setPage4210402(page4210402);
//        page4210402.setIin(taxPayer.getXin());
//        page4210402.setPeriodYear(String.valueOf(year));
//
//        Page4210403 page4210403 = new Page4210403();
//        form42104.setPage4210403(page4210403);
//        page4210403.setIin(taxPayer.getXin());
//        page4210403.setPeriodYear(String.valueOf(year));

        return retVal;
    }

    public Fno getDocument(Long id, UserInfo userInfo) throws JAXBException, TransformerException {
        String documentXml = docsRegistryService.getDocumentXml(id, getRnn(userInfo));
        return getRestDTOFromXML(documentXml);
    }

    private String getRnn(UserInfo userInfo) {
        String rnn;
        if (userInfo.getUinType() == EUinType.RNN)
            rnn = userInfo.getUin();
        else
            rnn = taxPayersService.findByXin(userInfo.getUin()).getRnn();
        return rnn;
    }

    private Fno getRestDTOFromXML(String xml) throws JAXBException, TransformerException {
        if (xml == null)
            return null;
        xml = XSLTTransformer.convertFromOldSonoFormat(xml);
        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno fno = (kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno) unmarshaller.unmarshal(new StringReader(xml));
        return new XMLToRestConverter(fno).convert();
    }

    private JAXBContext getJaxbContext() throws JAXBException {
        if (jaxbContext == null)
            jaxbContext = JAXBContext.newInstance(kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno.class);
        return jaxbContext;
    }

    public Fno getDraft(Long id, UserInfo userInfo) throws JAXBException, TransformerException {
        String xmlOld = draftsService.getDraftXml(id, getRnn(userInfo));
        return getRestDTOFromXML(xmlOld);
    }

    private List<EDocType> getDocTypes(IPage4210001 page4210001) { //TODO Реализовать превый интерфейс
        List<EDocType> docTypes = new ArrayList<>();
        if (isFinal(page4210001))
            docTypes.add(EDocType.LIQUIDATION);
        if (isMain(page4210001))
            docTypes.add(EDocType.PRIMARY);
        else if (isAdditional(page4210001))
            docTypes.add(EDocType.ADDITIONAL);
        else if (isNotice(page4210001))
            docTypes.add(EDocType.NOTICE);
        else if (isRegular(page4210001))
            docTypes.add(EDocType.REGULAR);
        return docTypes;
    }


    public SaveDraftResponse saveDraft(Fno fno, Long id, UserInfo userInfo) throws JAXBException, TransformerException {
        SaveDraftResponse retVal = new SaveDraftResponse();
        List<FormError> errors = new ArrayList<>();
        Page4210001 page4210001 = fno.getForm42100().getPage4210001();

        if (page4210001.getPeriodYear() == null) {
            errors.add(new FormError("form_421_00", "page_421_00_01", "period_year",
                    "Налоговый период, за который представляется налоговая отчетность не указан", "Налоговый период, за который представляется налоговая отчетность не указан"));
        }

        Integer periodMonth = Integer.valueOf(page4210001.getPeriodMonth());
        if (periodMonth == null || periodMonth < 13 || periodMonth > 0) {
            errors.add(new FormError("form_421_00", "page_421_00_01", "period_quarter",
                    "Налоговый период, за который представляется налоговая отчетность не указан", "Налоговый период, за который представляется налоговая отчетность не указан"));
        }


        if (!isMain(page4210001) && !isRegular(page4210001) && !isAdditional(page4210001) && !isNotice(page4210001)) {
            errors.add(new FormError("form_421_00", "page_421_00_01", "dt_main",
                    "Реквизит Не указан вид расчета", "Реквизит Не указан вид расчета"));
        }

        if (!errors.isEmpty()) {
            retVal.setErrors(errors);
            return retVal;
        }

        String rnn = getRnn(userInfo);
        String xml = serializeToXml(fno);
        SaveDraftRequest saveDraftRequest = new SaveDraftRequest();
        saveDraftRequest.setDocXml(xml);
        saveDraftRequest.setFormCode(FORM_CODE);
        saveDraftRequest.setFormVersion(VXXConstants.VERSION);
        saveDraftRequest.setDraftId(id);
        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setRnn(rnn);
        saveDraftRequest.setPayerInfo(payerInfo);

        Page4210002 page4210002 = fno.getForm42100().getPage4210002();
        saveDraftRequest.setTaxOrgCode(page4210002.getRatingAuthCode());
        DocPeriod docPeriod = new DocPeriod();
        docPeriod.setYear(Integer.valueOf(page4210001.getPeriodYear()));

        docPeriod.setPeriod(EPeriod.getQuartalByNumber(periodMonth));
        saveDraftRequest.setDocPeriod(docPeriod);
        saveDraftRequest.setDocTypes(getDocTypes(page4210001));
        retVal.setId(String.valueOf(draftsService.saveDraft(saveDraftRequest)));
        return retVal;
    }

    private String serializeToXml(Fno form) throws JAXBException, TransformerException {
        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno fno = new RestToXmlConverter(form).convert();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        StringWriter writer = new StringWriter();
        marshaller.marshal(fno, writer);
        return XSLTTransformer.convertToOldSonoFormat(writer.toString());
    }


    public String serializeToXmlForSign(Fno form) throws JAXBException, TransformerException {
        String strNow = date(new Date());
        form.getForm42100().getPage4210002().setAcceptDate(strNow);
        form.getForm42100().getPage4210002().setSubmitDate(strNow);
        return serializeToXml(form);
    }

    public String serializeToXmlForPrint(Fno form) throws JAXBException, TransformerException {
        return  serializeToXml(form);
    }

    public AcceptResult acceptForm(String signedXml, Long draftId) throws TransformerException, SAXException, JAXBException, ParseException {
        String xml = XSLTTransformer.convertFromOldSonoFormat(signedXml);
        XSDChecker.checkXml(xml, "/xsds/421.00vXX.xsd", VXXService.class);  //TODO название формы
        TaxPayerCheckEDSResult taxPayerCheckEDSResult = checkSignService.checkTaxPayerEDS(signedXml);

        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno fno = (kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno) unmarshaller.unmarshal(new StringReader(xml));
        List<FormError> errors = new ArrayList<>();

        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Page4210002 page1010402 = fno.getForm42100().getSheetGroup().getPage4210002();
        if (taxPayerCheckEDSResult.getStatus() == EStatus.INVALID) {
            errors.add(new FormError("form_421_00", "page_421_00_01", "iin", "ЭЦП не верна", "ЭЦП не верна")); //TODO перебить как в xml парметры
            return createErrorsResponse(errors);
        }

        Date now = new Date();

        String acceptDate = page1010402.getAcceptDate();
        if (acceptDate != null && !date(now).equals(acceptDate))
            errors.add(new FormError("form_421_00", "page_421_00_02", "accept_date", "Дата приема не равна текущей дате", "Дата приема не равна текущей дате"));

        String strSubmitDate = page1010402.getSubmitDate();
        if (!date(now).equals(strSubmitDate))
            errors.add(new FormError("form_421_00", "page_421_00_02", "submit_date", "Дата приема не равна текущей дате", "Дата приема не равна текущей дате"));

        String edsXin = taxPayerCheckEDSResult.getBin();
        if (edsXin == null)
            edsXin = taxPayerCheckEDSResult.getIin();
        String rnn = taxPayerCheckEDSResult.getRnn();
        BaseTaxPayer taxPayer = null;
        if (edsXin == null) {
            taxPayer = taxPayersService.findByRnn(rnn);
            if (taxPayer == null) {
                errors.add(new FormError("form_421_00", "page_421_00_01", "iin", "РНН декларанта из ЭЦП на найден в базе налогоплательщиков", "РНН декларанта из ЭЦП на найден в базе налогоплательщиков"));
                return createErrorsResponse(errors);
            }
            edsXin = taxPayer.getXin();
        }

        if (!page1010402.getIin().equals(edsXin))
            errors.add(new FormError("form_421_00", "page_421_00_01", "iin", "ИИН декларанта не совпадает с ИИН в ЭЦП", "ИИН декларанта не совпадает с ИИН в ЭЦП"));

        if (rnn == null) {
            taxPayer = taxPayersService.findByXin(edsXin);
            if (taxPayer == null) {
                errors.add(new FormError("form_421_00", "page_421_00_01", "iin", "ИИН/БИН декларанта из ЭЦП на найден в базе налогоплательщиков", "ИИН/БИН декларанта из ЭЦП на найден в базе налогоплательщиков"));
                return createErrorsResponse(errors);
            }
            rnn = taxPayer.getRnn();
        }

        Integer periodYear = Integer.valueOf(page1010402.getPeriodYear());
        Integer periodMonth = Integer.valueOf(page1010402.getPeriodMonth()); // TODO указать нужный, разработчик должен разобраться, закоментирую
        if (periodYear < minYear ||
                periodYear == minYear && periodMonth != null && periodMonth < minMonth ||
                periodYear > maxYear ||
                periodYear == maxYear && periodMonth != null && periodMonth > maxMonth)
            errors.add(new FormError("form_421_00", "page_421_00_01", "period_year", "Документ имеет не допустимый период", "Документ имеет не допустимый период"));


        Integer periodQuarter = Integer.valueOf(page7100001.getPeriodQuarter());
        if (periodYear < minYear ||
                periodYear == minYear && periodQuarter != null && periodQuarter < minQuarter ||
                periodYear > maxYear ||
                periodYear == maxYear && periodQuarter != null && periodQuarter > maxQuarter)
            errors.add(new FormError("form_710_00", "page_710_00_01", "period_year", "Документ имеет не допустимый период", "Документ имеет не допустимый период"));


        Calendar cal = GregorianCalendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        if (periodYear > year || periodYear == year && periodMonth != null && periodMonth > month)
            errors.add(new FormError("form_421_00", "page_421_00_01", "period_year", "Налоговое обязательство по представлению налоговой отчетности исполняется налогоплательщиком (налоговым агентом) по окончании налогового периода, если иное не установлено Налоговым кодексом",
                    "Налоговое обязательство по представлению налоговой отчетности исполняется налогоплательщиком (налоговым агентом) по окончании налогового периода, если иное не установлено Налоговым кодексом"));

        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Page4210001 page4210001 = fno.getForm42100().getSheetGroup().getPage4210001();

        EPeriod priodType = periodMonth == null ? EPeriod.YEAR : EPeriod.getQuartalByNumber(periodMonth);
        if (isAdditional(page4210001) || isNotice(page4210001)) {
            AuditRequest request = new AuditRequest();
            request.setYear(periodYear);
            request.setDocPeriod(priodType);
            request.setRnn(rnn);
            request.setKbkCodes(Arrays.asList(VXXChargeInfoBuilder.KBKXXXXX, VXXChargeInfoBuilder.KBKXXXXX));
            if (auditService.hasActiveAudit(request)) {
                errors.add(new FormError("form_421_00", "page_421_00_01", "iin",
                        "Регистрация данной ФНО невозможна. Проводится налоговая проверка.",
                        "Ұсынылған  СЕН тіркеу мүмкін емес. Салықтық тексеру жүргізілуде."));
            }
        }

        String taxOrg = page1010402.getRatingAuthCode();
        if (dictTaxOrgService.getByCode(taxOrg) == null)
            errors.add(new FormError("form_421_00", "page_421_00_02", "rating_auth_code", "КОГД1. Введенное значение в справочнике отсутствует", "КОГД1. Енгізілген мағына кұжатта жоқ"));

        List<FormError> formErrors = new VXXFLKProcessor(fno).doFlk();
        if (!formErrors.isEmpty())
            errors.addAll(formErrors);

        // Если к этому моменту имеются ошибки, то не будем проводить тяжелую проверку на наличие ранее поданных документов. Сразу выдадим ошибку
        if (!errors.isEmpty()) {
            return createErrorsResponse(errors);
        }

        DocPeriod docPeriod = new DocPeriod();
        docPeriod.setYear(periodYear);
        docPeriod.setPeriod(priodType);

        List<EDocType> docTypes = getDocTypes(page4210001);

        ErrorMsg errorMsg = relatedDocsService.completeStandardChecks(taxPayer, Collections.singletonList(FORM_CODE), taxOrg, docPeriod, docTypes);
        if (errorMsg != null) {
            String fieldName = "";
            if (isFinal(page7100001))
                fieldName = "dt_final";
            if (isMain(page4210001))
                fieldName = "dt_main";
            if (isRegular(page4210001))
                fieldName = "dt_regular";
            if (isAdditional(page4210001))
                fieldName = "dt_additional";
            if (isNotice(page4210001))
                fieldName = "dt_notice";
            errors.add(new FormError("form_421_00", "page_421_00_01", fieldName, errorMsg.getMsgRu(), errorMsg.getMsgKz()));
            return createErrorsResponse(errors);
        }

        RegisterDocResponce registerDocResponce = registerDocument(fno, signedXml, taxPayer, docTypes, docPeriod, draftId, now, now);
        AcceptResult retVal = new AcceptResult();
        retVal.setRegNumber(registerDocResponce.getDocNumber());
        retVal.setDocId(registerDocResponce.getDocId().toString());
        return retVal;
    }

    private RegisterDocResponce registerDocument(kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno fno, String docXml, BaseTaxPayer taxPayer, List<EDocType> docTypes,
                                                 DocPeriod docPeriod, Long draftId, Date acceptDate, Date submitDate) throws ParseException {

        kz.inessoft.sono.app.fno.f710.v22.services.dto.xml.Page7100001 page7100001 = fno.getForm71000().getSheetGroup().getPage7100001(); //TODO первый класс

        ChargeInfo chargeInfo = new V22ChargeInfoBuilder(fno, taxPayer, docPeriod, daysOffService, relatedDocsService).build();

        RegisterDocRequest registerDocRequest = RegisterDocRequestBuilder.newRequest()
                .setFormInfo(FORM_CODE, V22Constants.VERSION)
                .setDocumentXml(docXml)
                .setTaxPayer(taxPayer)
                .setDraftId(draftId)
                .setAcceptDate(acceptDate)
                .setSubmitDate(submitDate)
                .setDocTypes(docTypes)
                .setDocPeriod(docPeriod)
                .setTaxOrgCode(page7100001.getRatingAuthCode())
                .setChargeInfo(chargeInfo)
                .setGenerateReceptionNotification(true)
                .buildStandardRequest();

        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setIin(page7100001.getIin());
        payerInfo.setRnn(taxPayer.getRnn());
        registerDocRequest.setPayerInfo(payerInfo);

        return docsRegistryService.registerDocument(registerDocRequest);
    }



    private AcceptResult createErrorsResponse(List<FormError> errors) {
        AcceptResult retVal = new AcceptResult();
        retVal.setErrors(errors);
        return retVal;
    }
}
