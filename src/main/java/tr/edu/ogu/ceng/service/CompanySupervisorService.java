package tr.edu.ogu.ceng.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tr.edu.ogu.ceng.dao.CompanyRepository;
import tr.edu.ogu.ceng.dao.CompanySupervisorRepository;
import tr.edu.ogu.ceng.dto.CompanySupervisorDto;
import tr.edu.ogu.ceng.dto.requests.CompanySupervisorRequestDto;
import tr.edu.ogu.ceng.dto.responses.CompanySupervisorResponseDto;
import tr.edu.ogu.ceng.enums.UserType;
import tr.edu.ogu.ceng.internationalization.MessageResource;
import tr.edu.ogu.ceng.model.Company;
import tr.edu.ogu.ceng.model.CompanySupervisor;
import tr.edu.ogu.ceng.model.User;
import tr.edu.ogu.ceng.security.UserPrincipal;
import tr.edu.ogu.ceng.service.Exception.EntityNotFoundException;
import tr.edu.ogu.ceng.service.Exception.UserAlreadyExistsException;

@Slf4j
@AllArgsConstructor
@Service
public class CompanySupervisorService {

	private final CompanySupervisorRepository repository;
	private final CompanyRepository companyRepository;
	private final ModelMapper mapper;
	private final UserService userService;

	@Autowired
	private MessageResource messageResource;

	public Page<CompanySupervisorResponseDto> getAll(Pageable pageable) {

		Page<CompanySupervisor> companySupervisors = repository.findAll(pageable);
		Page<CompanySupervisorResponseDto> response = companySupervisors
				.map(companySupervisor -> mapper.map(companySupervisor, CompanySupervisorResponseDto.class));
		log.info("Company Supervisors are fetched from database");
		return response;
	}

	public CompanySupervisorResponseDto getById(Long id) {
		CompanySupervisor companySupervisor = repository.findById(id).orElseThrow();
		CompanySupervisorResponseDto response = mapper.map(companySupervisor, CompanySupervisorResponseDto.class);

		log.info("Company Supervisor is fetched from database id: {}, name: {}", companySupervisor.getId(), companySupervisor.getName());
		return response;
	}

	public CompanySupervisor add(CompanySupervisor companySupervisor) {
		checkIfCompanySupervisorExistsByUserId(companySupervisor.getUser().getId());
		companySupervisor.setCreateDate(LocalDateTime.now());
		companySupervisor.setUpdateDate(LocalDateTime.now());
		CompanySupervisor createdCompanySupervisor = repository.save(companySupervisor);

		log.info("Company Supervisor is added to database id: {}, name: {}", companySupervisor.getId(), companySupervisor.getName());
		return createdCompanySupervisor;
	}

	public Company getUsersCompany() {
		User user = ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUser();
		return companyRepository.findCompanyByCompanyUserId(user.getId());
	}

	public CompanySupervisorResponseDto addCompany(CompanySupervisorRequestDto request) {

		LocalDateTime now = LocalDateTime.now();

		User user = mapper.map(request.getUser(), User.class);
		user.setCreateDate(now);
		user.setUpdateDate(now);
		user.setUserType(UserType.COMPANYSUPERVISOR);

		checkIfCompanySupervisorExistsByUserId(request.getUser().getId());
		CompanySupervisor companySupervisor = mapper.map(request, CompanySupervisor.class);
		companySupervisor.setUser(userService.saveUser(user));
		companySupervisor.setCreateDate(now);
		companySupervisor.setUpdateDate(now);
		CompanySupervisor createdCompanySupervisor = repository.save(companySupervisor);

		log.info("CompanySupervisorResponseDto is mapped to CompanySupervisor entity id: {}, name: {}", companySupervisor.getId(), companySupervisor.getName());
		return mapper.map(createdCompanySupervisor, CompanySupervisorResponseDto.class);

	}

	public CompanySupervisorDto update(CompanySupervisorDto request) {
		CompanySupervisor companySupervisor = repository.findById(request.getId())
				.orElseThrow(() -> new EntityNotFoundException(messageResource.getMessage("companySupervisorNotFound")));
		if (companySupervisor.getUser().getId() != request.getUser().getId()) {
			log.error("Company Supervisor not found with the user id: " + request.getUser().getId());
			// checkIfCompanySupervisorExistsByUserId(request.getUser().getId());
		}
		request.setCreateDate(companySupervisor.getCreateDate());
		companySupervisor = mapper.map(request, CompanySupervisor.class);
		companySupervisor.setUpdateDate(LocalDateTime.now());
		CompanySupervisor updatedCompanySupervisor = repository.save(companySupervisor);

		CompanySupervisorDto response = mapper.map(updatedCompanySupervisor, CompanySupervisorDto.class);
		log.info("Company Supervisor is updated in database id: {}, name: {}", companySupervisor.getId(), companySupervisor.getName());
		return response;
	}

	public void delete(Long id) {

		repository.findById(id).orElseThrow(() -> new EntityNotFoundException(messageResource.getMessage("companySupervisorNotFound")));

		repository.deleteById(id);
		log.info("Company Supervisor is deleted from database id: {}", id);
	}

	void checkIfCompanySupervisorExistsByUserId(Long userId) {
		if (repository.existsByUserId(userId)) {
			log.warn("Company Supervisor already exists with the user id: " + userId);
			throw new UserAlreadyExistsException();
		}
	}

	public List<CompanySupervisorDto> getCompanySupervisorsByCompanyId(Long companyId) {
		List<CompanySupervisor> companySupervisors = repository.findAllByCompanyId(companyId);

		List<CompanySupervisorDto> companySupervisorDtos = companySupervisors.stream()
				.map(companySupervisor -> mapper.map(companySupervisor, CompanySupervisorDto.class))
				.collect(Collectors.toList());

		log.info("Company Supervisors are fetched from database");
		return companySupervisorDtos;
	}

	public CompanySupervisorDto getCompanySupervisorByUserId(Long userId) {
		CompanySupervisor companySupervisor = repository.findCompanySupervisorByUserId(userId);
		log.info("Company Supervisor is fetched from database id: {}, name: {}", companySupervisor.getId(), companySupervisor.getName());
		return mapper.map(companySupervisor, CompanySupervisorDto.class);
	}
}
