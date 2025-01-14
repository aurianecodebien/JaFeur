package services;

import entities.Application;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ApplicationRepository;

import java.util.List;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    @Autowired
    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public Application getById(Integer id) {
        return applicationRepository.findById(id).orElse(null);
    }

    public List<Application> getAll() {
            return applicationRepository.findAll();
    }

}
