package net.catenax.irs.requests;

import com.github.javafaker.Faker;
import net.catenax.irs.dtos.ItemsTreeView;

import static net.catenax.irs.dtos.ValidationConstants.VIN_FIELD_LENGTH;

public class RequestMother {
    /**
     * JavaFaker instance used to generate random data.
     */
    private final Faker faker = new Faker();

    public PartsTreeByVinRequest byVin(String vin) {
        return PartsTreeByVinRequest.builder()
                .vin(vin)
                .view(faker.options().option(ItemsTreeView.class).name())
                .build();
    }

    public PartsTreeByVinRequest byVin() {
        return byVin(faker.lorem().characters(VIN_FIELD_LENGTH));
    }
}
