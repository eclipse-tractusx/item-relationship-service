package net.catenax.irs.connector.consumer.service;

import com.github.javafaker.Faker;
import net.catenax.irs.component.*;
import net.catenax.irs.connector.requests.JobsTreeByCatenaXIdRequest;
import net.catenax.irs.connector.requests.JobsTreeRequest;
import org.eclipse.dataspaceconnector.spi.types.domain.transfer.TransferProcess;
import org.junit.jupiter.api.Disabled;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Disabled
public class RequestMother {

    Faker faker = new Faker();

    public JobsTreeByCatenaXIdRequest.JobsTreeByCatenaXIdRequestBuilder request() {
        return JobsTreeByCatenaXIdRequest.builder()
                .childCatenaXId(faker.company().name())
                .lifecycleContext(faker.lorem().characters(10, 20))
                .assembledOn(LocalDateTime.now())
                .lastModifiedOn(LocalDateTime.now())
                .depth(faker.number().numberBetween(1, 5));
    }

    public JobsTreeRequest jobsTreeRequest() {
        return JobsTreeRequest.builder()
                .byObjectIdRequest(request().build())
                .build();
    }

    public TransferProcess transferProcess() {
        return TransferProcess.Builder.newInstance()
                .id(faker.lorem().characters())
                .build();
    }

    public Job job() {
        var job = new Job(null, null, null, null, null, null, null, null, null, null);
        job.toBuilder().jobId(faker.lorem().characters(0, 10));
        job.toBuilder().owner(faker.lorem().characters(10, 20));
        job.toBuilder().lastModifiedOn(Instant.now());
        job.toBuilder().jobFinished(Instant.now());
        return job;
    }

    public ChildItem child() {
        var child = new ChildItem(null, null, null, null, null);
        child.toBuilder().childCatenaXId(faker.lorem().characters(10, 20));
        child.toBuilder().lifecycleContext(faker.lorem().characters(0, 10));
        child.toBuilder().assembledOn(Instant.now());
        child.toBuilder().lastModifiedOn(Instant.now());
        return child;
    }

    public Jobs irsOutput() {
        var obj = new Jobs(null, null, null);
        obj.toBuilder().job(new Job(null, null, null,null,null,null,null,null, null, null));
        obj.toBuilder().relationships(new ArrayList<Relationship>());
        obj.toBuilder().shells(Optional.of(new ArrayList<Shells>()));
        return obj;
    }

    public Relationship relationship() {
        return relationship(child(), child());
    }

    public Relationship relationship(ChildItem parent, ChildItem child) {
        var obj = new Relationship(null, null, null);
        obj.toBuilder().parentItem(parent);
        obj.toBuilder().childItem(child);
        return obj;
    }
}
