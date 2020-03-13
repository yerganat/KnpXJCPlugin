package kz.inessoft.sono.app.fno.fXXX;

import kz.inessoft.sono.lib.charge.client.ChangeStatusReqBuilder;
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
        ChangeStatusRequest changeStatusRequest = new ChangeStatusReqBuilder(chargeProcessedInfo).buildStandardReq();
        docsRegistryService.setDocumentStatus(changeStatusRequest);
    }
}