package net.catenax.irs.services;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import lombok.NonNull;
import net.catenax.irs.component.IrsPartRelationshipsWithInfos;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.requests.IrsPartsTreeRequest;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;

public interface IIrsPartTreeQueryService {

   IrsPartRelationshipsWithInfos registerItemJob(@NonNull final IrsPartsTreeRequest request);

   Jobs jobLifecycle(@NonNull final String jobId);

   Optional<List<Job>> getJobsByProcessingState(@NonNull final String processingState);

   Job cancelJobById(@NonNull final String jobId);

}
