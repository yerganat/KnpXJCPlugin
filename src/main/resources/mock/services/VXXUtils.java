package kz.inessoft.sono.app.fno.fXXX.vXX.services;

import kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.IPage4210001;

import static org.apache.commons.lang.BooleanUtils.isTrue;

public class VXXUtils {
    public static boolean isFinal(IPage4210001 page4210001) { //TODO реализовать интерфейс самой первой страницы
        return page4210001.isDtFinal() != null && page4210001.isDtFinal();
    }

    public static boolean isMain(IPage4210001 page4210001) {
        return isTrue(page4210001.isDtMain());
    }

    public static boolean isAdditional(IPage4210001 page4210001) {
        return isTrue(page4210001.isDtAdditional());
    }

    public static boolean isNotice(IPage4210001 page4210001) {
        return isTrue(page4210001.isDtNotice());
    }

    public static boolean isRegular(IPage4210001 page4210001) {
        return isTrue(page4210001.isDtRegular());
    }
}
