package kz.inessoft.sono.app.fno.fXXX.vXX.rest;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXService;
import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.rest.Fno;
import kz.inessoft.sono.lib.fno.utils.rest.AcceptResult;
import kz.inessoft.sono.lib.fno.utils.rest.SaveDraftResponse;
import kz.inessoft.sono.lib.sso.UserInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import java.text.ParseException;

@RestController
@RequestMapping("/fno/x_form_path/x_fno_version")
public class VXXRestController {
    private VXXService vXXService;

    @Autowired
    public VXXRestController(VXXService vXXService) {
        this.vXXService = vXXService;
    }

    @GetMapping(value = "/newPrefilledForm/{year}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fno getPrefilledForm(@PathVariable("year") int year, Authentication authentication) {
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        return vXXService.getPrefilledForm(userInfo, year);
    }

    @GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fno getDocument(@PathVariable("id") Long id, Authentication authentication) throws TransformerException, JAXBException {
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        return vXXService.getDocument(id, userInfo);
    }

    @PostMapping(value = "/serializeToXmlForSign", produces = MediaType.APPLICATION_XML_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String serializeToXmlForSign(@RequestBody Fno form) throws JAXBException, TransformerException {
        return vXXService.serializeToXmlForSign(form);
    }

    @PostMapping(value = "/serializeToXmlForPrint", produces = MediaType.APPLICATION_XML_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String serializeToXmlForPrint(@RequestBody Fno form) throws JAXBException, TransformerException {
        return vXXService.serializeToXmlForPrint(form);
    }

    @PostMapping(value = "/acceptForm", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_XML_VALUE)
    public AcceptResult acceptForm(@RequestBody String signedXml) throws JAXBException, TransformerException, SAXException, ParseException {
        return acceptForm(signedXml, null);
    }

    @PostMapping(value = "/acceptForm/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_XML_VALUE)
    public AcceptResult acceptForm(@RequestBody String signedXml, @PathVariable("id") Long draftId) throws JAXBException, TransformerException, SAXException, ParseException {
        return vXXService.acceptForm(signedXml, draftId);
    }

    @PostMapping(value = "/saveDraft", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SaveDraftResponse saveNewDraft(@RequestBody Fno form, Authentication authentication) throws JAXBException, TransformerException {
        return saveDraft(form, null, authentication);
    }

    @PostMapping(value = "/saveDraft/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public SaveDraftResponse saveDraft(@RequestBody Fno form, @PathVariable("id") Long id, Authentication authentication) throws JAXBException, TransformerException {
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        return vXXService.saveDraft(form, id, userInfo);
    }

    @GetMapping(value = "/draft/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Fno getDraft(@PathVariable("id") Long id, Authentication authentication) throws TransformerException, JAXBException {
        UserInfo userInfo = (UserInfo) authentication.getPrincipal();
        return vXXService.getDraft(id, userInfo);
    }
}
