package tr.edu.ogu.ceng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import tr.edu.ogu.ceng.dao.*;
import tr.edu.ogu.ceng.dto.CompanyDto;
import tr.edu.ogu.ceng.dto.FacultyDto;
import tr.edu.ogu.ceng.dto.UserDto;
import tr.edu.ogu.ceng.dto.requests.InternshipRequestDto;
import tr.edu.ogu.ceng.enums.InternshipStatus;
import tr.edu.ogu.ceng.enums.UserType;
import tr.edu.ogu.ceng.internationalization.MessageResource;
import tr.edu.ogu.ceng.model.*;
import tr.edu.ogu.ceng.security.AuthService;

public class InternshipTest {
	@Mock
	InternshipRepository internshipRepository;

	@Mock
	AuthService authService;
	@Mock
	StudentRepository studentRepository;
	@Mock
	CompanySupervisorRepository companySupervisorRepository;
	@Mock
	CompanyRepository companyRepository;


	@Mock
	FacultySupervisorRepository facultySupervisorRepository;

	@Mock
	UserRepository userRepository;

	@Mock
	FacultyRepository facultyRepository;

	@Mock
	InternshipService internshipService;

	@Mock
	CompanyService companyService;
	@Mock
	StudentService studentService;

	MessageResource messageResource;

	InternshipStatus status = InternshipStatus.PENDING;

	@SuppressWarnings("deprecation")
	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		internshipService = new InternshipService(internshipRepository, studentRepository, companySupervisorRepository,
				facultySupervisorRepository, userRepository, facultyRepository, new ModelMapper(), messageResource, authService);
	}

	@Test
	public void is_internship_added_successfully() {

		LocalDateTime localDateTime = LocalDateTime.now();

		var modelCompany = new Company(1L, "Test", "Test", "Test", "Test", "Test", "Test", "Test", localDateTime,
				localDateTime);
		var modelUser = new User(3L, "password", "email", UserType.FACULTYSUPERVISOR, localDateTime,
				localDateTime, null, false);
		var modelFaculty = new Faculty(1L, "Faculty", localDateTime, localDateTime);
		var modelFacultySupervisor = new FacultySupervisor(4L, "Name", "Surname", "Phone", "No", localDateTime,
				localDateTime, modelUser, modelFaculty);

		var modelStudent = new Student(6L, "test", "test", "test", "test", "test", "test", "test", null, localDateTime,
				localDateTime, null, modelFaculty, "address");

		var modelInternship = new Internship(1L, InternshipStatus.APPROVED, null, null, 0, localDateTime, localDateTime,
				modelStudent, modelCompany, modelFacultySupervisor);

		when(studentRepository.save(any(Student.class))).thenReturn(modelStudent);
		when(companyRepository.save(any(Company.class))).thenReturn(modelCompany);
		when(userRepository.save(any(User.class))).thenReturn(modelUser);
		when(facultyRepository.save(any(Faculty.class))).thenReturn(modelFaculty);
		when(facultySupervisorRepository.save(any(FacultySupervisor.class))).thenReturn(modelFacultySupervisor);
		when(internshipRepository.save(any(Internship.class))).thenReturn(modelInternship);

		var Dtointernship = InternshipRequestDto.builder().id(1L).status(InternshipStatus.APPROVED)
				.startDate(new Timestamp(2000, 01, 01, 0, 0, 0, 0)).endDate(new Timestamp(2000, 01, 01, 0, 0, 0, 0))
				.days(1).studentId(1004L).companyId(9001L).facultySupervisorId(400L).build();

		var actual = internshipService.addInternship(Dtointernship);

		assertNotNull(actual);
		assertEquals(modelInternship.getId(), actual.getId());
		assertEquals(modelInternship.getStatus(), actual.getStatus());
		assertEquals(modelInternship.getDays(), actual.getDays());
		assertEquals(modelInternship.getCompany().getId(), actual.getCompanyId());
		assertEquals(modelInternship.getStudent().getId(), actual.getStudent().getId());
		assertEquals(modelInternship.getFacultySupervisor().getId(), actual.getFacultySupervisorId());
	}

	@Test
	public void is_internship_mark_completed() {
			Long facultySupervisorId = 1L;
			Long internshipId = 1L;

			FacultySupervisor mockFacultySupervisor = new FacultySupervisor();
			Internship mockInternship = new Internship();

			when(facultySupervisorRepository.findById(facultySupervisorId)).thenReturn(Optional.of(mockFacultySupervisor));
			when(internshipRepository.findById(internshipId)).thenReturn(Optional.of(mockInternship));

			boolean result = internshipService.markInternshipCompleted(facultySupervisorId, internshipId);

			assertTrue(result);
			assertEquals(InternshipStatus.SUCCESS, mockInternship.getStatus());
			verify(facultySupervisorRepository, times(1)).findById(facultySupervisorId);
			verify(internshipRepository, times(1)).findById(internshipId);
			verify(internshipRepository, times(1)).save(mockInternship);
	}
}
