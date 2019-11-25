package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.*;
import kz.inessoft.sono.app.fno.fXXX.vXX.services.flk.FXXXFormXXVXXFlk;
import kz.inessoft.sono.lib.fno.utils.rest.FormError;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils.*;

public class VXXFLKProcessor {
    private Fno fno;
    public VXXFLKProcessor(Fno fno) {
        this.fno = fno;
    }

    public List<FormError> doFlk() {
        List<FormError> errors = new ArrayList<>();

        //TODO реализовать проверку ФЛК(желательно для каждой формы своя реализация )
        new FXXXFormXXVXXFlk(fno, errors).doFlk();
        new FXXXFormXXVXXFlk(fno, errors).doFlk();
        new FXXXFormXXVXXFlk(fno, errors).doFlk();
        return errors;
    }
}
