import com.hsbc.ci.engine.core.cli.PipelineOutputFormatter;
import com.hsbc.ci.engine.core.model.StageResult;
import java.util.LinkedHashMap;
import java.util.Map;

public class FormatterDemo {
    public static void main(String[] args) {
        Map<String, StageResult> stages = new LinkedHashMap<>();
        
        // Add successful stages
        stages.put("containerize", StageResult.builder().stageName("containerize").success(true).build());
        stages.put("build", StageResult.builder().stageName("build").success(true).build());
        stages.put("class-integration-tests", StageResult.builder().stageName("class-integration-tests").success(true).build());
        stages.put("microservice-integration-tests", StageResult.builder().stageName("microservice-integration-tests").success(true).build());
        stages.put("inter-service-tests", StageResult.builder().stageName("inter-service-tests").success(true).build());
        stages.put("unit-tests", StageResult.builder().stageName("unit-tests").success(true).build());
        
        // Add failed stage
        stages.put("deploy-sandbox", StageResult.builder().stageName("deploy-sandbox").success(false).build());
        
        // Test with very long stage name
        stages.put("very-long-stage-name-that-exceeds-maximum-width", 
            StageResult.builder().stageName("very-long-stage-name-that-exceeds-maximum-width").success(true).build());
        
        String output = PipelineOutputFormatter.formatPipelineOutput("microservice-cd", "sandbox", stages, true);
        System.out.println(output);
    }
}
