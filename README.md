xjc.bat -d gen form_421_00_v19_transformed.xml -p kz.inessoft.sono.app.fno.f421.v19.services.dto.xml -Xknp-generate
xjc.bat -d gen form_710_00_v22_r53_transformed.xml -p kz.inessoft.sono.app.fno.f710.v22.services.dto.xml -Xknp-generate

xjc.bat -d gen form_421_00_v19_transformed.xml -p kz.dto.xml -Xknp-generate

sh xjc.sh -d gen form_421_00_v19_transformed.xml -p kz.inessoft.sono.app.fno.f421.v19.services.dto.xml -Xknp-generate

sh xjc.sh -d gen form_710_00_v22_r53_transformed.xml -p kz.inessoft.sono.app.fno.f710.v22.services.dto.xml -Xknp-generate
