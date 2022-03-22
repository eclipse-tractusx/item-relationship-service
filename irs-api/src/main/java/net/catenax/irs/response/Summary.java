package net.catenax.irs.response;

import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@Builder
public class Summary {

   /**
    * Asynchronous fetch item informaiton
    */
   AsyncFetchedItems asyncFetchedItems;

}
