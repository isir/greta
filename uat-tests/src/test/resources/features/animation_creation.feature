Feature: Animation Creation and Management
  As a content creator
  I want to create and manage animations
  So that I can produce engaging virtual agent content

  Background:
    Given the Greta platform is running
    And I am logged in as a content creator

  Scenario: Create simple gesture animation
    When I navigate to the animation creation page
    And I select "gesture" as the animation type
    And I enter "wave" as the animation name
    And I set the duration to "2.0" seconds
    And I click the create animation button
    Then I should see a success message
    And the animation should appear in my animations list
    And the animation should have status "ready"

  Scenario: Preview animation before saving
    Given I am on the animation creation page
    When I configure a "nod" gesture animation
    And I click the preview button
    Then I should see a 3D preview of the animation
    And the preview should last approximately 1.5 seconds
    And I should see animation controls (play, pause, restart)

  Scenario: Create complex multimodal animation
    When I start creating a new animation
    And I select "multimodal" as the type
    And I add a facial expression "smile" with intensity 0.8
    And I add a gesture "pointing" with target coordinates (100, 200)
    And I add a gaze direction towards "user"
    And I set the overall duration to 3.0 seconds
    And I synchronize all modalities to start at 0.5 seconds
    Then the animation timeline should show all three modalities
    And each modality should be properly timed
    When I save the animation
    Then it should be saved successfully with all modalities

  Scenario: Edit existing animation
    Given I have an existing animation called "greeting_wave"
    When I open the animation for editing
    And I change the duration from 2.0 to 2.5 seconds
    And I adjust the intensity from 0.8 to 1.0
    And I save the changes
    Then the animation should be updated with new parameters
    And the version history should show the modification

  Scenario: Delete animation with confirmation
    Given I have an animation called "test_animation"
    When I select the animation in the list
    And I click the delete button
    Then I should see a confirmation dialog
    When I confirm the deletion
    Then the animation should be removed from the list
    And I should see a deletion success message

  Scenario: Batch operations on animations
    Given I have multiple animations in my list
    When I select 3 animations using checkboxes
    And I choose "Export" from the batch actions menu
    Then I should see an export configuration dialog
    When I select "FBX" format and click export
    Then all 3 animations should be exported as a single file
    And I should receive a download link

  Scenario: Animation search and filtering
    Given I have animations with various types and names
    When I enter "wave" in the search box
    Then I should only see animations containing "wave" in the name
    When I clear the search and filter by "gesture" type
    Then I should only see gesture animations
    When I apply both name filter "nod" and type filter "gesture"
    Then I should see only gesture animations with "nod" in the name

  Scenario: Animation performance optimization
    Given I am creating a complex animation with 10 modalities
    When the system detects performance impact
    Then I should see a performance warning
    And suggestions for optimization should be displayed
    When I accept the suggested optimizations
    Then the animation should be optimized automatically
    And performance metrics should improve

  Scenario Outline: Create animations with different parameters
    When I create a "<type>" animation named "<name>"
    And I set the duration to <duration> seconds
    And I set the intensity to <intensity>
    Then the animation should be created successfully
    And should match the specified parameters

    Examples:
      | type     | name          | duration | intensity |
      | gesture  | wave_hello    | 2.0      | 0.8       |
      | facial   | smile_warm    | 1.5      | 0.9       |
      | posture  | lean_forward  | 3.0      | 0.7       |
      | gaze     | look_around   | 4.0      | 0.6       |