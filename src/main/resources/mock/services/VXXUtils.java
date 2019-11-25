package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.IPageX000001;

import static org.apache.commons.lang.BooleanUtils.isTrue;

public class VXXUtils {
    public static boolean isFinal(IPageX000001 pageX000001) {
        return pageX000001.isDtFinal() != null && pageX000001.isDtFinal();
    }

    public static boolean isMain(IPageX000001 pageX000001) {
        return isTrue(pageX000001.isDtMain());
    }

    public static boolean isAdditional(IPageX000001 pageX000001) {
        return isTrue(pageX000001.isDtAdditional());
    }

    public static boolean isNotice(IPageX000001 pageX000001) {
        return isTrue(pageX000001.isDtNotice());
    }

    public static boolean isRegular(IPageX000001 pageX000001) {
        return isTrue(pageX000001.isDtRegular());
    }
}
