package net.catenax.irs.connector.consumer.service;

import net.catenax.irs.component.Jobs;
import org.eclipse.dataspaceconnector.monitor.ConsoleMonitor;
import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class JobsTreesAssemblerTest {

    RequestMother generate = new RequestMother();
    Jobs output = generate.irsOutput();
    @Spy
    Monitor monitor = new ConsoleMonitor();
    @InjectMocks
    JobsTreesAssembler sut;

    @Test
    void retrieveJobsTrees_WithEmptyInput_ReturnsEmptyResult() {
        // Act
        var result = sut.retrieveJobsTrees(Stream.empty());

        // Assert
        assertThat(result).isEqualTo(output);
    }

    @Test
    void retrieveJobsTrees_WithOneInput_ReturnsRelationshipInput() {
        // Arrange
        var relationship = generate.relationship();
        output.toBuilder().relationship(relationship);

        // Act
        var result = sut.retrieveJobsTrees(Stream.of(output));

        // Assert
        assertThat(result).isEqualTo(output);
    }


    @Test
    void retrieveJobsTrees_WithMultipleInputs_CombinesJobRelationshipsWithoutDuplicates() {
        // Arrange
        var relationship1 = generate.relationship();
        var relationship2 = generate.relationship();
        var relationship3 = generate.relationship();
        var relationship4 = generate.relationship();
        var irsOutput1 = generate.irsOutput()
                .toBuilder().relationship(relationship1)
                            .relationship(relationship2).build();
        var irsOutput2 = generate.irsOutput()
                .toBuilder().relationship(relationship1)
                .relationship(relationship3).build();
        var irsOutput3 = generate.irsOutput()
                .toBuilder().relationship(relationship4).build();

        // Act
        var result = sut.retrieveJobsTrees(Stream.of(
                irsOutput1,
                irsOutput2,
                irsOutput1,
                irsOutput3));

        // Assert
        var irsOutput = generate.irsOutput()
                .toBuilder().relationship(relationship1)
                .relationship(relationship2)
                .relationship(relationship3)
                .relationship(relationship4).build();
        assertThat(result).isEqualTo(irsOutput);
    }

    @Test
    void retrieveJobsTrees_WithOneInput_ReturnsJobInput() {
        // Arrange
        var job = generate.job();
        var irsOutput = generate.irsOutput().toBuilder().job(job).build();

        // Act
        var result = sut.retrieveJobsTrees(Stream.of(irsOutput));

        // Assert
        assertThat(result).isEqualTo(irsOutput);
    }

    @Test
    void retrieveJobsTrees_WithMultipleInputs_CombinesJobsWithoutDuplicates() {
        // Arrange
        var job1 = generate.job();
        var job2 = generate.job();
        var job3 = generate.job();
        var job4 = generate.job();
        var irsOutput1 = generate.irsOutput().toBuilder()
                .job(job1)
                .job(job2).build();
        var irsOutput2 = generate.irsOutput().toBuilder()
                .job(job1)
                .job(job3).build();
        var irsOutput3 = generate.irsOutput().toBuilder()
                .job(job4).build();

        // Act
        var result = sut.retrieveJobsTrees(Stream.of(
                irsOutput1,
                irsOutput2,
                irsOutput1,
                irsOutput3));

        // Assert
        var irsOutput = generate.irsOutput().toBuilder()
                .job(job1)
                .job(job2)
                .job(job3)
                .job(job4).build();
        assertThat(result).isEqualTo(irsOutput);
    }

}
