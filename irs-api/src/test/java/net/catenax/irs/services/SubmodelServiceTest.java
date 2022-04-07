package net.catenax.irs.services;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import net.catenax.irs.aaswrapper.submodel.domain.AssemblyPartRelationship;
import net.catenax.irs.aaswrapper.submodel.domain.SubmodelClient;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SubmodelServiceTest {

    @Autowired
    private SubmodelClient client;

    @BeforeEach
    void setup() {
        when(this.client.getSubmodel("", "", "")).thenReturn(new AssemblyPartRelationship());
    }

    @Test
    public void whenGetSubmodel_thenListPayments() {
        final AssemblyPartRelationship json = this.client.getSubmodel("", "", "");

        assertNull(json);
    }
}