package <%=packageName%>;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import no.skatteetaten.aurora.annotations.AuroraApplication;

@SpringBootApplication
@AuroraApplication
public class Main {

    protected Main() {
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

}
