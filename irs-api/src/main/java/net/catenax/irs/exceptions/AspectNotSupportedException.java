package net.catenax.irs.exceptions;

import net.catenax.irs.controllers.ApiErrorsConstants;

public class AspectNotSupportedException extends RuntimeException {

   public AspectNotSupportedException() {
      super(ApiErrorsConstants.ASPECT_NOT_SUPPORTED);
   }

   public AspectNotSupportedException(final String message) {
      super(message);
   }
}
