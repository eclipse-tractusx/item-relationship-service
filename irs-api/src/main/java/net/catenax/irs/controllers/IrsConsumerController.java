package net.catenax.irs.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class IrsConsumerController {

    private final JobHandler jobhandler;

    @Autowired
    public IrsConsumerController(JobHandler jobhandler) {
        this.jobhandler = jobhandler;
    }

}
