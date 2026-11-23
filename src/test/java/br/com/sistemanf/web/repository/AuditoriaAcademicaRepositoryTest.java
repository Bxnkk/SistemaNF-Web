package br.com.sistemanf.web.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditoriaAcademicaRepositoryTest {

	@Autowired
	private AuditoriaAcademicaRepository auditoriaRepository;

	@Test
	void deveConfirmarIntegridadeDaCadeia() {
		assertTrue(auditoriaRepository.verificarIntegridade(), "A cadeia de auditoria apresenta inconsistências.");
	}
}