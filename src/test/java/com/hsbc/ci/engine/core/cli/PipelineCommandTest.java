package com.hsbc.ci.engine.core.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PipelineCommandTest {

    @Autowired(required = false)
    private PipelineCommand pipelineCommand;

    @BeforeEach
    void setUp() {
        if (pipelineCommand == null) {
            pipelineCommand = new PipelineCommand();
        }
    }

    @Test
    void shouldCreatePipelineCommand() {
        assertNotNull(pipelineCommand);
    }

    @Test
    void testRunWithNoOptions() {
        // When run is called with no options, it should print usage
        assertDoesNotThrow(() -> pipelineCommand.run());
    }
}
