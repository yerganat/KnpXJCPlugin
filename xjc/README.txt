!!! плагин работает только на Oracle Jre 10/11 версии(если у вас JAVA_HOME другой то на xjc.sh/xjc.bat(метка ORACLE JRE 10/11) установить путь к 10/11 jave)

Для запуска планига в атрибутах комманодной строке укажите параметр -Xknp-generate
Параметр -onlydto используется если надо сгенерировать только пакет kz.inessoft.sono.app.fno.fXXX.vXX.services.dto


Примеры запуска KnpXJCPlugin генератора из папки bin(для UNIX систем sh xjc.sh):
xjc.bat -Xknp-generate -d genSRC 200v29.xsd -p kz.inessoft.sono.app.fno.f200.v29.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 300v25format1.xsd -p kz.inessoft.sono.app.fno.f300.v25.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 101.04v20.xsd -p kz.inessoft.sono.app.fno.f101.app04.v20.services.dto.xml
xjc.bat -Xknp-generate -d genSRC 400.00v20.xsd -p kz.inessoft.sono.app.fno.f400.v20.services.dto.xml
*файлы генерируется в папке genSRC, пожно указать папку своего проекта
 -p указывает пакет куда надо xml классы генерировать, лучше указать в фармате kz.inessoft.sono.app.fno.fXXX.vXX.services.dto.xml где fXXX код фно, vXX версия фно

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
