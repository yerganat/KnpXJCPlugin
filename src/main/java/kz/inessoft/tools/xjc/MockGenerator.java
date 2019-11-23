package kz.inessoft.tools.xjc;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static kz.inessoft.tools.xjc.KNPPlugin.*;

public class MockGenerator {

    public void generateMocks() {
        if(!GENERATE_ONLY_DTO) {
            this.genMockResourceContent(PKG_REST, "VXXRestController.java", "rest/");

            this.genMockResourceContent(PKG_SERVICE, "VXXChargeInfoBuilder.java", "services/");
            this.genMockResourceContent(PKG_SERVICE, "VXXConstants.java", "services/");
            this.genMockResourceContent(PKG_SERVICE, "VXXFLKProcessor.java", "services/");
            this.genMockResourceContent(PKG_SERVICE, "VXXService.java", "services/");
            this.genMockResourceContent(PKG_SERVICE, "VXXUtils.java", "services/");

            this.genMockResourceContent(PKG_SERVICE_FLK, "ABaseFXXXVXXFlk.java", "services/flk/");
            this.genMockResourceContent(PKG_SERVICE_FLK, "FXXXFormXXVXXFlk.java", "services/flk/"); //TODO FormXX заменить на имя Формы

            this.genMockResourceContent(PKG_BASE + ".", "FXXXApplication.java", "");
            this.genMockResourceContent(PKG_BASE + ".", "FXXXChargeCallback.java", "");
            this.genMockResourceContent(PKG_BASE + ".", "FXXXConfiguration.java", "");
            this.genMockResourceContent(PKG_BASE + ".", "FXXXConstants.java", "");
        }
    }


    private  void genMockResourceContent(String addPkg, String resName, String subDir) {
        try {
            String restDir = targetDir.getAbsolutePath() + File.separator + addPkg.replace('.', File.separatorChar);
            new File(restDir).mkdirs();
            String restPath = restDir + resName.replace("VXX", "V" + FNO_VERSION ).replace("FXXX", "F" + FORM_CODE_VALUE );

            logger.debug("Gen file " + restPath);


            InputStream inputStream = getClass().getResourceAsStream("/mock/" + subDir + resName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String restController = reader.lines()
                    .collect(Collectors.joining(System.lineSeparator()));

            restController = restController
                    .replace("fXXX", "f" + FORM_CODE_VALUE)
                    .replace("FXXX", "F" + FORM_CODE_VALUE)
                    .replace("vXX", "v"+FNO_VERSION)
                    .replace("VXX", "V"+FNO_VERSION)
                    .replace("x_form_path", FORM_CODE)
                    .replace("x_fno_version", "v" + FNO_VERSION)
                    .replace("x_only_fno_version", FNO_VERSION);
            Files.write( Paths.get(restPath), restController.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
