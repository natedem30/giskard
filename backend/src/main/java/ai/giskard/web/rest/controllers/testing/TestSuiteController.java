package ai.giskard.web.rest.controllers.testing;

import ai.giskard.domain.ml.TestSuite;
import ai.giskard.repository.ml.DatasetRepository;
import ai.giskard.repository.ml.ModelRepository;
import ai.giskard.repository.ml.TestSuiteRepository;
import ai.giskard.service.TestService;
import ai.giskard.service.TestSuiteExecutionService;
import ai.giskard.service.TestSuiteService;
import ai.giskard.web.dto.TestSuiteCompleteDTO;
import ai.giskard.web.dto.TestSuiteDTO;
import ai.giskard.web.dto.mapper.GiskardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping("/api/v2/testing/")
@RequiredArgsConstructor
public class TestSuiteController {
    private final TestSuiteService testSuiteService;
    private final TestService testService;
    private final GiskardMapper giskardMapper;
    private final TestSuiteRepository testSuiteRepository;
    private final DatasetRepository datasetRepository;
    private final ModelRepository modelRepository;
    private final TestSuiteExecutionService testSuiteExecutionService;


    @PostMapping("project/{projectKey}/suites")
    @PreAuthorize("@permissionEvaluator.canWriteProjectKey(#projectKey)")
    @Transactional
    public Long saveTestSuite(@PathVariable("projectKey") @NotNull String projectKey, @Valid @RequestBody TestSuiteDTO dto) {
        TestSuite savedSuite = testSuiteRepository.save(giskardMapper.fromDTO(dto));
        return savedSuite.getId();
    }

    @GetMapping("project/{projectId}/suites")
    @PreAuthorize("@permissionEvaluator.canReadProject(#projectId)")
    @Transactional
    public List<TestSuiteDTO> listTestSuites(@PathVariable("projectId") @NotNull Long projectId) {
        return giskardMapper.toDTO(testSuiteRepository.findAllByProjectId(projectId));
    }

    @GetMapping("project/{projectId}/suite/{suiteId}")
    @PreAuthorize("@permissionEvaluator.canReadProject(#projectId)")
    @Transactional
    public TestSuiteDTO listTestSuiteComplete(@PathVariable("projectId") @NotNull Long projectId,
                                              @PathVariable("suiteId") @NotNull Long suiteId) {
        return giskardMapper.toDTO(testSuiteRepository.findOneByProjectIdAndId(projectId, suiteId));
    }

    @GetMapping("project/{projectId}/suite/{suiteId}/complete")
    @PreAuthorize("@permissionEvaluator.canReadProject(#projectId)")
    @Transactional(readOnly = true)
    public TestSuiteCompleteDTO listTestSuite(@PathVariable("projectId") @NotNull Long projectId,
                                              @PathVariable("suiteId") @NotNull Long suiteId) {
        return new TestSuiteCompleteDTO(
            giskardMapper.toDTO(testSuiteRepository.findOneByProjectIdAndId(projectId, suiteId)),
            testService.listTestsFromRegistry(projectId),
            giskardMapper.datasetsToDatasetDTOs(datasetRepository.findAllByProjectId(projectId)),
            giskardMapper.modelsToModelDTOs(modelRepository.findAllByProjectId(projectId)),
            testSuiteExecutionService.listAllExecution(suiteId),
            testSuiteService.getSuiteInputs(projectId, suiteId)
        );
    }

    @PostMapping("project/{projectId}/suite/{suiteId}/schedule-execution")
    @PreAuthorize("@permissionEvaluator.canReadProject(#projectId)")
    @Transactional
    public UUID scheduleTestSuiteExecution(@PathVariable("projectId") @NotNull Long projectId,
                                           @PathVariable("suiteId") @NotNull Long suiteId,
                                           @Valid @RequestBody Map<@NotBlank String, @NotNull String> inputs) {
        return testSuiteService.scheduleTestSuiteExecution(projectId, suiteId, inputs);
    }

    @PutMapping("project/{projectId}/suite/{suiteId}/test/{testId}/inputs")
    @PreAuthorize("@permissionEvaluator.canWriteProject(#projectId)")
    @Transactional
    public TestSuiteDTO updateTestInputs(@PathVariable("projectId") long projectId,
                                         @PathVariable("suiteId") long suiteId,
                                         @PathVariable("testId") @NotBlank String testId,
                                         @Valid @RequestBody Map<@NotBlank String, @NotNull String> inputs) {
        return testSuiteService.updateTestInputs(suiteId, testId, inputs);
    }

}
