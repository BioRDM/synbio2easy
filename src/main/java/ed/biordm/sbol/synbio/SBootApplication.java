package ed.biordm.sbol.synbio;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class SBootApplication {

	public static void main(String[] args) {
            new SpringApplicationBuilder(SBootApplication.class)
              .web(WebApplicationType.NONE)
              .run(args);            

	}

}
