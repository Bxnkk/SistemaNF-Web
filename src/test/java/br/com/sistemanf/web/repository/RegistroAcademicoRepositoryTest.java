package br.com.sistemanf.web.repository;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RegistroAcademicoRepositoryTest {

	private RegistroAcademicoRepository repository;

	@BeforeEach
	void configurar() {
		repository = new RegistroAcademicoRepository(null, null, null);
	}

	@Test
	void deveRejeitarNotaAcimaDeDez() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvarNota(1, "RA001", "10.01"));
	}

	@Test
	void deveRejeitarNotaNegativa() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvarNota(1, "RA001", "-0.01"));
	}

	@Test
	void deveRejeitarNotaComMaisDeDuasCasasDecimais() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvarNota(1, "RA001", "7.555"));
	}

	@Test
	void deveAceitarLimiteInferiorDaNotaNaValidacao() {
		assertThrows(SecurityException.class, () -> repository.salvarNota(0, "RA001", "0"));
	}

	@Test
	void deveAceitarLimiteSuperiorDaNotaNaValidacao() {
		assertThrows(SecurityException.class, () -> repository.salvarNota(0, "RA001", "10"));
	}

	@Test
	void deveRejeitarFaltasNegativas() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvarFaltas(1, "RA001", "-1"));
	}

	@Test
	void deveRejeitarFaltasDecimais() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvarFaltas(1, "RA001", "2.5"));
	}

	@Test
	void deveRejeitarMaisDeCentoEVinteFaltas() {
		assertThrows(IllegalArgumentException.class, () -> repository.salvarFaltas(1, "RA001", "121"));
	}

	@Test
	void deveAceitarLimiteInferiorDasFaltasNaValidacao() {
		assertThrows(SecurityException.class, () -> repository.salvarFaltas(0, "RA001", "0"));
	}

	@Test
	void deveAceitarLimiteSuperiorDasFaltasNaValidacao() {
		assertThrows(SecurityException.class, () -> repository.salvarFaltas(0, "RA001", "120"));
	}
}