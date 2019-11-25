!!! плагин работает только на Oracle Jre 11 версии(если у вас JAVA_HOME другой то на xjc.sh/xjc.bat(метка ORACLE JRE 11) установить путь к 11 jave)

Для запска планига в атрибутах гомманодной строке укажите -Xknp-generate
Параметр -onlydto используется если надо сгенерировать только пакет kz.inessoft.sono.app.fno.fXXX.vXX.services.dto


Примеры запуска KnpXJCPlugin генератора(для UNIX систем sh xjc.sh):
 файлы генерируется в папке genSRC, пожно указать папку своего проекта
 -p указывает пакет куда xml классы генерировать, лучше указать в фармате kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml где fXXX код фно, vXX версия фно
xjc.bat -d genSRC 200v29.xsd -p kz.inessoft.sono.app.fno.f200.v29.services.dto.xml -Xknp-generate
xjc.bat -d genSRC 300v25format1.xsd -p kz.inessoft.sono.app.fno.f300.v25.services.dto.xml -Xknp-generate
xjc.bat -d genSRC 101.04v20.xsd -p kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.xml -Xknp-generate
xjc.bat -d genSRC 400.00v20.xsd -p kz.inessoft.sono.app.fno.f400.v20.services.dto.xml -Xknp-generate


Плагин генерирует следующие классы(DTO пакет)
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.BaseV29Converter
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.RestToXMLConverter
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.XMLToRestConverter
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.IPage* интерфейсы для страницы

kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.* классы которые генерируется самой xjc 
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.rest.* соответствующие рест классы


(Вспомогательный MOCK файлы, можно отключить если указать -onlydto)
kz.inessoft.sono.app.fno.fXXX.vXX.rest.VXXRestController

kz.inessoft.sono.app.fno.fXXX.vXX.services.flk.ABaseFXXXVXXFlk
kz.inessoft.sono.app.fno.fXXX.vXX.services.flk.FXXXForm00VXXFlk

kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXChargeInfoBuilder
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXConstants
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXFLKProcessor
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXService
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils

kz.inessoft.sono.app.fno.fXXX.FXXXApplication
kz.inessoft.sono.app.fno.fXXX.FXXXChargeCallback
kz.inessoft.sono.app.fno.fXXX.FXXXConfiguration
kz.inessoft.sono.app.fno.fXXX.FXXXConstants