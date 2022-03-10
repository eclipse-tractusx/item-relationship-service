package net.catenax.prs.connector.requests;

import com.github.javafaker.Faker;


class RequestMother {

    static Faker faker = new Faker();

    static PartsTreeByObjectIdRequest generateApiRequest() {
        return PartsTreeByObjectIdRequest.builder()
                .oneIDManufacturer(faker.lorem().characters())
                .objectIDManufacturer(faker.lorem().characters())
                .view(faker.lorem().word())
                .build();
    }

    static PartsTreeRequest generatePartsTreeRequest() {
        return PartsTreeRequest.builder()
                .byObjectIdRequest(generateApiRequest())
                .build();
    }

    static String blank() {
        return faker.regexify("\\s+");
    }
}
