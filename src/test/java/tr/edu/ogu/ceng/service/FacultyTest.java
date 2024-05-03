package tr.edu.ogu.ceng.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Arrays;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import tr.edu.ogu.ceng.dao.FacultyRepository;
import tr.edu.ogu.ceng.dto.FacultyDto;
import tr.edu.ogu.ceng.model.Faculty;

public class FacultyTest {

	@Mock
	FacultyRepository facultyRepository;
	FacultyService facultyService;
	ModelMapper modelMapper;

	@BeforeEach
	public void init() {
		MockitoAnnotations.initMocks(this);
		modelMapper = new ModelMapper();
		facultyService = new FacultyService(facultyRepository, modelMapper);
	}

	@Test
	public void is_faculty_added_successfully() {

		LocalDateTime localDateTime = LocalDateTime.now();

		var modelFaculty = new Faculty(1L, "Faculty", localDateTime, localDateTime);
		when(facultyRepository.save(any(Faculty.class))).thenReturn(modelFaculty);
		var DtoFaculty = FacultyDto.builder().id(1L).name("Faculty").createDate(localDateTime).updateDate(localDateTime)
				.build();

		var actual = facultyService.addFaculty(DtoFaculty);

		assertNotNull(actual);
		assertEquals(modelFaculty.getId(), actual.getId());
		assertEquals(modelFaculty.getName(), actual.getName());
	}
	
	@Test
    public void testGetFaculties() {
        LocalDateTime localDateTime = LocalDateTime.now();
        Faculty faculty1 = new Faculty(1L, "Faculty1", localDateTime, localDateTime);
        Faculty faculty2 = new Faculty(2L, "Faculty2", localDateTime, localDateTime);

        Page<Faculty> page = new PageImpl<>(Arrays.asList(faculty1, faculty2));
        when(facultyRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<FacultyDto> result = facultyService.getFaculties(Pageable.unpaged());
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals("Faculty1", result.getContent().get(0).getName());
        assertEquals("Faculty2", result.getContent().get(1).getName());
    }

    @Test
    public void testCountFaculties() {
        when(facultyRepository.count()).thenReturn(10L);
        Long count = facultyService.countFaculties();
        assertNotNull(count);
        assertEquals(10L, count);
    }
	  

	@Test
	public void is_faculty_retrieved_by_id() {
	    LocalDateTime localDateTime = LocalDateTime.now();
	    Faculty faculty = new Faculty(1L, "Faculty", localDateTime, localDateTime);

	    when(facultyRepository.findById(1L)).thenReturn(Optional.of(faculty));

	    FacultyDto retrievedFacultyDto = facultyService.getFacultyById(1L);

	    assertNotNull(retrievedFacultyDto);
	    assertEquals(faculty.getId(), retrievedFacultyDto.getId());
	    assertEquals(faculty.getName(), retrievedFacultyDto.getName());
	}


	@Test
	public void is_faculty_deleted_successfully() {
	    when(facultyRepository.existsById(1L)).thenReturn(true);

	    boolean isDeleted = facultyService.deleteFaculty(1L);

	    assertTrue(isDeleted);
	    verify(facultyRepository, times(1)).deleteById(1L);
	}

@Test
	public void is_faculty_not_found_when_deleting() {
	    when(facultyRepository.existsById(1L)).thenReturn(false);

	    boolean isDeleted = facultyService.deleteFaculty(1L);

	    assertTrue(isDeleted);
	    verify(facultyRepository, never()).deleteById(1L);
	}

}
