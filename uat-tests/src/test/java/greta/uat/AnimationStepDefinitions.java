package greta.uat;

import io.cucumber.java.en.*;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Step definitions for Animation Creation and Management UAT scenarios
 */
public class AnimationStepDefinitions {
    
    private WebDriver driver;
    private WebDriverWait wait;
    private Response lastResponse;
    private String currentAnimationId;
    private String baseUrl = System.getProperty("greta.test.url", "http://localhost:8080");
    
    public AnimationStepDefinitions() {
        RestAssured.baseURI = baseUrl;
        // WebDriver initialization would be done in @Before hook
    }
    
    @Given("the Greta platform is running")
    public void theGretaPlatformIsRunning() {
        // Verify platform is accessible
        given()
        .when()
            .get("/health")
        .then()
            .statusCode(200);
    }
    
    @Given("I am logged in as a content creator")
    public void iAmLoggedInAsAContentCreator() {
        // Perform login via API or UI
        lastResponse = given()
            .contentType("application/json")
            .body("""
                {
                    "username": "content_creator",
                    "password": "test_password",
                    "role": "creator"
                }
                """)
        .when()
            .post("/api/auth/login")
        .then()
            .statusCode(200)
            .extract()
            .response();
        
        String token = lastResponse.jsonPath().getString("token");
        RestAssured.requestSpecification = given().header("Authorization", "Bearer " + token);
    }
    
    @When("I navigate to the animation creation page")
    public void iNavigateToTheAnimationCreationPage() {
        if (driver != null) {
            driver.get(baseUrl + "/animations/create");
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("animation-form")));
        }
    }
    
    @When("I select {string} as the animation type")
    public void iSelectAsTheAnimationType(String animationType) {
        if (driver != null) {
            WebElement typeSelect = driver.findElement(By.id("animation-type"));
            typeSelect.click();
            WebElement option = driver.findElement(By.xpath("//option[@value='" + animationType + "']"));
            option.click();
        }
    }
    
    @When("I enter {string} as the animation name")
    public void iEnterAsTheAnimationName(String animationName) {
        if (driver != null) {
            WebElement nameInput = driver.findElement(By.id("animation-name"));
            nameInput.clear();
            nameInput.sendKeys(animationName);
        }
    }
    
    @When("I set the duration to {string} seconds")
    public void iSetTheDurationToSeconds(String duration) {
        if (driver != null) {
            WebElement durationInput = driver.findElement(By.id("animation-duration"));
            durationInput.clear();
            durationInput.sendKeys(duration);
        }
    }
    
    @When("I click the create animation button")
    public void iClickTheCreateAnimationButton() {
        if (driver != null) {
            WebElement createButton = driver.findElement(By.id("create-animation-btn"));
            createButton.click();
        } else {
            // API-based approach
            lastResponse = given()
                .contentType("application/json")
                .body("""
                    {
                        "type": "gesture",
                        "name": "wave",
                        "duration": 2.0,
                        "parameters": {}
                    }
                    """)
            .when()
                .post("/api/animation/create")
            .then()
                .statusCode(anyOf(is(200), is(201)))
                .extract()
                .response();
            
            currentAnimationId = lastResponse.jsonPath().getString("id");
        }
    }
    
    @Then("I should see a success message")
    public void iShouldSeeASuccessMessage() {
        if (driver != null) {
            WebElement successMessage = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("success-message"))
            );
            assertTrue(successMessage.isDisplayed());
            assertTrue(successMessage.getText().toLowerCase().contains("success"));
        } else {
            // API response validation
            assertNotNull(currentAnimationId);
            assertTrue(lastResponse.getStatusCode() == 200 || lastResponse.getStatusCode() == 201);
        }
    }
    
    @Then("the animation should appear in my animations list")
    public void theAnimationShouldAppearInMyAnimationsList() {
        if (driver != null) {
            driver.get(baseUrl + "/animations");
            WebElement animationList = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("animations-list"))
            );
            
            List<WebElement> animations = animationList.findElements(By.className("animation-item"));
            boolean found = animations.stream()
                .anyMatch(elem -> elem.getText().contains("wave"));
            assertTrue(found, "Animation should appear in the list");
        } else {
            // API validation
            Response response = given()
            .when()
                .get("/api/animation/list")
            .then()
                .statusCode(200)
                .extract()
                .response();
            
            List<Map<String, Object>> animations = response.jsonPath().getList("animations");
            boolean found = animations.stream()
                .anyMatch(anim -> currentAnimationId.equals(anim.get("id")));
            assertTrue(found, "Animation should be in the list");
        }
    }
    
    @Then("the animation should have status {string}")
    public void theAnimationShouldHaveStatus(String expectedStatus) {
        if (currentAnimationId != null) {
            given()
                .pathParam("id", currentAnimationId)
            .when()
                .get("/api/animation/{id}/status")
            .then()
                .statusCode(200)
                .body("status", equalTo(expectedStatus));
        }
    }
    
    @Given("I am on the animation creation page")
    public void iAmOnTheAnimationCreationPage() {
        iNavigateToTheAnimationCreationPage();
    }
    
    @When("I configure a {string} gesture animation")
    public void iConfigureAGestureAnimation(String gestureName) {
        if (driver != null) {
            iSelectAsTheAnimationType("gesture");
            iEnterAsTheAnimationName(gestureName);
            iSetTheDurationToSeconds("1.5");
        }
    }
    
    @When("I click the preview button")
    public void iClickThePreviewButton() {
        if (driver != null) {
            WebElement previewButton = driver.findElement(By.id("preview-animation-btn"));
            previewButton.click();
        }
    }
    
    @Then("I should see a 3D preview of the animation")
    public void iShouldSeeA3DPreviewOfTheAnimation() {
        if (driver != null) {
            WebElement previewContainer = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("animation-preview"))
            );
            assertTrue(previewContainer.isDisplayed());
            
            // Check for 3D canvas element
            WebElement canvas = previewContainer.findElement(By.tagName("canvas"));
            assertTrue(canvas.isDisplayed());
        }
    }
    
    @Then("the preview should last approximately {double} seconds")
    public void thePreviewShouldLastApproximatelySeconds(double expectedDuration) {
        // This would require timing the preview animation
        // Implementation would depend on the specific preview mechanism
        if (driver != null) {
            long startTime = System.currentTimeMillis();
            
            // Wait for animation to complete
            wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.className("animation-playing")
            ));
            
            long endTime = System.currentTimeMillis();
            double actualDuration = (endTime - startTime) / 1000.0;
            
            // Allow for 0.5 second tolerance
            assertTrue(Math.abs(actualDuration - expectedDuration) < 0.5,
                "Preview duration should be approximately " + expectedDuration + " seconds");
        }
    }
    
    @Then("I should see animation controls \\(play, pause, restart)")
    public void iShouldSeeAnimationControls() {
        if (driver != null) {
            WebElement playButton = driver.findElement(By.id("play-btn"));
            WebElement pauseButton = driver.findElement(By.id("pause-btn"));
            WebElement restartButton = driver.findElement(By.id("restart-btn"));
            
            assertTrue(playButton.isDisplayed());
            assertTrue(pauseButton.isDisplayed());
            assertTrue(restartButton.isDisplayed());
        }
    }
    
    @When("I start creating a new animation")
    public void iStartCreatingANewAnimation() {
        iNavigateToTheAnimationCreationPage();
    }
    
    @When("I add a facial expression {string} with intensity {double}")
    public void iAddAFacialExpressionWithIntensity(String expression, double intensity) {
        if (driver != null) {
            WebElement addModalityBtn = driver.findElement(By.id("add-modality-btn"));
            addModalityBtn.click();
            
            WebElement modalityType = driver.findElement(By.id("modality-type"));
            modalityType.sendKeys("facial");
            
            WebElement expressionField = driver.findElement(By.id("facial-expression"));
            expressionField.sendKeys(expression);
            
            WebElement intensityField = driver.findElement(By.id("facial-intensity"));
            intensityField.sendKeys(String.valueOf(intensity));
            
            WebElement addBtn = driver.findElement(By.id("add-modality-confirm"));
            addBtn.click();
        }
    }
    
    @When("I add a gesture {string} with target coordinates \\({int}, {int})")
    public void iAddAGestureWithTargetCoordinates(String gesture, int x, int y) {
        if (driver != null) {
            WebElement addModalityBtn = driver.findElement(By.id("add-modality-btn"));
            addModalityBtn.click();
            
            WebElement modalityType = driver.findElement(By.id("modality-type"));
            modalityType.sendKeys("gesture");
            
            WebElement gestureField = driver.findElement(By.id("gesture-name"));
            gestureField.sendKeys(gesture);
            
            WebElement xField = driver.findElement(By.id("target-x"));
            xField.sendKeys(String.valueOf(x));
            
            WebElement yField = driver.findElement(By.id("target-y"));
            yField.sendKeys(String.valueOf(y));
            
            WebElement addBtn = driver.findElement(By.id("add-modality-confirm"));
            addBtn.click();
        }
    }
    
    @When("I add a gaze direction towards {string}")
    public void iAddAGazeDirectionTowards(String target) {
        if (driver != null) {
            WebElement addModalityBtn = driver.findElement(By.id("add-modality-btn"));
            addModalityBtn.click();
            
            WebElement modalityType = driver.findElement(By.id("modality-type"));
            modalityType.sendKeys("gaze");
            
            WebElement targetField = driver.findElement(By.id("gaze-target"));
            targetField.sendKeys(target);
            
            WebElement addBtn = driver.findElement(By.id("add-modality-confirm"));
            addBtn.click();
        }
    }
    
    @When("I set the overall duration to {double} seconds")
    public void iSetTheOverallDurationToSeconds(double duration) {
        iSetTheDurationToSeconds(String.valueOf(duration));
    }
    
    @When("I synchronize all modalities to start at {double} seconds")
    public void iSynchronizeAllModalitiesToStartAtSeconds(double startTime) {
        if (driver != null) {
            List<WebElement> modalityItems = driver.findElements(By.className("modality-item"));
            for (WebElement item : modalityItems) {
                WebElement startTimeField = item.findElement(By.className("start-time"));
                startTimeField.clear();
                startTimeField.sendKeys(String.valueOf(startTime));
            }
        }
    }
    
    @Then("the animation timeline should show all three modalities")
    public void theAnimationTimelineShouldShowAllThreeModalities() {
        if (driver != null) {
            WebElement timeline = driver.findElement(By.id("animation-timeline"));
            List<WebElement> modalityTracks = timeline.findElements(By.className("modality-track"));
            assertEquals(3, modalityTracks.size(), "Timeline should show 3 modality tracks");
        }
    }
    
    @Then("each modality should be properly timed")
    public void eachModalityShouldBeProperlyTimed() {
        if (driver != null) {
            List<WebElement> modalityItems = driver.findElements(By.className("modality-item"));
            for (WebElement item : modalityItems) {
                WebElement startTime = item.findElement(By.className("start-time"));
                double time = Double.parseDouble(startTime.getAttribute("value"));
                assertEquals(0.5, time, 0.1, "All modalities should start at 0.5 seconds");
            }
        }
    }
    
    @When("I save the animation")
    public void iSaveTheAnimation() {
        if (driver != null) {
            WebElement saveButton = driver.findElement(By.id("save-animation-btn"));
            saveButton.click();
        }
    }
    
    @Then("it should be saved successfully with all modalities")
    public void itShouldBeSavedSuccessfullyWithAllModalities() {
        if (driver != null) {
            WebElement successMessage = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("save-success"))
            );
            assertTrue(successMessage.isDisplayed());
        }
        
        // Verify via API that animation was saved with modalities
        if (currentAnimationId != null) {
            given()
                .pathParam("id", currentAnimationId)
            .when()
                .get("/api/animation/{id}")
            .then()
                .statusCode(200)
                .body("modalities", hasSize(3))
                .body("modalities[0].type", notNullValue())
                .body("modalities[1].type", notNullValue())
                .body("modalities[2].type", notNullValue());
        }
    }
}