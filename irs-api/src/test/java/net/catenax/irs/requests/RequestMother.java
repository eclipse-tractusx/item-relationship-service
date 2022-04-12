package net.catenax.irs.requests;

import com.github.javafaker.Faker;
import net.catenax.irs.component.Job;
import net.catenax.irs.dtos.ItemsTreeView;
import net.catenax.irs.entities.EntitiesMother;

import static net.catenax.irs.dtos.ValidationConstants.VIN_FIELD_LENGTH;

public class RequestMother {
    /**
     * JavaFaker instance used to generate random data.
     */
    private final Faker faker = new Faker();

    private final EntitiesMother generate = new EntitiesMother();

    public PartsTreeByVinRequest byVin(String vin) {
        return PartsTreeByVinRequest.builder()
                .vin(vin)
                .view(faker.options().option(ItemsTreeView.class).name())
                .build();
    }

    public PartsTreeByObjectIdRequest byObjectId(Job partId) {
        return PartsTreeByObjectIdRequest.builder()
                .oneIDManufacturer(partId.getJobId().toString())
                .objectIDManufacturer(partId.getOwner())
                .view(faker.options().option(ItemsTreeView.class).name())
                .build();
    }

    public PartsTreeByObjectIdRequest byObjectId() {
        return byObjectId(generate.job());
    }

    public PartsTreeByVinRequest byVin() {
        return byVin(faker.lorem().characters(VIN_FIELD_LENGTH));
    }
}
