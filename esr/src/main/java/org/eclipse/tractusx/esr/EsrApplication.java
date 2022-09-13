package org.eclipse.tractusx.esr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@SpringBootApplication
public class EsrApplication {

    /**
     * Entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        SpringApplication.run(EsrApplication.class, args);
    }

}
