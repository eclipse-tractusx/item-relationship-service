//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.integrationtest;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import net.catenax.irs.IrsApplication;
import net.catenax.irs.configuration.IrsConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

@Tag("IntegrationTests")
@SpringBootTest(classes = { IrsApplication.class }, webEnvironment = RANDOM_PORT)
@DirtiesContext
public class IrsIntegrationTestsBase {

    /**
     * IRS Query path.
     */
    protected static final String PATH = "/api/v0.1/parts/{oneIDManufacturer}/{objectIDManufacturer}/partsTree";

    /**
     * IRS Query parameter name for selecting the query view.
     */
    protected static final String VIEW = "view";

    protected static final Faker faker = new Faker();

    @LocalServerPort
    private int port;

    protected final PartsTreeApiResponseMother expected = new PartsTreeApiResponseMother();

    /**
     * IRS configuration settings.
     */
    @Autowired
    protected IrsConfiguration configuration;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
    }
}
