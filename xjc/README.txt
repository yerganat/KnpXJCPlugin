!!! плагин работает только на Oracle Jre 10/11 версии(если у вас JAVA_HOME другой то на xjc.sh/xjc.bat(метка ORACLE JRE 10/11) установить путь к 10/11 jave)

Для запуска планига в атрибутах комманодной строке укажите параметр -Xknp-generate
Параметр -onlydto используется если надо сгенерировать только пакет kz.inessoft.sono.app.fno.fXXX.vXX.services.dto


Примеры запуска KnpXJCPlugin генератора из папки bin(для UNIX систем sh xjc.sh):
xjc.bat -Xknp-generate -d genSRC 510.00v21.xsd -p kz.inessoft.sono.app.fno.f510.v21.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 590.00v15.xsd -p kz.inessoft.sono.app.fno.f590.v15.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 200v29.xsd -p kz.inessoft.sono.app.fno.f200.v29.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 300v25format1.xsd -p kz.inessoft.sono.app.fno.f300.v25.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 101.04v20.xsd -p kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 400.00v20.xsd -p kz.inessoft.sono.app.fno.f400.v20.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 220.00v33.xsd -p kz.inessoft.sono.app.fno.f220.v33.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 421.00v19.xsd -p kz.inessoft.sono.app.fno.f421.v19.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 570.00v17.xsd -p kz.inessoft.sono.app.fno.f570.v17.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 230.00v19.xsd -p kz.inessoft.sono.app.fno.f230.v19.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 240.00v15.xsd -p kz.inessoft.sono.app.fno.f240.v15.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 100.00v28.xsd -p kz.inessoft.sono.app.fno.f100.v28.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 150.00v16r68.xsd -p kz.inessoft.sono.app.fno.f150.v16.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 110.00v33r106.xsd -p kz.inessoft.sono.app.fno.f110.v33.services.dto.xml
xjc.sh -Xknp-generate -d genSRC form_100_00_v27_r98.xsd -p kz.inessoft.sono.app.fno.f100.v27.services.dto.xml
xjc.sh -Xknp-generate -d genSRC form_110_00_v32_r104.xsd -p kz.inessoft.sono.app.fno.f110.v32.services.dto.xml
sh xjc.sh -d genSRC risk/SurDataExplanation_Request.xsd -p kz.inessoft.sono.app.notification.risk.ehd.wsclient.model.explanation.request
sh xjc.sh -d genSRC risk/SurDataExplanation_Response.xsd -p kz.inessoft.sono.app.notification.risk.ehd.wsclient.model.explanation.response
sh xjc.sh -d genSRC risk/SurDataSend_Request.xsd -p kz.inessoft.sono.app.notification.risk.ehd.wsclient.model.risk.request
sh xjc.sh -d genSRC risk/SurDataSend_Response.xsd -p kz.inessoft.sono.app.notification.risk.ehd.wsclient.model.risk.response
sh xjc.sh -Xknp-generate -d genSRC 700.xsd -p kz.inessoft.sono.app.fno.f007.v8.services.dto.xml
sh xjc.sh -Xknp-generate -d genSRC 001.00v7.xsd -p kz.inessoft.sono.app.fno.f001.services.dto.xml
sh xjc.sh -Xknp-generate -d genSRC 250_00_v2.xsd -p kz.inessoft.sono.app.fno.f250.v2.services.dto.xml -onlydto
*файлы генерируется в папке genSRC, пожно указать папку своего проекта
 -p указывает пакет куда надо xml классы генерировать, лучше указать в фармате kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml где fXXX код фно, vXX версия фно
 если не указывать -p(пакет) то в текущей директории создается generated папка для xml классов

Плагин генерирует следующие классы(DTO пакет)
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.BaseV29Converter
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.RestToXMLConverter
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.XMLToRestConverter
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.IPage* интерфейсы для страниц

kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml.* классы XML которые генерируется самой xjc
kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.rest.* соответствующие REST классы


(Вспомогательный MOCK файлы, можно отключить генерацию MOCK если указать -onlydto, *надо дописать внутри mock файлов где есть TODO метки)
kz.inessoft.sono.app.fno.fXXX.vXX.rest.VXXRestController

kz.inessoft.sono.app.fno.fXXX.vXX.services.flk.ABaseFXXXVXXFlk
kz.inessoft.sono.app.fno.fXXX.vXX.services.flk.FXXXFormXXVXXFlk

kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXChargeInfoBuilder
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXConstants
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXFLKProcessor
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXService
kz.inessoft.sono.app.fno.fXXX.vXX.services.VXXUtils

kz.inessoft.sono.app.fno.fXXX.FXXXApplication
kz.inessoft.sono.app.fno.fXXX.FXXXChargeCallback
kz.inessoft.sono.app.fno.fXXX.FXXXConfiguration
kz.inessoft.sono.app.fno.fXXX.FXXXConstants
