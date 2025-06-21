Feature: Behavior Planning and Execution
  As a researcher or developer
  I want to plan and execute complex behaviors
  So that the virtual agent can respond naturally to interactions

  Background:
    Given the Greta platform is running
    And I am logged in as a researcher

  Scenario: Plan simple communicative behavior
    When I navigate to the behavior planning interface
    And I select "greeting" as the communicative intention
    And I set the emotional context to "friendly"
    And I specify the target as "individual user"
    And I click plan behavior
    Then I should see a generated behavior plan
    And the plan should include appropriate gestures
    And the plan should include facial expressions
    And the estimated duration should be reasonable (1-3 seconds)

  Scenario: Real-time behavior adaptation
    Given I have started an interactive session
    And the agent is executing a "presentation" behavior
    When a user interrupts with a question
    And I signal the interruption to the system
    Then the current behavior should be gracefully interrupted
    And a new "acknowledgment" behavior should be planned
    And the agent should transition smoothly to answering mode

  Scenario: Emotional state management
    Given I am configuring agent behavior
    When I set the emotional state to "excited"
    And I plan a "explanation" behavior
    Then the generated behaviors should reflect excitement
    And gesture intensity should be higher than neutral
    And facial expressions should show positive emotion
    And speech prosody parameters should indicate enthusiasm

  Scenario: Context-aware behavior selection
    Given I am in a formal presentation context
    When I plan a "disagreement" behavior
    Then the generated behaviors should be formal and respectful
    And gestures should be subdued and professional
    When I change the context to "casual conversation"
    And I plan the same "disagreement" behavior
    Then the behaviors should be more relaxed and expressive

  Scenario: Multi-agent behavior coordination
    Given I have two virtual agents in the scene
    When I plan a "conversation" behavior between them
    Then Agent A should have speaker behaviors
    And Agent B should have listener behaviors
    And turn-taking should be properly coordinated
    And gaze patterns should support the interaction

  Scenario: Behavior conflict resolution
    Given I have planned overlapping behaviors
    When I add a "pointing gesture" at time 2.0 seconds
    And there's already a "waving gesture" at time 2.1 seconds
    Then the system should detect the conflict
    And suggest resolution options
    When I choose "adjust timing" resolution
    Then one gesture should be moved to avoid conflict

  Scenario: Cultural adaptation
    Given I am configuring behaviors for different cultures
    When I set the cultural context to "Japanese"
    And I plan a "greeting" behavior
    Then the behaviors should include appropriate bowing
    And eye contact patterns should be culturally appropriate
    When I change to "American" cultural context
    Then behaviors should include handshake gestures
    And direct eye contact should be emphasized

  Scenario: Accessibility considerations
    Given I need to create accessible interactions
    When I enable "hearing impaired" accessibility mode
    And I plan a "information sharing" behavior
    Then visual gestures should be more prominent
    And facial expressions should be clearer
    And sign language elements should be considered

  Scenario: Behavior learning and improvement
    Given I have executed multiple behaviors
    When I review the behavior analytics
    Then I should see effectiveness metrics
    And user engagement scores
    When I identify low-performing behaviors
    And I request optimization suggestions
    Then the system should provide improvement recommendations

  Scenario: Save and reuse behavior templates
    Given I have created an effective behavior plan
    When I save it as a template named "formal_introduction"
    Then it should appear in my templates library
    When I later create a similar scenario
    And I apply the "formal_introduction" template
    Then the behaviors should be automatically configured
    And I should be able to customize specific parameters