package net.catenax.irs.connector.consumer.service;

import net.catenax.irs.client.model.PartRelationshipsWithInfos;
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
class PartsTreesAssemblerTest {

    RequestMother generate = new RequestMother();
    PartRelationshipsWithInfos output = generate.irsOutput();
    @Spy
    Monitor monitor = new ConsoleMonitor();
    @InjectMocks
    PartsTreesAssembler sut;

    @Test
    void retrievePartsTrees_WithEmptyInput_ReturnsEmptyResult() {
        // Act
        var result = sut.retrievePartsTrees(Stream.empty());

        // Assert
        assertThat(result).isEqualTo(output);
    }

    @Test
    void retrievePartsTrees_WithOneInput_ReturnsPartRelationshipInput() {
        // Arrange
        var relationship = generate.relationship();
        output.addRelationshipsItem(relationship);

        // Act
        var result = sut.retrievePartsTrees(Stream.of(output));

        // Assert
        assertThat(result).isEqualTo(output);
    }


    @Test
    void retrievePartsTrees_WithMultipleInputs_CombinesPartRelationshipsWithoutDuplicates() {
        // Arrange
        var relationship1 = generate.relationship();
        var relationship2 = generate.relationship();
        var relationship3 = generate.relationship();
        var relationship4 = generate.relationship();
        var irsOutput1 = generate.irsOutput()
                .addRelationshipsItem(relationship1)
                .addRelationshipsItem(relationship2);
        var irsOutput2 = generate.irsOutput()
                .addRelationshipsItem(relationship1)
                .addRelationshipsItem(relationship3);
        var irsOutput3 = generate.irsOutput()
                .addRelationshipsItem(relationship4);

        // Act
        var result = sut.retrievePartsTrees(Stream.of(
                irsOutput1,
                irsOutput2,
                irsOutput1,
                irsOutput3));

        // Assert
        var irsOutput = generate.irsOutput()
                .addRelationshipsItem(relationship1)
                .addRelationshipsItem(relationship2)
                .addRelationshipsItem(relationship3)
                .addRelationshipsItem(relationship4);
        assertThat(result).isEqualTo(irsOutput);
    }

    @Test
    void retrievePartsTrees_WithOneInput_ReturnsPartInfoInput() {
        // Arrange
        var partInfo = generate.partInfo();
        var irsOutput = generate.irsOutput().addPartInfosItem(partInfo);

        // Act
        var result = sut.retrievePartsTrees(Stream.of(irsOutput));

        // Assert
        assertThat(result).isEqualTo(irsOutput);
    }

    @Test
    void retrievePartsTrees_WithMultipleInputs_CombinesPartInfosWithoutDuplicates() {
        // Arrange
        var partInfo1 = generate.partInfo();
        var partInfo2 = generate.partInfo();
        var partInfo3 = generate.partInfo();
        var partInfo4 = generate.partInfo();
        var irsOutput1 = generate.irsOutput()
                .addPartInfosItem(partInfo1)
                .addPartInfosItem(partInfo2);
        var irsOutput2 = generate.irsOutput()
                .addPartInfosItem(partInfo1)
                .addPartInfosItem(partInfo3);
        var irsOutput3 = generate.irsOutput()
                .addPartInfosItem(partInfo4);

        // Act
        var result = sut.retrievePartsTrees(Stream.of(
                irsOutput1,
                irsOutput2,
                irsOutput1,
                irsOutput3));

        // Assert
        var irsOutput = generate.irsOutput()
                .addPartInfosItem(partInfo1)
                .addPartInfosItem(partInfo2)
                .addPartInfosItem(partInfo3)
                .addPartInfosItem(partInfo4);
        assertThat(result).isEqualTo(irsOutput);
    }

}