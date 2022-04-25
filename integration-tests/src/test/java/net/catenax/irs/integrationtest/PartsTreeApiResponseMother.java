//
// Copyright (c) 2021 Copyright Holder (Catena-X Consortium)
//
// See the AUTHORS file(s) distributed with this work for additional
// information regarding authorship.
//
// See the LICENSE file(s) distributed with this work for
// additional information regarding license terms.
//
package net.catenax.irs.integrationtest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.catenax.irs.component.AsyncFetchedItems;
import net.catenax.irs.component.ChildItem;
import net.catenax.irs.component.Job;
import net.catenax.irs.component.Jobs;
import net.catenax.irs.component.QueryParameter;
import net.catenax.irs.component.Relationship;
import net.catenax.irs.component.Summary;
import net.catenax.irs.component.enums.AspectType;
import net.catenax.irs.component.enums.BomLifecycle;
import net.catenax.irs.component.enums.Direction;
import net.catenax.irs.controllers.ApiErrorsConstants;
import net.catenax.irs.dtos.ErrorResponse;
import net.catenax.irs.testing.BaseDtoMother;
import org.springframework.http.HttpStatus;

/**
 * Object Mother to generate data for integration tests.
 *
 * @see <a href="https://martinfowler.com/bliki/ObjectMother.html">
 * https://martinfowler.com/bliki/ObjectMother.html</a>
 */
public class PartsTreeApiResponseMother {

    /**
     * Base mother object that generates test data.
     */
    private final BaseDtoMother base = new BaseDtoMother();

    private static final UUID BOSCH_ONE_ID = new UUID(10L, 20L);
    private static final UUID ZF_ONE_ID = new UUID(10L, 20L);
    private static final UUID SCHAEFFLER_ONE_ID = new UUID(10L, 20L);
    private static final UUID BMW_ONE_ID = new UUID(10L, 20L);
    private static final String OBJECT_ID_GEARBOX = "I88HJHS45";
    private static final String OBJECT_ID_GEARWHEEL = "THYSFGG";
    private static final String OBJECT_ID_BEARING = "D47055319";
    private static final String VIN = "YS3DD78N4X7055320";
    private static final String ASPECT_CE = "CE";
    private static final Integer GEARBOX_QUEUE = 1;
    private static final Integer GEARBOX_RUNNING = 2;
    private static final Integer GEARBOX_COMPLETE = 3;
    private static final Integer GEARBOX_FAILED = 4;
    private static final BomLifecycle GEARBOX_BOMLIFECYCLE = BomLifecycle.AS_BUILT;
    private static final AspectType GEARBOX_ASPECT = AspectType.MATERIAL_ASPECT;
    private static final Integer GEARBOX_DEPTH = 6;
    private static final Direction GEARBOX_DIRECTION = Direction.DOWNWARD;

    private final Job gearboxJob = gearboxJob();
    private final ChildItem gearboxItem = gearboxChildItem();
    private final Job vehicleJob = vehicleJob();
    private final ChildItem vehicleItem = gearboxChildItem();
    private final Job bearingJob = bearingJob();
    private final ChildItem bearingItem = gearboxChildItem();
    private final Summary gearboxSummary = gearboxSummary();
    private final QueryParameter gearboxQueryParameter = gearboxQueryParameter();
    private final AsyncFetchedItems gearboxAsynchFetchedItems = gearboxAsynchFetchedItems();
    private final Job gearwheelJob = gearwheelJob();
    private final ChildItem gearwheelItem = gearboxChildItem();
    private final Summary gearwheelSummary = gearwheelSummary();
    private final AsyncFetchedItems gearwheelAsynchFetchedItems = gearwheelAsynchFetchedItems();
    private final Job gearwheelpinJob1 = gearwheelpinJob1();
    private final ChildItem gearwheelpinItem1 = gearboxChildItem();
    private final Summary gearwheelpinSummary1 = gearwheelpinSummary1();
    private final AsyncFetchedItems gearwheelpinAsynchFetchedItems1 = gearwheelpinAsynchFetchedItems1();
    private final Job gearwheelpinJob2 = gearwheelpinJob2();
    private final ChildItem gearwheelpinItem2 = gearboxChildItem();
    private final Summary gearwheelpinSummary2 = gearwheelpinSummary2();
    private final AsyncFetchedItems gearwheelpinAsynchFetchedItems2 = gearwheelpinAsynchFetchedItems2();

    private final List<Relationship> gearboxDirectChildren = List.of(relationship(gearboxItem, gearwheelItem));

    private final List<Relationship> gearboxPartsTree = List.of(relationship(gearboxItem, gearwheelItem),
            relationship(gearwheelItem, gearwheelpinItem1), relationship(gearwheelpinItem1, gearwheelpinItem2));

    private final List<Relationship> vehicleDirectChildren = List.of(relationship(vehicleItem, gearboxItem),
            relationship(vehicleItem, bearingItem));
    private final List<Relationship> vehiclePartsTree = Stream.concat(vehicleDirectChildren.stream(),
            gearboxPartsTree.stream()).collect(Collectors.toList());

    /**
     * Generate a {@link Jobs} containing fixed part tree of gearbox without aspects.
     *
     * @return a {@link Jobs} containing fixed part tree of gearbox without aspects.
     */
    public Jobs sampleGearboxPartTree() {

        return jobs(gearboxPartsTree, job(gearboxJob, gearboxSummary));
    }

    /**
     * Generate a {@link Jobs} containing fixed part tree of gearbox with aspects.
     *
     * @return a {@link Jobs} containing fixed part tree of gearbox with aspects.
     */
    public Jobs sampleGearboxPartTreeWithAspects() {
        return jobs(gearboxPartsTree, job(gearboxJob, gearboxSummary, gearboxQueryParameter));
    }

    /**
     * Generate a {@link Jobs} containing fixed data of gearbox direct children without aspects.
     *
     * @return a {@link Jobs} containing fixed data of gearbox direct children without aspects.
     */
    public Jobs sampleGearboxDirectChildren() {
        return jobs(gearboxDirectChildren, job(gearboxJob, gearboxSummary));
    }

    /**
     * Generate a {@link Jobs} containing fixed part tree of vehicle without aspects.
     *
     * @return a {@link Jobs} containing fixed part tree of vehicle without aspects.
     */
    public Jobs sampleVinPartTree() {

        return jobs(vehiclePartsTree, job(gearboxJob, gearboxSummary));
    }

    /**
     * Generate a {@link Jobs} containing fixed part tree of vehicle with aspects.
     *
     * @return a {@link Jobs} containing fixed part tree of vehicle with aspects.
     */
    public Jobs sampleVinPartTreeWithAspects() {
        return jobs(vehiclePartsTree, job(gearboxJob, gearboxSummary, gearboxQueryParameter));
    }

    /**
     * Generate a {@link Jobs} containing fixed data of vehicle direct children without aspects.
     *
     * @return a {@link Jobs} containing fixed data of vehicle direct children without aspects.
     */
    public Jobs sampleVinDirectChildren() {
        return jobs(vehicleDirectChildren, job(gearboxJob, gearboxSummary));
    }

    /**
     * Generate a {@link Jobs} containing fixed data of vehicle children and grandchildren without aspects.
     *
     * @return a {@link Jobs} containing fixed data of vehicle children and grandchildren without aspects.
     */
    public Jobs sampleVinGrandChildren() {
        return jobs(Stream.concat(vehicleDirectChildren.stream(), gearboxDirectChildren.stream())
                          .collect(Collectors.toList()), job(gearboxJob, gearboxSummary));
    }

    /**
     * Generate a {@link Jobs} containing fixed part tree of gearbox
     * with no children with part info.
     *
     * @return Guaranteed to never return {@literal null}.
     */
    public Jobs sampleLeafNodeGearboxPartTreeWithTypeName() {
        return jobs(List.of(), job(gearwheelpinJob2, gearwheelpinSummary2));
    }

    /**
     * Generates error response for entity not found scenario.
     *
     * @param errors List of errors.
     * @return An {@link ErrorResponse} object containing list of supplied errors.
     */
    public ErrorResponse entityNotFound(List<String> errors) {
        return ErrorResponse.builder()
                            .withStatusCode(HttpStatus.NOT_FOUND)
                            .withMessage(HttpStatus.NOT_FOUND.getReasonPhrase())
                            .withErrors(errors)
                            .build();
    }

    /**
     * Generates error response for invalid max depth provided scenario.
     *
     * @param errors List of errors.
     * @return An {@link ErrorResponse} object containing list of supplied errors.
     */
    public ErrorResponse invalidMaxDepth(List<String> errors) {
        return ErrorResponse.builder()
                            .withStatusCode(HttpStatus.BAD_REQUEST)
                            .withMessage(ApiErrorsConstants.INVALID_DEPTH)
                            .withErrors(errors)
                            .build();
    }

    /**
     * Generates error response for invalid arguments provided scenario.
     *
     * @param errors List of errors.
     * @return An {@link ErrorResponse} object containing list of supplied errors.
     */
    public ErrorResponse invalidArgument(List<String> errors) {
        return ErrorResponse.builder()
                            .withStatusCode(HttpStatus.BAD_REQUEST)
                            .withMessage(ApiErrorsConstants.INVALID_ARGUMENTS)
                            .withErrors(errors)
                            .build();
    }

    private Jobs jobs(List<Relationship> relationships, Job job) {
        return base.jobs(relationships, job);
    }

    private Relationship relationship(ChildItem child, ChildItem parent) {
        return base.relationship(child, parent);
    }

    private Job gearboxJob() {
        return base.job(ZF_ONE_ID, OBJECT_ID_GEARBOX);
    }

    private ChildItem gearboxChildItem() {
        return base.childItem(OBJECT_ID_GEARBOX, 1);
    }

    private Job vehicleJob() {
        return base.job(BMW_ONE_ID, VIN);
    }

    private Job bearingJob() {
        return base.job(SCHAEFFLER_ONE_ID, OBJECT_ID_BEARING);
    }

    private Summary gearboxSummary() {
        return base.summary(gearboxAsynchFetchedItems);
    }

    private QueryParameter gearboxQueryParameter() {
        return base.queryParameter(GEARBOX_BOMLIFECYCLE, GEARBOX_ASPECT, GEARBOX_DEPTH, GEARBOX_DIRECTION);
    }

    private AsyncFetchedItems gearboxAsynchFetchedItems() {
        return base.asynchFetchedItems(GEARBOX_QUEUE, GEARBOX_RUNNING, GEARBOX_COMPLETE, GEARBOX_FAILED);
    }

    private Job gearwheelJob() {
        return base.job(BOSCH_ONE_ID, OBJECT_ID_GEARWHEEL);
    }

    private Summary gearwheelSummary() {
        return base.summary(gearwheelAsynchFetchedItems);
    }

    private AsyncFetchedItems gearwheelAsynchFetchedItems() {
        return base.asynchFetchedItems(GEARBOX_QUEUE, GEARBOX_RUNNING, GEARBOX_COMPLETE, GEARBOX_FAILED);
    }

    private Job gearwheelpinJob1() {
        return base.job(BOSCH_ONE_ID, OBJECT_ID_GEARWHEEL);
    }

    private Summary gearwheelpinSummary1() {
        return base.summary(gearwheelpinAsynchFetchedItems1);
    }

    private AsyncFetchedItems gearwheelpinAsynchFetchedItems1() {
        return base.asynchFetchedItems(GEARBOX_QUEUE, GEARBOX_RUNNING, GEARBOX_COMPLETE, GEARBOX_FAILED);
    }

    private Job gearwheelpinJob2() {
        return base.job(BOSCH_ONE_ID, OBJECT_ID_GEARWHEEL);
    }

    private Summary gearwheelpinSummary2() {
        return base.summary(gearwheelpinAsynchFetchedItems2);
    }

    private AsyncFetchedItems gearwheelpinAsynchFetchedItems2() {
        return base.asynchFetchedItems(GEARBOX_QUEUE, GEARBOX_RUNNING, GEARBOX_COMPLETE, GEARBOX_FAILED);
    }

    private String ceAspectUrl(String name) {
        return "http://aspect-" + name + "/" + ASPECT_CE + "/1234";
    }

    private Job job(final Job job, final Summary summary, final QueryParameter queryParameter) {
        return base.job(job, summary, queryParameter);
    }

    private Job job(final Job job, final Summary summary) {
        return base.job(job, summary, null);
    }
}
