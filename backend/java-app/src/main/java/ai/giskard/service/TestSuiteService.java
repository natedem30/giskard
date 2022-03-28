package ai.giskard.service;

import ai.giskard.repository.ProjectRepository;
import ai.giskard.repository.ml.DatasetRepository;
import ai.giskard.repository.ml.ModelRepository;
import ai.giskard.repository.ml.TestSuiteRepository;
import ai.giskard.service.dto.ml.TestSuiteDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TestSuiteService {
    private final TestSuiteRepository testSuiteRepository;
    private final ModelRepository modelRepository;
    private final DatasetRepository datasetRepository;
    private final ProjectRepository projectRepository;

    public TestSuiteService(TestSuiteRepository testSuiteRepository,
                            ModelRepository modelRepository,
                            DatasetRepository datasetRepository,
                            ProjectRepository projectRepository) {
        this.testSuiteRepository = testSuiteRepository;
        this.modelRepository = modelRepository;
        this.datasetRepository = datasetRepository;
        this.projectRepository = projectRepository;
    }

    public Optional<TestSuiteDTO> updateTestSuite(TestSuiteDTO dto) {
        return Optional
            .of(testSuiteRepository.findById(dto.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(testSuite -> {
                testSuite.setName(dto.getName());
                if (dto.getProjectId() != null) {
                    projectRepository.findById(dto.getProjectId()).ifPresent(testSuite::setProject);
                }
                if (dto.getModel() != null) {
                    modelRepository.findById(dto.getModel().getId()).ifPresent(testSuite::setModel);
                }
                if (dto.getTrainDataset() != null) {
                    datasetRepository.findById(dto.getTrainDataset().getId()).ifPresent(testSuite::setTrainDataset);
                }
                if (dto.getTestDataset() != null) {
                    datasetRepository.findById(dto.getTestDataset().getId()).ifPresent(testSuite::setTestDataset);
                }
                return testSuiteRepository.save(testSuite);
            })
            .map(TestSuiteDTO::new);
    }
}
