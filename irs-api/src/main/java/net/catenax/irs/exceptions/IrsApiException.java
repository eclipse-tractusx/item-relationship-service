package net.catenax.irs.exceptions;

import java.time.Instant;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import net.catenax.irs.component.enums.StatusCodeEnum;

@Schema(description = "Detailed exception information")
@Value
@Jacksonized
@Builder(toBuilder = true)
public class IrsApiException {

   List<Throwable> exception;

   String errorDetails;

   Instant exceptionDate;

   StatusCodeEnum statusCode;

}
