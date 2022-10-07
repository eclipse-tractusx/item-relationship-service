package org.eclipse.tractusx.irs.cucumber;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class TestStepDefinition {
    private Integer int1;
    private Integer int2;
    private Integer result;

    @Given("I have entered {int} into the calculator")
    public void iHaveEnteredIntoTheCalculator(Integer int1) {
        this.int2 = this.int1;
        this.int1 = int1;
    }

    @When("I press add")
    public void iPressAdd() {
        this.result = this.int1 + this.int2;
    }

    @When("I press multiply")
    public void iPressMultiply() {
        this.result = this.int1 * this.int2;
    }

    @Then("the result should be {int} on the screen")
    public void theResultShouldBeOnTheScreen(Integer value) {
        assertThat(this.result).isEqualTo(value);
    }
}
