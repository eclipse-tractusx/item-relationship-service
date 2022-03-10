package net.catenax.prs.connector.consumer.service;

import com.github.javafaker.Faker;
import net.catenax.prs.client.model.PartId;
import net.catenax.prs.client.model.PartInfo;
import net.catenax.prs.client.model.PartRelationship;
import net.catenax.prs.client.model.PartRelationshipsWithInfos;
import net.catenax.prs.connector.requests.PartsTreeRequest;
import net.catenax.prs.connector.requests.PartsTreeByObjectIdRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;

import java.util.ArrayList;

public class RequestMother {

    Faker faker = new Faker();

    public PartsTreeByObjectIdRequest.PartsTreeByObjectIdRequestBuilder request() {
        return PartsTreeByObjectIdRequest.builder()
                .oneIDManufacturer(faker.company().name())
                .objectIDManufacturer(faker.lorem().characters(10, 20))
                .view("AS_BUILT")
                .depth(faker.number().numberBetween(1, 5));
    }

    public PartsTreeRequest partsTreeRequest() {
        return PartsTreeRequest.builder()
                .byObjectIdRequest(request().build())
                .build();
    }

    public TransferProcess transferProcess() {
        return TransferProcess.Builder.newInstance()
                .id(faker.lorem().characters())
                .build();
    }

    public PartId partId() {
        var partId = new PartId();
        partId.setOneIDManufacturer(faker.company().name());
        partId.setObjectIDManufacturer(faker.lorem().characters(10, 20));
        return partId;
    }

    public PartRelationshipsWithInfos prsOutput() {
        var obj = new PartRelationshipsWithInfos();
        obj.setRelationships(new ArrayList<>());
        obj.setPartInfos(new ArrayList<>());
        return obj;
    }

    public PartRelationship relationship() {
        return relationship(partId(), partId());
    }

    public PartRelationship relationship(PartId parent, PartId child) {
        var obj = new PartRelationship();
        obj.setParent(parent);
        obj.setChild(child);
        return obj;
    }

    public PartInfo partInfo() {
        var obj = new PartInfo();
        obj.setPart(partId());
        return obj;
    }
}
