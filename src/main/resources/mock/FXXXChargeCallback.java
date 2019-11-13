package kz.inessoft.sono.app.fno.fXXX;

import kz.inessoft.sono.lib.charge.client.api.ChargeCallBack;
import kz.inessoft.sono.lib.charge.dtos.processed.ChargeInfo;
import kz.inessoft.sono.lib.charge.dtos.processed.Registry;
import kz.inessoft.sono.lib.docs.registry.client.api.DocsRegistryService;
import kz.inessoft.sono.lib.services.commons.document.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@Service
public class FXXXChargeCallback implements ChargeCallBack {
    private DocsRegistryService docsRegistryService;

    @Autowired
    public FXXXChargeCallback(DocsRegistryService docsRegistryService) {
        this.docsRegistryService = docsRegistryService;
    }

    @Override
    public void chargeProcessed(ChargeInfo chargeProcessedInfo) {
        Set<String> errorsCharge = new HashSet<>();
        EDocumentStatus status = EDocumentStatus.CHARGED;
        ArrayList<Charge> charges = new ArrayList<>();
        for (Registry registry : chargeProcessedInfo.getRegistries()) {
            switch (registry.getChargeStatus()) {
                case CHARGED:
                    for (kz.inessoft.sono.lib.charge.dtos.processed.Charge charge : registry.getCharges()) {
                        Charge notifCharge = new Charge();
                        notifCharge.setAmount(charge.getAmount());
                        notifCharge.setCurrencyCode(registry.getCurrencyCode());
                        notifCharge.setKbk(charge.getKbk());
                        notifCharge.setPayDate(charge.getPaymentDate());
                        charges.add(notifCharge);
                    }
                    break;
                case NOT_CHARGED:
                case ERROR_CHARGED:
                    status = EDocumentStatus.CHARGE_ERROR;
                    if (registry.getError() != null && isNotBlank(registry.getError().getMessage())
                            && !errorsCharge.contains(registry.getError().getMessage()))
                        errorsCharge.add(registry.getError().getMessage());
            }
        }
        ChangeStatusRequest changeStatusRequest = new ChangeStatusRequest();
        changeStatusRequest.setDocRegistryId(chargeProcessedInfo.getDocumentId());
        changeStatusRequest.setStatus(status);
        NotificationStatusInfo notificationStatusInfo = new NotificationStatusInfo();
        changeStatusRequest.setNotificationStatusInfo(notificationStatusInfo);
        notificationStatusInfo.setStatusDate(new Date());
        notificationStatusInfo.setSystem(ESystem.CULS);
        notificationStatusInfo.setDescription("Обработка документа налоговой отчетности");
        notificationStatusInfo.setCharges(charges);
        notificationStatusInfo.setErrors(errorsCharge.stream().reduce(null, (s, s2) -> {
            if (s == null)
                return s2;
            return s + "\n" + s2;
        }));
        docsRegistryService.setDocumentStatus(changeStatusRequest);    }
}