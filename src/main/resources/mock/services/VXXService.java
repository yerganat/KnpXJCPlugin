package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.IPageX000001;
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
import kz.inessoft.sono.lib.docs.registry.dtos.RegisterDocRequestBuilder
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
//        TODO надо автозаполнить данные !!!
//        FormX0000 formX0000 = new FormX0000();
//        retVal.setFormX0000(formX0000);
//
//        PageX000001 pageX000001 = new PageX000001();
//        formX0000.setPageX000001(pageX000001);
/

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

    private List<EDocType> getDocTypes(IPageX000001 pageX000001) {
        List<EDocType> docTypes = new ArrayList<>();
        if (isFinal(pageX000001))
            docTypes.add(EDocType.LIQUIDATION);
        if (isMain(pageX000001))
            docTypes.add(EDocType.PRIMARY);
        else if (isAdditional(pageX000001))
            docTypes.add(EDocType.ADDITIONAL);
        else if (isNotice(pageX000001))
            docTypes.add(EDocType.NOTICE);
        else if (isRegular(pageX000001))
            docTypes.add(EDocType.REGULAR);
        return docTypes;
    }


    public SaveDraftResponse saveDraft(Fno fno, Long id, UserInfo userInfo) throws JAXBException, TransformerException {
        SaveDraftResponse retVal = new SaveDraftResponse();
        List<FormError> errors = new ArrayList<>();

        //TODO укажите нужную старницу !!!
        PageX000001 pageX000001 = fno.getFormX0000().getPageX000001();

        if (pageX000001.getPeriodYear() == null) {
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "period_year",
                    "Налоговый период, за который представляется налоговая отчетность не указан", "Налоговый период, за который представляется налоговая отчетность не указан"));
        }

        Integer periodMonth = Integer.valueOf(pageX000001.getPeriodMonth());
        if (periodMonth == null || periodMonth < 13 || periodMonth > 0) {
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "period_quarter",
                    "Налоговый период, за который представляется налоговая отчетность не указан", "Налоговый период, за который представляется налоговая отчетность не указан"));
        }


        if (!isMain(pageX000001) && !isRegular(pageX000001) && !isAdditional(pageX000001) && !isNotice(pageX000001)) {
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "dt_main",
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

        saveDraftRequest.setTaxOrgCode(pageX000001.getRatingAuthCode());
        DocPeriod docPeriod = new DocPeriod();
        docPeriod.setYear(Integer.valueOf(pageX000001.getPeriodYear()));

        docPeriod.setPeriod(EPeriod.getQuartalByNumber(periodMonth));
        saveDraftRequest.setDocPeriod(docPeriod);
        saveDraftRequest.setDocTypes(getDocTypes(pageX000001));
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

        //TODO укажите нужные поля
        form.getFormX0000().getPageX000001().setAcceptDate(strNow);
        form.getFormX0000().getPageX000001().setSubmitDate(strNow);
        return serializeToXml(form);
    }

    public String serializeToXmlForPrint(Fno form) throws JAXBException, TransformerException {
        return  serializeToXml(form);
    }

    public AcceptResult acceptForm(String signedXml, Long draftId) throws TransformerException, SAXException, JAXBException, ParseException {
        String xml = XSLTTransformer.convertFromOldSonoFormat(signedXml);
        XSDChecker.checkXml(xml, "/xsds/x_form_pathvXX.xsd", VXXService.class);  /
        TaxPayerCheckEDSResult taxPayerCheckEDSResult = checkSignService.checkTaxPayerEDS(signedXml);

        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno fno = (kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.Fno) unmarshaller.unmarshal(new StringReader(xml));
        List<FormError> errors = new ArrayList<>();

        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.PageX000001 pageX000001 = fno.getFormX0000().getSheetGroup().getPageX000001();
        if (taxPayerCheckEDSResult.getStatus() == EStatus.INVALID) {
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "iin", "ЭЦП не верна", "ЭЦП не верна")); //TODO перебить как в xml парметры
            return createErrorsResponse(errors);
        }

        Date now = new Date();

        String acceptDate = pageX000001.getAcceptDate();
        if (acceptDate != null && !date(now).equals(acceptDate))
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "accept_date", "Дата приема не равна текущей дате", "Дата приема не равна текущей дате"));

        String strSubmitDate = pageX000001.getSubmitDate();
        if (!date(now).equals(strSubmitDate))
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "submit_date", "Дата приема не равна текущей дате", "Дата приема не равна текущей дате"));

        String edsXin = taxPayerCheckEDSResult.getBin();
        if (edsXin == null)
            edsXin = taxPayerCheckEDSResult.getIin();
        String rnn = taxPayerCheckEDSResult.getRnn();
        BaseTaxPayer taxPayer = null;
        if (edsXin == null) {
            taxPayer = taxPayersService.findByRnn(rnn);
            if (taxPayer == null) {
                errors.add(new FormError("form_X00_00", "page_X00_00_01", "iin", "РНН декларанта из ЭЦП на найден в базе налогоплательщиков", "РНН декларанта из ЭЦП на найден в базе налогоплательщиков"));
                return createErrorsResponse(errors);
            }
            edsXin = taxPayer.getXin();
        }

        if (!pageX000001.getIin().equals(edsXin))
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "iin", "ИИН декларанта не совпадает с ИИН в ЭЦП", "ИИН декларанта не совпадает с ИИН в ЭЦП"));

        if (rnn == null) {
            taxPayer = taxPayersService.findByXin(edsXin);
            if (taxPayer == null) {
                errors.add(new FormError("form_X00_00", "page_X00_00_01", "iin", "ИИН/БИН декларанта из ЭЦП на найден в базе налогоплательщиков", "ИИН/БИН декларанта из ЭЦП на найден в базе налогоплательщиков"));
                return createErrorsResponse(errors);
            }
            rnn = taxPayer.getRnn();
        }

        Integer periodYear = Integer.valueOf(pageX000001.getPeriodYear());
        Integer periodMonth = Integer.valueOf(pageX000001.getPeriodMonth()); // TODO указать нужный период, месяц, квартал, полугодие
        if (periodYear < minYear ||
                periodYear == minYear && periodMonth != null && periodMonth < minMonth ||
                periodYear > maxYear ||
                periodYear == maxYear && periodMonth != null && periodMonth > maxMonth)
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "period_year", "Документ имеет не допустимый период", "Документ имеет не допустимый период"));


        Integer periodQuarter = Integer.valueOf(pageX000001.getPeriodQuarter());
        if (periodYear < minYear ||
                periodYear == minYear && periodQuarter != null && periodQuarter < minQuarter ||
                periodYear > maxYear ||
                periodYear == maxYear && periodQuarter != null && periodQuarter > maxQuarter)
            errors.add(new FormError("form_710_00", "page_X00_00_01", "period_year", "Документ имеет не допустимый период", "Документ имеет не допустимый период"));


        Calendar cal = GregorianCalendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        if (periodYear > year || periodYear == year && periodMonth != null && periodMonth > month)
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "period_year", "Налоговое обязательство по представлению налоговой отчетности исполняется налогоплательщиком (налоговым агентом) по окончании налогового периода, если иное не установлено Налоговым кодексом",
                    "Налоговое обязательство по представлению налоговой отчетности исполняется налогоплательщиком (налоговым агентом) по окончании налогового периода, если иное не установлено Налоговым кодексом"));

        EPeriod priodType = periodMonth == null ? EPeriod.YEAR : EPeriod.getQuartalByNumber(periodMonth);
        if (isAdditional(pageX000001) || isNotice(pageX000001)) {
            AuditRequest request = new AuditRequest();
            request.setYear(periodYear);
            request.setDocPeriod(priodType);
            request.setRnn(rnn);
            request.setKbkCodes(Arrays.asList(VXXChargeInfoBuilder.KBKXXXXX, VXXChargeInfoBuilder.KBKXXXXX));
            if (auditService.hasActiveAudit(request)) {
                errors.add(new FormError("form_X00_00", "page_X00_00_01", "iin",
                        "Регистрация данной ФНО невозможна. Проводится налоговая проверка.",
                        "Ұсынылған  СЕН тіркеу мүмкін емес. Салықтық тексеру жүргізілуде."));
            }
        }

        String taxOrg = pageX000001.getRatingAuthCode();
        if (dictTaxOrgService.getByCode(taxOrg) == null)
            errors.add(new FormError("form_X00_00", "page_X00_00_01", "rating_auth_code", "КОГД1. Введенное значение в справочнике отсутствует", "КОГД1. Енгізілген мағына кұжатта жоқ"));

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

        List<EDocType> docTypes = getDocTypes(pageX000001);

        ErrorMsg errorMsg = relatedDocsService.completeStandardChecks(taxPayer, Collections.singletonList(FORM_CODE), taxOrg, docPeriod, docTypes);
        if (errorMsg != null) {
            String fieldName = "";
            if (isFinal(pageX000001))
                fieldName = "dt_final";
            if (isMain(pageX000001))
                fieldName = "dt_main";
            if (isRegular(pageX000001))
                fieldName = "dt_regular";
            if (isAdditional(pageX000001))
                fieldName = "dt_additional";
            if (isNotice(pageX000001))
                fieldName = "dt_notice";
            errors.add(new FormError("form_X00_00", "page_X00_00_01", fieldName, errorMsg.getMsgRu(), errorMsg.getMsgKz()));
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

        kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.PageX000001 pageX000001 = fno.getFormX0000().getSheetGroup().getPageX000001(); //TODO первая старница ФНО

        ChargeInfo chargeInfo = new VXXChargeInfoBuilder(fno, taxPayer, docPeriod, daysOffService, relatedDocsService).build();

        RegisterDocRequest registerDocRequest = RegisterDocRequestBuilder.newRequest()
                .setFormInfo(FORM_CODE, VXXConstants.VERSION)
                .setDocumentXml(docXml)
                .setTaxPayer(taxPayer)
                .setDraftId(draftId)
                .setAcceptDate(acceptDate)
                .setSubmitDate(submitDate)
                .setDocTypes(docTypes)
                .setDocPeriod(docPeriod)
                .setTaxOrgCode(pageX000001.getRatingAuthCode())
                .setChargeInfo(chargeInfo)
                .setGenerateReceptionNotification(true)
                .buildStandardRequest();

        PayerInfo payerInfo = new PayerInfo();
        payerInfo.setIin(pageX000001.getIin());
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
